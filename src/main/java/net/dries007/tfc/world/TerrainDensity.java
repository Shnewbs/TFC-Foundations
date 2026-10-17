/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world;

import java.util.Map;

import net.dries007.tfc.world.river.RiverBlendType;
import net.dries007.tfc.world.river.RiverNoiseSampler;

/** Density composition shared by the chunk filler and native terrain regression checks. */
public final class TerrainDensity
{
    private TerrainDensity() {}

    public static double applyRivers(int y, double input, double[] weights, Map<RiverBlendType, RiverNoiseSampler> samplers)
    {
        // Each modifier already receives the complete incoming density. Its weighted
        // result replaces that input; retaining the input here would count it twice.
        double noise = 0;
        for (RiverBlendType type : RiverBlendType.ALL)
        {
            final double weight = weights[type.ordinal()];
            if (type == RiverBlendType.NONE)
            {
                noise += weight * input;
            }
            else if (weight > 0)
            {
                noise += weight * samplers.get(type).noise(y, input);
            }
        }
        return noise;
    }
}
