/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */
package dev.shnewbs.foundations.caveins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

final class NativeWorld implements CollapseEngine.World {
    static final TagKey<Block> COLLAPSIBLE = tag("collapsible");
    static final TagKey<Block> TRIGGERS = tag("triggers_collapse");
    static final TagKey<Block> SUPPORTS = tag("supports");
    static final TagKey<Block> IMMUNE = tag("immune");
    static final TagKey<Block> HEAVY = tag("heavy_blocks");
    static final TagKey<Block> LIGHT = tag("light_blocks");
    private final ServerLevel level;
    NativeWorld(ServerLevel level) { this.level = level; }
    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(CaveIns.MOD_ID, path));
    }
    static BlockPos nativePos(CollapseEngine.Pos p) { return new BlockPos(p.x(), p.y(), p.z()); }
    static CollapseEngine.Pos pos(BlockPos p) { return new CollapseEngine.Pos(p.getX(), p.getY(), p.getZ()); }
    @Override public boolean loaded(CollapseEngine.Pos p) {
        return p.y() >= level.getMinY() && p.y() <= level.getMaxY() && level.hasChunkAt(nativePos(p));
    }
    @Override public boolean collapsible(CollapseEngine.Pos p) {
        BlockPos at = nativePos(p);
        return eligible(level.getBlockState(at), at);
    }
    private boolean eligible(BlockState state, BlockPos at) {
        return state.is(COLLAPSIBLE) && !state.is(IMMUNE) && !state.hasBlockEntity()
            && state.getDestroySpeed(level, at) >= 0;
    }
    @Override public boolean open(CollapseEngine.Pos p) {
        return FallingBlock.isFree(level.getBlockState(nativePos(p)));
    }
    @Override public boolean support(CollapseEngine.Pos p) {
        return level.getBlockState(nativePos(p)).is(SUPPORTS);
    }
    @Override public boolean fall(CollapseEngine.Pos p) {
        BlockPos at = nativePos(p);
        BlockState state = level.getBlockState(at);
        if (!eligible(state, at) || !FallingBlock.isFree(level.getBlockState(at.below()))) return false;
        FallingBlockEntity entity = FallingBlockEntity.fall(level, at, state);
        BlockState after = level.getBlockState(at);
        // Native fall removes the block before posting the entity join event. Restore it
        // if another mod rejects the entity, and never duplicate a source that stayed put.
        if (!entity.isAddedToLevel() || after.equals(state)) {
            entity.discard();
            if (after.equals(state.getFluidState().createLegacyBlock())) level.setBlockAndUpdate(at, state);
            return false;
        }
        float mass = state.is(HEAVY) ? 2f : state.is(LIGHT) ? 0.5f : 1f;
        entity.setHurtsEntities((float) (CaveInConfig.DAMAGE.get() * mass), CaveInConfig.MAX_DAMAGE.get());
        return true;
    }
}
