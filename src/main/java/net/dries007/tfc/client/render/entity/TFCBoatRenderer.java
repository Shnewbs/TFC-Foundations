/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.object.boat.RaftModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.render.entity.state.TFCBoatRenderState;

public class TFCBoatRenderer extends AbstractBoatRenderer
{
    public static ModelLayerLocation boatName(String name)
    {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath("tfc", "boat/" + name), "main");
    }

    private final EntityModel<BoatRenderState> hull;
    private final Model.@Nullable Simple waterPatch;

    public TFCBoatRenderer(EntityRendererProvider.Context context, String name)
    {
        this(context, Pair.of(Identifier.fromNamespaceAndPath("tfc", "textures/entity/boat/" + name + ".png"),
            name.equals("palm") ? new RaftModel(context.bakeLayer(boatName(name))) : new BoatModel(context.bakeLayer(boatName(name)))));
    }

    public TFCBoatRenderer(EntityRendererProvider.Context context, Pair<Identifier, EntityModel<BoatRenderState>> pair)
    {
        super(context, pair.getFirst());
        hull = pair.getSecond();
        waterPatch = hull instanceof RaftModel ? null : new Model.Simple(
            context.bakeLayer(ModelLayers.BOAT_WATER_PATCH), ignored -> RenderTypes.waterMask());
    }

    @Override
    public TFCBoatRenderState createRenderState()
    {
        return new TFCBoatRenderState();
    }

    @Override
    public void extractRenderState(AbstractBoat boat, BoatRenderState state, float partialTick)
    {
        super.extractRenderState(boat, state, partialTick);
        ((TFCBoatRenderState) state).chestTexture = null;
    }

    @Override
    protected EntityModel<BoatRenderState> model()
    {
        return hull;
    }

    @Override
    protected void submitTypeAdditions(BoatRenderState state, PoseStack poses, SubmitNodeCollector collector, int light)
    {
        if (waterPatch != null && !state.isUnderWater)
        {
            collector.submitModel(waterPatch, Unit.INSTANCE, poses, texture, light,
                OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
