/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FlowingFluid.class)
public interface FlowingFluidAccessor
{
    @Invoker("canPassThroughWall")
    static boolean invoke$canPassThroughWall(Direction face, BlockGetter world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState)
    {
        throw new AssertionError("Mixin invoker has not been transformed");
    }

    @Invoker("isWaterHole")
    boolean invoke$isWaterHole(BlockGetter world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState);

    @Invoker("canPassThrough")
    boolean invoke$canPassThrough(BlockGetter world, Fluid fluid, BlockPos sourcePos, BlockState sourceState, Direction direction,
        BlockPos targetPos, BlockState targetState, FluidState targetFluid);
}
