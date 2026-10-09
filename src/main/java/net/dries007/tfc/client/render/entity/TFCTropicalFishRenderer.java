/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.world.entity.animal.fish.TropicalFish;

import net.dries007.tfc.client.render.entity.state.GuideRenderState;

public class TFCTropicalFishRenderer extends TropicalFishRenderer
{
    public TFCTropicalFishRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void extractRenderState(TropicalFish entity, TropicalFishRenderState state, float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        GuideRenderState.captureOrigin(state, entity.position());
    }

    @Override
    protected void setupRotations(TropicalFishRenderState state, PoseStack poseStack, float bodyRot, float scale)
    {
        if (!GuideRenderState.isAtOrigin(state))
        {
            super.setupRotations(state, poseStack, bodyRot, scale);
        }
    }
}
