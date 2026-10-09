/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;
import net.dries007.tfc.common.entities.livestock.OviparousAnimal;

public class OviparousRenderer<T extends OviparousAnimal, M extends EntityModel<? super TFCAnimalRenderState>> extends GenderedRenderer<T, M>
{
    public OviparousRenderer(EntityRendererProvider.Context ctx, M model, String name)
    {
        this(ctx, model, name, null);
    }

    public OviparousRenderer(EntityRendererProvider.Context ctx, M model, String name, @Nullable String maleName)
    {
        this(ctx, model, name, maleName, null);
    }

    public OviparousRenderer(EntityRendererProvider.Context ctx, M model, String name, @Nullable String maleName, @Nullable String babyName)
    {
        super(ctx, model, name, maleName, babyName);
    }

    // Wing-flap interpolation is captured by TFCAnimalRenderStateExtractor.
    // Keep the native ageInTicks value intact for all other animation consumers.
}
