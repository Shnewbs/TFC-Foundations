/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.earth;

/**
 * Spherical equidistant cylindrical projection, centered on zero longitude.
 * X points east and Minecraft Z points south. A projected metre is not a ground
 * metre at every latitude. This class supplies math only, not a world generator.
 */
public record EquirectangularProjection(double metresPerBlock, double standardParallelDegrees)
{
    /** Reference sphere radius; not an ellipsoidal surveying model. */
    public static final double RADIUS_METRES = 6_378_137;

    public EquirectangularProjection
    {
        if (!Double.isFinite(metresPerBlock) || metresPerBlock <= 0)
        {
            throw new IllegalArgumentException("Metres per block must be finite and positive");
        }
        if (!Double.isFinite(standardParallelDegrees) || Math.abs(standardParallelDegrees) >= 90)
        {
            throw new IllegalArgumentException("Standard parallel must be strictly between -90 and 90 degrees");
        }
        if (!Double.isFinite(Math.PI * RADIUS_METRES / metresPerBlock))
        {
            throw new IllegalArgumentException("Metres per block produces non-finite projected coordinates");
        }
    }

    public ProjectedPoint project(GeographicPoint point)
    {
        return new ProjectedPoint(
            RADIUS_METRES * Math.toRadians(point.longitudeDegrees()) * parallelCosine() / metresPerBlock,
            -RADIUS_METRES * Math.toRadians(point.latitudeDegrees()) / metresPerBlock
        );
    }

    /**
     * Reject positions outside the map instead of wrapping to a different place.
     * The eastern date-line edge is exclusive and the western edge is inclusive.
     */
    public GeographicPoint unproject(ProjectedPoint point)
    {
        final double halfWidth = Math.PI * RADIUS_METRES * parallelCosine() / metresPerBlock;
        final double halfHeight = Math.PI * RADIUS_METRES * 0.5 / metresPerBlock;
        if (point.x() < -halfWidth || point.x() >= halfWidth || Math.abs(point.z()) > halfHeight)
        {
            throw new IllegalArgumentException("Projected point is outside the Earth map");
        }
        return new GeographicPoint(
            -Math.toDegrees(point.z() / halfHeight * (Math.PI * 0.5)),
            Math.toDegrees(point.x() / halfWidth * Math.PI)
        );
    }

    /** Projected east-west distance divided by ground distance on the reference sphere. */
    public double eastWestScale(double latitudeDegrees)
    {
        if (!Double.isFinite(latitudeDegrees) || Math.abs(latitudeDegrees) > 90)
        {
            throw new IllegalArgumentException("Latitude must be finite and between -90 and 90 degrees");
        }
        return Math.abs(latitudeDegrees) == 90 ? Double.POSITIVE_INFINITY :
            parallelCosine() / Math.cos(Math.toRadians(latitudeDegrees));
    }

    private double parallelCosine()
    {
        return Math.cos(Math.toRadians(standardParallelDegrees));
    }

    /** Fractional block coordinates: no block rounding is performed here. */
    public record ProjectedPoint(double x, double z)
    {
        public ProjectedPoint
        {
            if (!Double.isFinite(x) || !Double.isFinite(z))
            {
                throw new IllegalArgumentException("Projected coordinates must be finite");
            }
        }
    }
}
