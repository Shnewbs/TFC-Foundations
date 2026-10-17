/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.world.biome.TFCBiomes;

/** Climate replaces biome colors before native timelines and weather modify them. */
@Mixin(EnvironmentAttributeSystem.class)
public abstract class EnvironmentAttributeSystemMixin
{
    @Inject(method = "addDefaultLayers", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;addBiomeLayer(Lnet/minecraft/world/attribute/EnvironmentAttributeSystem$Builder;Lnet/minecraft/core/HolderLookup;Lnet/minecraft/world/level/biome/BiomeManager;)V", shift = At.Shift.AFTER))
    private static void addClimateColors(EnvironmentAttributeSystem.Builder builder, Level world, CallbackInfo ci)
    {
        if (!(world instanceof ClientLevel level)) return;
        builder.addPositionalLayer(EnvironmentAttributes.SKY_COLOR, (base, pos, weights) -> {
            final BlockPos blockPos = BlockPos.containing(pos);
            final var biome = level.getBiome(blockPos).value();
            return TFCBiomes.hasExtension(level, biome) ? TFCColors.getSkyColor(level, biome, blockPos) : base;
        });
        builder.addPositionalLayer(EnvironmentAttributes.FOG_COLOR, (base, pos, weights) -> {
            final BlockPos blockPos = BlockPos.containing(pos);
            final var biome = level.getBiome(blockPos).value();
            return TFCBiomes.hasExtension(level, biome) ? TFCColors.getFogColor(level, biome, blockPos) : base;
        });
        builder.addPositionalLayer(EnvironmentAttributes.WATER_FOG_COLOR, (base, pos, weights) -> {
            final BlockPos blockPos = BlockPos.containing(pos);
            final var biome = level.getBiome(blockPos).value();
            return TFCBiomes.hasExtension(level, biome) ? TFCColors.getWaterFogColor(level, biome, blockPos) : base;
        });
    }
}
