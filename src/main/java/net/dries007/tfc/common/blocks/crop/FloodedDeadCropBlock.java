/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks.crop;

import net.minecraft.util.RandomSource;

import net.minecraft.world.level.ScheduledTickAccess;


import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.ClimateRange;

public class FloodedDeadCropBlock extends DeadCropBlock implements IFluidLoggable
{
    public static final FluidProperty FLUID = TFCBlockStateProperties.FRESH_WATER;

    public FloodedDeadCropBlock(ExtendedProperties properties, Crop crop)
    {
        this(properties, crop.getClimateRange());
    }

    public FloodedDeadCropBlock(ExtendedProperties properties, Supplier<ClimateRange> range)
    {
        super(properties, range);
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return IFluidLoggable.super.getFluidState(state);
    }

    @Override
    public FluidProperty getFluidProperty()
    {
        return FLUID;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        return Helpers.isBlock(level.getBlockState(pos.below()), TFCTags.Blocks.SEA_BUSH_PLANTABLE_ON) || super.canSurvive(state, level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random)
    {
        FluidHelpers.tickFluid(level, tickAccess, currentPos, state);
        return super.updateShape(state, level, tickAccess, currentPos, facing, facingPos, facingState, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(getFluidProperty()));
    }
}