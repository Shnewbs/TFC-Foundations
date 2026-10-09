/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.model.entity.WaterWheelModel;
import net.dries007.tfc.common.blockentities.rotation.WaterWheelBlockEntity;
import net.dries007.tfc.common.blocks.rotation.WaterWheelBlock;

public class WaterWheelBlockEntityRenderer implements BlockEntityRenderer<WaterWheelBlockEntity, WaterWheelBlockEntityRenderer.State>
{
    public static class State extends BlockEntityRenderState
    {
        public @Nullable Identifier texture;
        public Direction.Axis axis = Direction.Axis.X;
        public float angle;
    }
    private final WaterWheelModel model;

    public WaterWheelBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        model = new WaterWheelModel(context.bakeLayer(RenderHelpers.layerId("water_wheel")));
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(WaterWheelBlockEntity wheel, State state, float partialTick, Vec3 camera,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(wheel, state, partialTick, camera, breakProgress);
        state.texture = null;
        if (wheel.getLevel() != null && wheel.getBlockState().getBlock() instanceof WaterWheelBlock block)
        {
            state.texture = block.getTextureLocation();
            state.axis = wheel.getBlockState().getValue(WaterWheelBlock.AXIS);
            state.angle = wheel.getRotationAngle(partialTick);
        }
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera)
    {
        if (state.texture == null) return;
        poses.pushPose();
        poses.translate(0.5F, -0.5F, 0.5F);
        if (state.axis == Direction.Axis.Z) poses.mulPose(Axis.YN.rotationDegrees(90));
        collector.submitModel(model, state.angle, poses, RenderTypes.entityCutoutCull(state.texture), state.lightCoords,
            OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        poses.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() { return true; }

    @Override
    public AABB getRenderBoundingBox(WaterWheelBlockEntity wheel) { return AABB.INFINITE; }
}
