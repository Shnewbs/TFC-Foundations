/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;

import net.dries007.tfc.common.blockentities.GlassBasinBlockEntity;
import net.dries007.tfc.util.Helpers;

public class GlassBasinBlockEntityRenderer extends ItemDisplayBlockEntityRenderer<GlassBasinBlockEntity>
{
    public GlassBasinBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    private static final Identifier TEXTURE = Helpers.identifier("block/glass/3");

    @Override
    protected void extractItems(GlassBasinBlockEntity glass, float partialTicks, PoseStack poseStack, State output, int combinedLight, int combinedOverlay)
    {
        if (glass.getAnimationTicks() > -1)
        {
            final float lerp = Math.min(glass.getAnimationTicks(), 60) / 60f;
            addTexturedSurface(output, poseStack, 0xFFFFFF, 0, 0, 1, 1, lerp, combinedOverlay, combinedLight, TEXTURE);
        }
    }
}
