/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.model.entity.AnemometerModel;
import net.dries007.tfc.common.blockentities.AnemometerBlockEntity;
import net.dries007.tfc.util.Helpers;

public class AnemometerBlockEntityRenderer implements BlockEntityRenderer<AnemometerBlockEntity, AnemometerBlockEntityRenderer.RenderState>
{
    public static final Identifier TEXTURE = Helpers.identifier("textures/entity/anemometer.png");

    private final AnemometerModel model;

    public AnemometerBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.model = new AnemometerModel(context.bakeLayer(RenderHelpers.layerId("anemometer")));
    }

    @Override
    public RenderState createRenderState()
    {
        return new RenderState();
    }

    @Override
    public void extractRenderState(AnemometerBlockEntity anemometer, RenderState state, float partialTick,
        Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(anemometer, state, partialTick, cameraPosition, breakProgress);
        state.hasLevel = anemometer.getLevel() != null;
        state.modelState = new AnemometerModel.State(anemometer.getAngle(partialTick));
    }

    @Override
    public void submit(RenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera)
    {
        if (!state.hasLevel)
        {
            return;
        }

        stack.pushPose();

        stack.translate(0.5f, -0f, 0.5f);
        stack.translate(0, 1.0625f + 0.4375f, 0);
        stack.scale(-1, -1, 1);

        collector.submitModel(model, state.modelState, stack, TEXTURE,
            state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);

        stack.popPose();
    }

    public static class RenderState extends BlockEntityRenderState
    {
        public boolean hasLevel;
        public AnemometerModel.State modelState = new AnemometerModel.State(0);
    }
}
