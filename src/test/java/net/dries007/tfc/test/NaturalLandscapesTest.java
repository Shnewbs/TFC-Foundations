/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.data.providers.BuiltinWorldPreset;
import net.dries007.tfc.world.ChunkBiomeSampler;
import net.dries007.tfc.world.biome.BiomeBlendType;
import net.dries007.tfc.world.feature.StoneArchShape;
import net.dries007.tfc.world.feature.NaturalStoneArchFeature;
import net.dries007.tfc.world.settings.Settings;

import static org.junit.jupiter.api.Assertions.*;

public class NaturalLandscapesTest implements TestSetup
{
    @Test
    public void oldSettingsRetainOrdinaryWorldgenAndNewProfileRoundTrips()
    {
        final var rocks = new net.minecraft.core.MappedRegistry<net.dries007.tfc.world.settings.RockSettings>(net.dries007.tfc.world.settings.RockSettings.KEY, com.mojang.serialization.Lifecycle.stable());
        for (var rock : net.dries007.tfc.common.blocks.rock.Rock.values())
        {
            rocks.register(BuiltinWorldPreset.rockKey(rock), net.dries007.tfc.data.providers.BuiltinRockSettings.makeSetting(rock), net.minecraft.core.RegistrationInfo.BUILT_IN);
        }
        rocks.freeze();
        final var lookup = net.minecraft.core.HolderLookup.Provider.create(java.util.stream.Stream.concat(REGISTRIES.get().listRegistries(), java.util.stream.Stream.of(rocks)));
        final var ops = lookup.createSerializationContext(JsonOps.INSTANCE);
        final var standard = BuiltinWorldPreset.defaultSettings(rocks);
        final var json = Settings.CODEC.codec().encodeStart(ops, standard).getOrThrow().getAsJsonObject();
        json.remove("natural_landscapes");
        assertFalse(Settings.CODEC.codec().parse(ops, json).getOrThrow().naturalLandscapes());
        final var natural = standard.withNaturalLandscapes(true);
        final var encoded = Settings.CODEC.codec().encodeStart(ops, natural).getOrThrow();
        final var decoded = Settings.CODEC.codec().parse(ops, encoded).getOrThrow();
        assertTrue(decoded.naturalLandscapes());
        assertEquals(encoded, Settings.CODEC.codec().encodeStart(ops, decoded).getOrThrow());
    }

    @Test
    public void naturalBiomeWeightsAreNormalizedPositiveAndIndependentOfChunkOrigin()
    {
        for (int origin : new int[] {-64, -16, 0, 16, 64})
        {
            final var a = ChunkBiomeSampler.sampleBiomes(new ChunkPos(origin >> 4, -1), (x, z) -> Math.floorMod(x / 11 + z / 7, 3), b -> BiomeBlendType.LAND, true);
            final var b = ChunkBiomeSampler.sampleBiomes(new ChunkPos((origin >> 4) + 1, -1), (x, z) -> Math.floorMod(x / 11 + z / 7, 3), k -> BiomeBlendType.LAND, true);
            // Overlapping quart samples must agree bit-for-bit in either generation order.
            for (int z = 0; z < 7; z++)
            {
                for (int x = 4; x < 7; x++)
                {
                    for (int biome = 0; biome < 3; biome++) assertEquals(a[x + 7*z].getDouble(biome), b[x - 4 + 7*z].getDouble(biome), 1e-12);
                }
            }
            for (var sample : a)
            {
                assertEquals(1, sample.values().doubleStream().sum(), 1e-9);
                sample.values().forEach(v -> assertTrue(v >= 0 && v <= 1));
            }
        }
        for (double t : new double[] {0, .1, .5, .9, 1})
        {
            double sum = 0;
            for (int i = 0; i < 4; i++) sum += ChunkBiomeSampler.cubicWeight(i, t);
            assertEquals(1, sum, 1e-12);
        }
    }

    @Test
    public void landmarksExcludeWetLowlandsCoastsAndVolcanoes()
    {
        assertTrue(NaturalStoneArchFeature.eligible(net.dries007.tfc.world.biome.TFCBiomes.BADLANDS, 150, 90, 63));
        assertFalse(NaturalStoneArchFeature.eligible(net.dries007.tfc.world.biome.TFCBiomes.BADLANDS, 400, 90, 63));
        assertFalse(NaturalStoneArchFeature.eligible(net.dries007.tfc.world.biome.TFCBiomes.BADLANDS, 150, 64, 63));
        assertFalse(NaturalStoneArchFeature.eligible(net.dries007.tfc.world.biome.TFCBiomes.VOLCANIC_MOUNTAINS, 150, 160, 63));
        assertFalse(NaturalStoneArchFeature.eligible(net.dries007.tfc.world.biome.TFCBiomes.SHORE, 150, 90, 63));
    }

