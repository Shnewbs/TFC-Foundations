/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;

/** Retains the scale-then-translate order of TFC's former ageable draw path. */
public final class AgeableModelTransforms
{
    private AgeableModelTransforms() {}

    /** Apply after a pose reset; offsets use model pixels, not world units. */
    public static void scalePart(ModelPart part, float scale, float yOffset, float zOffset)
    {
        part.x *= scale;
        part.y = (part.y + yOffset) * scale;
        part.z = (part.z + zOffset) * scale;
        part.xScale *= scale;
        part.yScale *= scale;
        part.zScale *= scale;
    }
}
