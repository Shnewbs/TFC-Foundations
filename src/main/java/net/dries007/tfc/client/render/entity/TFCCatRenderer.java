/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.model.entity.TFCCatCollarLayer;
import net.dries007.tfc.client.model.entity.TFCCatModel;
import net.dries007.tfc.client.render.entity.state.TFCCatRenderState;
import net.dries007.tfc.common.entities.livestock.pet.TFCCat;

public class TFCCatRenderer extends MobRenderer<TFCCat, TFCCatRenderState, TFCCatModel>
{
    public TFCCatRenderer(EntityRendererProvider.Context context)
    {
        super(context, new TFCCatModel(RenderHelpers.bakeSimple(context, "cat")), 0.4F);
        addLayer(new TFCCatCollarLayer(this, context.getModelSet()));
    }

    @Override
    public TFCCatRenderState createRenderState()
    {
        return new TFCCatRenderState();
    }

    @Override
    public void extractRenderState(TFCCat cat, TFCCatRenderState state, float partialTick)
    {
        super.extractRenderState(cat, state, partialTick);
        state.texture = cat.getTextureLocation();
        state.hasOwner = cat.getOwnerUUID() != null;
        state.collarColor = cat.getCollarColor().getTextureDiffuseColor();
        state.sleeping = cat.isSleeping();
        state.isSitting = cat.isSitting();
        state.isCrouching = cat.isCrouching();
        state.isSprinting = cat.isSprinting();
        state.lieDownAmount = state.sleeping ? 1F : 0;
        state.lieDownAmountTail = state.sleeping ? 0.87F : 0;
        state.relaxStateOneAmount = 0;
    }

    @Override
    protected void scale(TFCCatRenderState state, PoseStack poseStack)
    {
        final float scale = state.isBaby ? 0.8F * 0.7F : 0.8F;
        poseStack.scale(scale, scale, scale);
        super.scale(state, poseStack);
    }

    @Override
    public Identifier getTextureLocation(TFCCatRenderState state)
    {
        return state.texture;
    }

    @Override
    protected void setupRotations(TFCCatRenderState state, PoseStack poseStack, float bodyRot, float scale)
    {
        super.setupRotations(state, poseStack, bodyRot, scale);
        if (state.sleeping)
        {
            poseStack.translate(0.4F, 0.15F, 0.15F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90F));
        }
    }
}
