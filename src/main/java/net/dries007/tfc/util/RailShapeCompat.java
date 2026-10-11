/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util;

import net.minecraft.world.level.block.state.properties.RailShape;

/** Compatibility for RailShape.isAscending(), removed in Minecraft 26.1.2. */
public final class RailShapeCompat
{
    private RailShapeCompat() {}

    public static boolean isAscending(RailShape shape)
    {
        return switch (shape)
        {
            case ASCENDING_NORTH, ASCENDING_SOUTH, ASCENDING_EAST, ASCENDING_WEST -> true;
            default -> false;
        };
    }
}
