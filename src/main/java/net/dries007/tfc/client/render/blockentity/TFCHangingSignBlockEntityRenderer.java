/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.HangingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.mixin.client.accessor.AbstractSignRendererAccessor;
import net.dries007.tfc.util.Helpers;

public class TFCHangingSignBlockEntityRenderer extends HangingSignRenderer
{
    public record Provider(SpriteId modelMaterial, Identifier textureLocation) {}

    public static final Map<Block, Provider> MODELS = RenderHelpers.mapOf(map -> {
        TFCBlocks.CEILING_HANGING_SIGNS.forEach((wood, metals) -> metals.forEach((metal, block) -> {
            final Provider provider = new Provider(
                new SpriteId(Sheets.SIGN_SHEET, Helpers.identifier("entity/signs/hanging/" + metal.getSerializedName() + "/" + wood.getSerializedName())),
                Helpers.identifier(wood.getSerializedName() + ".png").withPrefix("textures/gui/hanging_signs/" + metal.getSerializedName() + "/"));
            map.accept(block, provider);
            map.accept(TFCBlocks.WALL_HANGING_SIGNS.get(wood).get(metal), provider);
        }));
    });

    public static class State extends HangingSignRenderState
    {
        public @Nullable SpriteId sprite;
    }

    private final SpriteGetter sprites;

    public TFCHangingSignBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
        sprites = context.sprites();
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(SignBlockEntity sign, HangingSignRenderState state, float partialTicks, Vec3 camera,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breaking)
    {
        super.extractRenderState(sign, state, partialTicks, camera, breaking);
        final Provider provider = MODELS.get(sign.getBlockState().getBlock());
        ((State) state).sprite = provider == null ? null : provider.modelMaterial();
    }

    @Override
    public void submit(HangingSignRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera)
    {
        final SpriteId sprite = ((State) state).sprite;
        if (sprite == null)
        {
            super.submit(state, poses, collector, camera);
            return;
        }
        // Use the native attachment-specific model and text transforms; only the metal/wood sprite differs.
        poses.pushPose();
        poses.mulPose(state.transformations.body());
        collector.submitModel(getSignModel(state), Unit.INSTANCE, poses, state.lightCoords, OverlayTexture.NO_OVERLAY,
            -1, sprite, sprites, 0, state.breakProgress);
        poses.popPose();
        if (state.frontText != null)
        {
            poses.pushPose();
            poses.mulPose(state.transformations.frontText());
            ((AbstractSignRendererAccessor) this).invoke$submitSignText(state, poses, collector, state.frontText);
            poses.popPose();
        }
        if (state.backText != null)
        {
            poses.pushPose();
            poses.mulPose(state.transformations.backText());
            ((AbstractSignRendererAccessor) this).invoke$submitSignText(state, poses, collector, state.backText);
            poses.popPose();
        }
    }
}
