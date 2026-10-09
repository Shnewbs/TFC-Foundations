/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;

import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;
import net.dries007.tfc.util.Helpers;

public class DogCollarLayer extends RenderLayer<TFCAnimalRenderState, DogModel>
{
    private static final Identifier WOLF_COLLAR_LOCATION = Helpers.identifierMC("textures/entity/wolf/wolf_collar.png");

    public DogCollarLayer(RenderLayerParent<TFCAnimalRenderState, DogModel> renderer)
    {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, TFCAnimalRenderState state, float yaw, float pitch)
    {
        if (state.hasOwner && !state.isInvisible)
        {
            renderColoredCutoutModel(this.getParentModel(), WOLF_COLLAR_LOCATION, poseStack, collector, packedLight, state, state.collarColor, 1);
        }
    }
}
