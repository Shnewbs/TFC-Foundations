/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/** Extracts display items without retaining live inventories in the submitted state. */
public abstract class ItemDisplayBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, ItemDisplayBlockEntityRenderer.State>
{
    public static class State extends BlockEntityRenderState
    {
        private final Displays displays = new Displays();
    }

    static final class Displays
    {
        private final List<Display> items = new ArrayList<>();

        void clearItems() { items.clear(); }

        void add(ItemStackRenderState item, PoseStack poses, int light, int overlay)
        {
            items.add(new Display(item, new Matrix4f(poses.last().pose()), light, overlay));
        }

        void submitItems(PoseStack poses, SubmitNodeCollector collector)
        {
            for (Display display : items)
            {
                poses.pushPose();
                poses.mulPose(display.transform());
                display.item().submit(poses, collector, display.light(), display.overlay(), 0);
                poses.popPose();
            }
        }
    }

    private record Display(ItemStackRenderState item, Matrix4f transform, int light, int overlay) {}

    private final ItemModelResolver items;

    protected ItemDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        items = context.itemModelResolver();
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public final void extractRenderState(T entity, State state, float partialTick, Vec3 camera,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breaking)
    {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, breaking);
        state.displays.clearItems();
        extractItems(entity, partialTick, new PoseStack(), state, state.lightCoords, OverlayTexture.NO_OVERLAY);
    }

    protected abstract void extractItems(T entity, float partialTick, PoseStack poses, State state, int light, int overlay);

    protected final void addItem(State state, ItemStack stack, ItemDisplayContext context, int light, int overlay,
        PoseStack poses, @Nullable Level level, int seed)
    {
        if (stack.isEmpty()) return;
        final ItemStackRenderState item = new ItemStackRenderState();
        items.updateForTopItem(item, stack, context, level, null, seed);
        state.displays.add(item, poses, light, overlay);
    }

    @Override
    public final void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera)
    {
        state.displays.submitItems(poses, collector);
    }
}
