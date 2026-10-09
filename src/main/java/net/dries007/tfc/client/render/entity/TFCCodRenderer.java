/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.CodRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.animal.fish.Cod;

import net.dries007.tfc.client.render.entity.state.GuideRenderState;

public class TFCCodRenderer extends CodRenderer
{
    public TFCCodRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void extractRenderState(Cod entity, LivingEntityRenderState state, float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        GuideRenderState.captureOrigin(state, entity.position());
    }

    @Override
    protected void setupRotations(LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float scale)
    {
        if (!GuideRenderState.isAtOrigin(state))
        {
            super.setupRotations(state, poseStack, bodyRot, scale);
        }
    }
}
