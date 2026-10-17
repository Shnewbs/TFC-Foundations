/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.client.overworld.LevelRendererExtension;

/** Install climate colors before the native attribute samplers and camera probes are built. */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin
{
    @Inject(method = "addEnvironmentAttributeLayers", at = @At("RETURN"))
    private void addClimateLayers(EnvironmentAttributeSystem.Builder builder, CallbackInfoReturnable<EnvironmentAttributeSystem.Builder> ci)
    {
        final ClientLevel level = (ClientLevel) (Object) this;
        if (level.dimensionType().skybox() == DimensionType.Skybox.OVERWORLD)
        {
            builder.addConstantLayer(NeoForgeEnvironmentAttributes.CUSTOM_SKYBOX, base -> LevelRendererExtension.EFFECTS);
            builder.addConstantLayer(NeoForgeEnvironmentAttributes.CUSTOM_WEATHER_EFFECTS, base -> LevelRendererExtension.EFFECTS);
        }
    }
}
