"""Actual save-utility/API execution plus explicit entity source contracts, not gameplay."""
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys


def masked(text):
    """Mask comments/literals without changing offsets for a balanced method scan."""
    pattern = r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/'
    return re.sub(pattern, lambda match: ' ' * len(match[0]), text)


def method_body(text, name, parameter_type):
    clean = masked(text)
    match = re.search(r'\b' + re.escape(name) + r'\(\s*' + parameter_type + r'\s+(\w+)\s*\)\s*\{', clean)
    if not match:
        raise AssertionError('Missing target hook: ' + name + '(' + parameter_type + ')')
    depth, index = 1, match.end()
    while depth and index < len(clean):
        depth += (clean[index] == '{') - (clean[index] == '}')
        index += 1
    if depth:
        raise AssertionError('Unbalanced method body: ' + name)
    return match[1], text[match.end():index - 1]


def source_contracts(root=Path('.')):
    contracts = json.loads((root / 'tools/porting/entity-save-contracts.json').read_text())['hooks']
    for contract in contracts:
        text = (root / contract['path']).read_text()
        parameter, body = method_body(text, contract['method'], contract['parameter_type'])
        keys = set(re.findall(r'\b' + parameter + r'\.(?:put\w*|get\w*|store\w*|read|contains|child\w*)\(\s*"([^"\\]+)"', body))
        keys.update(re.findall(r'\b(?:EntityHelpers|NbtHelpers)\.\w+\(\s*' + parameter + r'\s*,\s*"([^"\\]+)"', body))
        missing = set(contract['keys']) - keys
        if missing:
            raise AssertionError(f"Lost literal save keys in {contract['path']}:{contract['method']}: {sorted(missing)}")
    base = root / 'src/main/java/net/dries007/tfc/common/entities'
    for relative in ('livestock/OviparousAnimal.java', 'prey/WingedPrey.java'):
        text = (base / relative).read_text()
        _, body = method_body(text, 'readAdditionalSaveData', 'ValueInput')
        if not re.search(r'lastPlucked\s*=\s*EntityHelpers.getLongOrDefault\(\w+,\s*"plucked",\s*Long.MIN_VALUE\)', body):
            raise AssertionError('Persisted plucking cooldown must be assigned: ' + relative)
    horse = (base / 'livestock/horse/TFCChestedHorse.java').read_text()
    _, body = method_body(horse, 'readAdditionalSaveData', 'ValueInput')
    if body.index('setChestItem(') > body.index('super.readAdditionalSaveData('):
        raise AssertionError('Horse chest must be restored before superclass inventory loading.')
    pet = (base / 'livestock/pet/TamableMammal.java').read_text()
    if 'OPTIONAL_LIVING_ENTITY_REFERENCE' not in pet or 'setOwnerUUID(tag.read("Owner", UUIDUtil.CODEC).orElse(null));' not in pet:
        raise AssertionError('Native pet owner reference or clearing missing owner regressed.')
    if 'setGenes(nbt.read("genes", CompoundTag.CODEC).orElse(null));' not in (base / 'livestock/MammalProperties.java').read_text():
        raise AssertionError('Missing genes must clear previous state.')
    for path in base.rglob('*.java'):
        text = path.read_text()
        if re.search(r'\b(?:addAdditionalSaveData|readAdditionalSaveData|saveCommonAnimalData|readCommonAnimalData)\(CompoundTag', text):
            raise AssertionError('Legacy entity hook remains: ' + str(path))
        if re.search(r'protected\s+Brain.Provider<[^\n]+\s+brainProvider\(', text) or 'makeBrain(Dynamic<' in text:
            raise AssertionError('Legacy brain construction remains: ' + str(path))
    frog = (base / 'prey/TFCFrogAi.java').read_text()
    if 'new ArrayList<>(getActivities())' not in frog or 'activity.activityType() == Activity.IDLE' not in frog:
        raise AssertionError('Frog must retain native non-idle activities when replacing idle.')
    cat = (base / 'livestock/pet/TFCCat.java').read_text()
    if 'BuiltInRegistries.CAT_VARIANT' in cat or 'CatVariantTags' in cat or 'assetInfo(false).texturePath()' not in cat:
        raise AssertionError('Cat data-driven registry or TFC model UV contract regressed.')
    return len(contracts)


def main():
    out = Path('port-diagnostics')
    cp_file = out / 'classpath.txt'
    if not cp_file.is_file():
        raise SystemExit('Resolve the exact target classpath first.')
    cp = cp_file.read_text().strip()
    classes = out / 'entity-smoke-classes'
    empty = out / 'entity-smoke-empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    java_bin = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else None

    def tool(name):
        return str(java_bin / name) if java_bin else name

    commands = [
        [sys.executable, 'tools/porting/test_entity_contracts.py'],
        [tool('javac'), '--release', '25', '-proc:none', '-classpath', cp,
         '-sourcepath', str(empty), '-d', str(classes),
         'src/main/java/net/dries007/tfc/util/NbtHelpers.java', 'tools/porting/EntitySaveSmoke.java'],
        [tool('java'), '-ea', '-classpath', str(classes) + os.pathsep + cp, 'EntitySaveSmoke'],
    ]
    log = out / 'entity-smoke.log'
    with log.open('w') as output:
        for command in commands:
            output.write('Running ' + Path(command[0]).name + ' against the resolved target classpath.\n')
            output.flush()
            result = subprocess.run(command, stdout=output, stderr=subprocess.STDOUT, text=True, timeout=90)
            if result.returncode:
                print(log.read_text())
                return result.returncode
        hooks = source_contracts()
        output.write(f'PASS: {hooks} entity save-hook signature/key source contracts; cooldown assignment, owner/genes clearing, and horse load-order guards.\n')
        # Record native constructor order; checking source factories alone misses lost saved memories.
        result = subprocess.run([tool('javap'), '-c', '-p', '-classpath', cp, 'net.minecraft.world.entity.ai.Brain'],
                                text=True, capture_output=True, timeout=90)
        (out / 'entity-brain-api.txt').write_text(result.stdout + result.stderr)
        if result.returncode:
            return result.returncode
        constructor = result.stdout.split('protected net.minecraft.world.entity.ai.Brain(', 1)[1].split('private void registerMemory', 1)[0]
        if constructor.index('Method addActivity:') > constructor.index('Method setMemoryInternal:'):
            raise AssertionError('Re-audit target API: activities must precede saved-memory restoration.')
        if 'Method setCoreActivities:' not in constructor or 'Method useDefaultActivity:' not in constructor:
            raise AssertionError('Re-audit native brain default activity initialization.')
        output.write('PASS: exact target Brain constructor registers activities before saved memories and initializes core/default activity.\n')
    text = log.read_text()
    count = int(re.search(r'PASS: (\d+) standalone entity-save', text)[1])
    report = {'standalone_checks': count, 'source_hook_contracts': hooks, 'exit_code': 0,
              'scope': 'Real utility/target API execution, source contracts and native bytecode inspection. No entity or game runtime is bootstrapped.'}
    (out / 'entity-smoke.json').write_text(json.dumps(report, indent=2) + '\n')
    print(text)
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
