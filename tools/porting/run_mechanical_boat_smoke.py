"""Validate actual mechanical/boat/equine models against the resolved target, without game stubs."""
import hashlib
import json
import os
from pathlib import Path
import shutil
import struct
import subprocess
import zipfile

ROOT = Path('src/main/java/net/dries007/tfc/client')
MODELS = ROOT / 'model/entity'


def method(text, signature):
    start = text.index(signature)
    opening = text.index('{', start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


def check_contracts():
    contracts = json.loads(Path('tools/porting/mechanical-boat-contracts.json').read_text())
    for name, expected in contracts['geometry_factories'].items():
        actual = method((MODELS / (name + '.java')).read_text(), '    public static LayerDefinition createBodyLayer()')
        if hashlib.sha256(actual.encode()).hexdigest() != expected:
            raise AssertionError('Unreviewed geometry change: ' + name)
    geometry = (ROOT / 'render/blockentity/AxleRenderGeometry.java').read_text()
    original = (ROOT / 'RenderHelpers.java').read_text()
    for axis in 'XYZ':
        a = method(geometry, '    private static float[][] get' + axis + 'Vertices(').replace('private static', 'public static', 1)
        b = method(original, '    public static float[][] get' + axis + 'Vertices(')
        if a != b:
            raise AssertionError('Axle/blade face ordering changed: ' + axis)
    for name in ('WindmillBladeModel', 'WindmillBladeLatticeModel', 'WindmillBladeRusticModel', 'WaterWheelModel', 'TFCChestedHorseModel', 'BoatChestModel'):
        text = (MODELS / (name + '.java')).read_text()
        if any(token in text for token in ('getRotationAngle(', 'getChestItem(', 'getLevel(', 'common.entities.', 'common.blockentities.')):
            raise AssertionError('Model reads live state: ' + name)
    for name in ('AxleBlockEntityRenderer', 'BladedAxleBlockEntityRenderer', 'WaterWheelBlockEntityRenderer', 'WindmillBlockEntityRenderer'):
        text = (ROOT / 'render/blockentity' / (name + '.java')).read_text()
        submit = method(text, '    public void submit(')
        if any(token in submit for token in ('getRotationAngle(', 'getInventory(', 'getBlockState(', 'getLevel(')):
            raise AssertionError('Submit reads live block entity: ' + name)
        if 'BlockEntityRenderer.super.extractRenderState(' not in text:
            raise AssertionError('Native base extraction omitted: ' + name)
    boats = (ROOT / 'render/entity/TFCBoatRenderer.java').read_text()
    chest = (ROOT / 'render/entity/TFCChestBoatRenderer.java').read_text()
    if '((TFCBoatRenderState) state).chestTexture = null;' not in boats:
        raise AssertionError('Boat attachment state no longer clears')
    if 'super.submitTypeAdditions(state, poses, collector, light);' not in chest:
        raise AssertionError('Chest boats lost the native water mask')
    if 'hull instanceof RaftModel ? null' not in boats or '!state.isUnderWater' not in boats:
        raise AssertionError('Raft/underwater water-mask conditions changed')
    if 'getChestItem(' in method(chest, '    protected void submitTypeAdditions('):
        raise AssertionError('Boat submit reads live inventory')
    mill = (ROOT / 'render/blockentity/WindmillBlockEntityRenderer.java').read_text()
    for required in ('state.blades = List.of();', 'state.fullIdenticalSet = false;', 'count == 5', 'first != provider.model()', 'List.copyOf(blades)', 'state.fullIdenticalSet && provider.model().hasExtras()', '0xff000000 | provider.color().getTextureDiffuseColor()', 'new WindmillBladeModel.BladePose(blade.angle(), portion)'):
        if required not in mill:
            raise AssertionError('Windmill snapshot/tint/extra contract lost: ' + required)
    for portion, color in (('FRAME', '-1'), ('BLADE', 'color'), ('EXTRAS', '-1')):
        if f'WindmillBladeModel.Portion.{portion}, {color}' not in mill:
            raise AssertionError('Windmill pass tint changed: ' + portion)
    horse = (ROOT / 'render/entity/TFCChestedHorseRenderer.java').read_text()
    if 'state.hasChest = state.chestTexture != null;' not in horse:
        raise AssertionError('TFC carried items narrowed to vanilla wooden chests')
    if 'state.chestTexture = HorseChestLayer.textureFor(horse.getChestItem());' not in horse:
        raise AssertionError('Horse carried appearance is not overwritten on capture')
    print('PASS: four retained model factories, three legacy face-order contracts and capture/submission guards.')


def check_textures(cp):
    expected = {}
    for name in ('white', 'creamy', 'chestnut', 'brown', 'black', 'gray', 'darkbrown'):
        for suffix in ('', '_baby'):
            expected[f'assets/minecraft/textures/entity/horse/horse_{name}{suffix}.png'] = (64, 64)
    for name in ('donkey', 'mule'):
        expected[f'assets/minecraft/textures/entity/horse/{name}.png'] = (64, 64)
    found = set()
    for entry in cp.split(os.pathsep):
        path = Path(entry)
        if not path.is_file() or path.suffix != '.jar':
            continue
        with zipfile.ZipFile(path) as jar:
            for name in expected.keys() & set(jar.namelist()):
                if struct.unpack('>II', jar.read(name)[16:24]) != expected[name]:
                    raise AssertionError('Unexpected native texture dimensions: ' + name)
                found.add(name)
    if found != set(expected):
        raise AssertionError('Missing native equine textures: ' + str(set(expected) - found))
    for subdir, size in (('boat', (128, 64)), ('chest_boat', (128, 128)), ('chest/horse', (64, 64))):
        images = list(Path('src/main/resources/assets/tfc/textures/entity', subdir).glob('*.png'))
        if not images:
            raise AssertionError('Missing TFC attachment textures: ' + subdir)
        for image in images:
            if struct.unpack('>II', image.read_bytes()[16:24]) != size:
                raise AssertionError('Unexpected attachment layout: ' + str(image))
    print('PASS: 16 native horse/donkey/mule texture layouts and all TFC hull/carried-chest texture dimensions.')


def main():
    out = Path('port-diagnostics')
    cp_file = out / 'classpath.txt'
    if not cp_file.is_file():
        raise SystemExit('Resolve the exact target first with writePortClasspath.')
    cp = cp_file.read_text().strip()
    check_contracts()
    check_textures(cp)
    classes = out / 'mechanical-boat-smoke-classes'
    empty = out / 'mechanical-boat-empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    sources = [MODELS / (name + '.java') for name in ('BoatChestModel', 'WaterWheelModel', 'WindmillBladeModel',
        'WindmillBladeLatticeModel', 'WindmillBladeRusticModel', 'TFCChestedHorseModel', 'HorseChestLayer', 'AgeableModelTransforms')]
    sources += [ROOT / 'render/entity/state' / (name + '.java') for name in ('TFCBoatRenderState', 'TFCChestedHorseRenderState')]
    sources += [ROOT / 'render/entity' / (name + '.java') for name in ('TFCBoatRenderer', 'TFCHorseRenderer')]
    sources += [ROOT / 'render/blockentity/AxleRenderGeometry.java', Path('tools/porting/MechanicalBoatSmoke.java')]
    java_home = os.environ.get('JAVA_HOME')
    def tool(name):
        return str(Path(java_home) / 'bin' / name) if java_home else name
    commands = [
        [tool('javac'), '--release', '25', '-proc:none', '-classpath', cp, '-sourcepath', str(empty), '-d', str(classes), *map(str, sources)],
        [tool('java'), '-ea', '-classpath', str(classes) + os.pathsep + cp, 'MechanicalBoatSmoke'],
    ]
    log = out / 'mechanical-boat-smoke.log'
    with log.open('w') as stream:
        for command in commands:
            stream.write('Running ' + Path(command[0]).name + ' against exact target classes; no game stubs.\n')
            stream.flush()
            try:
                result = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT, timeout=120)
            except subprocess.TimeoutExpired:
                stream.write('TIMEOUT: test run is incomplete.\n')
                return 124
            if result.returncode:
                print(log.read_text())
                return result.returncode
    print(log.read_text())
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
