/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.earth;

/**
 * Geographic degrees, with a single canonical representation for the date line.
 * These coordinates do not imply that terrain data is available at this location.
 */
public record GeographicPoint(double latitudeDegrees, double longitudeDegrees)
{
    public GeographicPoint
    {
        if (!Double.isFinite(latitudeDegrees) || latitudeDegrees < -90 || latitudeDegrees > 90)
        {
            throw new IllegalArgumentException("Latitude must be finite and between -90 and 90 degrees");
        }
        if (!Double.isFinite(longitudeDegrees) || longitudeDegrees < -180 || longitudeDegrees >= 180)
        {
            throw new IllegalArgumentException("Longitude must be finite and in [-180, 180) degrees");
        }
    }
}
