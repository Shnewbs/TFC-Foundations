/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jspecify.annotations.Nullable;

/** Save migration helpers that preserve list positions and legacy NBT type checks. */
public final class ValueIoHelpers
{
    private ValueIoHelpers() {}

    /**
     * Reject a heterogeneous list as a whole. A homogeneous list with an invalid
     * encoded element retains that element's position using the caller's default.
     * This matches legacy inventory reads instead of silently compacting slots.
     */
    public static <T> List<T> readHomogeneousList(ValueInput input, String key, int elementType, Codec<T> codec, T fallback)
    {
        final ListTag tags = NbtHelpers.getHomogeneousListOrEmpty(input, key, elementType);
        final List<T> values = new ArrayList<>(tags.size());
        if (!tags.isEmpty())
        {
            final DynamicOps<Tag> ops = input.lookup().createSerializationContext(NbtOps.INSTANCE);
            for (Tag tag : tags)
            {
                values.add(codec.parse(ops, tag).result().orElse(fallback));
            }
        }
        return values;
    }

    public static List<ItemStack> readItemStacks(ValueInput input, String key)
    {
        return readHomogeneousList(input, key, Tag.TAG_COMPOUND, ItemStack.OPTIONAL_CODEC, ItemStack.EMPTY);
    }

    public static void readItemStacks(ValueInput input, String key, List<ItemStack> stacks)
    {
        final List<ItemStack> loaded = readItemStacks(input, key);
        stacks.clear();
        stacks.addAll(loaded);
    }

    public static void writeItemStacks(ValueOutput output, String key, List<ItemStack> stacks)
    {
        final ValueOutput.TypedOutputList<ItemStack> list = output.list(key, ItemStack.OPTIONAL_CODEC);
        stacks.forEach(list::add);
    }

    /** Read both legacy JSON-string names and the target's NBT component format. */
    public static @Nullable Component readCustomName(ValueInput input, String key)
    {
        final String legacy = input.getStringOr(key, "").stripLeading();
        if (legacy.startsWith("{") || legacy.startsWith("[") || legacy.startsWith("\""))
        {
            try
            {
                final var parsed = ComponentSerialization.CODEC.parse(
                    input.lookup().createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(legacy));
                if (parsed.result().isPresent())
                {
                    return parsed.result().get();
                }
            }
            catch (JsonParseException ignored)
            {
                // A plain text component may legitimately start with a JSON delimiter.
            }
        }
        return BlockEntity.parseCustomNameSafe(input, key);
    }

    /** Read NeoForge's legacy Fluid child, accepting a flat stack as a fallback. */
    public static void readFluidTank(ValueInput input, String key, FluidTank tank)
    {
        final ValueInput child = input.childOrEmpty(key);
        final FluidStack fluid = child.child("Fluid").isPresent()
            ? child.read("Fluid", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY)
            : child.read(FluidStack.MAP_CODEC).orElse(FluidStack.EMPTY);
        tank.setFluid(fluid);
    }

    public static void writeFluidTank(ValueOutput output, String key, FluidTank tank)
    {
        // Both the legacy and target FluidTank serializers use a nested "Fluid" key.
        // Delegate rather than accidentally flattening a serialized FluidStack here.
        tank.serialize(output.child(key));
    }
}
