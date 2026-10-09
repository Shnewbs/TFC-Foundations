/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueInput;

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

    /** Retains concrete NBT types when a ValueInput is backed by NBT. */
    private static final Codec<Tag> RAW_TAG_CODEC = Codec.PASSTHROUGH.xmap(
        value -> value.convert(NbtOps.INSTANCE).getValue(),
        value -> new Dynamic<>(NbtOps.INSTANCE, value));

    public static boolean hasTag(ValueInput input, String key, int expectedType)
    {
        return input.read(key, RAW_TAG_CODEC).map(value -> expectedType == ANY_NUMERIC
            ? value instanceof NumericTag
            : value.getId() == expectedType).orElse(false);
    }

    public static ListTag getHomogeneousListOrEmpty(ValueInput input, String key, int elementType)
    {
        final Tag value = input.read(key, RAW_TAG_CODEC).orElse(null);
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
    /** Strict legacy animal-save defaults; do not accept a differently typed numeric tag. */
    public static int getIntOrDefault(CompoundTag nbt, String key, int defaultInt)
    {
        return hasTag(nbt, key, Tag.TAG_INT) ? nbt.getIntOr(key, 0) : defaultInt;
    }

    public static int getIntOrDefault(ValueInput nbt, String key, int defaultInt)
    {
        return hasTag(nbt, key, Tag.TAG_INT) ? nbt.getIntOr(key, 0) : defaultInt;
    }

    public static String getStringOrDefault(CompoundTag nbt, String key, String defaultString)
    {
        return hasTag(nbt, key, Tag.TAG_STRING) ? nbt.getStringOr(key, "") : defaultString;
    }

    public static String getStringOrDefault(ValueInput nbt, String key, String defaultString)
    {
        return hasTag(nbt, key, Tag.TAG_STRING) ? nbt.getStringOr(key, "") : defaultString;
    }

    public static float getFloatOrDefault(CompoundTag nbt, String key, float defaultFloat)
    {
        return hasTag(nbt, key, Tag.TAG_FLOAT) ? nbt.getFloatOr(key, 0f) : defaultFloat;
    }

    public static float getFloatOrDefault(ValueInput nbt, String key, float defaultFloat)
    {
        return hasTag(nbt, key, Tag.TAG_FLOAT) ? nbt.getFloatOr(key, 0f) : defaultFloat;
    }

    public static long getLongOrDefault(CompoundTag nbt, String key, long defaultLong)
    {
        return hasTag(nbt, key, Tag.TAG_LONG) ? nbt.getLongOr(key, 0L) : defaultLong;
    }

    public static long getLongOrDefault(ValueInput nbt, String key, long defaultLong)
    {
        return hasTag(nbt, key, Tag.TAG_LONG) ? nbt.getLongOr(key, 0L) : defaultLong;
    }
}
