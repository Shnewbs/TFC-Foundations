/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.model.entity.WindmillBladeLatticeModel;
import net.dries007.tfc.client.model.entity.WindmillBladeModel;
import net.dries007.tfc.client.model.entity.WindmillBladeRusticModel;
import net.dries007.tfc.common.blockentities.rotation.WindmillBlockEntity;
import net.dries007.tfc.common.blocks.rotation.WindmillBlock;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;

public class WindmillBlockEntityRenderer implements BlockEntityRenderer<WindmillBlockEntity, WindmillBlockEntityRenderer.State>
{
    public static final Map<Item, Provider<Function<BlockEntityRendererProvider.Context, WindmillBladeModel>>> BLADE_MODELS = RenderHelpers.mapOf(map -> {
        final Identifier defaultTexture = Helpers.identifier("textures/entity/misc/windmill_blade.png");
        final Function<BlockEntityRendererProvider.Context, WindmillBladeModel> defaultModel = defaultModelFactory();

        TFCItems.WINDMILL_BLADES.forEach((color, item) -> map.accept(item, new Provider<>(defaultTexture, color, defaultModel)));
        map.accept(TFCItems.LATTICE_WINDMILL_BLADE, new Provider<>(
            Helpers.identifier("textures/entity/misc/windmill_blade_lattice.png"),
            DyeColor.WHITE,
            context -> new WindmillBladeLatticeModel(context.bakeLayer(RenderHelpers.layerId("windmill_blade_lattice")))
        ));
        map.accept(TFCItems.RUSTIC_WINDMILL_BLADE, new Provider<>(
            Helpers.identifier("textures/entity/misc/windmill_blade_rustic.png"),
            DyeColor.WHITE,
            context -> new WindmillBladeRusticModel(context.bakeLayer(RenderHelpers.layerId("windmill_blade_rustic")))
        ));
    });

    private static Function<BlockEntityRendererProvider.Context, WindmillBladeModel> defaultModelFactory()
    {
        return context -> new WindmillBladeModel(context.bakeLayer(RenderHelpers.layerId("windmill_blade")));
    }

    public record Provider<T>(Identifier texture, DyeColor color, T model) {}
    public record Blade(Provider<WindmillBladeModel> provider, int slot, float angle) {}
    public static class State extends AxleBlockEntityRenderer.State
    {
        public List<Blade> blades = List.of();
        public boolean fullIdenticalSet;
    }

    private final Map<Item, Provider<WindmillBladeModel>> bladeModels = new HashMap<>();
    private final SpriteGetter sprites;

    public WindmillBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        sprites = context.sprites();
        final Map<Function<BlockEntityRendererProvider.Context, WindmillBladeModel>, WindmillBladeModel> models = new IdentityHashMap<>();
        BLADE_MODELS.forEach((item, provider) -> bladeModels.put(item,
            new Provider<>(provider.texture(), provider.color(), models.computeIfAbsent(provider.model(), factory -> factory.apply(context)))));
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(WindmillBlockEntity windmill, State state, float partialTick, Vec3 camera,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(windmill, state, partialTick, camera, breakProgress);
        state.texture = null;
        state.blades = List.of();
        state.fullIdenticalSet = false;
        if (windmill.getLevel() == null || !(windmill.getBlockState().getBlock() instanceof WindmillBlock block)) return;
        state.axis = windmill.getBlockState().getValue(WindmillBlock.AXIS);
        state.angle = -windmill.getRotationAngle(partialTick);
        state.texture = AxleRenderGeometry.Texture.capture(sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, block.getAxleTextureLocation())));
        final int count = windmill.getBlockState().getValue(WindmillBlock.COUNT);
        if (count <= 0) return;
        final List<Blade> blades = new ArrayList<>(count);
        WindmillBladeModel first = null;
        boolean identical = count == 5;
        for (int i = 0; i < count; i++)
        {
            final ItemStack item = windmill.getInventory().getStackInSlot(i);
            final Provider<WindmillBladeModel> provider = bladeModels.get(item.getItem());
            if (item.isEmpty() || provider == null)
            {
                identical = false;
                continue;
            }
            if (first != null && first != provider.model()) identical = false;
            first = provider.model();
            blades.add(new Blade(provider, i, -state.angle + Mth.TWO_PI / count * i));
        }
        state.blades = List.copyOf(blades);
        state.fullIdenticalSet = identical;
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera)
    {
        if (state.texture == null) return;
        AxleBlockEntityRenderer.submitAxle(poses, collector, state.texture, state.axis, state.lightCoords, state.angle);
        poses.pushPose();
        final boolean axisX = state.axis == Direction.Axis.X;
        if (!axisX) poses.mulPose(Axis.YN.rotationDegrees(90));
        poses.mulPose(Axis.XN.rotationDegrees(90));
        poses.translate(0.5F, axisX ? -2 : -1, 0.5F);
        for (Blade blade : state.blades)
        {
            final Provider<WindmillBladeModel> provider = blade.provider();
            final int color = provider.color() == DyeColor.WHITE ? -1 : 0xff000000 | provider.color().getTextureDiffuseColor();
            poses.pushPose();
            poses.translate(0.0001F * blade.slot(), 0.0001F * blade.slot(), 0.0001F * blade.slot());
            submitPortion(collector, poses, state, blade, WindmillBladeModel.Portion.FRAME, -1);
            submitPortion(collector, poses, state, blade, WindmillBladeModel.Portion.BLADE, color);
            if (state.fullIdenticalSet && provider.model().hasExtras())
                submitPortion(collector, poses, state, blade, WindmillBladeModel.Portion.EXTRAS, -1);
            poses.popPose();
        }
        poses.popPose();
    }

    private static void submitPortion(SubmitNodeCollector collector, PoseStack poses, State state, Blade blade,
        WindmillBladeModel.Portion portion, int color)
    {
        final Provider<WindmillBladeModel> provider = blade.provider();
        collector.submitModel(provider.model(), new WindmillBladeModel.BladePose(blade.angle(), portion), poses,
            provider.model().renderType(provider.texture()), state.lightCoords, OverlayTexture.NO_OVERLAY,
            color, null, 0, state.breakProgress);
    }

    @Override
    public boolean shouldRenderOffScreen() { return true; }

    @Override
    public AABB getRenderBoundingBox(WindmillBlockEntity windmill) { return AABB.INFINITE; }
}
