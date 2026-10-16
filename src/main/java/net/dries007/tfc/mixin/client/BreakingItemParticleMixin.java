/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.client.ItemRenderTint;
import net.dries007.tfc.mixin.client.accessor.ItemStackRenderStateAccessor;

/** Resolve the same native tint layers used by item drawing, including rotten-food colors. */
@Mixin(BreakingItemParticle.Provider.class)
public abstract class BreakingItemParticleMixin
{
    @Inject(method = "createParticle(Lnet/minecraft/core/particles/ItemParticleOption;Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/util/RandomSource;)Lnet/minecraft/client/particle/Particle;", at = @At("RETURN"))
    private void tintParticle(ItemParticleOption option, ClientLevel level, double x, double y, double z,
        double xa, double ya, double za, RandomSource random, CallbackInfoReturnable<Particle> callback)
    {
        if (callback.getReturnValue() instanceof SingleQuadParticle particle)
        {
            final ItemStackRenderState state = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, option.getItem().create(), ItemDisplayContext.GROUND, level, null, 0);
            final ItemStackRenderStateAccessor access = (ItemStackRenderStateAccessor) state;
            final int color = ItemRenderTint.firstColor(access.tfc$layers(), access.tfc$activeLayerCount());
            particle.setColor((color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f);
        }
    }
}