    @Test
    public void naturalPresetPreservesRegionalClimateAndGeology()
    {
        final var standardSeed = net.dries007.tfc.world.Seed.of(42);
        final var naturalSeed = net.dries007.tfc.world.Seed.of(42);
        final var settings = BuiltinWorldPreset.defaultSettings();
        final var standard = new net.dries007.tfc.world.region.RegionGenerator(settings, standardSeed);
        final var natural = new net.dries007.tfc.world.region.RegionGenerator(settings.withNaturalLandscapes(true), naturalSeed);
        final var layer = new net.dries007.tfc.world.layer.framework.ConcurrentArea<>(net.dries007.tfc.world.layer.TFCLayers.createRegionBiomeLayer(natural, naturalSeed), net.dries007.tfc.world.layer.TFCLayers::getFromLayerId);
        int candidates = 0;
        for (int x = -8192; x <= 8192; x += 1024)
        for (int z = -8192; z <= 8192; z += 1024)
        {
            final var pos = new ChunkPos(x >> 4, z >> 4);
            final var a = standard.chunkDataGenerator().createAndGenerate(pos);
            final var b = natural.chunkDataGenerator().createAndGenerate(pos);
            assertEquals(a.getAverageRainfall(x, z), b.getAverageRainfall(x, z));
            assertEquals(a.getRockData().getSurfaceRock(x, z), b.getRockData().getSurfaceRock(x, z));
            final var biome = layer.get(x >> 2, z >> 2);
            if (candidates < 12 && NaturalStoneArchFeature.eligible(biome, b.getAverageRainfall(x, z), 100, 63))
            {
                System.out.println("Dry upland seed 42 candidate: " + x + ", " + z + " biome=" + biome.key().identifier() + " rainfall=" + b.getAverageRainfall(x, z));
                candidates++;
            }
        }
    }

    @Test
    public void archFoundationsClearGentleSlopesWithoutBridgingSteepDrops()
    {
        for (boolean axis : new boolean[] {false, true})
        {
            final var shape = new StoneArchShape(axis, 6, 0);
            assertEquals(82, NaturalStoneArchFeature.foundationHeight((x, z) -> 82, shape));
            assertEquals(83, NaturalStoneArchFeature.foundationHeight((x, z) -> x == 7 && z == 7 ? 83 : 82, shape));
            assertEquals(85, NaturalStoneArchFeature.foundationHeight((x, z) -> x < 8 ? 82 : 85, shape));
            assertEquals(Integer.MIN_VALUE, NaturalStoneArchFeature.foundationHeight((x, z) -> x < 8 ? 82 : 90, shape));
        }
    }

    @Test
    public void everyArchHasConnectedStoneGroundedFeetAndAnOpenWalkwayWithinOneChunk()
    {
        for (boolean axis : new boolean[] {false, true})
        for (int rise = 5; rise <= 7; rise++)
        for (int phase = 0; phase < 20; phase++)
        for (double detail : new double[] {-1, 0, 1})
        {
            final var shape = new StoneArchShape(axis, rise, phase * Math.PI / 10);
            final Set<BlockPos> blocks = new HashSet<>();
            for (int x = -1; x <= 16; x++) for (int z = -1; z <= 16; z++) for (int y = 0; y <= 12; y++)
            {
                if (shape.contains(x, y, z, detail))
                {
                    assertTrue(x >= 1 && x <= 14 && z >= 1 && z <= 14);
                    blocks.add(new BlockPos(x, y, z));
                }
            }
            assertFalse(blocks.isEmpty());
            final Set<BlockPos> visited = new HashSet<>();
            final var queue = new ArrayDeque<BlockPos>(); queue.add(blocks.iterator().next());
            while (!queue.isEmpty())
            {
                final var pos = queue.remove();
                if (!blocks.contains(pos) || !visited.add(pos)) continue;
                for (var direction : net.minecraft.core.Direction.values()) queue.add(pos.relative(direction));
            }
            assertEquals(blocks, visited, "No detached floating stones");
            assertTrue(blocks.stream().anyMatch(p -> p.getY() == 0 && (axis ? p.getX() : p.getZ()) < 8));
            assertTrue(blocks.stream().anyMatch(p -> p.getY() == 0 && (axis ? p.getX() : p.getZ()) > 8));
            for (int y = 0; y < 3; y++) assertFalse(shape.contains(7, y, 7, detail), "Three-block-high walkway");
        }
        assertEquals(0, NaturalStoneArchFeature.normalizedDetail(Double.NaN, -2, 0));
        assertEquals(-1, NaturalStoneArchFeature.normalizedDetail(-100, -2, 0));
        assertEquals(1, NaturalStoneArchFeature.normalizedDetail(100, -2, 0));
    }
}
