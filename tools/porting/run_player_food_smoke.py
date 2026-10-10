"""Check hunger/thirst migration against the real native FoodData bytecode and codecs."""
from pathlib import Path
import json
import os
import re
import shutil
import subprocess

ROOT = Path('port-diagnostics')
CP = (ROOT / 'classpath.txt').read_text().strip()
CLASSES = ROOT / 'player-food-smoke-classes'
EMPTY = ROOT / 'player-food-smoke-empty-sourcepath'
shutil.rmtree(CLASSES, ignore_errors=True)
CLASSES.mkdir(parents=True)
EMPTY.mkdir(parents=True, exist_ok=True)
JAVA_BIN = (Path(os.environ['JAVA_HOME']) / 'bin') if os.environ.get('JAVA_HOME') else Path('')

def run(name, *args):
    p = subprocess.run([str(JAVA_BIN / name), *map(str, args)], text=True, capture_output=True, timeout=60)
    if p.returncode:
        raise AssertionError(p.stdout + p.stderr)
    return p.stdout + p.stderr

# This catches a version change to private native fields before the accessor silently breaks runtime.
api = run('javap', '-p', '-classpath', CP, 'net.minecraft.world.food.FoodData')
assert 'private float exhaustionLevel;' in api
assert 'void tick(net.minecraft.server.level.ServerPlayer);' in api
assert 'void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput);' in api
assert 'void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput);' in api

src = Path('src/main/java/net/dries007/tfc/common/player/PlayerInfo.java').read_text()
mixin = Path('src/main/java/net/dries007/tfc/mixin/accessor/FoodDataAccessor.java').read_text()
cfg = json.loads(Path('src/main/resources/tfc.mixins.json').read_text())
assert 'accessor.FoodDataAccessor' in cfg['mixins']
assert '@Mixin(FoodData.class)' in mixin
assert mixin.count('@Accessor("exhaustionLevel")') == 2
for preserved in ('tfc:food', 'lastDrinkTick', 'thirst', 'chiselMode', 'nutrition', 'intoxication'):
    assert preserved in src, preserved
assert 'void tick(ServerPlayer player)' in src and 'food.tick(player)' in src
assert 'food.readAdditionalSaveData(input)' in src and 'food.addAdditionalSaveData(output)' in src
assert 'input.read("tfc:food", CompoundTag.CODEC)' in src
assert 'output.store("tfc:food", CompoundTag.CODEC, tag)' in src
assert 'MobEffects.SLOWNESS' in src and 'MobEffects.MINING_FATIGUE' in src
assert 'food.getExhaustionLevel()' not in src and 'food.setExhaustion(' not in src

run('javac', '--release', '25', '-proc:none', '-classpath', CP, '-sourcepath', EMPTY,
    '-d', CLASSES, 'tools/porting/PlayerFoodValueIOSmoke.java')
print(run('java', '-ea', '-classpath', str(CLASSES) + os.pathsep + CP, 'PlayerFoodValueIOSmoke').strip())
print('PASS: 26.1.2 food tick and private exhaustion accessor signatures, mixin registration, effects and serialization source contracts.')
