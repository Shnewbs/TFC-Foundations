"""Compile and exercise item display render-state snapshots against the resolved target API."""
import os
from pathlib import Path
import shutil
import subprocess


def main():
    out = Path('port-diagnostics')
    cp = (out / 'classpath.txt').read_text().strip()
    classes = out / 'item-display-smoke-classes'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty = out / 'item-display-empty-sourcepath'
    empty.mkdir(exist_ok=True)
    sources = [Path('src/main/java/net/dries007/tfc/client/render/blockentity/ItemDisplayBlockEntityRenderer.java'), Path('tools/porting/ItemDisplaySmoke.java')]
    java_bin = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else Path('')
    commands = [
        [str(java_bin / 'javac'), '--release', '25', '-proc:none', '-classpath', cp,
         '-sourcepath', str(empty), '-d', str(classes), *map(str, sources)],
        [str(java_bin / 'java'), '-ea', '-classpath', str(classes) + os.pathsep + cp, 'net.dries007.tfc.client.render.blockentity.ItemDisplaySmoke'],
    ]
    log = out / 'item-display-smoke.log'
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
