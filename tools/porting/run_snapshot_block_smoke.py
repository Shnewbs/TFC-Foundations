"""Verify snapshot-only world-model contracts and CPU geometry against the exact target libraries."""
import hashlib
import json
import os
import re
from pathlib import Path
import shutil
import subprocess
import sys

ASSETS = Path('src/main/resources/assets')
MODELS = Path('src/main/java/net/dries007/tfc/client/model')
BLOCK_ENTITIES = Path('src/main/java/net/dries007/tfc/common/blockentities')
CONTRACTS = Path('tools/porting/snapshot-block-contracts.json')
PRODUCTION_MODELS = (
    'DynamicBlockModel', 'StaticBlockMesh', 'StaticBlockMeshBaker', 'SnapshotBlockStateModel',
    'SimpleStaticBlockEntityModel', 'IngotPileBlockModel', 'DoubleIngotPileBlockModel',
    'ScrapingBlockModel', 'MoldTableBlockModel',
)
GUARDED_FILES = {
    'tools/porting/PileLegacyReference.java', 'resources/assets.py',
    'src/main/java/net/dries007/tfc/client/ClientEventHandler.java',
    *(str(MODELS / (name + '.java')) for name in (*PRODUCTION_MODELS, 'BlockModelRegistration')),
    *(str(BLOCK_ENTITIES / (name + '.java')) for name in (
        'BlockEntityModelData', 'IngotPileBlockEntity', 'ScrapingBlockEntity', 'MoldTableBlockEntity')),
}


def digest(data):
    return hashlib.sha256(data).hexdigest()


def canonical(data):
    return digest(json.dumps(data, sort_keys=True, separators=(',', ':')).encode())


def method(text, signature):
    start = text.index(signature)
    opening = text.index('{', start)
    depth, end = 1, opening + 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


def without_dispatch(node):
    if isinstance(node, dict):
        return {k: without_dispatch(v) for k, v in node.items() if not (k == 'type' and v == 'tfc:dynamic')}
    if isinstance(node, list):
        return [without_dispatch(v) for v in node]
    return node


