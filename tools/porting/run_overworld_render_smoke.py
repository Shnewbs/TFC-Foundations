"""Compile native CPU geometry and verify the exact callback anchors; no GPU/gameplay claim."""
import os
from pathlib import Path
import shutil
import subprocess
import json
import hashlib
import re
import platform

ROOT = Path('src/main/java/net/dries007/tfc')
OUT = Path('port-diagnostics')
CP = (OUT/'classpath.txt').read_text().strip()
BIN = Path(os.environ['JAVA_HOME'])/'bin'
classes = OUT/'overworld-smoke-classes'
shutil.rmtree(classes, ignore_errors=True)
classes.mkdir(parents=True)
empty = OUT/'overworld-empty-sourcepath'
empty.mkdir(exist_ok=True)
sources = [ROOT/'client/overworld'/name for name in ['SkyGeometry.java','SkyPos.java','Star.java']]
sources += [ROOT/'client/render/blockentity/CapturedGeometry.java', Path('tools/porting/OverworldGeometrySmoke.java')]
subprocess.run([str(BIN/'javac'),'--release','25','-proc:none','-classpath',CP,'-sourcepath',str(empty),'-d',str(classes),*map(str,sources)],check=True)
classifier = {'Linux':'linux','Windows':'windows','Darwin':'macos'}[platform.system()]
if platform.machine().lower() in ('aarch64','arm64'): classifier += '-arm64'
natives = set()
for name in CP.split(os.pathsep):
    jar = Path(name)
    if jar.name.startswith('lwjgl') and '-natives-' not in jar.name:
        natives.update(jar.parent.parent.glob('*/'+jar.stem+'-natives-'+classifier+'.jar'))
runtime_cp = os.pathsep.join([str(classes), CP, *map(str, sorted(natives))])
subprocess.run([str(BIN/'java'),'-ea','-cp',runtime_cp,'net.dries007.tfc.client.overworld.OverworldGeometrySmoke'],check=True)
def native(cls):
    return subprocess.check_output([str(BIN/'javap'),'-p','-c','-classpath',CP,cls],text=True)
level = native('net.minecraft.world.level.Level')
assert 'getOverworldClockTime()' in level
assert 'Biome.getPrecipitationAt:(Lnet/minecraft/core/BlockPos;I)' in level
attr = native('net.minecraft.world.attribute.EnvironmentAttributeSystem')
assert attr.index('Method addBiomeLayer:') < attr.index('DimensionType.timelines:') < attr.index('WeatherAttributes.addBuiltinLayers:')
client = native('net.minecraft.client.multiplayer.ClientLevel')
assert 'addEnvironmentAttributeLayers(net.minecraft.world.attribute.EnvironmentAttributeSystem$Builder)' in client
worlds = native('net.minecraft.client.gui.screens.worldselection.WorldSelectionList$WorldListEntry')
assert 'renderExperimentalWarning(net.minecraft.client.gui.GuiGraphicsExtractor, int, int, boolean)' in worlds
context = native('net.minecraft.world.level.chunk.status.WorldGenContext')
assert 'mainThreadExecutor()' in context and 'unsavedListener()' in context
for cls, method in [('net.neoforged.neoforge.client.CustomSkyboxRenderer','renderSky'),('net.neoforged.neoforge.client.CustomWeatherEffectRenderer','renderSnowAndRain')]:
    assert method in native(cls)
src = (ROOT/'client/overworld/LevelRendererExtension.java').read_text()
assert 'BlockEntityWithoutLevelRenderer' not in src
assert 'getDeclaredField' not in src and 'getDeclaredMethod' not in src
assert 'RenderSystem.setShader(' not in src and 'BufferUploader' not in src
assert 'event.registerPipeline(COLORED_STARS)' in src and 'event.registerPipeline(COVER)' in src
assert 'List.copyOf(batches)' in src and 'starBuffer.close()' in src
assert 'getAtlasOrThrow(AtlasIds.CELESTIALS)' in src
assert 'INSTANCE.captureWeather(' in src
assert 'setRenderData(FRAME' in src
assert 'ClientLevelMixin' in Path('src/main/resources/tfc.mixins.json').read_text()
assert 'client.EnvironmentAttributeSystemMixin' in json.loads(Path('src/main/resources/tfc.mixins.json').read_text())['client']
assert not (ROOT/'mixin/LevelAccessorMixin.java').exists()
print('PASS native environment interfaces, attribute-layer order, clock/precipitation/UI/WorldGenContext signatures, immutable publication and atlas/pipeline lifecycle source contracts')
print('NOT RUN: Mixin application, GPU pipelines, appearance, native timeline timing, reload-in-world or gameplay')

# Frozen pre-migration geometry protects all rain/snow vertices and astronomical rotations.
contracts = json.loads(Path('tools/porting/overworld-geometry-contracts.json').read_text())
geometry = (ROOT/'client/overworld/SkyGeometry.java').read_text()
def body(source, name):
    match = re.search(r'\b'+name+r'\([^;]*?\)\s*\{', source, re.S)
    start = source.index('{', match.start())
    end, depth = start + 1, 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]
for name in ('drawStar','rotateTo','rotateSkyTo'):
    assert hashlib.sha256(body(geometry, name).encode()).hexdigest() == contracts[name], name
for index, token in enumerate(('.setLight(light);','.setUv2(brightLightU, brightLightV);')):
    start = src.index('                            Vector3f vert0', 0 if index == 0 else src.index('                            Vector3f vert0') + 1)
    end = start
    for _ in range(4): end = src.index(token, end) + len(token)
    assert hashlib.sha256(src[start:end].encode()).hexdigest() == contracts['weather_vertices_'+str(index)]
print('PASS frozen star/rotation and complete rain/snow vertex-body preservation')
