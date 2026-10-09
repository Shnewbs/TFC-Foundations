/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.client.renderer.entity.state.SquidRenderState;
import net.minecraft.resources.Identifier;

import net.dries007.tfc.common.entities.aquatic.TFCSquid;

public class TFCSquidRenderer<T extends TFCSquid> extends SquidRenderer<T>
{
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/squid/squid.png");

    public TFCSquidRenderer(EntityRendererProvider.Context context, SquidModel model)
    {
        // TFC retains its adult-layout model for both ages, rather than mixing baby UVs with it.
        super(context, model, model);
    }

    @Override
    public Identifier getTextureLocation(SquidRenderState state)
    {
        return TEXTURE;
    }
}
