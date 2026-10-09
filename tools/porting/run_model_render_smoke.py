"""Compile and exercise real hierarchical models without bootstrapping a game client."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import struct
import subprocess
import zipfile

ROOT = Path('src/main/java/net/dries007/tfc')
MODELS = ROOT / 'client/model/entity'
STATE = ROOT / 'client/render/entity/state'


def method_body(text, start):
    begin = text.index('{', start)
    end, depth = begin + 1, 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[begin:end]


def validate_contracts(contracts):
    for name, expected in contracts.items():
        source = (MODELS / (name + '.java')).read_text()
        match = re.search(r'public static LayerDefinition createBodyLayer\(\)', source)
        if match is None:
            raise AssertionError('Missing production geometry factory: ' + name)
        geometry = method_body(source, match.start())
        animations = re.findall(r'(?:public |private |protected )?static final AnimationDefinition\s+\w+\s*=.*?\.build\(\);', source, re.S)
        for key, value in [('geometry_sha256', geometry), ('animation_sha256', '\n'.join(animations))]:
            if hashlib.sha256(value.encode()).hexdigest() != expected[key]:
                raise AssertionError('Unreviewed geometry/keyframe change: ' + name + ':' + key)
        if re.search(r'net\.dries007\.tfc\.common\.entities|\bCalendars\b|\bClientHelpers\b|\bEntityHelpers\b|\.getBrain\(|\.level\(', source):
            raise AssertionError('Live-world dependency returned to model: ' + name)
    extractor = (STATE / 'TFCAnimalRenderStateExtractor.java').read_text()
    state = (STATE / 'TFCAnimalRenderState.java').read_text()
    for animation in re.findall(r'public final AnimationState (\w+) =', state):
        if 'state.' + animation + '.copyFrom(' not in extractor:
            raise AssertionError('Extraction must copy the animation timeline: ' + animation)
        if animation + '.stop();' not in state:
            raise AssertionError('State reuse must stop missing animation: ' + animation)
    renderer = (ROOT / 'client/render/entity/SimpleMobRenderer.java').read_text()
    if not renderer.index('super.extractRenderState(') < renderer.index('TFCAnimalRenderStateExtractor.extract(') < renderer.index('state.texture = textureGetter.apply(entity);'):
        raise AssertionError('Native/custom/texture extraction order changed')


def main():
    out = Path('port-diagnostics')
    cp_file = out / 'classpath.txt'
    if not cp_file.is_file():
        raise SystemExit('Resolve the exact target classpath first.')
    cp = cp_file.read_text().strip()
    contracts = json.loads(Path('tools/porting/model-render-contracts.json').read_text())['models']
    validate_contracts(contracts)
    reviewed = json.loads(Path('tools/porting/model-render-contracts.json').read_text())['reviewed_missing_bones']
    for name in contracts:
        source = (MODELS / (name + '.java')).read_text()
        match = re.search(r'protected Set<String> optionalAnimationBones\(\)', source)
        actual = sorted(re.findall(r'"([^"]+)"', method_body(source, match.start()))) if match else []
        if actual != reviewed.get(name, []):
            raise AssertionError('Unreviewed missing-bone allowance: ' + name)
    classes, empty = out / 'model-render-smoke-classes', out / 'model-render-empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    sources = [MODELS / (name + '.java') for name in contracts]
    sources += [MODELS / (name + '.java') for name in ('HierarchicalAnimatedModel', 'AquaticCritterModel', 'FelinePredatorModel')]
    sources += [STATE / 'TFCAnimalRenderState.java', STATE / 'package-info.java', ROOT / 'client/animation/BactrianCamelAnimation.java', Path('tools/porting/ModelRenderSmoke.java')]
    tools = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else None

    def tool(name):
        return str(tools / name) if tools else name

    log = out / 'model-render-smoke.log'
    commands = [
        [tool('javac'), '--release', '25', '-proc:none', '-classpath', cp, '-sourcepath', str(empty), '-d', str(classes), *map(str, sources)],
        [tool('java'), '-ea', '-classpath', str(classes) + os.pathsep + cp, 'ModelRenderSmoke', *contracts],
    ]
    with log.open('w') as stream:
        for command in commands:
            stream.write('Running ' + Path(command[0]).name + ' against the exact target, without platform stubs.\n')
            stream.flush()
            result = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT, text=True, timeout=120)
            if result.returncode:
                print(log.read_text())
                return result.returncode
        # Verify the target's actual adult-layout wolf texture and collar assets.
        assets = ['assets/minecraft/textures/entity/wolf/wolf_tame.png', 'assets/minecraft/textures/entity/wolf/wolf_collar.png']
        checked = set()
        for entry in cp.split(os.pathsep):
            path = Path(entry)
            if path.suffix != '.jar' or not path.is_file():
                continue
            with zipfile.ZipFile(path) as jar:
                names = set(jar.namelist())
                for asset in assets:
                    if asset in names:
                        image = jar.read(asset)
                        if image[:8] != b'\x89PNG\r\n\x1a\n' or struct.unpack('>II', image[16:24]) != (64, 32):
                            raise AssertionError('Adult dog texture layout changed: ' + asset)
                        checked.add(asset)
        if len(checked) != len(assets):
            raise AssertionError('Native adult dog texture assets were not verified')
        stream.write('PASS: target dog/collar texture presence and 64x32 layout; 48 geometry/keyframe source contracts.\n')
    text = log.read_text()
    final = re.search(r'PASS: (\d+) concrete production models; (\d+) model/scenario combinations; (\d+) assertions; (\d+) CPU vertices checked\.', text)
    if final is None:
        raise AssertionError('Model runtime probe did not report completion')
    report = dict(zip(('models_executed', 'model_scenarios', 'model_assertions', 'cpu_vertices'), map(int, final.groups())))
    report.update({'exit_code': 0, 'geometry_keyframe_contracts': len(contracts), 'native_textures_checked': len(checked), 'scope': 'Real model construction/pose/CPU vertex execution and source contracts. No live extraction, GPU rendering, client startup or gameplay.'})
    (out / 'model-render-smoke.json').write_text(json.dumps(report, indent=2) + '\n')
    print(text)
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
