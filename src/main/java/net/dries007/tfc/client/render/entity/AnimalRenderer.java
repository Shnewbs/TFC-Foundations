/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;
import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderStateExtractor;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;

public class AnimalRenderer<T extends TFCAnimal, M extends EntityModel<? super TFCAnimalRenderState>> extends MobRenderer<T, TFCAnimalRenderState, M>
{
    private final Identifier young;
    private final Identifier old;

    public AnimalRenderer(EntityRendererProvider.Context ctx, M model, String name)
    {
        this(ctx, model, name, 0.3F);
    }

    public AnimalRenderer(EntityRendererProvider.Context ctx, M model, String name, float shadow)
    {
        super(ctx, model, shadow);
        this.young = RenderHelpers.animalTexture(name + "_young");
        this.old = RenderHelpers.animalTexture(name + "_old");
    }

    @Override
    public TFCAnimalRenderState createRenderState()
    {
        return new TFCAnimalRenderState();
    }

    @Override
    public void extractRenderState(T animal, TFCAnimalRenderState state, float partialTick)
    {
        super.extractRenderState(animal, state, partialTick);
        TFCAnimalRenderStateExtractor.extract(animal, state, partialTick);
    }

    @Override
    protected void scale(TFCAnimalRenderState state, PoseStack poseStack)
    {
        // Vanilla retains its age scale; this is only TFC's genetic-size multiplier.
        poseStack.scale(state.geneticSizeScale, state.geneticSizeScale, state.geneticSizeScale);
        super.scale(state, poseStack);
    }

    @Override
    public Identifier getTextureLocation(TFCAnimalRenderState state)
    {
        return state.isOld ? old : young;
    }
}
