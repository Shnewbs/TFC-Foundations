/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blockentities.ChannelBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ChannelBlockEntityRenderer extends ItemDisplayBlockEntityRenderer<ChannelBlockEntity>
{
    public ChannelBlockEntityRenderer(BlockEntityRendererProvider.Context context) { super(context); }

    @Override
    protected void extractItems(ChannelBlockEntity channel, float partialTicks, PoseStack poseStack, State outputState,
            int combinedLight, int combinedOverlay)
    {
        if (!channel.hasFlow())
        {
            return;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(channel.getFluid());
        int color = RenderHelpers.getFluidColor(fluid);
        TextureAtlasSprite sprite = RenderHelpers.getFluidSprite(fluid);

        VertexConsumer builder = captureGeometry(outputState, Sheets.cutoutBlockSheet());

        RenderHelpers.renderChannelFlowCenter(poseStack, builder, sprite, color, combinedLight, combinedOverlay);
        RenderHelpers.renderChannelFlow(poseStack, builder, sprite, color, combinedLight, combinedOverlay,
                channel.getFlowSource(), channel.isConnectedToAnotherChannel());
    }

    @Override
    public AABB getRenderBoundingBox(ChannelBlockEntity channel)
    {
        if (channel.hasFlow())
        {
            Vec3 worldPosition = channel.getBlockPos().getCenter();
            if (channel.getFlowSource().getLeft() == Direction.UP)
            {
                int height = 1 + channel.getFlowSource().getRight();
                return new AABB(
                        worldPosition.add(-1.5, -0.5, -1.5),
                        worldPosition.add(1.5, height, 1.5));
            }
            else
            {
                return new AABB(
                        worldPosition.add(-1.5, -0.5, -1.5),
                        worldPosition.add(1.5, 1.5, 1.5));
            }
        }
        else
        {
            return new AABB(channel.getBlockPos());
        }
    }
}
