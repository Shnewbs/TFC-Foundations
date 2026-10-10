/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Adapts TFC's original whole-compound attachment payloads to the 26.x value I/O API.
 *
 * <p>Do not wrap the compound under a new key: both world and chunk attachments
 * previously stored their fields directly in the attachment root. Reading with
 * {@link Codec#PASSTHROUGH} also retains list and array tag types which would be
 * lost if we used only ValueInput's primitive convenience getters.
 */
public final class AttachmentValueIO
{
    public static CompoundTag read(ValueInput input)
    {
        final CompoundTag result = new CompoundTag();
        for (String key : input.keySet())
        {
            input.read(key, Codec.PASSTHROUGH)
                .map(value -> value.convert(NbtOps.INSTANCE).getValue())
                .ifPresent(tag -> result.put(key, tag));
        }
        return result;
    }

    public static boolean write(ValueOutput output, CompoundTag value)
    {
        output.store(value);
        return true;
    }

    private AttachmentValueIO() {}
}
