"""26.1.2 native-bed API contracts and persistence/removal safeguards.

These protect the port source, not a substitute for bed sleeping in a live server.
"""
from pathlib import Path
import subprocess
import unittest

ROOT = Path('src/main/java/net/dries007/tfc')
CP = Path('port-diagnostics/classpath.txt')


class ThatchBedContracts(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        if not CP.is_file():
            raise AssertionError('Target classpath required')
        cls.cp = CP.read_text().strip()
        cls.block = (ROOT / 'common/blocks/ThatchBedBlock.java').read_text()
        cls.entity = (ROOT / 'common/blockentities/ThatchBedBlockEntity.java').read_text()

    @classmethod
    def api(cls, name, *args):
        result = subprocess.run(['javap', '-classpath', cls.cp, *args, name],
                                capture_output=True, text=True, timeout=45)
        if result.returncode:
            raise AssertionError(f'Unavailable native API {name}: {result.stderr}')
        return result.stdout

    def test_native_bed_rule_preserves_dimension_spawn_constraint(self):
        self.assertIn('level.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, pos).canSetSpawn(level)', self.block)
        self.assertNotIn('if (!canSetSpawn(level))', self.block)
        self.assertIn('EnvironmentAttribute<net.minecraft.world.attribute.BedRule> BED_RULE', self.api('net.minecraft.world.attribute.EnvironmentAttributes'))
        self.assertIn('boolean canSetSpawn(net.minecraft.world.level.Level)', self.api('net.minecraft.world.attribute.BedRule'))

    def test_respawn_capture_restores_exact_unchanged_config(self):
        self.assertIn('final ServerPlayer.RespawnConfig lastRespawnConfig = serverPlayer.getRespawnConfig();', self.block)
        self.assertIn('serverPlayer.setRespawnPosition(lastRespawnConfig, false);', self.block)
        self.assertIn('LevelData.RespawnData.of(level.dimension(), pos, 0F, 0F)', self.block)
        self.assertIn('new ServerPlayer.RespawnConfig(', self.block)
        server = self.api('net.minecraft.server.level.ServerPlayer')
        self.assertIn('setRespawnPosition(net.minecraft.server.level.ServerPlayer$RespawnConfig, boolean)', server)
        self.assertIn('getRespawnConfig()', server)
        respawn = self.api('net.minecraft.world.level.storage.LevelData$RespawnData')
        self.assertIn('of(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>, net.minecraft.core.BlockPos, float, float)', respawn)
        self.assertIn('boolean forced()', self.api('net.minecraft.server.level.ServerPlayer$RespawnConfig'))

    def test_sleep_problem_uses_native_record_message(self):
        self.assertIn('problem.message()', self.block)
        self.assertNotIn('problem.getMessage()', self.block)
        self.assertIn('message()', self.api('net.minecraft.world.entity.player.Player$BedSleepingProblem'))
        for text in ('tfc.thatch_bed.use_no_sleep_spawn', 'tfc.thatch_bed.use_no_sleep_no_spawn',
                     'tfc.thatch_bed.use_sleep_spawn', 'tfc.thatch_bed.use_sleep_no_spawn'):
            self.assertIn(text, self.block)

    def test_drops_before_removal_through_native_block_entity_hook(self):
        self.assertNotIn('void onRemove(', self.block)
        self.assertIn('public void preRemoveSideEffects(BlockPos pos, BlockState previousState)', self.entity)
        self.assertIn('previousState.getValue(BedBlock.PART) == BedPart.HEAD', self.entity)
        self.assertIn('destroyBed();', self.entity)
        self.assertIn('super.preRemoveSideEffects(pos, previousState);', self.entity)
        self.assertIn('ejectInventory();', self.entity)
        self.assertIn('preRemoveSideEffects(net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState)',
                      self.api('net.minecraft.world.level.block.entity.BlockEntity'))
        chunk = self.api('net.minecraft.world.level.chunk.LevelChunk', '-c', '-p')
        first = chunk.index('BlockEntity.preRemoveSideEffects:')
        self.assertIn('removeBlockEntity', chunk[first:first+400])


if __name__ == '__main__':
    unittest.main(verbosity=2)
