/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import net.dries007.tfc.common.entities.ai.PredatorScheduleMath;

public final class PredatorScheduleSmoke
{
    private static int checks;

    private static void check(boolean condition)
    {
        if (!condition) throw new AssertionError("Predator schedule parity mismatch at check " + checks);
        checks++;
    }

    public static void main(String[] args)
    {
        // Frozen pre-port ScheduleBuilder: diurnal HUNT 0..10999 then REST,
        // nocturnal REST 0..10999 then HUNT. Compare complete cycles and
        // negative/multi-day timestamps, not merely threshold samples.
        for (long tick = -48000; tick < 72000; tick++)
        {
            final long dayTick = Math.floorMod(tick, 24000L);
            for (boolean diurnal : new boolean[] {true, false})
            {
                final boolean expected = diurnal ? dayTick < 11000L : dayTick >= 11000L;
                check(PredatorScheduleMath.shouldHunt(diurnal, tick) == expected);
            }
        }
        check(PredatorScheduleMath.shouldHunt(true, Long.MAX_VALUE) ==
            (Math.floorMod(Long.MAX_VALUE, 24000L) < 11000L));
        check(PredatorScheduleMath.shouldHunt(false, Long.MIN_VALUE) ==
            (Math.floorMod(Long.MIN_VALUE, 24000L) >= 11000L));
        System.out.println("PASS: " + checks + " actual production predator-schedule checks.");
    }
}
