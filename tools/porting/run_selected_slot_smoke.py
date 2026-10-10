"""Check slot APIs and preserve TFC item-hand handling without Minecraft stubs.

This is a source/API probe. Menu and world interaction still require game tests.
"""
import os
from pathlib import Path
import subprocess
import unittest

ROOT = Path(__file__).resolve().parents[2]
JAVA = Path(os.environ['JAVA_HOME']) / 'bin' / 'javap' if 'JAVA_HOME' in os.environ else Path('javap')
CP = ROOT / 'port-diagnostics/classpath.txt'
SRC = ROOT / 'src/main/java/net/dries007/tfc'


class InventorySlotContracts(unittest.TestCase):
    def test_native_inventory_slot_accessors_exist(self):
        self.assertTrue(CP.is_file(), 'Use the resolved target classpath')
        result = subprocess.run([str(JAVA), '-classpath', CP.read_text().strip(), '-public',
                                 'net.minecraft.world.entity.player.Inventory'],
                                text=True, capture_output=True, timeout=15, check=True)
        self.assertIn('public int getSelectedSlot();', result.stdout)
        self.assertIn('public net.minecraft.world.item.ItemStack getItem(int);', result.stdout)

    def test_provider_does_not_change_selection_during_decode(self):
        src = (SRC / 'common/container/ItemStackContainerProvider.java').read_text()
        self.assertIn('final InteractionHand hand = slot == -1 ? InteractionHand.OFF_HAND', src)
        self.assertIn('stack = playerInventory.player.getOffhandItem();', src)
        self.assertIn('stack = playerInventory.getItem(slot);', src)
        self.assertNotIn('playerInventory.setSelectedSlot(', src)
        self.assertNotIn('playerInventory.selected', src)
        self.assertNotIn('playerInventory.getSelected()', src)
        self.assertIn('hand == InteractionHand.OFF_HAND ? -1 : player.getInventory().getSelectedSlot()', src)
        self.assertIn('buffer.writeByte(encodedSlot);', src)

    def test_mold_table_uses_public_slot_accessor(self):
        src = (SRC / 'common/blockentities/MoldTableBlockEntity.java').read_text()
        self.assertEqual(src.count('player.getInventory().getSelectedSlot()'), 4)
        self.assertNotIn('player.getInventory().selected', src)
        self.assertIn('inventory.extractItem(OUTPUT_SLOT, 99, false)', src)

    def test_tool_rack_uses_public_slot_accessor(self):
        src = (SRC / 'common/blockentities/ToolRackBlockEntity.java').read_text()
        self.assertEqual(src.count('player.getInventory().getSelectedSlot()'), 2)
        self.assertNotIn('player.getInventory().selected', src)
        self.assertIn('insertItem(slot, heldItem.split(1))', src)


if __name__ == '__main__':
    unittest.main(verbosity=2)
