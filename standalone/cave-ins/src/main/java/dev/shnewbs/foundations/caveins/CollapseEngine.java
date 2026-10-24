/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package dev.shnewbs.foundations.caveins;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Bounded, server-thread collapse scheduler. No Minecraft or sibling-mod imports. */
public final class CollapseEngine {
    public record Pos(int x, int y, int z) {
        public Pos offset(int dx, int dy, int dz) { return new Pos(x + dx, y + dy, z + dz); }
    }

    public record Limits(int radius, int rise, int supportHorizontal, int supportUp,
                         int supportDown, int readsPerTick, int fallsPerTick,
                         int maxJobs, int maxPositionsPerJob) {
        public Limits {
            if (radius < 1 || radius > 8 || rise < 1 || rise > 32
                || supportHorizontal < 0 || supportHorizontal > 8
                || supportUp < 0 || supportUp > 8 || supportDown < 0 || supportDown > 8
                || readsPerTick < 1 || fallsPerTick < 1 || fallsPerTick > 128
                || maxJobs < 1 || maxJobs > 128 || maxPositionsPerJob < 1 || maxPositionsPerJob > 8192) {
                throw new IllegalArgumentException("Invalid cave-in limits");
            }
            int cost = (2 * supportHorizontal + 1) * (2 * supportHorizontal + 1)
                * (supportUp + supportDown + 1) + 5;
            if (readsPerTick < cost) throw new IllegalArgumentException("Read budget cannot cover one atomic support check");
        }
        int worstCost() {
            return (2 * supportHorizontal + 1) * (2 * supportHorizontal + 1)
                * (supportUp + supportDown + 1) + 5;
        }
    }

    public interface World {
        /** Must not load a chunk. Unknown positions conservatively stop collapse. */
        boolean loaded(Pos pos);
        boolean collapsible(Pos pos);
        boolean open(Pos pos);
        boolean support(Pos pos);
        /** Recheck and remove exactly one source block, spawning its falling entity. */
        boolean fall(Pos pos);
    }

    public record JobState(Pos center, List<Pos> pending, List<Pos> visited) {
        public JobState { pending = List.copyOf(pending); visited = List.copyOf(visited); }
    }
    /** worldReads is a conservative bound on explicit probes, not native neighbor-update internals. */
    public record TickResult(int inspected, int fell, int worldReads, int pendingJobs) {}

    private static final class Job {
        final Pos center;
        final ArrayDeque<Pos> pending = new ArrayDeque<>();
        final Set<Pos> visited = new HashSet<>();
        Job(Pos center) { this.center = center; }
    }

    private final Limits limits;
    private final ArrayDeque<Job> jobs = new ArrayDeque<>();
    private final Set<Pos> centers = new HashSet<>();

    public CollapseEngine(Limits limits) { this.limits = limits; }
    public int pendingJobs() { return jobs.size(); }

    public boolean trigger(Pos center) {
        if (jobs.size() >= limits.maxJobs() || !centers.add(center)) return false;
        Job job = new Job(center);
        // Lowest candidates first, then propagate upward only after a successful fall.
        for (int y = 0; y <= Math.min(3, limits.rise()); y++) {
            for (int x = -limits.radius(); x <= limits.radius(); x++) {
                for (int z = -limits.radius(); z <= limits.radius(); z++) {
                    Pos pos = center.offset(x, y, z);
                    if (!pos.equals(center)) add(job, pos);
                }
            }
        }
        jobs.addLast(job);
        return true;
    }

    private void add(Job job, Pos pos) {
        if (Math.abs((long) pos.x() - job.center.x()) <= limits.radius()
            && Math.abs((long) pos.z() - job.center.z()) <= limits.radius()
            && (long) pos.y() - job.center.y() >= 0
            && (long) pos.y() - job.center.y() <= limits.rise()
            && job.visited.size() < limits.maxPositionsPerJob() && job.visited.add(pos)) {
            job.pending.addLast(pos);
        }
    }

    public TickResult tick(World world) {
        int inspected = 0, fell = 0, reads = 0;
        // Reserve a complete support scan before each candidate; never split it over ticks.
        while (!jobs.isEmpty() && reads + limits.worstCost() <= limits.readsPerTick()
            && fell < limits.fallsPerTick()) {
            Job job = jobs.removeFirst();
            Pos pos = job.pending.removeFirst();
            inspected++;
            if (world.loaded(pos) && world.loaded(pos.offset(0, -1, 0))) {
                reads++;
                if (world.collapsible(pos)) {
                    reads++;
                    if (world.open(pos.offset(0, -1, 0))) {
                        boolean supported = false;
                        supportSearch:
                        for (int x = -limits.supportHorizontal(); x <= limits.supportHorizontal(); x++) {
                            for (int z = -limits.supportHorizontal(); z <= limits.supportHorizontal(); z++) {
                                // A support below reaches up; a support above reaches down.
                                for (int y = -limits.supportUp(); y <= limits.supportDown(); y++) {
                                    Pos at = pos.offset(x, y, z);
                                    if (!world.loaded(at)) { supported = true; break supportSearch; }
                                    reads++;
                                    if (world.support(at)) { supported = true; break supportSearch; }
                                }
                            }
                        }
                        if (!supported) {
                            reads += 3; // Native source/below revalidation and source-removal audit.
                            if (world.fall(pos)) {
                                fell++;
                                add(job, pos.offset(0, 1, 0));
                            }
                        }
                    }
                }
            }
            if (job.pending.isEmpty()) centers.remove(job.center);
            else jobs.addLast(job); // Fair across simultaneous triggers.
        }
        return new TickResult(inspected, fell, reads, jobs.size());
    }

    public List<JobState> snapshot() {
        return jobs.stream().map(j -> new JobState(j.center, new ArrayList<>(j.pending),
            j.visited.stream().sorted(java.util.Comparator.comparingInt(Pos::x)
                .thenComparingInt(Pos::y).thenComparingInt(Pos::z)).toList())).toList();
    }

    public void restore(List<JobState> states) {
        jobs.clear(); centers.clear();
        for (JobState state : states) {
            if (jobs.size() >= limits.maxJobs()) break;
            if (centers.contains(state.center())) continue;
            Job job = new Job(state.center());
            for (Pos pos : state.visited()) {
                if (job.visited.size() >= limits.maxPositionsPerJob()) break;
                // Reuse the normal bounds check; later replace its temporary pending queue.
                add(job, pos);
            }
            job.pending.clear();
            Set<Pos> queued = new HashSet<>();
            for (Pos pos : state.pending()) {
                if (job.visited.contains(pos) && queued.add(pos)) job.pending.addLast(pos);
            }
            if (!job.pending.isEmpty()) { centers.add(job.center); jobs.addLast(job); }
        }
    }
}
