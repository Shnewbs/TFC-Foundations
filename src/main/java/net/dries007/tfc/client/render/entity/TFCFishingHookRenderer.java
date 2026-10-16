/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemDisplayContext;

import net.dries007.tfc.common.entities.misc.TFCFishingHook;

public class TFCFishingHookRenderer extends FishingHookRenderer
{
    public static class State extends FishingHookRenderState
    {
        public final ItemStackRenderState bait = new ItemStackRenderState();
    }

    private final ItemModelResolver items;

    public TFCFishingHookRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        items = context.getItemModelResolver();
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(FishingHook entity, FishingHookRenderState state, float partialTicks)
    {
        super.extractRenderState(entity, state, partialTicks);
        final State output = (State) state;
        output.bait.clear();
        if (entity instanceof TFCFishingHook hook && entity.getPlayerOwner() != null)
        {
            items.updateForTopItem(output.bait, hook.getBait(), ItemDisplayContext.FIXED, entity.level(), null, 0);
        }
    }

    @Override
    public void submit(FishingHookRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera)
    {
        final State output = (State) state;
        if (!output.bait.isEmpty())
        {
            poses.pushPose();
            poses.translate(0, -0.05f, 0);
            poses.scale(0.5f, 0.5f, 0.5f);
            poses.mulPose(camera.orientation);
            poses.mulPose(Axis.YP.rotationDegrees(180));
            output.bait.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poses.popPose();
        }
        super.submit(state, poses, collector, camera);
    }
}
