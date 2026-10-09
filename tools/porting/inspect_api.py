"""Record public signatures from the exact resolved target classpath, not a guessed version."""
import os
from pathlib import Path
import subprocess
import zipfile

OUT = Path('port-diagnostics')
CLASSPATH = OUT / 'classpath.txt'
NAMES = {
    'LootContext', 'LootParams', 'LootItemCondition', 'LootItemFunction',
    'LootItemConditionalFunction', 'NumberProvider', 'NumberProviders',
    'LootContextUser', 'ValidationContext', 'Validatable', 'ContextKey',
    'ContextKeySet', 'DeferredRegister', 'DeferredHolder', 'Registry',
    'LootContextParams', 'LootContextParamSets', 'NullMarked', 'Nullable',
    'EnumProperty', 'ContainerInput', 'TeleportTransition', 'IdentifierException',
}


def main():
    if not CLASSPATH.is_file():
        raise SystemExit('Target classpath unavailable; API inspection did not run.')
    cp = CLASSPATH.read_text().strip()
    classes = set()
    for entry in cp.split(os.pathsep):
        path = Path(entry)
        if path.suffix != '.jar' or not path.is_file():
            continue
        with zipfile.ZipFile(path) as jar:
            for name in jar.namelist():
                if name.startswith(('net/minecraft/', 'net/neoforged/', 'org/jspecify/')) and name.endswith('.class'):
                    classes.add(name[:-6].replace('/', '.'))
    (OUT / 'target-classes.txt').write_text('\n'.join(sorted(classes)) + '\n')
    selected = sorted(name for name in classes if name.rsplit('.', 1)[-1] in NAMES)
    result = subprocess.run(['javap', '-classpath', cp, '-public', *selected], text=True, capture_output=True, timeout=90)
    (OUT / 'target-api.txt').write_text(result.stdout + result.stderr)
    print(f'Inspected {len(selected)} API types from {len(classes)} target classes; javap exit {result.returncode}.')
    raise SystemExit(result.returncode)


if __name__ == '__main__':
    main()
