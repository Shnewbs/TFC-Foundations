/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface IHighlightHandler
{
    /** Extracts selection geometry while the world and player are safe to read. */
    @Nullable
    Highlight extractHighlight(Level level, BlockPos pos, Player player, BlockHitResult hit);

    /** Immutable geometry only; the client renderer owns drawing. */
    record Highlight(VoxelShape shape, int color) {}
}
