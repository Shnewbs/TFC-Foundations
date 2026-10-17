/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import java.util.List;
import java.util.Set;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jspecify.annotations.Nullable;

/** Adapts TFC's two authored tint indices to the native per-layer block sources. */
public record BlockTintRegistration(RegisterColorHandlersEvent.BlockTintSources event)
{
    public void register(Color color, Block... blocks)
    {
        for (Block block : blocks)
        {
            final Set<Property<?>> properties = block.defaultBlockState().hasProperty(BlockStateProperties.SNOWY)
                ? Set.of(BlockStateProperties.SNOWY) : Set.of();
            event.register(List.of(new Layer(color, 0, properties), new Layer(color, 1, properties)), block);
        }
    }

    @FunctionalInterface
    public interface Color
    {
        int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex);
    }

    public record Layer(Color color, int index, Set<Property<?>> relevantProperties) implements BlockTintSource
    {
        @Override public int color(BlockState state) { return color.getColor(state, null, null, index) | 0xFF000000; }
        @Override public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) { return color.getColor(state, level, pos, index) | 0xFF000000; }
    }
}
