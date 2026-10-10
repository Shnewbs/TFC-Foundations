"""Exact-target API and source contracts for TFC piled snow and ice.

Does not replace live melting, placement, or save/reload gameplay tests.
"""
from pathlib import Path
import subprocess
import unittest

ROOT = Path('src/main/java/net/dries007/tfc/common/blocks')
CP = Path('port-diagnostics/classpath.txt')


class SnowIceContracts(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        if not CP.is_file():
            raise AssertionError('Resolved target classpath required')
        cls.snow = (ROOT / 'SnowPileBlock.java').read_text()
        cls.ice = (ROOT / 'IcePileBlock.java').read_text()
        cls.classpath = CP.read_text().strip()

    @classmethod
    def javap(cls, name):
        result = subprocess.run(['javap', '-classpath', cls.classpath, name],
                                capture_output=True, text=True, timeout=40)
        if result.returncode:
            raise AssertionError(f'Cannot inspect target class {name}: {result.stderr}')
        return result.stdout

    def test_native_neighbor_updates_preserve_both_positions(self):
        self.assertEqual(self.snow.count('level.updateNeighborsAt('), 4)
        self.assertEqual(self.ice.count('level.updateNeighborsAt('), 4)
        self.assertNotIn('level.blockUpdated(', self.snow + self.ice)
        api = self.javap('net.minecraft.world.level.LevelAccessor')
        self.assertIn('updateNeighborsAt(net.minecraft.core.BlockPos, net.minecraft.world.level.block.Block)', api)

    def test_snowy_state_uses_target_property(self):
        self.assertIn('BlockStateProperties.SNOWY', self.snow)
        self.assertNotIn('SnowyDirtBlock', self.snow)
        api = self.javap('net.minecraft.world.level.block.state.properties.BlockStateProperties')
        self.assertIn('BooleanProperty SNOWY;', api)

    def test_player_destroy_contract_and_original_drop_flow(self):
        signature = 'Player player, ItemStack heldItem, boolean willHarvest, FluidState fluid)'
        self.assertIn(signature, self.snow)
        self.assertIn(signature, self.ice)
        self.assertIn('super.onDestroyedByPlayer(state, level, pos, player, heldItem, willHarvest, fluid);', self.snow)
        self.assertIn('removePileOrSnow(level, pos, state, snowPile);', self.snow)
        self.assertIn('removeIcePileOrIce(level, pos, state);', self.ice)
        api = self.javap('net.neoforged.neoforge.common.extensions.IBlockExtension')
        self.assertIn('onDestroyedByPlayer(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.Level, net.minecraft.core.BlockPos, net.minecraft.world.entity.player.Player, net.minecraft.world.item.ItemStack, boolean, net.minecraft.world.level.material.FluidState)', api)

    def test_ice_uses_environment_evaporation_not_deleted_dimension_field(self):
        self.assertIn('level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)', self.ice)
        self.assertNotIn('.ultraWarm()', self.ice)
        self.assertIn('WATER_EVAPORATES;', self.javap('net.minecraft.world.attribute.EnvironmentAttributes'))
        self.assertIn('environmentAttributes()', self.javap('net.minecraft.world.level.LevelReader'))
        self.assertIn('getValue(net.minecraft.world.attribute.EnvironmentAttribute<Value>, net.minecraft.core.BlockPos)', self.javap('net.minecraft.world.attribute.EnvironmentAttributeReader'))


if __name__ == '__main__':
    unittest.main(verbosity=2)
