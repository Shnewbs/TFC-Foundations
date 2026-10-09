/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.jspecify.annotations.Nullable;

/** Immutable baked alternatives; the selected part participates in the geometry cache key. */
public abstract class SeasonalBlockStateModel implements DynamicBlockStateModel
{
    private final List<BlockStateModelPart> parts;
    private final int fallback;
    private final int flags;

    protected SeasonalBlockStateModel(List<BlockStateModelPart> parts, int expectedSize, int fallback)
    {
        if (parts.size() != expectedSize || fallback < 0 || fallback >= expectedSize)
        {
            throw new IllegalArgumentException("Invalid seasonal model alternatives");
        }
        this.parts = List.copyOf(parts);
        this.fallback = fallback;
        this.flags = parts.stream().mapToInt(BlockStateModelPart::materialFlags).reduce(0, (a, b) -> a | b);
    }

    protected abstract int select(@Nullable BlockState state, @Nullable BlockPos pos);

    private BlockStateModelPart selected(@Nullable BlockState state, @Nullable BlockPos pos)
    {
        return parts.get(select(state, pos));
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output)
    {
        output.add(selected(null, null));
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> output)
    {
        output.add(selected(state, pos));
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random)
    {
        return new GeometryKey(this, selected(state, pos));
    }

    @Override
    public Material.Baked particleMaterial()
    {
        return parts.get(fallback).particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state)
    {
        return selected(state, pos).particleMaterial();
    }

    @Override
    public int materialFlags()
    {
        // Conservative union: no seasonal alternative may disappear from layer classification.
        return flags;
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state)
    {
        return selected(state, pos).materialFlags();
    }

    private record GeometryKey(SeasonalBlockStateModel model, BlockStateModelPart part) {}
}
