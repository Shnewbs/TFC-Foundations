/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package dev.shnewbs.foundations.caveins;

import com.mojang.logging.LogUtils;
import java.util.function.Supplier;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

@Mod(CaveIns.MOD_ID)
public final class CaveIns {
    public static final String MOD_ID = "foundations_caveins";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
    static final Supplier<AttachmentType<CollapseTracker>> TRACKER = ATTACHMENTS.register("collapses",
        () -> AttachmentType.builder(CollapseTracker::new).serialize(CollapseTracker.CODEC.fieldOf("jobs")).build());

    public CaveIns(IEventBus bus, ModContainer container) {
        ATTACHMENTS.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, CaveInConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(this::onBreak);
        NeoForge.EVENT_BUS.addListener(this::onExplosion);
        NeoForge.EVENT_BUS.addListener(this::onTick);
        NeoForge.EVENT_BUS.addListener(this::commands);
        LOGGER.info("Foundations Cave-Ins standalone loaded; TFC present: {}", ModList.get().isLoaded("tfc"));
    }

    private void onBreak(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || event.isCanceled()) return;
        if (!CaveInConfig.ENABLED.get() || (event.getPlayer().isCreative() && !CaveInConfig.CREATIVE.get())) return;
        if (event.getState().is(NativeWorld.TRIGGERS)
            && level.getRandom().nextDouble() < CaveInConfig.TRIGGER_CHANCE.get()) {
            level.getData(TRACKER).watchRemoval(event.getPos(), event.getState());
        }
    }
    private void onExplosion(ExplosionEvent.Detonate event) {
        if (!CaveInConfig.ENABLED.get() || !CaveInConfig.EXPLOSIONS.get()) return;
        if (event.getLevel() instanceof ServerLevel level) {
            int accepted = 0;
            for (BlockPos pos : event.getAffectedBlocks()) {
                if (accepted >= CaveInConfig.JOBS.get()) break;
                if (level.hasChunkAt(pos) && level.getBlockState(pos).is(NativeWorld.TRIGGERS)) {
                    level.getData(TRACKER).watchRemoval(pos, level.getBlockState(pos)); accepted++;
                }
            }
        }
    }
    private void onTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) level.getData(TRACKER).tick(level);
    }
    private void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("fcaveins")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("status").executes(context -> {
                context.getSource().sendSuccess(() -> Component.literal(
                    context.getSource().getLevel().getData(TRACKER).status()), false);
                return 1;
            }))
            .then(Commands.literal("trigger").then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(context -> {
                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                    boolean accepted = CaveInConfig.ENABLED.get() && context.getSource().getLevel()
                        .getData(TRACKER).engine().trigger(NativeWorld.pos(pos));
                    context.getSource().sendSuccess(() -> Component.literal("Collapse queued: " + accepted), false);
                    return accepted ? 1 : 0;
                }))));
    }
}
