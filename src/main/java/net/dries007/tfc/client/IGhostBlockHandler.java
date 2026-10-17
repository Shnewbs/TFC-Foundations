/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IGhostBlockHandler
{
    /**
     * Controls if the model should be grown slightly to avoid clipping issues
     */
    default boolean shouldGrowSlightly()
    {
        return true;
    }

    /**
     * The transparency of the ghost block
     */
    default float alpha()
    {
        return 0.66F;
    }

    /**
     * @param player     The player.
     * @param lookState  The block 'highlighted' by the player
     * @param direction  The direction of the raytrace.
     * @param pos        The block position of the highilighted block
     * @param x          [0-1] The relative position of the hit in the x direction
     * @param y          [0-1] The relative position of the hit in the y direction
     * @param z          [0-1] The relative position of the hit in the z direction
     * @param item       The item held in the main hand.
     * @return The BlockState to be rendered as a ghost block, or null if nothing extra should be rendered.
     */
    @Nullable
    BlockState getStateToDraw(Level level, Player player, BlockState lookState, Direction direction, BlockPos pos, double x, double y, double z, ItemStack item);

}
