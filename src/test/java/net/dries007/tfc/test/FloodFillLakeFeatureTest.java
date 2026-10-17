/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.world.feature.FloodFillLakeConfig;
import net.dries007.tfc.world.feature.FloodFillLakeFeature;

import static org.junit.jupiter.api.Assertions.*;

/** Runs the production feature against native block states in a bounded basin world fixture. */
public class FloodFillLakeFeatureTest implements TestSetup
{
    private static final int FLOOR = 100, RIM = 103, RADIUS = 5;

    @Test
    public void waterFilledCalderaReachesTheFloorAndReplacesTheEntirePool() throws Exception
    {
        final var basin = new Basin(8, 8, Blocks.WATER.defaultBlockState(), true);
        assertTrue(place(basin, calderaConfig()));
        assertPool(basin, Blocks.LAVA.defaultBlockState());
    }

    @Test
    public void emptyCalderaStillFillsWithLava() throws Exception
    {
        final var basin = new Basin(8, 8, Blocks.AIR.defaultBlockState(), true);
        assertTrue(place(basin, calderaConfig()));
        assertPool(basin, Blocks.LAVA.defaultBlockState());
    }

    @Test
    public void waterFeatureRetainsItsConfiguredFluid()
    {
        final var basin = new Basin(8, 8, Blocks.AIR.defaultBlockState(), true);
        assertTrue(place(basin, new FloodFillLakeConfig(Blocks.WATER.defaultBlockState(), List.of(), true)));
        assertPool(basin, Blocks.WATER.defaultBlockState());
    }

    @Test
    public void uncontainedBasinDoesNotPlaceOrScheduleAnything() throws Exception
    {
        final var basin = new Basin(8, 8, Blocks.WATER.defaultBlockState(), false);
        assertFalse(place(basin, calderaConfig()));
        assertTrue(basin.writes.isEmpty());
        assertEquals(0, basin.ticks);
    }

    @Test
    public void unlistedLavaCannotBeReplacedByAWaterFeature()
    {
        final var basin = new Basin(8, 8, Blocks.LAVA.defaultBlockState(), true);
        assertFalse(place(basin, new FloodFillLakeConfig(Blocks.WATER.defaultBlockState(), List.of(), true)));
        assertTrue(basin.writes.isEmpty());
        assertEquals(0, basin.ticks);
    }

    @Test
    public void waterReplacementAlsoWorksAcrossNegativeChunkBoundaries() throws Exception
    {
        for (var center : List.of(new BlockPos(-16, 0, -17), new BlockPos(-1, 0, -1), new BlockPos(15, 0, 15)))
        {
            final var basin = new Basin(center.getX(), center.getZ(), Blocks.WATER.defaultBlockState(), true);
            assertTrue(place(basin, calderaConfig()), center.toString());
            assertPool(basin, Blocks.LAVA.defaultBlockState());
        }
    }

    private static FloodFillLakeConfig calderaConfig() throws Exception
    {
        final Path file = Path.of(System.getProperty("tfc.test.projectDir"))
            .resolve("src/main/resources/data/tfc/worldgen/configured_feature/volcano_caldera.json");
        final var json = JsonParser.parseString(Files.readString(file)).getAsJsonObject().get("config");
        final var config = FloodFillLakeConfig.CODEC.parse(REGISTRIES.get().createSerializationContext(JsonOps.INSTANCE), json).getOrThrow();
        assertEquals(Blocks.LAVA.defaultBlockState(), config.getState());
        assertTrue(config.shouldReplace(Fluids.WATER));
        assertTrue(config.shouldOverfill());
        return config;
    }

    private static boolean place(Basin basin, FloodFillLakeConfig config)
    {
        return new FloodFillLakeFeature(FloodFillLakeConfig.CODEC).place(new FeaturePlaceContext<>(
            Optional.empty(), basin.world(), null, RandomSource.create(0), new BlockPos(basin.x, RIM + 1, basin.z), config));
    }

    private static void assertPool(Basin basin, BlockState expected)
    {
        final int width = 2 * RADIUS + 1;
        assertEquals(width * width * (RIM - FLOOR), basin.writes.size());
        assertEquals(basin.writes.size(), basin.ticks);
        for (int x = basin.x - RADIUS; x <= basin.x + RADIUS; x++)
        {
            for (int z = basin.z - RADIUS; z <= basin.z + RADIUS; z++)
            {
                assertEquals(Blocks.STONE.defaultBlockState(), basin.state(new BlockPos(x, FLOOR, z)));
                for (int y = FLOOR + 1; y <= RIM; y++)
                    assertEquals(expected, basin.state(new BlockPos(x, y, z)));
                assertTrue(basin.state(new BlockPos(x, RIM + 1, z)).isAir());
            }
        }
    }

    private static final class Basin
    {
        final int x, z;
        final BlockState initialFluid;
        final boolean enclosed;
        final Map<BlockPos, BlockState> writes = new HashMap<>();
        int ticks;

        Basin(int x, int z, BlockState initialFluid, boolean enclosed)
        {
            this.x = x;
            this.z = z;
            this.initialFluid = initialFluid;
            this.enclosed = enclosed;
        }

        BlockState state(BlockPos pos)
        {
            if (writes.containsKey(pos)) return writes.get(pos);
            if (pos.getY() <= FLOOR) return Blocks.STONE.defaultBlockState();
            final boolean inside = Math.abs(pos.getX() - x) <= RADIUS && Math.abs(pos.getZ() - z) <= RADIUS;
            if (pos.getY() <= RIM)
            {
                if (inside) return initialFluid;
                if (enclosed) return Blocks.STONE.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        }

        WorldGenLevel world()
        {
            return (WorldGenLevel) Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(), new Class<?>[] {WorldGenLevel.class}, (proxy, method, args) -> switch (method.getName()) {
                case "getBlockState" -> state((BlockPos) args[0]);
                case "isEmptyBlock" -> state((BlockPos) args[0]).isAir();
                case "setBlock" -> {
                    writes.put(((BlockPos) args[0]).immutable(), (BlockState) args[1]);
                    yield true;
                }
                case "scheduleTick" -> { ticks++; yield null; }
                case "toString" -> "Bounded basin fixture";
                default -> throw new UnsupportedOperationException(method.toString());
            });
        }
    }
}
