/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jspecify.annotations.Nullable;

/** Immutable, client-class-free snapshots passed to chunk mesh workers by ModelDataManager. */
public final class BlockEntityModelData
{
    public static final ModelProperty<Pile> PILE = new ModelProperty<>();
    public static final ModelProperty<Scraping> SCRAPING = new ModelProperty<>();
    public static final ModelProperty<Identifier> MOLD = new ModelProperty<>();

    private BlockEntityModelData() {}

    public record Pile(List<Identifier> textures, Identifier fallback)
    {
        public Pile
        {
            textures = List.copyOf(textures);
            Objects.requireNonNull(fallback);
        }

        public Identifier texture(int index)
        {
            return index >= 0 && index < textures.size() ? textures.get(index) : fallback;
        }
    }

    public record Scraping(@Nullable Identifier input, @Nullable Identifier output,
        short positions, int inputColor, int outputColor)
    {
        public Scraping
        {
            // Dye colors are RGB; vertex colors must carry an opaque alpha channel.
            inputColor |= 0xff000000;
            outputColor |= 0xff000000;
        }
    }

    /** Refresh both the snapshot and its chunk mesh after a client-side data change. */
    public static void refresh(BlockEntity entity)
    {
        final var level = entity.getLevel();
        if (level != null && level.isClientSide())
        {
            entity.requestModelDataUpdate();
            level.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
