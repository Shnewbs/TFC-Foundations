"""Apply real client Mixins without opening a game window (not a gameplay test)."""
import argparse
import json
import os
from pathlib import Path
import re
import subprocess


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--game-output', type=Path, default=Path('.'))
    parser.add_argument('--fml-agent', type=Path)
    parser.add_argument('--all-client-targets', action='store_true')
    args = parser.parse_args()
    root = args.game_output.resolve()
    out = Path('port-diagnostics/client-mixin-smoke').resolve()
    out.mkdir(parents=True, exist_ok=True)
    cp = Path('port-diagnostics/classpath.txt').read_text().strip()
    java = Path(os.environ['JAVA_HOME']) / 'bin'
    subprocess.run([str(java / 'javac'), '-proc:none', '-cp', cp, '-d', str(out),
                    'tools/porting/ClientMixinLoadSmoke.java'], check=True)
    env = os.environ.copy()
    env['MOD_CLASSES'] = os.pathsep.join('tfc%%' + str(root / p) for p in
        ['build/classes/java/main', 'build/resources/main', 'build/classes/java/data'])
    command = [str(java / 'java'), '--enable-native-access=ALL-UNNAMED',
               '--add-opens', 'java.base/java.lang.invoke=ALL-UNNAMED',
               '--add-exports', 'jdk.naming.dns/com.sun.jndi.dns=java.naming']
    if args.fml_agent:
        command.append('-javaagent:' + str(args.fml_agent.resolve()))
    command += ['-cp', str(out) + os.pathsep + cp, 'ClientMixinLoadSmoke',
                str(out / 'game')]
    if args.all_client_targets:
        config = json.loads(Path('src/main/resources/tfc.mixins.json').read_text())
        targets = set()
        for mixin in config['client']:
            source = Path('src/main/java', (config['package'] + '.' + mixin).replace('.', '/') + '.java').read_text()
            imports = {name.rsplit('.', 1)[-1]: name for name in re.findall(r'^import ([\w.]+);', source, re.M)}
            annotation = re.search(r'@Mixin\((.*?)\)', source, re.S).group(1)
            for name in re.findall(r'([\w.]+)\.class', annotation):
                parts = name.split('.')
                if parts[0] in imports:
                    targets.add(imports[parts[0]] + (('$' + '$'.join(parts[1:])) if len(parts) > 1 else ''))
                elif name.startswith('net.'):
                    targets.add(name)
                else:
                    raise ValueError('Unresolved Mixin target: ' + name)
            targets.update(re.findall(r'"(net\.[^"]+)"', annotation))
        command += sorted(targets)
    result = subprocess.run(command, env=env, timeout=120)
    return result.returncode


if __name__ == '__main__':
    raise SystemExit(main())
