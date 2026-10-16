/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blockentities.NestBoxBlockEntity;

public class NestBoxBlockEntityRenderer extends ItemDisplayBlockEntityRenderer<NestBoxBlockEntity>
{
    public NestBoxBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    protected void extractItems(NestBoxBlockEntity nestBox, float partialTicks, PoseStack poseStack, State output, int combinedLight, int combinedOverlay)
    {
        final float timeD = RenderHelpers.itemTimeRotation();

        poseStack.translate(0.5D, 0.25D, 0.5D);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        for (int i = 0; i < nestBox.getInventory().getSlots(); i++)
        {
            ItemStack stack = nestBox.getInventory().getStackInSlot(i);
            if (stack.isEmpty()) continue;
            poseStack.pushPose();
            poseStack.translate((i % 2 == 0 ? -1 : 1) * 0.33f, 0, (i < 2 ? -1 : 1) * 0.33f);
            poseStack.mulPose(Axis.YP.rotationDegrees(timeD));
            addItem(output, stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, nestBox.getLevel(), 0);
            poseStack.popPose();
        }
    }
}
