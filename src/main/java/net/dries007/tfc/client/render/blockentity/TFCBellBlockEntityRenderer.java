/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.bell.BellModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.Helpers;

public class TFCBellBlockEntityRenderer implements BlockEntityRenderer<BellBlockEntity, TFCBellBlockEntityRenderer.State>
{
    public static final Map<Block, SpriteId> MATERIALS = RenderHelpers.mapOf(map -> {
        map.accept(TFCBlocks.BRASS_BELL, new SpriteId(RenderHelpers.BLOCKS_ATLAS, Helpers.identifier("entity/bell/brass")));
        map.accept(TFCBlocks.BRONZE_BELL, new SpriteId(RenderHelpers.BLOCKS_ATLAS, Helpers.identifier("entity/bell/bronze")));
    });

    public static class State extends BellRenderState { public @Nullable SpriteId sprite; }
    private final BellModel model;
    private final SpriteGetter sprites;

    public TFCBellBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        model = new BellModel(context.bakeLayer(RenderHelpers.layerId("bell_body")));
        sprites = context.sprites();
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(BellBlockEntity bell, State state, float partialTicks, Vec3 camera,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breaking)
    {
        BlockEntityRenderer.super.extractRenderState(bell, state, partialTicks, camera, breaking);
        state.ticks = bell.ticks + partialTicks;
        state.shakeDirection = bell.shaking ? bell.clickDirection : null;
        state.sprite = MATERIALS.get(bell.getBlockState().getBlock());
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera)
    {
        if (state.sprite != null)
        {
            final BellModel.State animation = new BellModel.State(state.ticks, state.shakeDirection);
            collector.submitModel(model, animation, poses, state.lightCoords, OverlayTexture.NO_OVERLAY,
                -1, state.sprite, sprites, 0, state.breakProgress);
        }
    }
}
