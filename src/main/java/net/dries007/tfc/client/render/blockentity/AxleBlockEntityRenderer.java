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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.common.blockentities.rotation.AxleBlockEntity;
import net.dries007.tfc.common.blocks.rotation.AxleBlock;

public class AxleBlockEntityRenderer implements BlockEntityRenderer<AxleBlockEntity, AxleBlockEntityRenderer.State>
{
    public static class State extends BlockEntityRenderState
    {
        public Direction.Axis axis = Direction.Axis.Z;
        public float angle;
        public AxleRenderGeometry.@Nullable Texture texture;
    }

    private final SpriteGetter sprites;

    public AxleBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        sprites = context.sprites();
    }

    public static void applyRotation(PoseStack poses, Direction.Axis axis, float angle)
    {
        AxleRenderGeometry.applyRotation(poses, axis, angle);
    }

    public static void submitAxle(PoseStack poses, SubmitNodeCollector collector, AxleRenderGeometry.Texture texture,
        Direction.Axis axis, int light, float angle)
    {
        poses.pushPose();
        applyRotation(poses, axis, angle);
        collector.submitCustomGeometry(poses, RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS),
            (pose, out) -> AxleRenderGeometry.drawAxle(pose, out, texture, light, OverlayTexture.NO_OVERLAY));
        poses.popPose();
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(AxleBlockEntity axle, State state, float partialTick, Vec3 camera,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(axle, state, partialTick, camera, breakProgress);
        state.texture = null;
        if (axle.getLevel() != null && axle.getBlockState().getBlock() instanceof AxleBlock block)
        {
            state.axis = axle.getBlockState().getValue(AxleBlock.AXIS);
            state.angle = -axle.getRotationAngle(partialTick);
            state.texture = AxleRenderGeometry.Texture.capture(sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, block.getAxleTextureLocation())));
        }
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera)
    {
        if (state.texture != null) submitAxle(poses, collector, state.texture, state.axis, state.lightCoords, state.angle);
    }
}
