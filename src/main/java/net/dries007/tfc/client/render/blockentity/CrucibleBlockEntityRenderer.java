/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.neoforge.fluids.FluidStack;

import net.dries007.tfc.common.blockentities.CrucibleBlockEntity;
import net.dries007.tfc.config.TFCConfig;

public class CrucibleBlockEntityRenderer extends ItemDisplayBlockEntityRenderer<CrucibleBlockEntity>
{
    public CrucibleBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    protected void extractItems(CrucibleBlockEntity crucible, float partialTicks, PoseStack poseStack, State output, int combinedLight, int combinedOverlay)
    {
        final FluidStack fluidStack = crucible.getAlloyResult();
        if (!fluidStack.isEmpty())
        {
            final float height = ((float) fluidStack.getAmount() / TFCConfig.SERVER.crucibleCapacity.get() * 13f + 2f) / 16f;
            addFluidSurface(output, poseStack, fluidStack, 3f / 16, 3f / 16, 13f / 16, 13f / 16, height, combinedOverlay, combinedLight);
        }
    }
}
