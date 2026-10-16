/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

/** Adapts guide preview Levels to the native client lighting view without replacing their block data. */
public record BlockModelRenderView(Level level) implements BlockAndTintGetter
{
    public static BlockAndTintGetter of(Level level)
    {
        return level instanceof BlockAndTintGetter view ? view : new BlockModelRenderView(level);
    }

    @Override public CardinalLighting cardinalLighting()
    {
        final var client = Minecraft.getInstance().level;
        return client == null ? CardinalLighting.DEFAULT : client.cardinalLighting();
    }
    @Override public int getBlockTint(BlockPos pos, ColorResolver resolver) { return resolver.getColor(level.getBiome(pos).value(), pos.getX(), pos.getZ()); }
    @Override public int getBrightness(LightLayer layer, BlockPos pos) { return level.getBrightness(layer, pos); }
    @Override public int getRawBrightness(BlockPos pos, int darkening) { return level.getRawBrightness(pos, darkening); }
    @Override public boolean canSeeSky(BlockPos pos) { return level.canSeeSky(pos); }
    @Override public LevelLightEngine getLightEngine() { return level.getLightEngine(); }
    @Override public @Nullable BlockEntity getBlockEntity(BlockPos pos) { return level.getBlockEntity(pos); }
    @Override public BlockState getBlockState(BlockPos pos) { return level.getBlockState(pos); }
    @Override public FluidState getFluidState(BlockPos pos) { return level.getFluidState(pos); }
    @Override public int getHeight() { return level.getHeight(); }
    @Override public int getMinY() { return level.getMinY(); }
}
