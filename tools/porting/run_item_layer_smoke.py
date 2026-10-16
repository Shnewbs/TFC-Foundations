"""Compile production native item models and test real codecs, sprites and quad remapping on the CPU."""
import os
from pathlib import Path
import platform
import shutil
import subprocess


def main():
    out = Path('port-diagnostics')
    cp = (out / 'classpath.txt').read_text().strip()
    classes = out / 'item-layer-smoke-classes'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty = out / 'item-layer-empty-sourcepath'
    empty.mkdir(exist_ok=True)
    java_bin = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else Path('')
    sources = [Path('src/main/java/net/dries007/tfc/client/model') / name for name in
               ('ContainedFluidModel.java', 'TrimmedItemModel.java', 'ItemLayerGeometry.java')]
    sources.append(Path('tools/porting/ItemLayerSmoke.java'))
    # NativeImage needs LWJGL's real host library, not an OpenGL context. These
    # artifacts are already resolved by the pinned Gradle runtime configuration.
    classifier = {'Linux': 'linux', 'Windows': 'windows', 'Darwin': 'macos'}[platform.system()]
    if platform.machine().lower() in ('aarch64', 'arm64'):
        classifier += '-arm64'
    native_jars = set()
    for name in cp.split(os.pathsep):
        jar = Path(name)
        if jar.name.startswith('lwjgl') and '-natives-' not in jar.name:
            native_jars.update(jar.parent.parent.glob('*/' + jar.stem + '-natives-' + classifier + '.jar'))
    runtime_cp = os.pathsep.join([str(classes), cp, *map(str, sorted(native_jars))])
    commands = [
        [str(java_bin / 'javac'), '--release', '25', '-proc:none', '-classpath', cp,
         '-sourcepath', str(empty), '-d', str(classes), *map(str, sources)],
        [str(java_bin / 'java'), '-ea', '-classpath', runtime_cp, 'net.dries007.tfc.client.model.ItemLayerSmoke'],
    ]
    log = out / 'item-layer-smoke.log'
    with log.open('w') as stream:
        for command in commands:
            result = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT, timeout=120)
            if result.returncode:
                print(log.read_text())
                return result.returncode
    print(log.read_text())
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
