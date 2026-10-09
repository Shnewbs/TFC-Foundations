/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import net.dries007.tfc.common.entities.misc.GlowArrow;
import net.dries007.tfc.util.Helpers;

public class GlowArrowRenderer extends ArrowRenderer<GlowArrow, ArrowRenderState>
{
    public static final Identifier LOCATION = Helpers.identifier("textures/entity/projectiles/glow_arrow.png");

    public GlowArrowRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState()
    {
        return new ArrowRenderState();
    }

    @Override
    public Identifier getTextureLocation(ArrowRenderState state)
    {
        return LOCATION;
    }

    @Override
    public int getBlockLightLevel(GlowArrow squid, BlockPos pos)
    {
        return 15;
    }
}
