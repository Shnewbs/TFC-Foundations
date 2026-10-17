/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.items.CreativeMiningTool;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin
{
    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;

    // The first native removal is the preventsBlockDrops (creative) branch; the second is survival mining.
    @Inject(method = "destroyBlock", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerPlayerGameMode;removeBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZLnet/minecraft/world/item/ItemStack;)Z",
        ordinal = 0, remap = false))
    private void applySpecialMiningEffectsInCreativeMode(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        final BlockState state = this.level.getBlockState(pos);
        final ItemStack stack = this.player.getMainHandItem();
        if (stack.getItem() instanceof CreativeMiningTool tool)
        {
            tool.mineBlockInCreative(stack, this.level, state, pos, this.player);
        }
    }
}
