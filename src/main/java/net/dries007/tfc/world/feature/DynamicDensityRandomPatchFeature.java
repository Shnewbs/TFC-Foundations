/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ForestType;

public class DynamicDensityRandomPatchFeature extends RandomPatchFeature
{
    public DynamicDensityRandomPatchFeature(Codec<RandomPatchConfig> codec)
    {
        super(codec);
    }

    @Override
    protected int getTries(FeaturePlaceContext<RandomPatchConfig> context, int tries)
    {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final ChunkData data = ChunkData.get(level, pos);
        final ForestType forestType = data.getForestType();
        final int density = forestType.getDensity();

        final int seaLevel = context.chunkGenerator().getSeaLevel();
        if (pos.getY() > seaLevel + 25)
        {
            tries *= 1f - Mth.clampedMap(pos.getY(), seaLevel + 25, seaLevel + 100, 0f, 0.8f);
        }
        switch (density)
        {
            case 4 -> tries = Math.min(tries, 8);
            case 3 -> tries = Math.min(tries, 14);
            case 2 -> tries = Math.min(tries, 40);
            default -> {
                if (context.chunkGenerator() instanceof TFCChunkGenerator generator)
                {
                    tries *= generator.settings().grassDensity() * 2f;
                }
            }
        }

        return tries;
    }
}
