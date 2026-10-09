/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity.state;

import net.minecraft.util.Mth;

/** Pure extraction calculations, shared with the headless regression checks. */
public final class LivestockRenderStateMath
{
    private LivestockRenderStateMath() {}

    public static float geneticScale(int size)
    {
        return Mth.clampedMap(size, 1F, 32F, 0.9F, 1.1F);
    }

    public static float wingFlap(float previousFlap, float flap, float previousSpeed, float speed, float partialTick)
    {
        return (Mth.sin(Mth.lerp(partialTick, previousFlap, flap)) + 1F)
            * Mth.lerp(partialTick, previousSpeed, speed);
    }
}
