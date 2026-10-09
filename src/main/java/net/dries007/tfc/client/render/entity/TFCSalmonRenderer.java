/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.state.SalmonRenderState;
import net.minecraft.world.entity.animal.fish.Salmon;

import net.dries007.tfc.client.render.entity.state.GuideRenderState;

public class TFCSalmonRenderer extends SalmonRenderer
{
    public TFCSalmonRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void extractRenderState(Salmon entity, SalmonRenderState state, float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        GuideRenderState.captureOrigin(state, entity.position());
    }

    @Override
    protected void setupRotations(SalmonRenderState state, PoseStack poseStack, float bodyRot, float scale)
    {
        if (!GuideRenderState.isAtOrigin(state))
        {
            super.setupRotations(state, poseStack, bodyRot, scale);
        }
    }
}
