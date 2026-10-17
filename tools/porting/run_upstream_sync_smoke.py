"""Upstream gameplay/resource contracts; CPU-only, no Minecraft world or runtime claims."""
import json
from pathlib import Path
import re
import subprocess
import tempfile
import os

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java/net/dries007/tfc'

def read(path):
    return (SRC / path).read_text()

def main():
    # Compile the actual production bit predicate, without constructing block entities.
    source = read('common/blockentities/ScrapingBlockEntity.java')
    method = re.search(r'    public static boolean checkPosition\(.*?\n    }', source, re.S).group()
    java = '''public class ScrapingProgressProbe {
METHOD
public static void main(String[] args) {
  int checks = 0;
  for (int bits = 0; bits <= 65535; bits++) {
    short data = (short) bits;
    for (int x = 0; x < 4; x++) for (int z = 0; z < 4; z++) {
      int expected = (bits & (1 << (x + 4 * z))) == 0 ? 0 : 1;
      if (!checkPosition(data, x, z, expected) || checkPosition(data, x, z, 1 - expected))
        throw new AssertionError("signed 16-tile mask");
      checks += 2;
    }
  }
  System.out.println("PASS " + checks + " production scraping-mask checks");
}}
'''.replace('METHOD', method)
    with tempfile.TemporaryDirectory() as d:
        f = Path(d) / 'ScrapingProgressProbe.java'
        f.write_text(java)
        binpath = Path(os.environ.get('JAVA_HOME', '')) / 'bin'
        subprocess.run([str(binpath/'javac') if binpath.is_absolute() else 'javac', '--release', '25', str(f)], check=True)
        subprocess.run([str(binpath/'java') if binpath.is_absolute() else 'java', '-cp', d, 'ScrapingProgressProbe'], check=True)
    assert source.index('if (checkPosition(positions, xPos, zPos, 1))') < source.index('positions |=')
    block = read('common/blocks/devices/ScrapingBlock.java')
    assert block.index('if (scraping.onClicked(') < block.index('Helpers.damageItem(stack, player, hand)')
    assert '.min(Comparator.comparing(PotRecipe::isFallback))' in read('common/blockentities/PotBlockEntity.java')
    assert 'return true;' in read('common/recipes/SoupPotRecipe.java').split('boolean isFallback()')[1].split('}')[0]
    farmland = read('common/blocks/soil/FarmlandBlock.java')
    assert 'formatRange(' in farmland
    assert 'state.hasProperty(BloomeryBlock.LIT)' in read('common/blockentities/BloomeryBlockEntity.java')
    assert 'performSkippedHeating' in read('common/blockentities/FireboxBlockEntity.java')
    assert 'updatedState.getBlock() == this && updatedState.getValue(LIFECYCLE).active()' in read('common/blocks/plant/fruit/BananaPlantBlock.java')
    for folder in ['src/generated/resources', 'src/main/resources']:
        for f in (ROOT/folder).rglob('*.json'):
            json.loads(f.read_text())
    tag = ROOT/'src/generated/resources/data/minecraft/tags/block/prevent_mob_spawning_inside.json'
    assert 'tfc:thatch' in json.loads(tag.read_text())['values']
    junk = json.loads((ROOT/'src/generated/resources/data/tfc/tags/item/lobster_trap_junk.json').read_text())
    assert len(junk['values']) > 0
    for color in ('white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black'):
        for path in (f'src/main/resources/assets/tfc/blockstates/buoy/{color}.json', f'src/generated/resources/data/tfc/recipe/crafting/buoy/{color}.json', f'src/main/resources/assets/tfc/textures/block/buoy/{color}.png'):
            assert (ROOT/path).is_file(), path
    # Vanilla trim palettes were missing upstream; ensure their IDs survive merging.
    atlas = json.loads((ROOT/'src/main/resources/assets/minecraft/atlases/blocks.json').read_text())
    permutations = {key for row in atlas['sources'] for key in row.get('permutations', {})}
    assert {'quartz','iron','netherite','redstone','copper','gold','emerald','diamond','lapis','amethyst'} <= permutations
    print('PASS upstream guards, fallback ordering, all JSON, thatch tag, 16 buoy asset/recipe/texture sets and ten vanilla trim palettes')
    print('NOT RUN: kiln catch-up worlds, trap drops/movement, AI ticks, multiplayer, reload or rendering')

if __name__ == '__main__':
    main()
