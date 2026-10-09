/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.feature;

import java.util.stream.Stream;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** The patch parameters retained by TFC after vanilla removed random patches. */
public record RandomPatchConfig(int tries, int xzSpread, int ySpread, Holder<PlacedFeature> feature) implements FeatureConfiguration
{
    public static final Codec<RandomPatchConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("tries", 128).forGetter(RandomPatchConfig::tries),
        Codec.intRange(0, Integer.MAX_VALUE - 1).optionalFieldOf("xz_spread", 7).forGetter(RandomPatchConfig::xzSpread),
        Codec.intRange(0, Integer.MAX_VALUE - 1).optionalFieldOf("y_spread", 3).forGetter(RandomPatchConfig::ySpread),
        PlacedFeature.CODEC.fieldOf("feature").forGetter(RandomPatchConfig::feature)
    ).apply(instance, RandomPatchConfig::new));

    @Override
    public Stream<Holder<ConfiguredFeature<?, ?>>> getSubFeatures()
    {
        return feature.value().getFeatures();
    }
}
