/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import java.util.HashMap;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Util;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Unit;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.model.entity.JavelinModel;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.entities.misc.ThrownJavelin;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal;

public class ThrownJavelinRenderer extends EntityRenderer<ThrownJavelin, ThrownJavelinRenderer.RenderState>
{
    public static final Identifier DEFAULT_TEXTURE = Helpers.identifier("textures/entity/projectiles/stone_javelin.png");
    public static final Map<Item, Identifier> JAVELIN_TEXTURES = Util.make(new HashMap<>(), map -> {
        TFCItems.ROCK_TOOLS.forEach((k, v) -> map.put(v.get(RockCategory.ItemType.JAVELIN).get(), Helpers.identifier("textures/entity/projectiles/stone_javelin.png")));
        map.put(TFCItems.OBSIDIAN_JAVELIN.get(), Helpers.identifier("textures/entity/projectiles/obsidian_javelin.png"));
        TFCItems.METAL_ITEMS.entrySet()
            .stream()
            .filter(e -> e.getValue().containsKey(Metal.ItemType.JAVELIN))
            .forEach(e -> map.put(e.getValue().get(Metal.ItemType.JAVELIN).get(), Helpers.identifier("textures/entity/projectiles/" + e.getKey().getSerializedName() + "_javelin.png")));
    });

    private final JavelinModel model;

    public ThrownJavelinRenderer(EntityRendererProvider.Context context)
    {
        super(context);
        this.model = new JavelinModel(context.bakeLayer(RenderHelpers.layerId("javelin")));
    }

    @Override
    public RenderState createRenderState()
    {
        return new RenderState();
    }

    @Override
    public void extractRenderState(ThrownJavelin javelin, RenderState state, float partialTick)
    {
        super.extractRenderState(javelin, state, partialTick);
        state.yRot = Mth.lerp(partialTick, javelin.yRotO, javelin.getYRot());
        state.xRot = Mth.lerp(partialTick, javelin.xRotO, javelin.getXRot());
        state.foil = javelin.isEnchantGlowing();
        state.texture = JAVELIN_TEXTURES.getOrDefault(javelin.getItem().getItem(), DEFAULT_TEXTURE);
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera)
    {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot + 90.0F));

        collector.order(0).submitModel(model, Unit.INSTANCE, poseStack, state.texture,
            state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        if (state.foil)
        {
            collector.order(1).submitModel(model, Unit.INSTANCE, poseStack,
                ItemFeatureRenderer.getFoilRenderType(model.renderType(state.texture), false),
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static class RenderState extends EntityRenderState
    {
        public float xRot;
        public float yRot;
        public boolean foil;
        public Identifier texture = DEFAULT_TEXTURE;
    }
}
