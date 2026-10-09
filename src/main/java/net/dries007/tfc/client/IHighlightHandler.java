/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.renderer.ShapeRenderer;

public interface IHighlightHandler
{
    /** Extracts selection geometry while the world and player are safe to read. */
    @Nullable
    Highlight extractHighlight(Level level, BlockPos pos, Player player, BlockHitResult hit);

    /** Only immutable selection data is retained for rendering; never capture a world or player. */
    record Highlight(VoxelShape shape, int color)
    {
        public CustomBlockOutlineRenderer renderer(BlockPos pos)
        {
            return outline(pos, shape, color, true);
        }
    }

    static CustomBlockOutlineRenderer outline(BlockPos pos, VoxelShape shape, int color, boolean suppressVanilla)
    {
        return (renderState, buffers, stack, translucentPass, levelState) -> {
            final Vec3 camera = levelState.cameraRenderState.pos;
            ShapeRenderer.renderShape(stack, buffers.getBuffer(RenderTypes.lines()), shape,
                pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z, color, 2f);
            return suppressVanilla;
        };
    }
}
