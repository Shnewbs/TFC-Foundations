/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.mixin.accessor.ServerExplosionAccessor;

public class PowderKegExplosion extends ServerExplosion
{
    private static final WeightedList<ExplosionParticleInfo> BLOCK_PARTICLES = WeightedList.<ExplosionParticleInfo>builder()
        .add(new ExplosionParticleInfo(ParticleTypes.POOF, 0.5F, 1.0F))
        .add(new ExplosionParticleInfo(ParticleTypes.SMOKE, 1.0F, 1.0F))
        .build();

    private final ServerLevel level;
    private List<BlockPos> affectedBlockPositions = List.of();
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity source;
    private final float size;

    public PowderKegExplosion(ServerLevel level, @Nullable Entity entity, double x, double y, double z, float size)
    {
        super(level, null, null, null, new Vec3(x, y, z), size, false, BlockInteraction.DESTROY);
        this.level = level;
        this.size = size;
        this.x = x;
        this.y = y;
        this.z = z;
        this.source = entity;
    }

    @Override
    public int explode()
    {
        level.gameEvent(getDirectSourceEntity(), GameEvent.EXPLODE, center());
        final ServerExplosionAccessor nativeExplosion = (ServerExplosionAccessor) this;
        affectedBlockPositions = nativeExplosion.tfc$calculateExplodedPositions();
        nativeExplosion.tfc$hurtEntities(affectedBlockPositions);
        return affectedBlockPositions.size();
    }

    /**
     * Does the second part of the explosion (sound, particles, drop spawn)
     * This will only be called on the logical server. The client side particles and sounds are
     * handled by {@link #sendExplosionPacketToClients()}, which should be called
     * immediately after this.
     *
     * (Forgive the Mojang copypasta)
     */
    public void finalizeExplosion(boolean spawnParticles)
    {
        assert !level.isClientSide();

        final ObjectArrayList<Pair<ItemStack, BlockPos>> allDrops = new ObjectArrayList<>();
        Collections.shuffle(affectedBlockPositions, new Random());

        final boolean easyMode = TFCConfig.SERVER.powderKegOnlyBreaksNaturalBlocks.get();

        for (BlockPos pos : affectedBlockPositions)
        {
            final BlockState state = level.getBlockState(pos);

            if (!easyMode)
            {
                if (Helpers.isBlock(state, TFCTags.Blocks.POWDERKEG_CANNOT_BREAK))
                    continue;
            }
            else
            {
                if (!Helpers.isBlock(state, TFCTags.Blocks.POWDERKEG_CAN_BREAK))
                    continue;
            }

            if (!state.isAir())
            {
                final BlockPos dropPos = pos.immutable();
                Profiler.get().push("explosion_blocks");
                if (state.canDropFromExplosion(this.level, pos, this))
                {
                    final BlockEntity blockentity = state.hasBlockEntity() ? this.level.getBlockEntity(pos) : null;
                    final LootParams.Builder lootContext = (new LootParams.Builder(this.level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);

                    state.getDrops(lootContext).forEach((drop) -> addBlockDrops(allDrops, drop, dropPos));
                }

                state.onBlockExploded(this.level, pos, this);
                Profiler.get().pop();
            }

        }

        for (Pair<ItemStack, BlockPos> pair : allDrops)
        {
            Block.popResource(this.level, pair.getSecond(), pair.getFirst());
        }
    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> allDrops, ItemStack drop, BlockPos dropPos)
    {
        int i = allDrops.size();

        for (int j = 0; j < i; ++j)
        {
            Pair<ItemStack, BlockPos> pair = allDrops.get(j);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, drop))
            {
                ItemStack itemstack1 = ItemEntity.merge(itemstack, drop, 16);
                allDrops.set(j, Pair.of(itemstack1, pair.getSecond()));
                if (drop.isEmpty())
                {
                    return;
                }
            }
        }

        allDrops.add(Pair.of(drop, dropPos));
    }

    /**
     * Notifies clients of the powderkeg explosion to play sounds and show particles.
     * Should be called after {@link #finalizeExplosion(boolean)},
     * since we don't use the vanilla explosion code that sends packets for us.
     */
    public void sendExplosionPacketToClients()
    {
        // Since we don't use the vanilla explosion logic in ServerLevel#explode,
        // we must send explosion packets to clients ourselves
        for (ServerPlayer serverplayer : level.players())
        {
            if (serverplayer.distanceToSqr(x, y, z) < 4096.0)
            {
                serverplayer.connection.send(new ClientboundExplodePacket(
                    center(),
                    size,
                    affectedBlockPositions.size(),
                    Optional.ofNullable(getHitPlayers().get(serverplayer)),
                    isSmall() ? ParticleTypes.EXPLOSION : ParticleTypes.EXPLOSION_EMITTER,
                    SoundEvents.GENERIC_EXPLODE,
                    BLOCK_PARTICLES
                ));
            }
        }
    }
}
