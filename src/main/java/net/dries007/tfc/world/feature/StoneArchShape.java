/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.feature;

import net.minecraft.util.Mth;

/** Original arch geometry. All coordinates are local to one chunk, including its two grounded feet. */
public record StoneArchShape(boolean alongX, double rise, double phase)
{
    public boolean contains(int x, int y, int z, double detail)
    {
        if (x < 1 || x > 14 || z < 1 || z > 14 || y < 0 || y > 12) return false;
        final double along = (alongX ? x : z) - 7.5;
        final double across = (alongX ? z : x) - 7.5;
        // Unequal abutments and a leaning, thinner crown avoid a uniform masonry-gate silhouette.
        final double warpedAlong = along - 0.18 * y * Math.sin(phase);
        final double localRise = rise * (1 + 0.10 * Math.sin(phase) * along / 4.8);
        final double norm = Math.hypot(warpedAlong / 4.8, y / localRise);
        final double gradient = Math.hypot(warpedAlong / (4.8 * 4.8), y / (localRise * localRise));
        final double radial = gradient > 0 ? (norm - 1) * norm / gradient : -4.8;
        final double thickness = 1.65 - 0.30 * Mth.clamp(y / localRise, 0, 1)
            + 0.22 * Math.sin(phase) * along / 4.8
            + 0.10 * Math.sin(phase + along * 0.8 + y * 0.55)
            + 0.15 * Mth.clamp(detail, -1, 1);
        return Math.hypot(radial, across) <= thickness;
    }

    public boolean isFoot(int x, int z)
    {
        final double along = (alongX ? x : z) - 7.5;
        final double across = (alongX ? z : x) - 7.5;
        return Math.abs(along) >= 3 && Math.abs(along) <= 5.5 && Math.abs(across) <= 1.5;
    }
}
