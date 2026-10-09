/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

/**
 * Type-preserving reads for existing TFC save data. Numeric getters can coerce
 * values, but the older explicit type checks must not silently become coercive.
 */
public final class NbtHelpers
{
    private NbtHelpers() {}

    /** The legacy save format's wildcard for any of the six numeric tag types. */
    public static final int ANY_NUMERIC = 99;

    public static boolean hasTag(CompoundTag parent, String key, int expectedType)
    {
        final Tag value = parent.get(key);
        return value != null && (expectedType == ANY_NUMERIC
            ? value instanceof NumericTag
            : value.getId() == expectedType);
    }

    /**
     * Retains the all-or-nothing type check of legacy homogeneous NBT lists.
     * New NBT permits mixed lists, so checking only the first element is unsafe.
     * Valid lists are returned unchanged; missing, mistyped or mixed lists yield
     * a fresh empty list and never mutate the source compound.
     */
    public static ListTag getHomogeneousListOrEmpty(CompoundTag parent, String key, int elementType)
    {
        final Tag value = parent.get(key);
        if (value instanceof ListTag list)
        {
            for (Tag element : list)
            {
                if (element.getId() != elementType)
                {
                    return new ListTag();
                }
            }
            return list;
        }
        return new ListTag();
    }
}
