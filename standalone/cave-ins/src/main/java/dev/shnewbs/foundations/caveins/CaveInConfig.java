/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package dev.shnewbs.foundations.caveins;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CaveInConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED, EXPLOSIONS, CREATIVE;
    public static final ModConfigSpec.IntValue RADIUS, RISE, SUPPORT_HORIZONTAL,
        SUPPORT_UP, SUPPORT_DOWN, READS, FALLS, JOBS, POSITIONS, MAX_DAMAGE;
    public static final ModConfigSpec.DoubleValue TRIGGER_CHANCE, DAMAGE;
    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.comment("Enable server cave-ins.").define("enabled", true);
        EXPLOSIONS = b.define("explosionsTriggerCollapses", true);
        CREATIVE = b.define("creativeMiningTriggersCollapses", false);
        TRIGGER_CHANCE = b.defineInRange("miningTriggerChance", 0.1, 0.0, 1.0);
        RADIUS = b.defineInRange("collapseRadius", 4, 1, 8);
        RISE = b.defineInRange("maximumPropagationHeight", 16, 1, 32);
        SUPPORT_HORIZONTAL = b.defineInRange("supportHorizontalRange", 4, 0, 8);
        SUPPORT_UP = b.comment("Blocks above a support that it protects.").defineInRange("supportUpRange", 2, 0, 8);
        SUPPORT_DOWN = b.comment("Blocks below a support that it protects.").defineInRange("supportDownRange", 0, 0, 8);
        // Minimum covers the largest permitted support volume plus revalidation.
        READS = b.comment("Explicit scheduler probes/revalidation; excludes native block-update callbacks.")
            .defineInRange("maximumProbeReadsPerLevelTick", 32768, 4918, 131072);
        FALLS = b.defineInRange("maximumFallingBlocksPerLevelTick", 16, 1, 128);
        JOBS = b.defineInRange("maximumConcurrentCollapsesPerLevel", 32, 1, 128);
        POSITIONS = b.defineInRange("maximumPositionsPerCollapse", 4096, 324, 8192);
        DAMAGE = b.defineInRange("damagePerBlockFallen", 2.0, 0.0, 100.0);
        MAX_DAMAGE = b.defineInRange("maximumFallDamage", 20, 0, 1000);
        SPEC = b.build();
    }
    public static CollapseEngine.Limits limits() {
        return new CollapseEngine.Limits(RADIUS.get(), RISE.get(), SUPPORT_HORIZONTAL.get(),
            SUPPORT_UP.get(), SUPPORT_DOWN.get(), READS.get(), FALLS.get(), JOBS.get(), POSITIONS.get());
    }
    private CaveInConfig() {}
}
