/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.blocks.devices.PlacedItemBlock;
import net.dries007.tfc.common.blocks.rock.AqueductBlock;

/** Native removal no longer has the old block callback, and same-block slot changes also need it. */
@Mixin(LevelChunk.class)
public abstract class LevelChunkRemovalMixin
{
    @Shadow @Final private Level level;

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z", ordinal = 0))
    private void preserveTfcRemoval(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir,
        @Local(name = "oldState") BlockState oldState)
    {
        if (level.isClientSide() || (flags & 256) != 0) return;
        if (oldState.getBlock() instanceof PlacedItemBlock placed) placed.handleStateRemoval(oldState, level, pos, state);
        if (oldState.getBlock() instanceof AqueductBlock aqueduct) aqueduct.handleStateRemoval(oldState, level, pos, state);
    }
}
