/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package dev.shnewbs.foundations.caveins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Per-level persisted work; no global world references or TFC attachment types. */
final class CollapseTracker {
    static final Codec<CollapseEngine.Pos> POS_CODEC = Codec.LONG.xmap(
        value -> NativeWorld.pos(BlockPos.of(value)), value -> NativeWorld.nativePos(value).asLong());
    static final Codec<CollapseEngine.JobState> JOB_CODEC = RecordCodecBuilder.create(i -> i.group(
        POS_CODEC.fieldOf("center").forGetter(CollapseEngine.JobState::center),
        POS_CODEC.listOf(0, 8192).fieldOf("pending").forGetter(CollapseEngine.JobState::pending),
        POS_CODEC.listOf(0, 8192).fieldOf("visited").forGetter(CollapseEngine.JobState::visited)
    ).apply(i, CollapseEngine.JobState::new));
    static final Codec<CollapseTracker> CODEC = JOB_CODEC.listOf(0, 128).xmap(CollapseTracker::new, CollapseTracker::savedJobs);

    private final Map<BlockPos, BlockState> triggers = new LinkedHashMap<>();
    private List<CollapseEngine.JobState> restored = List.of();
    private CollapseEngine engine;
    private CollapseEngine.Limits appliedLimits;
    private long totalFallen;
    private long droppedTriggers;

    CollapseTracker() {}
    private CollapseTracker(List<CollapseEngine.JobState> jobs) { restored = List.copyOf(jobs); }
    private List<CollapseEngine.JobState> savedJobs() { return engine == null ? restored : engine.snapshot(); }

    CollapseEngine engine() {
        CollapseEngine.Limits limits = CaveInConfig.limits();
        if (!limits.equals(appliedLimits)) {
            List<CollapseEngine.JobState> jobs = savedJobs();
            engine = new CollapseEngine(limits);
            engine.restore(jobs);
            appliedLimits = limits;
            restored = List.of();
        }
        return engine;
    }

    void watchRemoval(BlockPos at, BlockState before) {
        if (triggers.size() >= CaveInConfig.JOBS.get()) { droppedTriggers++; return; }
        triggers.put(at.immutable(), before);
    }

    void tick(ServerLevel level) {
        if (!CaveInConfig.ENABLED.get()) return;
        for (Map.Entry<BlockPos, BlockState> entry : new ArrayList<>(triggers.entrySet())) {
            BlockPos at = entry.getKey();
            // Events precede actual destruction: cancelled/protected removals do not trigger.
            if (level.hasChunkAt(at) && !level.getBlockState(at).equals(entry.getValue())) {
                if (!engine().trigger(NativeWorld.pos(at))) droppedTriggers++;
            }
        }
        triggers.clear();
        CollapseEngine.TickResult result = engine().tick(new NativeWorld(level));
        totalFallen += result.fell();
    }
    String status() {
        return "enabled=" + CaveInConfig.ENABLED.get() + " jobs=" + engine().pendingJobs()
            + " fell=" + totalFallen + " rejectedTriggers=" + droppedTriggers;
    }
}
