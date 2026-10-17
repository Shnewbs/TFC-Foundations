/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test;

import java.util.EnumMap;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.world.TerrainDensity;
import net.dries007.tfc.world.river.RiverBlendType;
import net.dries007.tfc.world.river.RiverNoiseSampler;

import static org.junit.jupiter.api.Assertions.*;

public class TerrainDensityTest
{
    @Test
    public void noRiverPreservesTheOriginalDensityAndSolidAirBoundary()
    {
        final double[] weights = new double[RiverBlendType.SIZE];
        weights[RiverBlendType.NONE.ordinal()] = 1;
        for (double density : new double[] {-1, 0, 0.2, 0.3, 0.399, 0.4, 0.401, 0.8, 1})
        {
            assertEquals(density, TerrainDensity.applyRivers(63, density, weights, new EnumMap<>(RiverBlendType.class)), 1e-12);
        }
    }

    @Test
    public void blendingIdentityModifiersPreservesDensity()
    {
        final var samplers = new EnumMap<RiverBlendType, RiverNoiseSampler>(RiverBlendType.class);
        final double[] weights = new double[RiverBlendType.SIZE];
        for (var type : RiverBlendType.ALL)
        {
            samplers.put(type, RiverNoiseSampler.NONE);
            weights[type.ordinal()] = 1.0 / RiverBlendType.SIZE;
        }
        assertEquals(0.3, TerrainDensity.applyRivers(52, 0.3, weights, samplers), 1e-12);
    }

    @Test
    public void fullRiverReplacementDoesNotRetainTheUnmodifiedTerrain()
    {
        final var samplers = new EnumMap<RiverBlendType, RiverNoiseSampler>(RiverBlendType.class);
        samplers.put(RiverBlendType.WIDE, new RiverNoiseSampler()
        {
            @Override public double noise(int y, double input) { return 0.7; }
        });
        final double[] weights = new double[RiverBlendType.SIZE];
        weights[RiverBlendType.WIDE.ordinal()] = 1;
        assertEquals(0.7, TerrainDensity.applyRivers(60, 0.3, weights, samplers), 1e-12);
        weights[RiverBlendType.WIDE.ordinal()] = 0.5;
        weights[RiverBlendType.NONE.ordinal()] = 0.5;
        assertEquals(0.5, TerrainDensity.applyRivers(60, 0.3, weights, samplers), 1e-12);
    }
}
