/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin;

import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.dries007.tfc.common.component.CachedMut;
import net.dries007.tfc.world.biome.BiomeBridge;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;


@Mixin(Biome.class)
public abstract class BiomeMixin implements BiomeBridge
{
    @Unique
    private final CachedMut<BiomeExtension> tfc$cachedExtension = CachedMut.unloaded();

    @Nullable
    @Override
    public BiomeExtension tfc$getExtension(@NotNull CommonLevelAccessor level)
    {
        if (!tfc$cachedExtension.isLoaded())
        {
            tfc$cachedExtension.load(TFCBiomes.findExtension(level, (Biome) (Object) this));
        }
        return tfc$cachedExtension.value();
    }


}
