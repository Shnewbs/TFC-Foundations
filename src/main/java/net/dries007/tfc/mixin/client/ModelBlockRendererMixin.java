/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.client;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.blocks.plant.KrummholzBlock;
import net.dries007.tfc.common.blocks.wood.FallenLeavesBlock;
import net.dries007.tfc.common.blocks.wood.ILeavesBlock;

/** Retains vanilla's fast-leaf opacity for TFC foliage in chunks and moving blocks. */
@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin
{
    @Inject(method = "forceOpaque", at = @At("HEAD"), cancellable = true)
    private static void forceTFCLeavesOpaque(boolean cutoutLeaves, BlockState state, CallbackInfoReturnable<Boolean> callback)
    {
        final Block block = state.getBlock();
        if (block instanceof ILeavesBlock || block instanceof KrummholzBlock || block instanceof FallenLeavesBlock)
        {
            callback.setReturnValue(!cutoutLeaves);
        }
    }
}
