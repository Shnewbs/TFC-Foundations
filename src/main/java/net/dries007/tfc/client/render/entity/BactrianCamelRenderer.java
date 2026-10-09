/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.camel.Camel;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.model.entity.HierarchicalAnimatedModel;
import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;
import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderStateExtractor;

public class BactrianCamelRenderer<T extends Camel, M extends HierarchicalAnimatedModel> extends MobRenderer<T, TFCAnimalRenderState, M>
{
    private final Identifier young;
    private final Identifier old;
    private final Identifier saddled;
    private final Identifier old_saddled;

    public BactrianCamelRenderer(EntityRendererProvider.Context ctx, M model, float shadow)
    {
        super(ctx, model, shadow);
        this.young = RenderHelpers.animalTexture("bactrian_camel_young");
        this.old = RenderHelpers.animalTexture("bactrian_camel_old");
        this.saddled = RenderHelpers.animalTexture("bactrian_camel_saddle");
        this.old_saddled = RenderHelpers.animalTexture("bactrian_camel_old_saddle");
    }

    @Override
    public TFCAnimalRenderState createRenderState()
    {
        return new TFCAnimalRenderState();
    }

    @Override
    public void extractRenderState(T entity, TFCAnimalRenderState state, float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        TFCAnimalRenderStateExtractor.extract(entity, state, partialTick);
    }

    @Override
    public Identifier getTextureLocation(TFCAnimalRenderState state)
    {
        return state.isSaddled ? (state.isOld ? old_saddled : saddled) : (state.isOld ? old : young);
    }
}
