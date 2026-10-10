"""Exact 26.1.2 method contracts for entity spawning, tags, tools and item particles."""
from pathlib import Path
import os
import re
import subprocess

ROOT = Path('src/main/java/net/dries007/tfc')
CP = Path('port-diagnostics/classpath.txt').read_text().strip()
JAVA_BIN = (Path(os.environ['JAVA_HOME']) / 'bin') if os.environ.get('JAVA_HOME') else Path('')

def native(cls):
    p = subprocess.run([str(JAVA_BIN / 'javap'), '-p', '-classpath', CP, cls], text=True, capture_output=True, timeout=50)
    assert p.returncode == 0, p.stderr
    return p.stdout

ent = native('net.minecraft.world.entity.EntityType')
assert 'create(net.minecraft.world.level.Level, net.minecraft.world.entity.EntitySpawnReason)' in ent
assert 'snapTo(net.minecraft.world.phys.Vec3)' in native('net.minecraft.world.entity.Entity')
assert 'getTagOrEmpty(net.minecraft.tags.TagKey' in native('net.minecraft.core.Registry')
assert 'fromNonEmptyStack(net.minecraft.world.item.ItemStack)' in native('net.minecraft.world.item.ItemStackTemplate')
assert 'net.minecraft.world.item.ItemStackTemplate)' in native('net.minecraft.core.particles.ItemParticleOption')
assert 'MAINHAND;' in native('net.minecraft.world.entity.EquipmentSlot')
assert 'OFFHAND;' in native('net.minecraft.world.entity.EquipmentSlot')

helper = (ROOT / 'util/Helpers.java').read_text()
assert helper.count('getTagOrEmpty(tag)') == 4  # 3 enumeration paths + random selection
assert 'getOrCreateTag(tag)' not in helper and 'registry.getTag(tag)' not in helper
assert 'entity.builtInRegistryHolder().is(tag)' in helper
assert 'type.create(level, EntitySpawnReason.EVENT)' in helper
assert 'mob.snapTo(checkPos)' in helper
assert helper.count('(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND)') == 2
assert 'values.get(random.nextInt(values.size()))' in helper
assert 'values.isEmpty() ? Optional.empty()' in helper

paths = (
    'common/entities/livestock/TFCAnimalProperties.java',
    'common/blocks/StainedWattleBlock.java',
    'common/blockentities/QuernBlockEntity.java',
    'common/blocks/devices/ScrapingBlock.java',
    'common/entities/prey/Pest.java',
)
for path in paths:
    src = (ROOT / path).read_text()
    assert 'ItemStackTemplate.fromNonEmptyStack(' in src, path
    assert 'new ItemParticleOption(ParticleTypes.ITEM,' in src, path
    assert re.search(r'import net.minecraft.world.item.ItemStackTemplate;', src), path
    assert not re.search(r'new ItemParticleOption\(ParticleTypes.ITEM,\s*(?:stack|item|held|scraping\.getInventory\()', src), path
assert 'if (scraped.isEmpty()) return;' in (ROOT / paths[3]).read_text()
assert 'if (item.isEmpty()) return;' in (ROOT / paths[2]).read_text()
assert 'if (!stack.isEmpty())' in (ROOT / paths[0]).read_text()
assert 'if (!held.isEmpty())' in (ROOT / paths[4]).read_text()
print('PASS: native 26.1.2 entity creation/tag lookup/equipment-slot signatures and 5 particle-template call sites; empty item guard and uniform registry selection intact.')
