"""Compile real loot source against the resolved game API and run a bounded codec probe."""
import os
from pathlib import Path
import shutil
import subprocess

OUT = Path('port-diagnostics')


def main():
    classpath_file = OUT / 'classpath.txt'
    if not classpath_file.is_file():
        raise SystemExit('Loot smoke check did not run: target classpath unavailable.')
    classpath = classpath_file.read_text().strip()
    classes = OUT / 'loot-smoke-classes'
    empty_sourcepath = OUT / 'empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty_sourcepath.mkdir(exist_ok=True)
    sources = Path('src/main/java/net/dries007/tfc/util/loot')
    commands = [
        ['javac', '--release', '25', '-classpath', classpath, '-sourcepath', str(empty_sourcepath), '-d', str(classes),
         str(sources / 'package-info.java'), str(sources / 'AlwaysTrueCondition.java'), str(sources / 'MinMaxProvider.java'),
         'tools/porting/LootCodecSmoke.java'],
        ['java', '-ea', '-classpath', str(classes) + os.pathsep + classpath, 'LootCodecSmoke'],
    ]
    log = OUT / 'loot-codec-smoke.log'
    with log.open('w') as output:
        for command in commands:
            output.write('Running ' + command[0] + ' against the exact target classpath.\n')
            output.flush()
            try:
                result = subprocess.run(command, text=True, stdout=output, stderr=subprocess.STDOUT, timeout=90)
            except subprocess.TimeoutExpired:
                output.write('FAIL: smoke-check command timed out.\n')
                print('Loot smoke check timed out; see loot-codec-smoke.log.')
                return 1
            if result.returncode:
                print(f'Loot smoke check failed in {command[0]} with exit {result.returncode}; see loot-codec-smoke.log.')
                return result.returncode
    print(log.read_text())
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
