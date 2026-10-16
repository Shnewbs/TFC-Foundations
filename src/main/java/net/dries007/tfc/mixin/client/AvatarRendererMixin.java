/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.items.GlassBlowpipeItem;

/** Capture the HEAD item state while the avatar and its inventory are available. */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin
{
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void extractBlowpipe(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci)
    {
        if (state.isUsingItem && entity.getUseItem().getItem() instanceof GlassBlowpipeItem)
        {
            Minecraft.getInstance().getItemModelResolver().updateForLiving(state.heldOnHead, entity.getUseItem(), ItemDisplayContext.HEAD, entity);
        }
    }
}
