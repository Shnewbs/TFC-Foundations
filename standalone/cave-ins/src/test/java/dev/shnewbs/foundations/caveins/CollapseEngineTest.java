/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package dev.shnewbs.foundations.caveins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollapseEngineTest {
    private static final CollapseEngine.Pos CENTER = new CollapseEngine.Pos(0, 64, 0);
    private static CollapseEngine.Limits limits(int budget, int falls) {
        return new CollapseEngine.Limits(1, 16, 2, 2, 0, budget, falls, 4, 512);
    }
    private static final class World implements CollapseEngine.World {
        final Map<CollapseEngine.Pos, String> blocks = new HashMap<>();
        final List<CollapseEngine.Pos> fallen = new ArrayList<>();
        Predicate<CollapseEngine.Pos> available = pos -> true;
        int reads;
        boolean rejectFall;
        void stone(CollapseEngine.Pos pos) { blocks.put(pos, "stone"); }
        void beam(CollapseEngine.Pos pos) { blocks.put(pos, "beam"); }
        String read(CollapseEngine.Pos pos) { assertTrue(available.test(pos), "Read an unloaded chunk"); reads++; return blocks.getOrDefault(pos, "air"); }
        public boolean loaded(CollapseEngine.Pos pos) { return available.test(pos); }
        public boolean collapsible(CollapseEngine.Pos pos) { return read(pos).equals("stone"); }
        public boolean open(CollapseEngine.Pos pos) { return read(pos).equals("air"); }
        public boolean support(CollapseEngine.Pos pos) { return read(pos).equals("beam"); }
        public boolean fall(CollapseEngine.Pos pos) {
            if (!collapsible(pos) || !open(pos.offset(0, -1, 0)) || rejectFall) return false;
            blocks.remove(pos); fallen.add(pos); return true;
        }
    }
    private static void drain(CollapseEngine engine, World world) {
        int ticks = 0;
        while (engine.pendingJobs() > 0) {
            assertTrue(++ticks < 1000, "Scheduler stalled"); engine.tick(world);
        }
    }

    @Test void unsupportedRoofFallsAndPropagatesAboveInitialScan() {
        World world = new World();
        for (int y = 1; y <= 12; y++) world.stone(CENTER.offset(0, y, 0));
        CollapseEngine e = new CollapseEngine(limits(5000, 4));
        assertTrue(e.trigger(CENTER)); drain(e, world);
        assertEquals(12, world.fallen.size());
        assertEquals(12, world.fallen.stream().distinct().count());
    }
    @Test void structuralBeamProtectsRoof() {
        World world = new World(); world.stone(CENTER.offset(0, 1, 0)); world.beam(CENTER.offset(2, 0, 0));
        CollapseEngine e = new CollapseEngine(limits(5000, 4)); e.trigger(CENTER); drain(e, world);
        assertTrue(world.fallen.isEmpty());
    }
    @Test void unknownSupportRegionProtectsRatherThanLoadingChunks() {
        World world = new World(); world.available = p -> p.x() >= 0; world.stone(CENTER.offset(0, 1, 0));
        CollapseEngine e = new CollapseEngine(limits(5000, 4)); e.trigger(CENTER); drain(e, world);
        assertTrue(world.fallen.isEmpty());
    }
    @Test void nonCollapsibleAndBlockedBlocksStay() {
        World world = new World(); world.blocks.put(CENTER.offset(1, 1, 0), "protected");
        world.stone(CENTER.offset(0, 1, 0)); world.blocks.put(CENTER, "floor");
        CollapseEngine e = new CollapseEngine(limits(5000, 4)); e.trigger(CENTER); drain(e, world);
        assertTrue(world.fallen.isEmpty()); assertEquals(3, world.blocks.size());
    }
    @Test void tickBudgetsCoverRealReadsAndEntityCount() {
        World world = new World();
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) world.stone(CENTER.offset(x, 1, z));
        CollapseEngine e = new CollapseEngine(limits(160, 1)); e.trigger(CENTER);
        int ticks = 0;
        while (e.pendingJobs() != 0) {
            assertTrue(++ticks < 100); int before = world.reads;
            CollapseEngine.TickResult result = e.tick(world);
            assertTrue(world.reads - before <= result.worldReads());
            assertTrue(result.worldReads() <= 160); assertTrue(result.fell() <= 1);
        }
        assertEquals(9, world.fallen.size());
    }
    @Test void triggerDeduplicationAndQueueBounds() {
        CollapseEngine e = new CollapseEngine(limits(5000, 4));
        assertTrue(e.trigger(CENTER)); assertFalse(e.trigger(CENTER));
        for (int i = 1; i < 4; i++) assertTrue(e.trigger(CENTER.offset(i * 100, 0, 0)));
        assertFalse(e.trigger(CENTER.offset(400, 0, 0))); assertEquals(4, e.pendingJobs());
    }
    @Test void restartSnapshotResumesIdenticalRemainingWork() {
        World a = new World(), b = new World();
        for (int y = 1; y <= 12; y++) a.stone(CENTER.offset(0, y, 0));
        CollapseEngine before = new CollapseEngine(limits(160, 1)); before.trigger(CENTER);
        for (int i = 0; i < 4; i++) before.tick(a);
        b.blocks.putAll(a.blocks);
        CollapseEngine after = new CollapseEngine(limits(160, 1)); after.restore(before.snapshot());
        int already = a.fallen.size(); drain(before, a); drain(after, b);
        assertEquals(a.fallen.subList(already, a.fallen.size()), b.fallen);
        assertEquals(a.blocks, b.blocks);
    }
    @Test void snapshotsDoNotExposeMutableQueues() {
        CollapseEngine e = new CollapseEngine(limits(5000, 4)); e.trigger(CENTER);
        var snapshot = e.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getFirst().pending().clear());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getFirst().visited().clear());
    }
    @Test void restoredOutOfBoundsAndDuplicatePositionsCannotFall() {
        CollapseEngine e = new CollapseEngine(limits(5000, 4));
        var roof = CENTER.offset(0, 1, 0); var far = CENTER.offset(1000, 1, 0);
        e.restore(List.of(new CollapseEngine.JobState(CENTER, List.of(roof, roof, far), List.of(roof, far))));
        World world = new World(); world.stone(roof); world.stone(far); drain(e, world);
        assertEquals(List.of(roof), world.fallen); assertTrue(world.blocks.containsKey(far));
    }
    @Test void supportChangesBeforeProcessingAreObserved() {
        CollapseEngine e = new CollapseEngine(limits(80, 1)); e.trigger(CENTER);
        World world = new World(); world.stone(CENTER.offset(0, 1, 0));
        e.tick(world); world.beam(CENTER.offset(1, 0, 0)); drain(e, world);
        assertTrue(world.fallen.isEmpty());
    }
    @Test void failedNativeRevalidationDoesNotPropagateOrDuplicate() {
        World world = new World(); world.stone(CENTER.offset(0, 1, 0)); world.rejectFall = true;
        CollapseEngine e = new CollapseEngine(limits(5000, 4)); e.trigger(CENTER); drain(e, world);
        assertTrue(world.fallen.isEmpty()); assertEquals(1, world.blocks.size());
    }
    @Test void negativeChunkBoundaryMatchesPositiveCoordinates() {
        List<Integer> outcomes = new ArrayList<>();
        for (int x : new int[]{-17, -16, -1, 0, 15, 16}) {
            CollapseEngine.Pos center = new CollapseEngine.Pos(x, 64, -16);
            World world = new World(); for (int y = 1; y <= 5; y++) world.stone(center.offset(0, y, 0));
            CollapseEngine e = new CollapseEngine(limits(5000, 4)); e.trigger(center); drain(e, world);
            outcomes.add(world.fallen.size());
        }
        assertEquals(List.of(5, 5, 5, 5, 5, 5), outcomes);
    }
    @Test void supportVerticalRangesHaveCorrectDirection() {
        for (int down : new int[]{0, 2}) {
            CollapseEngine e = new CollapseEngine(new CollapseEngine.Limits(1, 16, 2, 0, down, 5000, 4, 4, 512));
            World world = new World(); world.stone(CENTER.offset(0, 1, 0)); world.beam(CENTER.offset(2, 3, 0));
            e.trigger(CENTER); drain(e, world);
            assertEquals(down == 0 ? 1 : 0, world.fallen.size());
        }
    }
    @Test void insufficientAtomicReadBudgetIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> limits(1, 1));
    }
}
