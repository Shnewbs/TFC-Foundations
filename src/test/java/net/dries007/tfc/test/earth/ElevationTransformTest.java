/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.earth;

import org.junit.jupiter.api.Test;

import net.dries007.tfc.world.earth.ElevationTransform;

import static org.junit.jupiter.api.Assertions.*;

public class ElevationTransformTest
{
    @Test
    public void preservesSeaLevelAndHandlesBathymetry()
    {
        final ElevationTransform transform = new ElevationTransform(63, 50, -48, 287);
        assertEquals(63, transform.surfaceY(0));
        assertEquals(163, transform.surfaceY(5_000));
        assertEquals(-37, transform.surfaceY(-5_000));
        assertEquals(5_000, transform.elevationMetres(163));
        assertEquals(-5_000, transform.elevationMetres(-37));
    }

    @Test
    public void rejectsReliefThatDoesNotFitInsteadOfClipping()
    {
        final ElevationTransform transform = new ElevationTransform(63, 1, -48, 287);
        assertThrows(IllegalArgumentException.class, () -> transform.surfaceY(8_849));
        assertThrows(IllegalArgumentException.class, () -> transform.surfaceY(-11_000));
        assertEquals(-48, transform.surfaceY(-111));
        assertEquals(287, transform.surfaceY(224));
        assertThrows(IllegalArgumentException.class, () -> transform.surfaceY(224.01));
        assertThrows(IllegalArgumentException.class, () -> transform.elevationMetres(288));
    }

    @Test
    public void quantizationStaysWithinHalfABlock()
    {
        final ElevationTransform transform = new ElevationTransform(63, 50, -48, 287);
        for (double height : new double[] {-5_000.2, -51, -1, 0, 26, 1_999.9, 8_849})
        {
            assertTrue(Math.abs(height - transform.elevationMetres(transform.surfaceY(height))) <= 25);
        }
    }

    @Test
    public void rejectsNonFiniteAndInvalidConfiguration()
    {
        assertThrows(IllegalArgumentException.class, () -> new ElevationTransform(63, 0, -48, 287));
        assertThrows(IllegalArgumentException.class, () -> new ElevationTransform(63, Double.NaN, -48, 287));
        assertThrows(IllegalArgumentException.class, () -> new ElevationTransform(63, Double.MAX_VALUE, -48, 287));
        assertThrows(IllegalArgumentException.class, () -> new ElevationTransform(63, 50, 64, 287));
        assertThrows(IllegalArgumentException.class, () -> new ElevationTransform(63, 50, 287, -48));
        final ElevationTransform transform = new ElevationTransform(63, 50, -48, 287);
        assertThrows(IllegalArgumentException.class, () -> transform.surfaceY(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> transform.surfaceY(Double.NEGATIVE_INFINITY));
    }
}
