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
import net.dries007.tfc.client.model.entity.VaneModel;
import net.dries007.tfc.common.blockentities.VaneBlockEntity;
import net.dries007.tfc.util.Helpers;

import static net.dries007.tfc.common.blocks.TFCBlockStateProperties.*;

public class VaneBlockEntityRenderer implements BlockEntityRenderer<VaneBlockEntity, VaneBlockEntityRenderer.RenderState>
{
    public static final Identifier TEXTURE = Helpers.identifier("textures/entity/vane.png");

    private final VaneModel model;

    public VaneBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.model = new VaneModel(context.bakeLayer(RenderHelpers.layerId("vane")));
    }

    @Override
    public RenderState createRenderState()
    {
        return new RenderState();
    }

    @Override
    public void extractRenderState(VaneBlockEntity vane, RenderState state, float partialTick,
        Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(vane, state, partialTick, cameraPosition, breakProgress);
        state.hasLevel = vane.getLevel() != null;
        state.modelState = new VaneModel.State(vane.getAngle(partialTick),
            !vane.getBlockState().getValue(ATTACHED_WIND_DEVICES));
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

        if (!state.modelState.showBase())
        {
            stack.translate(0, 0.5625f, 0);
        }

        collector.submitModel(model, state.modelState, stack, TEXTURE,
            state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);

        stack.popPose();
    }

    public static class RenderState extends BlockEntityRenderState
    {
        public boolean hasLevel;
        public VaneModel.State modelState = new VaneModel.State(0, true);
    }
}
