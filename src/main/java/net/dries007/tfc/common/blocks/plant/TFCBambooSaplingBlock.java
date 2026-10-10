/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks.plant;

import java.util.function.Supplier;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;

import net.dries007.tfc.util.Helpers;

public class TFCBambooSaplingBlock extends BambooSaplingBlock
{
    private final Supplier<? extends Block> stalk;

    public TFCBambooSaplingBlock(Properties properties, Supplier<? extends Block> stalk)
    {
        super(properties);
        this.stalk = stalk;
    }

    @Override
    protected void growBamboo(Level level, BlockPos state)
    {
        level.setBlock(state.above(), stalk.get().defaultBlockState().setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL), 3);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random)
    {
        if (!state.canSurvive(level, currentPos))
        {
            return Blocks.AIR.defaultBlockState();
        }
        // Return the new sapling replacement directly. The target shape API
        // applies returned states; it does not permit a write into LevelReader.
        if (facing == Direction.UP && Helpers.isBlock(facingState, stalk.get()))
        {
            return stalk.get().defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, currentPos, facing, facingPos, facingState, random);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player)
    {
        return new ItemStack(stalk.get());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state)
    {
        return false;
    }

}
