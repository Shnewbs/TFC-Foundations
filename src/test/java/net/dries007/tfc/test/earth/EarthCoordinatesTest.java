/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.earth;

import org.junit.jupiter.api.Test;

import net.dries007.tfc.world.earth.EquirectangularProjection;
import net.dries007.tfc.world.earth.EquirectangularProjection.ProjectedPoint;
import net.dries007.tfc.world.earth.GeographicPoint;

import static org.junit.jupiter.api.Assertions.*;

public class EarthCoordinatesTest
{
    private final EquirectangularProjection projection = new EquirectangularProjection(1, 0);

    @Test
    public void referenceCoordinatesAndAxisDirections()
    {
        assertEquals(new ProjectedPoint(0, -0.0), projection.project(new GeographicPoint(0, 0)));
        // One equatorial degree on the declared reference sphere.
        final ProjectedPoint point = projection.project(new GeographicPoint(1, 1));
        assertEquals(111_319.49079327357, point.x(), 1e-8);
        assertEquals(-111_319.49079327357, point.z(), 1e-8);
        assertEquals(20_037_508.342789244, -projection.project(new GeographicPoint(0, -180)).x(), 1e-8);
    }

    @Test
    public void roundTripsAcrossHemispheresAndNearDateLine()
    {
        for (double latitude : new double[] {-90, -60, -0.1, 0, 47.6, 90})
        {
            for (double longitude : new double[] {-180, -179.999999, -122.3, 0, 179.999999})
            {
                final GeographicPoint start = new GeographicPoint(latitude, longitude);
                final GeographicPoint result = projection.unproject(projection.project(start));
                assertEquals(latitude, result.latitudeDegrees(), 1e-10);
                assertEquals(longitude, result.longitudeDegrees(), 1e-10);
            }
        }
    }

    @Test
    public void reportsDistortionInsteadOfClaimingTrueOneToOne()
    {
        assertEquals(1, projection.eastWestScale(0), 1e-12);
        assertEquals(2, projection.eastWestScale(60), 1e-12);
        assertEquals(Double.POSITIVE_INFINITY, projection.eastWestScale(90));
        final EquirectangularProjection regional = new EquirectangularProjection(2, 45);
        assertEquals(1, regional.eastWestScale(45), 1e-12);
        assertEquals(projection.project(new GeographicPoint(1, 0)).z() / 2,
            regional.project(new GeographicPoint(1, 0)).z(), 1e-8);
    }

    @Test
    public void rejectsInvalidInputsAndDoesNotWrapMapEdges()
    {
        assertThrows(IllegalArgumentException.class, () -> new GeographicPoint(91, 0));
        assertThrows(IllegalArgumentException.class, () -> new GeographicPoint(0, 180));
        assertThrows(IllegalArgumentException.class, () -> new GeographicPoint(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> new ProjectedPoint(0, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new EquirectangularProjection(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new EquirectangularProjection(Double.MIN_VALUE, 0));
        assertThrows(IllegalArgumentException.class, () -> new EquirectangularProjection(1, 90));
        assertThrows(IllegalArgumentException.class, () -> projection.eastWestScale(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> projection.unproject(new ProjectedPoint(20_037_509, 0)));
        assertThrows(IllegalArgumentException.class, () -> projection.unproject(new ProjectedPoint(0, 10_018_755)));
        final double edge = -projection.project(new GeographicPoint(0, -180)).x();
        assertThrows(IllegalArgumentException.class, () -> projection.unproject(new ProjectedPoint(edge, 0)));
    }
}
