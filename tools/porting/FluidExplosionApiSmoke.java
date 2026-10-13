/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.dries007.tfc.mixin.accessor.FlowingFluidAccessor;
import net.dries007.tfc.mixin.accessor.ServerExplosionAccessor;

/** Pinned API and production mixin bridge checks; does not bootstrap or simulate a world. */
public final class FluidExplosionApiSmoke
{
    private static int checks;

    private static void invokers(Class<?> bridge, Class<?> target) throws Exception
    {
        for (Method method : bridge.getDeclaredMethods())
        {
            final Invoker invoker = method.getAnnotation(Invoker.class);
            if (invoker == null)
            {
                throw new AssertionError("Missing invoker annotation: " + method);
            }
            final Method nativeMethod = target.getDeclaredMethod(invoker.value(), method.getParameterTypes());
            if (method.getReturnType() != nativeMethod.getReturnType()
                || Modifier.isStatic(method.getModifiers()) != Modifier.isStatic(nativeMethod.getModifiers()))
            {
                throw new AssertionError("Invalid bridge return type/staticness: " + method);
            }
            checks++;
        }
    }

    private static void contract(Class<?> target, String name, Class<?> result, Class<?>... parameters) throws Exception
    {
        final Method method = target.getDeclaredMethod(name, parameters);
        if (method.getReturnType() != result || Modifier.isStatic(method.getModifiers()))
        {
            throw new AssertionError("Unexpected native callback contract: " + method);
        }
        checks++;
    }

    public static void main(String[] args) throws Exception
    {
        // Compile the real interfaces, then resolve every annotated target including private overloads.
        // A static/instance mismatch passes javac but fails when Mixin transforms the game class.
        invokers(FlowingFluidAccessor.class, FlowingFluid.class);
        invokers(ServerExplosionAccessor.class, ServerExplosion.class);
        contract(ServerExplosion.class, "explode", int.class);
        contract(FlowingFluid.class, "getNewLiquid", FluidState.class, ServerLevel.class, BlockPos.class, BlockState.class);
        contract(FlowingFluid.class, "spread", void.class, ServerLevel.class, BlockPos.class, BlockState.class, FluidState.class);
        contract(FlowingFluid.class, "tick", void.class, ServerLevel.class, BlockPos.class, BlockState.class, FluidState.class);
        contract(Fluid.class, "randomTick", void.class, ServerLevel.class, BlockPos.class, FluidState.class, RandomSource.class);
        contract(BlockBehaviour.class, "entityInside", void.class, BlockState.class, Level.class, BlockPos.class,
            Entity.class, InsideBlockEffectApplier.class, boolean.class);
        contract(BlockBehaviour.class, "getAnalogOutputSignal", int.class, BlockState.class, Level.class, BlockPos.class, Direction.class);
        contract(BlockBehaviour.class, "getLightDampening", int.class, BlockState.class);
        contract(BlockBehaviour.class, "propagatesSkylightDown", boolean.class, BlockState.class);
        contract(Block.class, "fallOn", void.class, Level.class, BlockState.class, BlockPos.class, Entity.class, double.class);
        contract(BucketPickup.class, "pickupBlock", ItemStack.class, LivingEntity.class, LevelAccessor.class, BlockPos.class, BlockState.class);
        contract(LiquidBlockContainer.class, "canPlaceLiquid", boolean.class, LivingEntity.class, BlockGetter.class, BlockPos.class,
            BlockState.class, Fluid.class);
        contract(IBlockExtension.class, "onDestroyedByPlayer", boolean.class, BlockState.class, Level.class, BlockPos.class,
            Player.class, ItemStack.class, boolean.class, FluidState.class);
        contract(IBlockExtension.class, "onBlockExploded", void.class, BlockState.class, ServerLevel.class, BlockPos.class, Explosion.class);
        if (checks != 19)
        {
            throw new AssertionError("Expected 19 bridge/callback contracts, got " + checks);
        }
        System.out.println("PASS: " + checks + " pinned fluid, explosion and block API contracts; no world or Mixin startup test.");
    }
}
