/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;
import net.dries007.tfc.client.model.entity.MongooseModel;
import net.dries007.tfc.common.entities.prey.Pest;

public class MongooseRenderer extends SimpleMobRenderer<Pest, MongooseModel>
{
    public MongooseRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx, new MongooseModel(RenderHelpers.bakeSimple(ctx, "mongoose")), "mongoose", 0.2f, false, 1f, false, true, null);
    }

    @Override
    protected void setupRotations(TFCAnimalRenderState state, PoseStack poseStack, float yBodyRot, float scale)
    {
        super.setupRotations(state, poseStack, yBodyRot, scale);
        if (state.climbing)
        {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
            poseStack.popPose();
        }
        if (state.draggingAnimation.isStarted())
        {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            poseStack.popPose();
        }
    }
}
