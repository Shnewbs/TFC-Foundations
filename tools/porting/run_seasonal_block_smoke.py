"""Verify scalar parity, native unbaked model geometry and asset dispatch without bootstrapping a game."""
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys

ROOT = Path('src/main/resources/assets')
MODEL_ROOT = Path('src/main/java/net/dries007/tfc/client/model')
CONTRACTS = Path('tools/porting/seasonal-model-contracts.json')


def validate_assets(contracts):
    def digest(value):
        return hashlib.sha256(json.dumps(value, sort_keys=True, separators=(',', ':')).encode()).hexdigest()

    model_hashes = sorted((name, hashlib.sha256(Path(name).read_bytes()).hexdigest()) for name in contracts['unchanged_model_assets'])
    if digest(model_hashes) != contracts['model_assets_sha256']:
        raise AssertionError('Unreviewed seasonal/mold model definition change')
    reference = Path('tools/porting/SeasonalLegacyReference.java')
    if hashlib.sha256(reference.read_bytes()).hexdigest() != contracts['reference_sha256']:
        raise AssertionError('Frozen pre-port seasonal oracle changed')
    models = {}
    for path in ROOT.glob('*/models/**/*.json'):
        namespace = path.relative_to(ROOT).parts[0]
        name = namespace + ':' + path.relative_to(ROOT / namespace / 'models').as_posix()[:-5]
        models[name] = json.loads(path.read_text())

    def loader(name, visited=()):
        if name in visited:
            raise AssertionError('Cyclic model ancestry: ' + name)
        model = models.get(name, {})
        if 'loader' in model:
            return model['loader']
        parent = model.get('parent', '')
        parent = parent if ':' in parent else 'minecraft:' + parent
        return loader(parent, (*visited, name)) if parent in models else None

    seasonal = {name for name in models if loader(name) in ('tfc:plant', 'tfc:leaves')}
    references = 0

    def visit(node):
        nonlocal references
        if isinstance(node, dict):
            if node.get('model') in seasonal:
                if node.get('type') != 'tfc:dynamic':
                    raise AssertionError('Seasonal world model bypasses dynamic dispatch: ' + node['model'])
                references += 1
            elif node.get('type') == 'tfc:dynamic' and loader(node.get('model', '')) not in ('tfc:ingot_pile', 'tfc:double_ingot_pile', 'tfc:scraping', 'tfc:mold_table'):
                raise AssertionError('Unexpected dynamic dispatch target: ' + str(node.get('model')))
            for value in node.values():
                visit(value)
        elif isinstance(node, list):
            for value in node:
                visit(value)

    def without_dispatch(node):
        if isinstance(node, dict):
            return {k: without_dispatch(v) for k, v in node.items() if not (k == 'type' and v == 'tfc:dynamic')}
        if isinstance(node, list):
            return [without_dispatch(v) for v in node]
        return node

    blockstates = sorted((name, without_dispatch(json.loads(Path(name).read_text()))) for name in contracts['blockstates'])
    if digest(blockstates) != contracts['blockstate_structures_sha256']:
        raise AssertionError('Changed model IDs, weights, rotations or multipart conditions')
    for path in ROOT.glob('*/blockstates/**/*.json'):
        visit(json.loads(path.read_text()))
    for name in models:
        if ':item/' in name and loader(name) in ('tfc:plant', 'tfc:leaves'):
            raise AssertionError('A seasonal item needs its own ItemModel adapter: ' + name)
    handler = Path('src/main/java/net/dries007/tfc/client/ClientEventHandler.java').read_text()
    for call in ('bus.addListener(BlockModelRegistration::registerLoaders);',
                 'bus.addListener(BlockModelRegistration::registerBlockStateModels);'):
        if handler.count(call) != 1:
            raise AssertionError('Missing or duplicate model registration listener: ' + call)
    registration = (MODEL_ROOT / 'BlockModelRegistration.java').read_text()
    for value in ('PlantBlockModel.Loader.INSTANCE', 'LeavesBlockModel.Loader.INSTANCE',
                  'new MoldsModelLoader()', 'DynamicBlockModel.Unbaked.CODEC'):
        if registration.count(value) != 1:
            raise AssertionError('Missing or duplicate model registration: ' + value)
    for name in ('PlantBlockModel', 'LeavesBlockModel'):
        text = (MODEL_ROOT / (name + '.java')).read_text()
        for forbidden in ('IDynamicBakedModel', 'IUnbakedGeometry', 'getQuads(', 'BakedModelData', 'ItemOverrides'):
            if forbidden in text:
                raise AssertionError('Legacy model path restored: ' + name + ': ' + forbidden)
    leaves = (MODEL_ROOT / 'LeavesBlockModel.java').read_text()
    for call in ('if (!ClientHelpers.useFancyGraphics())', 'block instanceof TFCLeavesBlock',
                 'SolarCalculator.getInNorthernHemisphere(pos.getZ()', '912381187503828153L',
                 '836494187578334123L', 'SeasonalModelMath.needsAverageRainfall', 'super(parts, 4, 3);'):
        if call not in leaves:
            raise AssertionError('Leaf climate/graphics capture changed: ' + call)
    plant = (MODEL_ROOT / 'PlantBlockModel.java').read_text()
    for call in ('new BlockPos(pos.getX(), 0, pos.getZ())', '836494186029734123L',
                 'plant.isWetSeasonBlooming()', 'randomScale > 0.25f',
                 'stage == 3 && startTime != endTime ? getModelByDayTime', 'if (level == null) return getModelFromCalendar();'):
        if call not in plant:
            raise AssertionError('Plant capture/default/lazy daytime contract changed: ' + call)
    return len(seasonal), references


def main():
    out = Path('port-diagnostics')
    cp_file = out / 'classpath.txt'
    if not cp_file.is_file():
        raise SystemExit('Resolve the exact target classpath first.')
    contracts = json.loads(CONTRACTS.read_text())
    models, references = validate_assets(contracts)
    classes = out / 'seasonal-block-smoke-classes'
    empty = out / 'seasonal-block-empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    java_home = os.environ.get('JAVA_HOME')
    tools = Path(java_home) / 'bin' if java_home else None
    tool = lambda name: str(tools / name) if tools else name
    cp = cp_file.read_text().strip()
    sources = [MODEL_ROOT / (name + '.java') for name in (
        'DynamicBlockModel', 'SeasonalModelMath', 'SeasonalBlockStateModel', 'SeasonalUnbakedModel', 'MoldsModelLoader')]
    sources += [Path('tools/porting/' + name + '.java') for name in ('SeasonalLegacyReference', 'SeasonalBlockModelSmoke')]
    commands = [
        [sys.executable, '-m', 'unittest', 'discover', '-s', 'tools/porting', '-p', 'test_seasonal_contracts.py'],
        [tool('javac'), '--release', '25', '-proc:none', '-encoding', 'UTF-8', '-sourcepath', str(empty),
         '-classpath', cp, '-d', str(classes), *(str(path) for path in sources)],
        [tool('java'), '-ea', '-classpath', str(classes) + os.pathsep + cp, 'SeasonalBlockModelSmoke'],
    ]
    with (out / 'seasonal-block-smoke.log').open('w') as log:
        for command in commands:
            result = subprocess.run(command, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=180)
            log.write(result.stdout)
            print(result.stdout, end='')
            if result.returncode:
                return result.returncode
        message = f'PASS: {models} seasonal model definitions, {references} dynamic state references, {len(contracts["blockstates"])} preserved blockstate structures.\n'
        log.write(message)
        print(message, end='')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
