/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.volcano.CenteredFeatureNoise;

import static org.junit.jupiter.api.Assertions.*;

public class CinderConePlacementTest implements TestSetup
{
    @Test
    public void isolatedConesAreNotSuppressedByCrowdingAtWorldOrigin()
    {
        int checked = 0;
        for (long seed = 0; seed < 32; seed++)
        {
            final var sampler = CenteredFeatureNoise.cinder(Seed.unsafeOf(seed));
            final var noise = sampler.getCellularNoise();
            if (noise.cell(0, 0).f2() >= 0.40) continue;
            for (int x = -2048; x <= 2048; x += 512)
            {
                for (int z = -2048; z <= 2048; z += 512)
                {
                    final var cell = noise.cell(x, z);
                    // Wide margin around the 0.46 cutoff: the local cone is
                    // clearly isolated even after rounding its block center.
                    if (noise.cell(cell.x(), cell.y()).f2() > 0.55)
                    {
                        assertEquals(new BlockPos((int) cell.x(), 123, (int) cell.y()), sampler.calculateCenter(x, 123, z, 1),
                            "Isolated cone suppressed at seed=" + seed + ", x=" + x + ", z=" + z);
                        checked++;
                    }
                }
            }
        }
        assertTrue(checked > 0, "Missing isolated cones in the deterministic sample");
    }

    @Test
    public void crowdedConesAreNotAcceptedBecauseWorldOriginIsClear()
    {
        int checked = 0;
        for (long seed = 0; seed < 32; seed++)
        {
            final var sampler = CenteredFeatureNoise.cinder(Seed.unsafeOf(seed));
            final var noise = sampler.getCellularNoise();
            if (noise.cell(0, 0).f2() <= 0.55) continue;
            for (int x = -2048; x <= 2048; x += 512)
            {
                for (int z = -2048; z <= 2048; z += 512)
                {
                    final var cell = noise.cell(x, z);
                    if (noise.cell(cell.x(), cell.y()).f2() < 0.38)
                    {
                        assertNull(sampler.calculateCenter(x, 123, z, 1),
                            "Crowded cone accepted at seed=" + seed + ", x=" + x + ", z=" + z);
                        checked++;
                    }
                }
            }
        }
        assertTrue(checked > 0, "Missing crowded cones in the deterministic sample");
    }

    @Test
    public void disabledConeFrequencyStillProducesNoDecorationCenters()
    {
        final var sampler = CenteredFeatureNoise.cinder(Seed.unsafeOf(42));
        for (int x = -2048; x <= 2048; x += 256)
            for (int z = -2048; z <= 2048; z += 256)
                assertNull(sampler.calculateCenter(x, 123, z, 0));
    }
}
