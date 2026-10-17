/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.feature;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntBinaryOperator;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.settings.RockSettings;

/** Dry upland landmarks, planned atomically and confined to their generating chunk. */
public final class NaturalStoneArchFeature extends Feature<NoneFeatureConfiguration>
{
    public NaturalStoneArchFeature(Codec<NoneFeatureConfiguration> codec)
    {
        super(codec);
    }

    public static boolean eligible(BiomeExtension biome, float rainfall, int height, int seaLevel)
    {
        return rainfall >= 0 && rainfall <= 250 && height >= seaLevel + 12
            && (biome == TFCBiomes.BADLANDS || biome == TFCBiomes.PLATEAU
                || biome == TFCBiomes.PLATEAU_WIDE || biome == TFCBiomes.HIGHLANDS
                || biome == TFCBiomes.WHORLED_CANYONS);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
    {
        if (!(context.chunkGenerator() instanceof TFCChunkGenerator generator)) return false;
        final WorldGenLevel level = context.level();
        final ChunkPos chunk = ChunkPos.containing(context.origin());
        final int minX = chunk.getMinBlockX(), minZ = chunk.getMinBlockZ();
        final int centerX = minX + 8, centerZ = minZ + 8;
        final StoneArchShape shape = new StoneArchShape(context.random().nextBoolean(), 5 + context.random().nextInt(3), context.random().nextDouble() * Math.PI * 2);
        final int baseY = foundationHeight((x, z) -> level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, minX + x, minZ + z), shape);
        if (baseY == Integer.MIN_VALUE || baseY + 13 >= level.getMaxY()) return false;
        final BlockPos center = new BlockPos(centerX, baseY, centerZ);
        final ChunkData data = ChunkData.get(level, center);
        if (!eligible(TFCBiomes.getExtensionOrThrow(level, level.getBiome(center).value()), data.getAverageRainfall(center), baseY, generator.getSeaLevel())) return false;

        final Map<BlockPos, BlockState> plan = new LinkedHashMap<>();
        boolean firstFoot = false, secondFoot = false;
        for (int x = 1; x <= 14; x++)
        {
            for (int z = 1; z <= 14; z++)
            {
                // Validate both feet, including uneven ground, before writing a single block.
                if (shape.isFoot(x, z) && shape.contains(x, 0, z, 0))
                {
                    final int groundY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, minX + x, minZ + z);
                    if (Math.abs(groundY - baseY) > 3) return false;
                    final BlockPos ground = new BlockPos(minX + x, groundY - 1, minZ + z);
                    final RockSettings rock = data.getRockData().getRock(ground);
                    if (!naturalGround(level.getBlockState(ground), rock)) return false;
                    for (int y = groundY; y < baseY; y++)
                    {
                        if (!add(level, data, plan, new BlockPos(minX + x, y, minZ + z))) return false;
                    }
                    if ((shape.alongX() ? x : z) < 8) firstFoot = true;
                    else secondFoot = true;
                }
                for (int y = 0; y <= 12; y++)
                {
                    final BlockPos pos = new BlockPos(minX + x, baseY + y, minZ + z);
                    final double detail = generator.archDetail().map(f -> normalizedDetail(f.compute(new DensityFunction.SinglePointContext(pos.getX(), pos.getY(), pos.getZ())), f.minValue(), f.maxValue())).orElse(0d);
                    if (shape.contains(x, y, z, detail) && !add(level, data, plan, pos)) return false;
                }
            }
        }
        if (!firstFoot || !secondFoot || plan.size() < 40) return false;
        // Do not seal an existing rise, water, plant or structure inside the opening.
        for (int y = 0; y <= 2; y++)
        {
            for (int across = 5; across <= 10; across++)
            {
                final BlockPos opening = new BlockPos(minX + (shape.alongX() ? 7 : across), baseY + y, minZ + (shape.alongX() ? across : 7));
                if (!level.getBlockState(opening).isAir() || plan.containsKey(opening)) return false;
            }
        }
        plan.forEach((pos, state) -> level.setBlock(pos, state, 2));
        if (Boolean.getBoolean("tfc.debug.natural_landmarks"))
        {
            net.dries007.tfc.TerraFirmaCraft.LOGGER.info("Natural stone arch at {}: {} blocks, Terralith detail={}", center, plan.size(), generator.archDetail().isPresent());
        }
        return true;
    }

    /** Seat the opening above gently uneven ground, with both feet grounded by at most three blocks. */
    public static int foundationHeight(IntBinaryOperator height, StoneArchShape shape)
    {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int x = 1; x <= 14; x++)
        {
            for (int z = 1; z <= 14; z++)
            {
                final boolean opening = (shape.alongX() ? x == 7 && z >= 5 && z <= 10 : z == 7 && x >= 5 && x <= 10);
                if (shape.isFoot(x, z) || opening)
                {
                    final int y = height.applyAsInt(x, z);
                    min = Math.min(min, y);
                    max = Math.max(max, y);
                }
            }
        }
        return max - min <= 3 ? max : Integer.MIN_VALUE;
    }

    /** Respect the installed density function's range without copying or assuming its definition. */
    public static double normalizedDetail(double value, double min, double max)
    {
        final double midpoint = (min + max) * 0.5, halfRange = (max - min) * 0.5;
        return Double.isFinite(value) && Double.isFinite(midpoint) && Double.isFinite(halfRange) && halfRange > 0
            ? Math.clamp((value - midpoint) / halfRange, -1, 1) : 0;
    }

    private static boolean naturalGround(BlockState state, RockSettings rock)
    {
        return state.getFluidState().isEmpty() && (rock.isRawOrHardened(state)
            || state.is(TFCTags.Blocks.DIRT) || state.is(TFCTags.Blocks.GRASS) || state.is(TFCTags.Blocks.COARSE_DIRT));
    }

    private static boolean add(WorldGenLevel level, ChunkData data, Map<BlockPos, BlockState> plan, BlockPos pos)
    {
        final BlockState current = level.getBlockState(pos);
        final RockSettings rock = data.getRockData().getRock(pos);
        if (!current.isAir() && !naturalGround(current, rock)) return false;
        if (!current.getFluidState().isEmpty()) return false;
        // Hardened rock is TFC's naturally stable exposed rock, so an arch does not collapse on first update.
        if (current.isAir()) plan.put(pos, rock.hardened().defaultBlockState());
        return true;
    }
}
