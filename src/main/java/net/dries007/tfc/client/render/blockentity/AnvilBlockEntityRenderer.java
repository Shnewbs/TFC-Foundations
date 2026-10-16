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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blockentities.AnvilBlockEntity;

public class AnvilBlockEntityRenderer extends ItemDisplayBlockEntityRenderer<AnvilBlockEntity>
{
    public AnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    protected void extractItems(AnvilBlockEntity anvil, float partialTicks, PoseStack poseStack, State output, int combinedLight, int combinedOverlay)
    {
        if (anvil.getLevel() == null)
        {
            return;
        }

        final BlockState state = anvil.getLevel().getBlockState(anvil.getBlockPos());
        final int rotation = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING).get2DDataValue() : 0;
        final float yOffset = anvil.getTier() == 0 ? 0.875f : 0.6875f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.003125D + yOffset, 0.5);
        poseStack.scale(0.3f, 0.3f, 0.3f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90f * rotation + 270f));
        poseStack.translate(-0.4f, 0, 0);

        final AnvilBlockEntity.AnvilInventory inventory = anvil.getInventory();
        final ItemStack hammer = inventory.getStackInSlot(AnvilBlockEntity.SLOT_HAMMER);
        if (!hammer.isEmpty())
        {
            addItem(output, hammer, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, anvil.getLevel(), 0);
        }

        poseStack.translate(1.15f, 0, 0);
        final ItemStack input1 = inventory.getStackInSlot(AnvilBlockEntity.SLOT_INPUT_MAIN);
        if (!input1.isEmpty())
        {
            addItem(output, input1, ItemDisplayContext.FIXED, RenderHelpers.getHeatedBrightness(input1, combinedLight), combinedOverlay, poseStack,  anvil.getLevel(), 0);
        }

        poseStack.translate(0.4f, 0, -0.05f);
        final ItemStack input2 = inventory.getStackInSlot(AnvilBlockEntity.SLOT_INPUT_SECOND);
        if (!input2.isEmpty())
        {
            addItem(output, input2, ItemDisplayContext.FIXED, RenderHelpers.getHeatedBrightness(input2, combinedLight), combinedOverlay, poseStack,  anvil.getLevel(),0);
        }

        final ItemStack catalyst = inventory.getStackInSlot(AnvilBlockEntity.SLOT_CATALYST);
        if (!catalyst.isEmpty())
        {
            poseStack.pushPose();
            poseStack.translate(-0.9f, -0.25f, 0.05f);
            poseStack.scale(0.6f, 0.6f, 0.6f);
            addItem(output, catalyst, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack,  anvil.getLevel(),0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