def validate_contracts(contracts):
    for name in ('IBakedGeometry', 'IStaticBakedModel'):
        if (MODELS / (name + '.java')).exists():
            raise AssertionError('Obsolete baked-model interface restored: ' + name)
    if digest(Path('tools/porting/PileLegacyReference.java').read_bytes()) != contracts['reference_sha256']:
        raise AssertionError('Frozen pile geometry oracle changed')
    for name, expected in contracts['unchanged_assets'].items():
        if digest(Path(name).read_bytes()) != expected:
            raise AssertionError('Unreviewed pile/scraping/mold geometry asset change: ' + name)
    for name, expected in contracts['blockstates'].items():
        if canonical(without_dispatch(json.loads(Path(name).read_text()))) != expected:
            raise AssertionError('Changed model IDs, rotations or multipart conditions: ' + name)
    mesh = (MODELS / 'StaticBlockMesh.java').read_text()
    for axis, expected in contracts['face_sources_sha256'].items():
        source = method(mesh, '    public static float[][] getTrapezoidalCuboid' + axis + 'Vertices(')
        if digest(source.encode()) != expected:
            raise AssertionError('Legacy face geometry/winding changed: ' + axis)
    for name, expected in contracts['save_writers_sha256'].items():
        source = method(Path(name).read_text(), 'void saveAdditional(')
        if digest(source.encode()) != expected:
            raise AssertionError('Unreviewed save writer change: ' + name)

    models = {}
    for path in ASSETS.glob('*/models/**/*.json'):
        namespace = path.relative_to(ASSETS).parts[0]
        name = namespace + ':' + path.relative_to(ASSETS / namespace / 'models').as_posix()[:-5]
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

    targets = contracts['targets']
    references = 0
    seen = set()
    def visit(node):
        nonlocal references
        if isinstance(node, dict):
            model = node.get('model', '')
            if loader(model) in targets.values():
                if node.get('type') != 'tfc:dynamic':
                    raise AssertionError('Snapshot world model bypasses dynamic dispatch: ' + model)
                references += 1
                seen.add(model)
            for value in node.values():
                visit(value)
        elif isinstance(node, list):
            for value in node:
                visit(value)
    for path in ASSETS.glob('*/blockstates/**/*.json'):
        visit(json.loads(path.read_text()))
    if not set(targets).issubset(seen):
        raise AssertionError('Missing snapshot model state references')
    for name in models:
        if ':item/' in name and loader(name) in ('tfc:ingot_pile', 'tfc:double_ingot_pile', 'tfc:scraping'):
            raise AssertionError('World-only item inheritance needs a separate ItemModel adapter: ' + name)

    handler = Path('src/main/java/net/dries007/tfc/client/ClientEventHandler.java').read_text()
    for call in ('bus.addListener(BlockModelRegistration::registerLoaders);',
                 'bus.addListener(BlockModelRegistration::registerBlockStateModels);',
                 'bus.addListener(MoldTableBlockModel::registerStandaloneModels);'):
        if handler.count(call) != 1:
            raise AssertionError('Missing or duplicate model registration listener: ' + call)
    registration = (MODELS / 'BlockModelRegistration.java').read_text()
    for value in ('IngotPileBlockModel.INSTANCE', 'DoubleIngotPileBlockModel.INSTANCE',
                  'ScrapingBlockModel.INSTANCE', 'MoldTableBlockModel.Loader.INSTANCE',
                  'DynamicBlockModel.Unbaked.CODEC'):
        if len(re.findall(r'\b' + re.escape(value) + r'\b', registration)) != 1:
            raise AssertionError('Missing or duplicate model registration: ' + value)
    generator = Path('resources/assets.py').read_text()
    for target in targets:
        if "{'model': '" + target + "', 'type': 'tfc:dynamic'}" not in generator:
            raise AssertionError('Resource generator drops snapshot dispatch: ' + target)

    common = (BLOCK_ENTITIES / 'BlockEntityModelData.java').read_text()
    if 'net.minecraft.client' in common or 'net.dries007.tfc.client' in common:
        raise AssertionError('Common model data depends on client classes')
    for token in ('textures = List.copyOf(textures);', 'inputColor |= 0xff000000;', 'outputColor |= 0xff000000;',
                  'level != null && level.isClientSide()', 'entity.requestModelDataUpdate();', 'level.sendBlockUpdated('):
        if token not in common:
            raise AssertionError('Immutable snapshot/refresh contract lost: ' + token)
    for name in PRODUCTION_MODELS:
        text = (MODELS / (name + '.java')).read_text()
        if any(token in text for token in ('getBlockEntity(', 'getOrCacheMetal(', 'getInventory(', 'getMoldStack(')):
            raise AssertionError('Model path reads live block entity: ' + name)
    cache = (MODELS / 'SnapshotBlockStateModel.java').read_text()
    for token in ('maximumWeight(4096)', 'new GeometryKey(this, value)', 'level.getModelData(pos)', 'FLAG_TRANSLUCENT | BakedQuad.FLAG_ANIMATED'):
        if token not in cache:
            raise AssertionError('Bounded snapshot cache/dispatch contract lost: ' + token)
    for name, property_name in (('IngotPileBlockEntity', 'PILE'), ('ScrapingBlockEntity', 'SCRAPING'), ('MoldTableBlockEntity', 'MOLD')):
        text = (BLOCK_ENTITIES / (name + '.java')).read_text()
        if 'BlockEntityModelData.' + property_name not in method(text, '    public ModelData getModelData()'):
            raise AssertionError('Block entity no longer supplies a snapshot: ' + name)
        if 'BlockEntityModelData.refresh(this);' not in method(text, 'void loadAdditional('):
            raise AssertionError('Load no longer refreshes model data: ' + name)
    pile = (BLOCK_ENTITIES / 'IngotPileBlockEntity.java').read_text()
    for signature in ('    public void addIngot(', '    public void removeAllIngots(', '    public ItemStack removeIngot('):
        if 'BlockEntityModelData.refresh(this);' not in method(pile, signature):
            raise AssertionError('Pile mutation does not refresh snapshot: ' + signature)
    scraping = (BLOCK_ENTITIES / 'ScrapingBlockEntity.java').read_text()
    if 'if (level.isClientSide()) requestModelDataUpdate();' not in method(scraping, '    public void onClicked('):
        raise AssertionError('Scraping click does not refresh snapshot')
    if method(scraping, '    public boolean dye(').count('BlockEntityModelData.refresh(this);') != 2:
        raise AssertionError('Scraping dye does not refresh both colors')
    table = (MODELS / 'MoldTableBlockModel.java').read_text()
    for token in ('this.molds = Map.copyOf(molds);', 'discovered = Map.copyOf(catalog);',
                  'catalog = discovered;', 'catalog.values().forEach(resolver::markDependency);',
                  'final JsonObject vanilla = json.deepCopy();', 'new GeometryKey(this, mold(item))',
                  'UnbakedElementsHelper.composeRootTransformIntoModelState(state, root)'):
        if token not in table:
            raise AssertionError('Mold reload/dependency contract lost: ' + token)
    if 'net.dries007.tfc.client' in (BLOCK_ENTITIES / 'MoldTableBlockEntity.java').read_text():
        raise AssertionError('Mold table common source depends on client classes')
    return references


def main():
    out = Path('port-diagnostics')
    contracts = json.loads(CONTRACTS.read_text())
    references = validate_contracts(contracts)
    cp_file = out / 'classpath.txt'
    if not cp_file.is_file():
        raise SystemExit('Resolve the exact target classpath first.')
    cp = cp_file.read_text().strip()
    classes = out / 'snapshot-block-smoke-classes'
    empty = out / 'snapshot-block-empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    home = os.environ.get('JAVA_HOME')
    tool = lambda name: str(Path(home) / 'bin' / name) if home else name
    sources = [MODELS / (name + '.java') for name in PRODUCTION_MODELS]
    sources += [BLOCK_ENTITIES / 'BlockEntityModelData.java']
    sources += [Path('tools/porting/' + name + '.java') for name in ('PileLegacyReference', 'SnapshotBlockSmoke')]
    commands = [
        [sys.executable, '-m', 'unittest', 'discover', '-s', 'tools/porting', '-p', 'test_snapshot_block_contracts.py'],
        [tool('javac'), '--release', '25', '-proc:none', '-encoding', 'UTF-8', '-sourcepath', str(empty),
         '-classpath', cp, '-d', str(classes), *(str(path) for path in sources)],
        [tool('java'), '-ea', '-Xmx512M', '-classpath', str(classes) + os.pathsep + cp, 'SnapshotBlockSmoke'],
    ]
    with (out / 'snapshot-block-smoke.log').open('w') as log:
        for command in commands:
            result = subprocess.run(command, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, timeout=180)
            log.write(result.stdout)
            print(result.stdout, end='')
            if result.returncode:
                return result.returncode
        message = f'PASS: {references} snapshot state references, 20 unchanged model assets, four state structures, three face bodies and three unchanged save writers.\n'
        log.write(message)
        print(message, end='')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
