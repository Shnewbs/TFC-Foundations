/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.earth;

/**
 * Explicit linear mapping from source metres above sea level to surface block Y.
 * The caller supplies a surface interval inside the actual dimension, reserving
 * space for bedrock and construction. Out-of-range terrain is rejected, never
 * clipped. Physical elevation must remain available separately for climate.
 */
public record ElevationTransform(int seaLevelY, double metresPerBlock, int minSurfaceY, int maxSurfaceY)
{
    public ElevationTransform
    {
        if (!Double.isFinite(metresPerBlock) || metresPerBlock <= 0)
        {
            throw new IllegalArgumentException("Vertical metres per block must be finite and positive");
        }
        if (minSurfaceY > maxSurfaceY || seaLevelY < minSurfaceY || seaLevelY > maxSurfaceY)
        {
            throw new IllegalArgumentException("Surface bounds must be ordered and contain sea level");
        }
        final double largestOffset = Math.max((double) seaLevelY - minSurfaceY, (double) maxSurfaceY - seaLevelY);
        if (!Double.isFinite(largestOffset * metresPerBlock))
        {
            throw new IllegalArgumentException("Vertical scale produces non-finite elevations");
        }
    }

    /** Round to the nearest block after validating the unrounded height. */
    public int surfaceY(double elevationMetres)
    {
        if (!Double.isFinite(elevationMetres))
        {
            throw new IllegalArgumentException("Elevation must be finite");
        }
        final double y = seaLevelY + elevationMetres / metresPerBlock;
        if (!Double.isFinite(y) || y < minSurfaceY || y > maxSurfaceY)
        {
            throw new IllegalArgumentException("Elevation is outside the configured surface interval");
        }
        return (int) Math.round(y);
    }

    /** Inverse for a quantized surface height; original sub-block precision is lost. */
    public double elevationMetres(int surfaceY)
    {
        if (surfaceY < minSurfaceY || surfaceY > maxSurfaceY)
        {
            throw new IllegalArgumentException("Surface height is outside the configured interval");
        }
        return ((double) surfaceY - seaLevelY) * metresPerBlock;
    }
}
