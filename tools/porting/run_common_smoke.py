"""Test actual standalone utilities and target APIs, without stubbing Minecraft/TFC."""
import os
from pathlib import Path
import shutil
import subprocess


def main():
    out = Path('port-diagnostics')
    cp_file = out / 'classpath.txt'
    if not cp_file.is_file():
        raise SystemExit('Common smoke checks did not run: resolved target classpath missing.')
    cp = cp_file.read_text().strip()
    classes = out / 'common-smoke-classes'
    empty = out / 'empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    java = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else None
    javac = str(java / 'javac') if java else 'javac'
    runtime = str(java / 'java') if java else 'java'
    util = Path('src/main/java/net/dries007/tfc/util')
    commands = [[javac, '--release', '25', '-proc:none', '-classpath', cp, '-sourcepath', str(empty), '-d', str(classes),
        str(util / 'package-info.java'), str(util / 'NbtHelpers.java'), str(util / 'Unchecked.java'), 'tools/porting/CommonApiSmoke.java']]
    for source_set in ('main', 'data', 'test'):
        packages = sorted(Path(f'src/{source_set}/java').rglob('package-info.java'))
        if packages:
            commands.append([javac, '--release', '25', '-proc:none', '-classpath', cp, '-sourcepath', str(empty), '-d', str(classes / source_set), *map(str, packages)])
    commands.append([runtime, '-ea', '-classpath', str(classes) + os.pathsep + cp, 'CommonApiSmoke'])
    log = out / 'common-api-smoke.log'
    with log.open('w') as stream:
        for command in commands:
            stream.write('Running ' + Path(command[0]).name + ' against the resolved target classpath.\n')
            stream.flush()
            try:
                result = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT, text=True, timeout=90)
            except subprocess.TimeoutExpired:
                stream.write('FAIL: timeout\n')
                return 1
            if result.returncode:
                print(f'FAIL: common smoke checks ({Path(command[0]).name}); see {log}.')
                return result.returncode
    print(log.read_text())
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
