"""Actual-source contracts for 26.1.2's read-only shape and tick update split.

These checks are intentionally source/bytecode checks, NOT a running world test.
"""
from __future__ import annotations

from pathlib import Path
import re
import subprocess
import unittest

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / 'src/main/java/net/dries007/tfc'
CLASSPATH = ROOT / 'port-diagnostics/classpath.txt'


class NeighborShapeContracts(unittest.TestCase):
    @staticmethod
    def source(path: str) -> str:
        return (JAVA / path).read_text()

    def test_exact_target_shape_signature(self):
        cp = CLASSPATH.read_text().strip()
        output = subprocess.run(['javap', '-classpath', cp, '-protected',
                                 'net.minecraft.world.level.block.state.BlockBehaviour'], capture_output=True,
                                text=True, check=True).stdout
        self.assertIn('updateShape(net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.LevelReader, net.minecraft.world.level.ScheduledTickAccess, net.minecraft.core.BlockPos, net.minecraft.core.Direction, net.minecraft.core.BlockPos, net.minecraft.world.level.block.state.BlockState, net.minecraft.util.RandomSource)', output)

    def test_all_old_mutating_hooks_have_server_tick_or_returned_state(self):
        files = list((JAVA / 'common/blocks').rglob('*.java'))
        old = {p.stem for p in files if re.search(r'updateShape\(BlockState \w+, Direction \w+, BlockState \w+, LevelAccessor \w+, BlockPos \w+, BlockPos \w+\)', p.read_text())}
        self.assertFalse(old, f'Stale pre-26.x shape hooks: {sorted(old)}')
        expected = {'CharcoalPileBlock', 'LogPileBlock', 'PitKilnBlock',
                    'TFCBambooSaplingBlock', 'TFCBambooStalkBlock', 'FruitTreeLeavesBlock'}
        for name in expected:
            found = [p for p in files if p.stem == name]
            self.assertEqual(len(found), 1)
            self.assertIn('ScheduledTickAccess tickAccess', found[0].read_text())

    def test_new_shape_hooks_keep_separate_scheduler(self):
        files = list((JAVA / 'common/blocks').rglob('*.java'))
        targets = [p for p in files if re.search(r'\bBlockState updateShape\(BlockState \w+, LevelReader level, ScheduledTickAccess tickAccess,', p.read_text())]
        self.assertGreaterEqual(len(targets), 78)
        for p in targets:
            t=p.read_text()
            self.assertIn('LevelReader level', t, str(p))
            self.assertIn('RandomSource random', t, str(p))
        self.assertIn('tickAccess.scheduleTick', self.source('common/blocks/rock/AqueductBlock.java'))
        self.assertIn('FluidHelpers.tickFluid(level, tickAccess, currentPos, state)', self.source('common/blocks/rotation/FluidPipeBlock.java'))

    def test_read_only_multiblock_filters(self):
        multiblock = self.source('util/MultiBlock.java')
        self.assertIn('implements BiPredicate<LevelReader, BlockPos>', multiblock)
        self.assertNotIn('LevelAccessor', multiblock)
        self.assertIn('BiPredicate<LevelReader, BlockPos> stoneMatcher', self.source('common/blocks/devices/BloomeryBlock.java'))
        self.assertIn('BiPredicate<LevelReader, BlockPos> skyMatcher', self.source('common/blocks/devices/CharcoalForgeBlock.java'))

    def test_fluid_tick_keeps_legacy_and_target_overloads(self):
        helper=self.source('common/fluids/FluidHelpers.java')
        self.assertIn('tickFluid(LevelReader level, ScheduledTickAccess ticks, BlockPos', helper)
        self.assertIn('ticks.scheduleTick(', helper)
        self.assertIn('tickFluid(LevelAccessor level, BlockPos', helper)
        self.assertIn('FluidHelpers.tickFluid(level, tickAccess, currentPos, state, true)',
                      self.source('common/blocks/plant/KelpTreeFlowerBlock.java'))

    def test_corals_preserve_delay_and_legacy_placement(self):
        coral=self.source('common/blocks/plant/coral/TFCCoralPlantBlock.java')
        self.assertIn('tryScheduleDieTick(state, level, level, pos, level.getRandom())',coral)
        self.assertIn('ticks.scheduleTick(pos, this, 60 + random.nextInt(40))',coral)
        for name in ('LivingCoralPlantBlock', 'LivingCoralWallFanBlock'):
            self.assertIn('tryScheduleDieTick(state, level, tickAccess, currentPos, random)',
                          self.source(f'common/blocks/plant/coral/{name}.java'))

    def test_shelf_state_reader_does_not_mutate_the_world(self):
        t=self.source('common/blocks/devices/PlacedItemBlock.java')
        m = re.search(r'BlockState updateStateValues\(LevelReader level,.*?\n    }', t, re.S)
        self.assertIsNotNone(m)
        self.assertNotRegex(m.group(),r'\.setBlock\(|\.destroyBlock\(')
        self.assertIn('getBlockSupportShape(level, pos)', m.group())

    def test_build_height_stays_inclusive_top_block(self):
        t=self.source('common/blocks/plant/KelpTreeFlowerBlock.java')
        self.assertIn('abovePos.getY() <= level.getMaxY()',t)


if __name__=='__main__':
    unittest.main(verbosity=2)
