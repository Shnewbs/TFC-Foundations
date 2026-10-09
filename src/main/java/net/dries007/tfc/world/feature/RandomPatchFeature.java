/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/** Preserves the triangular spread and random draw order of TFC's existing patches. */
public class RandomPatchFeature extends Feature<RandomPatchConfig>
{
    public RandomPatchFeature(Codec<RandomPatchConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RandomPatchConfig> context)
    {
        final RandomPatchConfig config = context.config();
        final RandomSource random = context.random();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final int width = config.xzSpread() + 1;
        final int height = config.ySpread() + 1;
        final int tries = getTries(context, config.tries());
        int placed = 0;

        for (int i = 0; i < tries; i++)
        {
            pos.setWithOffset(context.origin(), random.nextInt(width) - random.nextInt(width), random.nextInt(height) - random.nextInt(height), random.nextInt(width) - random.nextInt(width));
            if (config.feature().value().place(context.level(), context.chunkGenerator(), random, pos))
            {
                placed++;
            }
        }
        return placed > 0;
    }

    protected int getTries(FeaturePlaceContext<RandomPatchConfig> context, int tries)
    {
        return tries;
    }
}
