/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks;

import net.minecraft.world.level.ScheduledTickAccess;

import net.minecraft.world.level.LevelReader;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TFCMagmaBlock extends MagmaBlock implements IForgeBlockExtension
{
    private final ExtendedProperties properties;

    public TFCMagmaBlock(ExtendedProperties properties)
    {
        super(properties.properties());
        this.properties = properties;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState faceState, RandomSource random)
    {
        if (facing == Direction.UP && TFCBubbleColumnBlock.canExistIn(faceState))
        {
            tickAccess.scheduleTick(currentPos, this, 20);
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        TFCBubbleColumnBlock.updateColumnForFluid(level, pos);
    }

    @Override
    public ExtendedProperties getExtendedProperties()
    {
        return properties;
    }
}
