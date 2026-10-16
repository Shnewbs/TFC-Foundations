/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.dries007.tfc.common.blockentities.PitKilnBlockEntity;
import net.dries007.tfc.common.blocks.devices.PitKilnBlock;

public class PitKilnBlockEntityRenderer extends PlacedItemBlockEntityRenderer<PitKilnBlockEntity>
{
    public PitKilnBlockEntityRenderer(BlockEntityRendererProvider.Context context) { super(context); }

    @Override
    protected void extractItems(PitKilnBlockEntity pitKiln, float partialTicks, PoseStack poseStack, State outputState, int combinedLight, int combinedOverlay)
    {
        if (pitKiln.getBlockState().getValue(PitKilnBlock.STAGE) > 9) return;
        super.extractItems(pitKiln, partialTicks, poseStack, outputState, combinedLight, combinedOverlay);
    }
}
