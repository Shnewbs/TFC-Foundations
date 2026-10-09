/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;

/** Captures the existing field-guide origin exemption before interpolation/drawing. */
public final class GuideRenderState
{
    private static final ContextKey<Boolean> AT_ORIGIN = new ContextKey<>(Identifier.fromNamespaceAndPath("tfc", "guide_entity_at_origin"));

    private GuideRenderState() {}

    public static void captureOrigin(EntityRenderState state, Vec3 entityPosition)
    {
        state.setRenderData(AT_ORIGIN, Math.abs(entityPosition.x) < 0.01F
            && Math.abs(entityPosition.y) < 0.01F && Math.abs(entityPosition.z) < 0.01F);
    }

    public static boolean isAtOrigin(EntityRenderState state)
    {
        return Boolean.TRUE.equals(state.getRenderData(AT_ORIGIN));
    }
}
