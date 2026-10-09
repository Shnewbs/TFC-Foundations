"""Exercise production livestock models and exact native render APIs without a game bootstrap."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import struct
import subprocess
import zipfile

ROOT = Path('src/main/java/net/dries007/tfc/client')
MODELS = ROOT / 'model/entity'
STATE = ROOT / 'render/entity/state'


def method_body(source, signature):
    start = source.index('{', source.index(signature))
    end, depth = start + 1, 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


def validate_contracts(contracts):
    for name, expected in contracts.items():
        source = (MODELS / (name + '.java')).read_text()
        body = method_body(source, 'public static LayerDefinition createBodyLayer()')
        if hashlib.sha256(body.encode()).hexdigest() != expected['geometry_sha256']:
            raise AssertionError('Unreviewed livestock geometry change: ' + name)
        for forbidden in ('net.dries007.tfc.common.entities', 'AgeableListModel', 'prepareMobModel(', '.getBrain(', '.level('):
            if forbidden in source:
                raise AssertionError('Legacy or live-world model dependency: ' + name + ': ' + forbidden)
    for name in ('TFCPigModel', 'TFCGoatModel', 'TFCCatModel'):
        source = (MODELS / (name + '.java')).read_text()
        if 'net.dries007.tfc.common.entities' in source or 'prepareMobModel(' in source:
            raise AssertionError('Live-world/native entity model regression: ' + name)
    extractor = (STATE / 'TFCAnimalRenderStateExtractor.java').read_text()
    for statement in (
        'state.resetCustomState();',
        'state.femaleCharacteristics = animal.displayFemaleCharacteristics();',
        'state.geneticSizeScale = LivestockRenderStateMath.geneticScale(animal.getGeneticSize());',
        'state.hasProduct = animal.hasProduct();',
        'state.wingFlap = LivestockRenderStateMath.wingFlap(bird.oFlap, bird.flap, bird.oFlapSpeed, bird.flapSpeed, partialTick);',
    ):
        if statement not in extractor:
            raise AssertionError('Missing extraction/reset contract: ' + statement)
    renderer = (ROOT / 'render/entity/AnimalRenderer.java').read_text()
    if not renderer.index('super.extractRenderState(') < renderer.index('TFCAnimalRenderStateExtractor.extract('):
        raise AssertionError('Livestock extraction order must be native then custom')
    if 'getBob(' in (ROOT / 'render/entity/OviparousRenderer.java').read_text() or 'state.ageInTicks =' in extractor:
        raise AssertionError('Wing phase must not overwrite native ageInTicks')
    gendered = (ROOT / 'render/entity/GenderedRenderer.java').read_text()
    for statement in ('if (baby != null && state.isBaby) return baby;', 'state.isMale', '(state.isOld ? maleOld : maleYoung)'):
        if statement not in gendered:
            raise AssertionError('Livestock texture precedence changed: ' + statement)
    for name in ('TFCCodRenderer', 'TFCSalmonRenderer', 'TFCPufferfishRenderer', 'TFCTropicalFishRenderer'):
        source = (ROOT / 'render/entity' / (name + '.java')).read_text()
        if not source.index('super.extractRenderState(') < source.index('GuideRenderState.captureOrigin(state, entity.position());'):
            raise AssertionError('Fish guide exemption must capture the actual position after native extraction: ' + name)
        if 'if (!GuideRenderState.isAtOrigin(state))' not in source:
            raise AssertionError('Fish guide exemption not used: ' + name)
    cat = (ROOT / 'render/entity/TFCCatRenderer.java').read_text()
    for statement in ('state.hasOwner = cat.getOwnerUUID() != null;', 'state.sleeping = cat.isSleeping();',
                      'state.isSitting = cat.isSitting();', 'state.isCrouching = cat.isCrouching();',
                      'state.isSprinting = cat.isSprinting();', 'state.lieDownAmount = state.sleeping ? 1F : 0;',
                      'state.lieDownAmountTail = state.sleeping ? 0.87F : 0;', 'state.relaxStateOneAmount = 0;'):
        if statement not in cat:
            raise AssertionError('Cat capture must overwrite its reusable appearance/pose inputs: ' + statement)
    collar = (MODELS / 'TFCCatCollarLayer.java').read_text()
    if 'state.hasOwner && !state.isInvisible' not in collar or 'state.collarColor, 1)' not in collar:
        raise AssertionError('Cat collar owner/invisibility/tint handling changed')


def main():
    out = Path('port-diagnostics')
    cp_file = out / 'classpath.txt'
    if not cp_file.is_file():
        raise SystemExit('Resolve the exact target classpath first.')
    cp = cp_file.read_text().strip()
    contracts = json.loads(Path('tools/porting/livestock-model-contracts.json').read_text())['models']
    validate_contracts(contracts)
    classes = out / 'livestock-render-smoke-classes'
    empty = out / 'livestock-render-empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    models = [*contracts, 'TFCPigModel', 'TFCGoatModel']
    sources = [MODELS / (name + '.java') for name in [*models, 'AgeableModelTransforms', 'LivestockModel', 'TFCCatModel']]
    sources += [STATE / (name + '.java') for name in ('TFCAnimalRenderState', 'TFCCatRenderState', 'LivestockRenderStateMath', 'GuideRenderState')]
    # These are the actual renderer classes, not simplified stand-ins.
    sources += [ROOT / 'render/entity' / (name + '.java') for name in ('TFCCodRenderer', 'TFCSalmonRenderer', 'TFCPufferfishRenderer', 'TFCTropicalFishRenderer')]
    sources += [Path('tools/porting/LivestockRenderSmoke.java')]
    tools = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else None

    def tool(name):
        return str(tools / name) if tools else name

    commands = [
        [tool('javac'), '--release', '25', '-proc:none', '-classpath', cp, '-sourcepath', str(empty), '-d', str(classes), *map(str, sources)],
        [tool('java'), '-ea', '-classpath', str(classes) + os.pathsep + cp, 'LivestockRenderSmoke', *models],
    ]
    log = out / 'livestock-render-smoke.log'
    with log.open('w') as stream:
        for command in commands:
            stream.write('Running ' + Path(command[0]).name + ' against the exact target, without game stubs.\n')
            stream.flush()
            result = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT, timeout=120)
            if result.returncode:
                print(log.read_text())
                return result.returncode
        assets = {
            'assets/minecraft/textures/entity/cat/cat_collar.png': (64, 32),
            'assets/minecraft/textures/entity/cat/cat_tabby.png': (64, 32),
            'assets/minecraft/textures/entity/squid/squid.png': (64, 32),
            'assets/minecraft/textures/entity/squid/glow_squid.png': (64, 32),
        }
        checked = set()
        for entry in cp.split(os.pathsep):
            path = Path(entry)
            if path.suffix != '.jar' or not path.is_file():
                continue
            with zipfile.ZipFile(path) as jar:
                names = set(jar.namelist())
                for asset, dimensions in assets.items():
                    if asset in names:
                        data = jar.read(asset)
                        if data[:8] != b'\x89PNG\r\n\x1a\n' or struct.unpack('>II', data[16:24]) != dimensions:
                            raise AssertionError('Native texture layout changed: ' + asset)
                        checked.add(asset)
        if checked != assets.keys():
            raise AssertionError('Native textures missing: ' + str(assets.keys() - checked))
        stream.write(f'PASS: {len(contracts)} preserved geometry factories, four actual fish renderer compilations, four native texture layouts and source capture contracts.\n')
    text = log.read_text()
    match = re.search(r'PASS: (\d+) livestock/cat models; (\d+) scenarios; (\d+) assertions; (\d+) finite CPU vertices\.', text)
    if match is None:
        raise AssertionError('Livestock runtime test did not report completion')
    report = dict(zip(('models_executed', 'model_scenarios', 'model_assertions', 'cpu_vertices'), map(int, match.groups())))
    report.update({'exit_code': 0, 'geometry_contracts': len(contracts), 'native_textures': len(checked),
                   'native_fish_renderers_compiled': 4,
                   'scope': 'Actual production model construction, poses, CPU vertices and detached helper execution. No live entities, renderer construction, GPU rendering or gameplay.'})
    (out / 'livestock-render-smoke.json').write_text(json.dumps(report, indent=2) + '\n')
    print(text)
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
