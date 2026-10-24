/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package dev.shnewbs.foundations.caveins;

import java.util.List;
import java.util.Collections;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PersistenceCodecTest {
    @Test void nativeNbtRoundTripRetainsNegativeCoordinatesAndPendingOrder() {
        var center = new CollapseEngine.Pos(-17, -40, -16);
        var a = center.offset(0, 1, 0);
        var b = center.offset(-1, 3, 1);
        var state = new CollapseEngine.JobState(center, List.of(b, a), List.of(a, b));
        var encoded = CollapseTracker.JOB_CODEC.encodeStart(NbtOps.INSTANCE, state).getOrThrow();
        assertEquals(state, CollapseTracker.JOB_CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow());
    }
    @Test void persistedPositionCountsAreBounded() {
        var p = new CollapseEngine.Pos(0, 64, 0);
        var oversized = new CollapseEngine.JobState(p, Collections.nCopies(8193, p), List.of(p));
        assertTrue(CollapseTracker.JOB_CODEC.encodeStart(NbtOps.INSTANCE, oversized).error().isPresent());
    }
}
