/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.entities.ai;

/** Simple parity-preserving schedule math, independent of Minecraft bootstrap. */
public final class PredatorScheduleMath
{
    public static final long DAY_TICKS = 24000L;
    public static final long HUNT_SWITCH_TICK = 11000L;

    /** True for the custom HUNT activity; false for vanilla REST. */
    public static boolean shouldHunt(boolean diurnal, long overworldClockTime)
    {
        final boolean beforeRest = Math.floorMod(overworldClockTime, DAY_TICKS) < HUNT_SWITCH_TICK;
        return diurnal == beforeRest;
    }

    private PredatorScheduleMath() {}
}
