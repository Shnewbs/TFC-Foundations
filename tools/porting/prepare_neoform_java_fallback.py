"""Prepare an opt-in local NeoForm 2.0.31 workaround for missing process command metadata.

The upstream source and binary JARs remain untouched. The only source changes use
java.home/bin/java when ProcessHandle.Info.command() is empty. No game patches,
access transformers, dependency pins, compilation tasks or release gates change.
"""
import argparse
import os
from pathlib import Path
import re
import subprocess
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--runtime', required=True, type=Path, help='NeoForm Runtime 2.0.31 all JAR')
    parser.add_argument('--sources', required=True, type=Path, help='Matching 2.0.31 sources JAR')
    parser.add_argument('--annotations', required=True, type=Path, help='JetBrains annotations JAR')
    parser.add_argument('--output', required=True, type=Path, help='Untracked local output directory')
    args = parser.parse_args()
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    classes = output / 'classes'
    classes.mkdir(exist_ok=True)
    names = ('engine/NeoFormEngine', 'actions/ExternalJavaToolAction')
    sources = []
    with zipfile.ZipFile(args.sources) as archive:
        for name in names:
            path = 'net/neoforged/neoform/runtime/' + name + '.java'
            text = archive.read(path).decode('utf-8')
            text, count = re.subn(
                r'ProcessHandle.current\(\)\s*\.info\(\)\s*\.command\(\)\s*\.orElseThrow\(\)',
                'ProcessHandle.current().info().command().orElseGet(() -> '
                'java.nio.file.Path.of(System.getProperty("java.home"), "bin", '
                'System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java").toString())',
                text,
            )
            if count != 1:
                raise RuntimeError('Unexpected upstream source; refusing to patch ' + path)
            source = output / 'src' / path
            source.parent.mkdir(parents=True, exist_ok=True)
            source.write_text(text, encoding='utf-8')
            sources.append(source)
    java_bin = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else Path('')
    subprocess.run([str(java_bin / 'javac'), '-proc:none', '-classpath',
                    os.pathsep.join(map(str, (args.runtime, args.annotations))),
                    '-d', str(classes), *map(str, sources)], check=True)
    patched = output / 'neoform-runtime-2.0.31-java-fallback.jar'
    with zipfile.ZipFile(args.runtime) as original, zipfile.ZipFile(patched, 'w', zipfile.ZIP_DEFLATED) as result:
        for entry in original.infolist():
            replacement = classes / entry.filename
            result.writestr(entry, replacement.read_bytes() if replacement.is_file() else original.read(entry))
    jar_path = str(patched).replace('\\', '\\\\').replace("'", "\\'")
    init = output / 'neoform-java-fallback.gradle'
    init.write_text("allprojects {\n    afterEvaluate {\n"
                    "        tasks.named('createMinecraftArtifacts') {\n"
                    f"            neoFormRuntime.setFrom(files('{jar_path}'))\n"
                    "        }\n    }\n}\n", encoding='utf-8')
    print('Local workaround prepared. Add -I ' + str(init) + ' to the normal Gradle command.')
    print('Keep the patched tool local; a normal unmodified toolchain remains the release reference.')


if __name__ == '__main__':
    main()
