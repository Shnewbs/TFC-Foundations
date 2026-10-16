/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.model.StandaloneBlockModels;

/** Captures display items, fill surfaces and meshes without retaining live worlds in submitted state. */
public abstract class ItemDisplayBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, ItemDisplayBlockEntityRenderer.State>
{
    public static class State extends BlockEntityRenderState
    {
        private final Displays displays = new Displays();
        private final List<Surface> surfaces = new ArrayList<>();
        private final List<PendingGeometry> pendingGeometry = new ArrayList<>();
        private final List<Geometry> geometry = new ArrayList<>();
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
    private final SpriteGetter sprites;

    protected ItemDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        items = context.itemModelResolver();
        sprites = context.sprites();
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public final void extractRenderState(T entity, State state, float partialTick, Vec3 camera,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breaking)
    {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, breaking);
        state.displays.clearItems();
        state.surfaces.clear();
        state.pendingGeometry.clear();
        state.geometry.clear();
        extractItems(entity, partialTick, new PoseStack(), state, state.lightCoords, OverlayTexture.NO_OVERLAY);
        for (PendingGeometry geometry : state.pendingGeometry)
        {
            state.geometry.add(new Geometry(geometry.type(), geometry.buffer().snapshot()));
        }
        state.pendingGeometry.clear();
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

    protected final void addBlockModel(State state, PoseStack poses, Identifier id, Level level, BlockPos pos, BlockState block)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final var model = minecraft.getModelManager().getStandaloneModel(StandaloneBlockModels.key(id));
        final VertexConsumer out = captureGeometry(state, Sheets.cutoutBlockSheet());
        final ModelBlockRenderer renderer = new ModelBlockRenderer(true, true, minecraft.getBlockColors());
        renderer.tesselateBlock((x, y, z, quad, lighting) -> {
            final PoseStack.Pose local = poses.last().copy();
            local.translate(x, y, z);
            out.putBakedQuad(local, quad, lighting);
        }, 0, 0, 0, BlockModelRenderView.of(level), pos, block, model, block.getSeed(pos));
    }

    protected final VertexConsumer captureGeometry(State state, RenderType type)
    {
        final CapturedGeometry buffer = new CapturedGeometry();
        state.pendingGeometry.add(new PendingGeometry(type, buffer));
        return buffer;
    }

    private record PendingGeometry(RenderType type, CapturedGeometry buffer) {}
    private record Geometry(RenderType type, CapturedGeometry.Snapshot snapshot) {}

    protected final void addFluidSurface(State state, PoseStack poses, FluidStack fluid, float minX, float minZ,
        float maxX, float maxZ, float y, int overlay, int light)
    {
        final var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState());
        final var tint = model.fluidTintSource();
        addFluidSurface(state, poses, fluid, tint == null ? -1 : tint.colorAsStack(fluid), minX, minZ, maxX, maxZ, y, overlay, light);
    }

    protected final void addFluidSurface(State state, PoseStack poses, FluidStack fluid, int color, float minX, float minZ,
        float maxX, float maxZ, float y, int overlay, int light)
    {
        final var model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.getFluid().defaultFluidState());
        addSurface(state, poses, model.stillMaterial().sprite(), color, true, minX, minZ, maxX, maxZ, y, overlay, light);
    }

    protected final void addTexturedSurface(State state, PoseStack poses, int color, float minX, float minZ,
        float maxX, float maxZ, float y, int overlay, int light, Identifier texture)
    {
        addSurface(state, poses, sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, texture)), color | 0xFF000000,
            false, minX, minZ, maxX, maxZ, y, overlay, light);
    }

    private static void addSurface(State state, PoseStack poses, TextureAtlasSprite sprite, int color, boolean translucent,
        float minX, float minZ, float maxX, float maxZ, float y, int overlay, int light)
    {
        state.surfaces.add(new Surface(new Matrix4f(poses.last().pose()), SurfaceGeometry.capture(sprite, color,
            minX, minZ, maxX, maxZ, y, overlay, light), translucent));
    }

    private record Surface(Matrix4f transform, SurfaceGeometry geometry, boolean translucent) {}

    @Override
    public final void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera)
    {
        state.displays.submitItems(poses, collector);
        for (Geometry geometry : state.geometry)
        {
            collector.submitCustomGeometry(poses, geometry.type(), (pose, out) -> geometry.snapshot().draw(pose, out));
        }
        for (Surface surface : state.surfaces)
        {
            poses.pushPose();
            poses.mulPose(surface.transform());
            collector.submitCustomGeometry(poses, surface.translucent() ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet(),
                (pose, out) -> surface.geometry().draw(pose, out));
            poses.popPose();
        }
    }
}
