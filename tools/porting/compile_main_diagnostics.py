"""Diagnostic main-source javac pass. This is not the Gradle/mixin/runtime release gate."""
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
from collections import Counter



ERROR_PATTERN = re.compile(r'^(?P<path>.+?\.java):(?P<line>\d+): error: (?P<message>.*)    out = Path('port-diagnostics')
    cp = out / 'classpath.txt'
    if not cp.is_file():
        raise SystemExit('Resolve the exact target first with writePortClasspath.')
    source = Path('src/main/java')
    # Match the unpinned optional adapters excluded by this branch's Gradle build.
    # Pinned optional integration source sets need their own Gradle validation.
    excluded = ('net/dries007/tfc/compat/emi/', 'net/dries007/tfc/compat/jade/',
                'net/dries007/tfc/compat/theoneprobe/', 'net/dries007/tfc/mixin/client/compat/jade/')
    sources = sorted(p for p in source.rglob('*.java') if not p.relative_to(source).as_posix().startswith(excluded))
    classes = out / 'diagnostic-main-classes'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    arguments = out / 'main-sources.txt'
    arguments.write_text('\n'.join('"' + str(p).replace('\\', '/') + '"' for p in sources) + '\n')
    java_home = os.environ.get('JAVA_HOME')
    javac = str(Path(java_home) / 'bin' / 'javac') if java_home else 'javac'
    command = [javac, '-J-Xmx2G', '--release', '25', '-encoding', 'UTF-8', '-parameters',
               '-proc:none', '-Xmaxerrs', '10000', '-classpath', cp.read_text().strip(),
               '-d', str(classes), '@' + str(arguments)]
    log = out / 'main-javac.log'
    timed_out = False
    with log.open('w') as stream:
        try:
            result = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT, timeout=180)
            code = result.returncode
        except subprocess.TimeoutExpired:
            timed_out = True
            code = 124
            stream.write('\nDiagnostic javac timed out; output is incomplete.\n')
    text = log.read_text(errors='replace')
    count = write_error_reports(out, text)
    report = {'source_count': len(sources), 'exit_code': code, 'displayed_errors': count,
              'timed_out': timed_out, 'annotation_processing': False,
              'note': 'Diagnostic main compilation only. Error counts can hide cascading or later failures; no gameplay or full Gradle success is implied.'}
    (out / 'main-javac-summary.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report, indent=2))
    return code


if __name__ == '__main__':
    raise SystemExit(main())
, re.MULTILINE)


def write_error_reports(output_directory, log_text):
    """Save stable diagnostic locations and rank files by compiler errors."""
    diagnostics = [
        {'file': match.group('path').replace('\\', '/'),
         'line': int(match.group('line')),
         'message': match.group('message').strip()}
        for match in ERROR_PATTERN.finditer(log_text)
    ]
    counts = Counter(item['file'] for item in diagnostics)
    (output_directory / 'main-javac-errors.json').write_text(
        json.dumps({'total': len(diagnostics), 'errors': diagnostics}, indent=2) + '\n'
    )
    (output_directory / 'main-javac-by-file.json').write_text(
        json.dumps({'total': len(diagnostics), 'files': [
            {'file': name, 'errors': number}
            for name, number in sorted(counts.items(), key=lambda entry: (-entry[1], entry[0]))
        ]}, indent=2) + '\n'
    )
    return len(diagnostics)


def main():
    out = Path('port-diagnostics')
    cp = out / 'classpath.txt'
    if not cp.is_file():
        raise SystemExit('Resolve the exact target first with writePortClasspath.')
    source = Path('src/main/java')
    # Match the unpinned optional adapters excluded by this branch's Gradle build.
    # Pinned optional integration source sets need their own Gradle validation.
    excluded = ('net/dries007/tfc/compat/emi/', 'net/dries007/tfc/compat/jade/',
                'net/dries007/tfc/compat/theoneprobe/', 'net/dries007/tfc/mixin/client/compat/jade/')
    sources = sorted(p for p in source.rglob('*.java') if not p.relative_to(source).as_posix().startswith(excluded))
    classes = out / 'diagnostic-main-classes'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    arguments = out / 'main-sources.txt'
    arguments.write_text('\n'.join('"' + str(p).replace('\\', '/') + '"' for p in sources) + '\n')
    java_home = os.environ.get('JAVA_HOME')
    javac = str(Path(java_home) / 'bin' / 'javac') if java_home else 'javac'
    command = [javac, '-J-Xmx2G', '--release', '25', '-encoding', 'UTF-8', '-parameters',
               '-proc:none', '-Xmaxerrs', '10000', '-classpath', cp.read_text().strip(),
               '-d', str(classes), '@' + str(arguments)]
    log = out / 'main-javac.log'
    timed_out = False
    with log.open('w') as stream:
        try:
            result = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT, timeout=180)
            code = result.returncode
        except subprocess.TimeoutExpired:
            timed_out = True
            code = 124
            stream.write('\nDiagnostic javac timed out; output is incomplete.\n')
    text = log.read_text(errors='replace')
    count = write_error_reports(out, text)
    report = {'source_count': len(sources), 'exit_code': code, 'displayed_errors': count,
              'timed_out': timed_out, 'annotation_processing': False,
              'note': 'Diagnostic main compilation only. Error counts can hide cascading or later failures; no gameplay or full Gradle success is implied.'}
    (out / 'main-javac-summary.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report, indent=2))
    return code


if __name__ == '__main__':
    raise SystemExit(main())
