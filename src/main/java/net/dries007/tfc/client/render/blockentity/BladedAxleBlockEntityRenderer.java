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
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.common.blockentities.rotation.BladedAxleBlockEntity;
import net.dries007.tfc.common.blocks.rotation.BladedAxleBlock;

public class BladedAxleBlockEntityRenderer implements BlockEntityRenderer<BladedAxleBlockEntity, BladedAxleBlockEntityRenderer.State>
{
    private static final Identifier BLADE_TEXTURE = Identifier.fromNamespaceAndPath("tfc", "block/metal/block/steel");
    public static class State extends AxleBlockEntityRenderer.State
    {
        public AxleRenderGeometry.@Nullable Texture bladeTexture;
    }

    private final SpriteGetter sprites;

    public BladedAxleBlockEntityRenderer(BlockEntityRendererProvider.Context context) { sprites = context.sprites(); }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(BladedAxleBlockEntity axle, State state, float partialTick, Vec3 camera,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(axle, state, partialTick, camera, breakProgress);
        state.texture = state.bladeTexture = null;
        if (axle.getLevel() != null && axle.getBlockState().getBlock() instanceof BladedAxleBlock block)
        {
            state.axis = axle.getBlockState().getValue(BladedAxleBlock.AXIS);
            state.angle = -axle.getRotationAngle(partialTick);
            state.texture = AxleRenderGeometry.Texture.capture(sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, block.getAxleTextureLocation())));
            state.bladeTexture = AxleRenderGeometry.Texture.capture(sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, BLADE_TEXTURE)));
        }
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera)
    {
        if (state.texture == null || state.bladeTexture == null) return;
        AxleBlockEntityRenderer.submitAxle(poses, collector, state.texture, state.axis, state.lightCoords, state.angle);
        final AxleRenderGeometry.Texture texture = state.bladeTexture;
        final int light = state.lightCoords;
        poses.pushPose();
        AxleRenderGeometry.applyRotation(poses, state.axis, state.angle);
        collector.submitCustomGeometry(poses, RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS),
            (pose, out) -> AxleRenderGeometry.drawBlade(pose, out, texture, light, OverlayTexture.NO_OVERLAY));
        poses.popPose();
    }
}
