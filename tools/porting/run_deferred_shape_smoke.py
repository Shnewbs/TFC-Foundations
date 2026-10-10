"""Source contracts for deferred, inventory-safe shape changes on Minecraft 26.1.2.

Run without pretending to simulate a world tick or inventory; the real game test
remains necessary after full compilation and client/dedicated-server startup.
"""
from pathlib import Path
import re
import unittest

BASE=Path(__file__).resolve().parents[2]/'src/main/java/net/dries007/tfc/common/blocks'


def body(text, method):
    m=re.search(method, text)
    assert m, method
    start=text.index('{',m.end())
    nest=1
    for i in range(start+1,len(text)):
        if text[i]=='{': nest+=1
        if text[i]=='}': nest-=1
        if not nest: return text[start+1:i]
    raise AssertionError('Unclosed method')


class DeferredShapeContracts(unittest.TestCase):
    def test_charcoal_merges_only_in_server_tick_without_double_drops(self):
        t=(BASE/'CharcoalPileBlock.java').read_text()
        shape=body(t,r'BlockState updateShape\(')
        tick=body(t,r'void tick\(')
        self.assertNotRegex(shape,r'\.(?:setBlock|destroyBlock)\(')
        self.assertIn('tickAccess.scheduleTick(currentPos, this, 1)',shape)
        self.assertIn('Math.min(space, original)',tick)
        self.assertIn('level.destroyBlock(pos, false)',tick)
        self.assertIn('level.setBlock(below,',tick)
        self.assertIn('getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player)',t)
        self.assertIn('ItemStack tool, boolean willHarvest',t)

    def test_log_pile_uses_existing_ignition_tick(self):
        t=(BASE/'devices/LogPileBlock.java').read_text()
        self.assertIn('tickAccess.scheduleTick(currentPos, this, 1)',body(t,r'BlockState updateShape\('))
        self.assertIn('BurningLogPileBlock.lightLogPile(level, pos)',body(t,r'void tick\('))
        self.assertNotIn('BurningLogPileBlock.lightLogPile(level, currentPos)',body(t,r'BlockState updateShape\('))

    def test_kiln_removes_fire_before_attempting_ignition(self):
        t=(BASE/'devices/PitKilnBlock.java').read_text()
        shape=body(t,r'BlockState updateShape\(')
        tick=body(t,r'void tick\(')
        self.assertIn('tickAccess.scheduleTick(currentPos, this, 1)',shape)
        self.assertLess(tick.index('level.setBlock(pos.above(), Blocks.AIR'),tick.index('kiln.tryLight()'))
        self.assertNotRegex(shape,r'\.(?:setBlock|tryLight)\(')

    def test_bamboo_sapling_replaces_by_return_value(self):
        t=(BASE/'plant/TFCBambooSaplingBlock.java').read_text()
        shape=body(t,r'BlockState updateShape\(')
        self.assertIn('return stalk.get().defaultBlockState()',shape)
        self.assertIn('!state.canSurvive(level, currentPos)',shape)
        self.assertNotIn('level.setBlock(currentPos',shape)
        self.assertIn('getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player)', t)

    def test_bamboo_stalk_age_change_preserves_schedule(self):
        t=(BASE/'plant/TFCBambooStalkBlock.java').read_text()
        shape=body(t,r'BlockState updateShape\(')
        self.assertIn('tickAccess.scheduleTick(pos, this, 1)',shape)
        self.assertIn('state = state.cycle(AGE)',shape)
        self.assertIn('return super.updateShape(state, level, tickAccess',shape)
        self.assertNotIn('level.setBlock(pos, state.cycle(AGE)',shape)
        self.assertIn('BlockTags.SUPPORTS_BAMBOO', t)

    def test_invalid_fruit_leaves_fade_in_existing_server_tick(self):
        t=(BASE/'plant/fruit/FruitTreeLeavesBlock.java').read_text()
        shape=body(t,r'BlockState updateShape\(')
        tick=body(t,r'void tick\(')
        self.assertIn('tickAccess.scheduleTick(currentPos, this, 1)',shape)
        self.assertIn('FluidHelpers.tickFluid(level, tickAccess, currentPos, state)',shape)
        self.assertIn('level.destroyBlock(pos, true)',tick)
        self.assertIn('TFCLeavesBlock.doParticles(level,',tick)
        self.assertNotIn('return Blocks.AIR.defaultBlockState()',shape)


if __name__=='__main__':
    unittest.main(verbosity=2)
