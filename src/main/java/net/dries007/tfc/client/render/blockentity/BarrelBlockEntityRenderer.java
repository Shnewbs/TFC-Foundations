/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blocks.devices.BarrelBlock;
import net.dries007.tfc.config.TFCConfig;

public class BarrelBlockEntityRenderer extends ItemDisplayBlockEntityRenderer<BarrelBlockEntity>
{
    public BarrelBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    protected void extractItems(BarrelBlockEntity barrel, float partialTicks, PoseStack poseStack, State output, int combinedLight, int combinedOverlay)
    {
        final BlockState state = barrel.getBlockState();
        if (state.getValue(BarrelBlock.SEALED) || state.getValue(BarrelBlock.FACING) != Direction.UP)
        {
            return;
        }

        final BarrelBlockEntity.BarrelInventory inventory = barrel.getInventory();
        final FluidStack fluidStack = inventory.getFluidInTank(0);
        if (!fluidStack.isEmpty())
        {
            final float fillPercent = (float) fluidStack.getAmount() / TFCConfig.SERVER.barrelCapacity.get();
            final float subtract = fillPercent > 0.03 ? 0f : (0.03f - fillPercent) * 7f;
            addFluidSurface(output, poseStack, fluidStack, 0.1875F + subtract, 0.1875F + subtract, 0.8125F - subtract, 0.8125F - subtract, 0.140625F + (0.75F - 0.015625F) * fillPercent, combinedOverlay, combinedLight);
        }

        final ItemStack itemStack = inventory.getStackInSlot(BarrelBlockEntity.SLOT_ITEM);
        if (!itemStack.isEmpty())
        {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.15625F, 0.5F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));

            addItem(output, itemStack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, barrel.getLevel(), 0);

            poseStack.popPose();
        }
    }
}
