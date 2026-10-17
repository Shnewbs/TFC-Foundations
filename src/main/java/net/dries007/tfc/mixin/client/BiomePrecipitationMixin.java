/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.dries007.tfc.util.tracker.WeatherHelpers;

@Mixin(Biome.class)
public abstract class BiomePrecipitationMixin
{
    /**
     * Replace {@link Biome#getPrecipitationAt(BlockPos, int)} with a method that takes our climate model into account
     * <p>
     * We only do this on the client, where we can get the client level.
     * This is safe to do even for levels that use biome based precipitation, because
     * {@link WeatherHelpers#getPrecipitationAt(Level, BlockPos, Biome.Precipitation)} checks for us what type of climate the level uses.
     * 
     * TFC doesn't currently directly use this, but it's needed for mod compatability.
     */
    @ModifyReturnValue(method = "getPrecipitationAt", at = @At("RETURN"))
    private Biome.Precipitation getPrecipitaitionFromClimate(Biome.Precipitation original, @Local BlockPos pos)
    {
        Level level = Minecraft.getInstance().level;
        if (level == null)
        {
            return original;
        }
        return WeatherHelpers.getPrecipitationAt(level, pos, original);
    }
}
