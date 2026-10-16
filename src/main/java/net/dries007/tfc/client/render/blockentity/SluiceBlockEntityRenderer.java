/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.SluiceBlockEntity;
import net.dries007.tfc.common.blocks.devices.SluiceBlock;
import net.dries007.tfc.util.Helpers;

public class SluiceBlockEntityRenderer extends ItemDisplayBlockEntityRenderer<SluiceBlockEntity>
{
    public SluiceBlockEntityRenderer(BlockEntityRendererProvider.Context context) { super(context); }

    private void drawItem(ItemStack stack, float x, float y, float z, float rotation, PoseStack poseStack, int combinedLight, int combinedOverlay, State outputState, @Nullable Level level)
    {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(0.3F, 0.3F, 0.3F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        addItem(outputState, stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, level, 0);
        poseStack.popPose();
    }

    private static void vertex(QuadRenderInfo info, float x, float y, float z, int color, float u, float v)
    {
        info.buffer.addVertex(info.pose, x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(info.packedOverlay)
            .setLight(info.packedLight)
            .setNormal(0, 0, 1);
    }

    @Override
    protected void extractItems(SluiceBlockEntity sluice, float partialTicks, PoseStack poseStack, State outputState, int packedLight, int packedOverlay)
    {
        if (sluice.getLevel() == null) return;
        final BlockState state = sluice.getBlockState();
        if (!state.getValue(SluiceBlock.UPPER)) return;

        poseStack.pushPose();
        final Direction facing = state.getValue(SluiceBlock.FACING);
        if (facing == Direction.NORTH)
        {
            poseStack.translate(1F, 0F, 1F);
        }
        else if (facing == Direction.WEST)
        {
            poseStack.translate(1F, 0F, 0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180F));
        }
        else if (facing == Direction.EAST)
        {
            poseStack.translate(0F, 0F, 1F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180F));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(state.getValue(SluiceBlock.FACING).get2DDataValue() * 90F));

        final float rotation = RenderHelpers.itemTimeRotation();
        for (int i = 0; i < sluice.getInventory().getSlots(); i++)
        {
            ItemStack stack = sluice.getInventory().getStackInSlot(i);
            if (!stack.isEmpty())
            {
                final int step = Mth.floor((float) i / 4F);
                final int across = i % 4;
                final float x = 0.125F + (0.25F * across);
                final float y = 0.96875F - 0.0125F - (0.125F * step);
                final float z = 0.15625F - 0.0125F + (0.25F * step);
                drawItem(stack, x, y, z, rotation, poseStack, packedLight, packedOverlay, outputState, sluice.getLevel());
            }
        }

        final Fluid fluid = sluice.getFlow();
        if (fluid == null)
        {
            poseStack.popPose();
            return;
        }

        final TextureAtlasSprite sprite = RenderHelpers.getFluidSprite(fluid);
        final int color = Helpers.isFluid(fluid, TFCTags.Fluids.ANY_INFINITE_WATER) ? TFCColors.getWaterColor(sluice.getBlockPos()) : RenderHelpers.getFluidColor(fluid);

        final VertexConsumer buffer = captureGeometry(outputState, Sheets.translucentBlockSheet());
        final Matrix4f pose = poseStack.last().pose();

        final QuadRenderInfo info = new QuadRenderInfo(buffer, pose, packedOverlay, packedLight);
        // Top
        vertex(info, 0.05F, 1.033F, 0F, color, sprite.getU0(), sprite.getV0());
        vertex(info, 0.05F, -0.15F, 2.45F, color, sprite.getU0(), sprite.getV1());
        vertex(info, 0.95F, -0.15F, 2.45F, color, sprite.getU1(), sprite.getV1());
        vertex(info, 0.95F, 1.033F, 0F, color, sprite.getU1(), sprite.getV0());

        // Bottom
        vertex(info, 0.05F, 0.833F, 0F, color, sprite.getU0(), sprite.getV0());
        vertex(info, 0.05F, -0.3F, 2.45F, color, sprite.getU0(), sprite.getV1());
        vertex(info, 0.95F, -0.3F, 2.45F, color, sprite.getU1(), sprite.getV1());
        vertex(info, 0.95F, 0.833F, 0F, color, sprite.getU1(), sprite.getV0());

        // Left
        vertex(info, 0.05F, -0.15F, 2.45F, color, sprite.getU0(), sprite.getV0());
        vertex(info, 0.05F, 1.033F, 0F, color, sprite.getU0(), sprite.getV1());
        vertex(info, 0.05F, 0.833F, 0F, color, sprite.getU1(), sprite.getV1());
        vertex(info, 0.05F, -0.3F, 2.45F, color, sprite.getU1(), sprite.getV0());

        // Right
        vertex(info, 0.95F, 1.033F, 0F, color, sprite.getU0(), sprite.getV0());
        vertex(info, 0.95F, -0.15F, 2.45F, color, sprite.getU0(), sprite.getV1());
        vertex(info, 0.95F, -0.3F, 2.45F, color, sprite.getU1(), sprite.getV1());
        vertex(info, 0.95F, 0.833F, 0F, color, sprite.getU1(), sprite.getV0());

        poseStack.popPose();
    }

    private record QuadRenderInfo(VertexConsumer buffer, Matrix4f pose, int packedOverlay, int packedLight) {}
}
