/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;

public class GenderedRenderer<T extends TFCAnimal, M extends EntityModel<? super TFCAnimalRenderState>> extends AnimalRenderer<T, M>
{
    @Nullable
    private final Identifier maleYoung;
    @Nullable
    private final Identifier maleOld;
    @Nullable
    private final Identifier baby;

    public GenderedRenderer(EntityRendererProvider.Context ctx, M model, String name)
    {
        this(ctx, model, name, null);
    }

    public GenderedRenderer(EntityRendererProvider.Context ctx, M model, String name, @Nullable String maleName)
    {
        this(ctx, model, name, maleName, null);
    }

    public GenderedRenderer(EntityRendererProvider.Context ctx, M model, String name, @Nullable String maleName, @Nullable String babyName)
    {
        super(ctx, model, name);
        maleYoung = maleName == null ? null : RenderHelpers.animalTexture(maleName + "_young");
        maleOld = maleName == null ? null : RenderHelpers.animalTexture(maleName + "_old");
        baby = babyName == null ? null : RenderHelpers.animalTexture(babyName);
    }

    @Override
    public Identifier getTextureLocation(TFCAnimalRenderState state)
    {
        if (baby != null && state.isBaby) return baby;
        return maleYoung != null && maleOld != null && state.isMale ? (state.isOld ? maleOld : maleYoung) : super.getTextureLocation(state);
    }
}
