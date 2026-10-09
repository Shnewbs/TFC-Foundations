/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.render.entity.state.TFCCatRenderState;

public class TFCCatCollarLayer extends RenderLayer<TFCCatRenderState, TFCCatModel>
{
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/cat/cat_collar.png");
    private final TFCCatModel model;

    public TFCCatCollarLayer(RenderLayerParent<TFCCatRenderState, TFCCatModel> renderer, EntityModelSet models)
    {
        super(renderer);
        this.model = new TFCCatModel(models.bakeLayer(RenderHelpers.layerId("cat_collar")));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, TFCCatRenderState state, float yaw, float pitch)
    {
        if (state.hasOwner && !state.isInvisible)
        {
            renderColoredCutoutModel(model, TEXTURE, poseStack, collector, packedLight, state, state.collarColor, 1);
        }
    }
}
