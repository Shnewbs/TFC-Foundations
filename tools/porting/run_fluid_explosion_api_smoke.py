"""Check real production fluid/explosion mixin bridges against the pinned game.

This compiles actual interfaces without stubs. It checks private target signatures
and staticness, but does not transform Minecraft or exercise gameplay in a world.
"""
import os
from pathlib import Path
import shutil
import subprocess


def main():
    out = Path('port-diagnostics')
    cp_file = out / 'classpath.txt'
    if not cp_file.is_file():
        raise SystemExit('Fluid/explosion checks require the resolved target classpath.')
    cp = cp_file.read_text().strip()
    classes = out / 'fluid-explosion-api-classes'
    empty = out / 'empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    java = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else None
    javac = str(java / 'javac') if java else 'javac'
    runtime = str(java / 'java') if java else 'java'
    source = Path('src/main/java/net/dries007/tfc/mixin/accessor')
    commands = [
        [javac, '--release', '25', '-proc:none', '-classpath', cp, '-sourcepath', str(empty), '-d', str(classes),
         str(source / 'FlowingFluidAccessor.java'), str(source / 'ServerExplosionAccessor.java'),
         'tools/porting/FluidExplosionApiSmoke.java'],
        [runtime, '-ea', '-classpath', str(classes) + os.pathsep + cp, 'FluidExplosionApiSmoke'],
    ]
    log = out / 'fluid-explosion-api-smoke.log'
    with log.open('w') as stream:
        for command in commands:
            stream.write('Running ' + Path(command[0]).name + ' against the resolved target classpath.\n')
            stream.flush()
            result = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT, text=True, timeout=90)
            if result.returncode:
                print(log.read_text())
                return result.returncode
    print(log.read_text())
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
