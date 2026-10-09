/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;

/** Bounded, reload-local geometry cache. Keys contain only immutable snapshot values and this model's identity. */
public final class SnapshotBlockStateModel<T> implements DynamicBlockStateModel
{
    @FunctionalInterface
    public interface Selector<T>
    {
        @Nullable T select(ModelData data, BlockState state);
    }

    private final Selector<T> selector;
    private final BlockStateModelPart empty;
    private final LoadingCache<T, BlockStateModelPart> cache;

    public SnapshotBlockStateModel(Selector<T> selector, Function<T, BlockStateModelPart> factory, BlockStateModelPart empty)
    {
        this.selector = selector;
        this.empty = empty;
        this.cache = CacheBuilder.newBuilder().maximumWeight(4096)
            .weigher((T key, BlockStateModelPart value) -> Math.max(1, value.getQuads(null).size()))
            .build(new CacheLoader<>() {
                @Override public BlockStateModelPart load(T value) { return Objects.requireNonNull(factory.apply(value)); }
            });
    }

    private @Nullable T value(BlockAndTintGetter level, BlockPos pos, BlockState state)
    {
        return selector.select(level.getModelData(pos), state);
    }

    public BlockStateModelPart part(@Nullable T value)
    {
        return value == null ? empty : cache.getUnchecked(value);
    }

    public Object geometryKey(@Nullable T value)
    {
        return new GeometryKey(this, value);
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random)
    {
        return geometryKey(value(level, pos, state));
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output)
    {
        output.add(empty);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> output)
    {
        output.add(part(value(level, pos, state)));
    }

    @Override public Material.Baked particleMaterial() { return empty.particleMaterial(); }
    @Override public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) { return part(value(level, pos, state)).particleMaterial(); }
    @Override public int materialFlags() { return BakedQuad.FLAG_TRANSLUCENT | BakedQuad.FLAG_ANIMATED | empty.materialFlags(); }
    @Override public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) { return part(value(level, pos, state)).materialFlags(); }

    private record GeometryKey(Object model, @Nullable Object value) {}
}
