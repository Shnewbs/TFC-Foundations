"""Source and exact-target API checks for 26.1.2 native item registration.

Tests protect identifier routes and native constructor signatures, not bootstrap or gameplay.
"""

import os
from pathlib import Path
import re
import subprocess
import unittest

ROOT = Path('src/main/java/net/dries007/tfc/common/items')


class NativeItemRegistrationContracts(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = (ROOT / 'TFCItems.java').read_text()
        cls.seeds = (ROOT / 'SeedItem.java').read_text()

    def test_wood_supports_and_coral_native_direction_before_properties(self):
        self.assertEqual(self.source.count(', Direction.DOWN, new Properties())'), 4)
        self.assertNotIn(', new Properties(), Direction.DOWN)', self.source)

    def test_signs_preserve_standing_and_wall_blocks(self):
        self.assertRegex(self.source, r'new SignItem\(TFCBlocks\.WOODS\.get\(wood\)\.get\(Wood\.BlockType\.SIGN\)\.get\(\), TFCBlocks\.WOODS\.get\(wood\)\.get\(Wood\.BlockType\.WALL_SIGN\)\.get\(\), new Properties\(\)\)')

    def test_spawn_egg_is_registered_using_native_entity_component(self):
        self.assertIn('new SpawnEggItem(new Properties().spawnEgg(entity.get()))', self.source)
        self.assertIn('register("spawn_egg/" + entity.getId().getPath()', self.source)
        self.assertNotIn('DeferredSpawnEggItem', self.source)

    def test_seed_uses_block_item_with_original_block_language_key(self):
        self.assertIn('extends BlockItem implements PlantableInfo', self.seeds)
        self.assertIn('super(block, properties.useBlockDescriptionPrefix())', self.seeds)
        self.assertIn('blockToItemMap.put(this.deadBlock, item)', self.seeds)

    def test_resolved_target_classes_expose_all_constructors(self):
        cp = Path('port-diagnostics/classpath.txt')
        self.assertTrue(cp.is_file(), 'No resolved 26.1.2 classpath available')
        exe = str(Path(os.environ['JAVA_HOME']) / 'bin/javap') if os.environ.get('JAVA_HOME') else 'javap'
        classes = ['net.minecraft.world.item.StandingAndWallBlockItem', 'net.minecraft.world.item.SignItem',
                   'net.minecraft.world.item.SpawnEggItem', 'net.minecraft.world.item.BlockItem',
                   'net.minecraft.world.item.Item$Properties']
        result = subprocess.run([exe, '-classpath', cp.read_text().strip(), '-public', *classes],
                                capture_output=True, text=True, timeout=30)
        self.assertEqual(result.returncode, 0, result.stderr[:350])
        for expected in (
            'StandingAndWallBlockItem(net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block, net.minecraft.core.Direction, net.minecraft.world.item.Item$Properties)',
            'SignItem(net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block, net.minecraft.world.item.Item$Properties)',
            'SpawnEggItem(net.minecraft.world.item.Item$Properties)',
            'BlockItem(net.minecraft.world.level.block.Block, net.minecraft.world.item.Item$Properties)',
            'spawnEgg(net.minecraft.world.entity.EntityType<?>)',
            'useBlockDescriptionPrefix()',
        ):
            self.assertIn(expected, result.stdout)


if __name__ == '__main__':
    unittest.main(verbosity=2)
