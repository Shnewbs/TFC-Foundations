/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.util.List;
import java.util.stream.Stream;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import net.dries007.tfc.util.NbtHelpers;
import net.dries007.tfc.util.ValueIoHelpers;

/** Exercises real NBT-backed ValueInput/Output. No game world or loader is faked. */
public final class SaveIoSmoke
{
    private static int checks;

    private static void check(boolean condition, String name)
    {
        if (!condition) throw new AssertionError(name);
        checks++;
    }

    private static ValueInput input(CompoundTag tag)
    {
        return TagValueInput.create(ProblemReporter.DISCARDING, HolderLookup.Provider.create(Stream.empty()), tag);
    }

    public static void main(String[] args)
    {
        final CompoundTag root = new CompoundTag();
        root.putByte("b", (byte) 3);
        root.putShort("s", (short) -32768);
        root.putInt("i", 42);
        root.putLong("l", 9007199254740993L);
        root.putFloat("f", 1.25F);
        root.putDouble("d", 9.5D);
        root.putString("text", "hello");
        root.put("compound", new CompoundTag());
        final ValueInput in = input(root);
        final String[] keys = {"b", "s", "i", "l", "f", "d"};
        for (int n = 0; n < keys.length; n++)
        {
            check(NbtHelpers.hasTag(in, keys[n], n + 1), "exact numeric type " + keys[n]);
            check(NbtHelpers.hasTag(in, keys[n], NbtHelpers.ANY_NUMERIC), "numeric wildcard " + keys[n]);
            check(!NbtHelpers.hasTag(in, keys[n], Tag.TAG_STRING), "numeric is not text " + keys[n]);
        }
        check(!NbtHelpers.hasTag(in, "f", Tag.TAG_INT), "float is not strict int");
        check(!NbtHelpers.hasTag(in, "i", Tag.TAG_LONG), "int is not strict long");
        check(!NbtHelpers.hasTag(in, "text", NbtHelpers.ANY_NUMERIC), "text is not numeric");
        check(!NbtHelpers.hasTag(in, "absent", Tag.TAG_INT), "missing strict tag");
        check(NbtHelpers.hasTag(in, "text", Tag.TAG_STRING), "string type");
        check(NbtHelpers.hasTag(in, "compound", Tag.TAG_COMPOUND), "compound type");
        check(in.getLongOr("l", 0L) == 9007199254740993L, "long precision");
        check((short) in.getShortOr("s", (short) 0) == Short.MIN_VALUE, "signed short");
        check(in.getLongOr("absent", -1L) == -1L, "missing creation tick sentinel");
        check(in.getByteOr("distance", (byte) 1) == 1, "flow distance default");
        check(in.getFloatOr("n", 0F) == 0F, "nutrient default");
        check(!in.getBooleanOr("invalid", false), "rotation default");

        final ListTag mixed = new ListTag();
        mixed.add(IntTag.valueOf(1));
        mixed.add(StringTag.valueOf("wrong"));
        root.put("mixed", mixed);
        check(NbtHelpers.getHomogeneousListOrEmpty(input(root), "mixed", Tag.TAG_INT).isEmpty(), "reject entire mixed list");
        check(mixed.size() == 2, "do not mutate mixed source");
        final ListTag ints = new ListTag();
        ints.add(IntTag.valueOf(10));
        ints.add(IntTag.valueOf(20));
        root.put("ints", ints);
        check(NbtHelpers.getHomogeneousListOrEmpty(input(root), "ints", Tag.TAG_INT).equals(ints), "homogeneous int list preserved");
        check(NbtHelpers.getHomogeneousListOrEmpty(input(root), "ints", Tag.TAG_COMPOUND).isEmpty(), "wrong element type");
        check(NbtHelpers.getHomogeneousListOrEmpty(in, "text", Tag.TAG_COMPOUND).isEmpty(), "non-list value");
        check(NbtHelpers.getHomogeneousListOrEmpty(in, "missing", Tag.TAG_COMPOUND).isEmpty(), "missing list");
        check(ValueIoHelpers.readHomogeneousList(input(root), "ints", Tag.TAG_INT, Codec.INT, -1).equals(List.of(10, 20)), "typed list decode");

        final Codec<Integer> positive = Codec.intRange(0, 100);
        final ListTag withInvalid = new ListTag();
        withInvalid.add(IntTag.valueOf(1));
        withInvalid.add(IntTag.valueOf(-10));
        withInvalid.add(IntTag.valueOf(2));
        root.put("positions", withInvalid);
        check(ValueIoHelpers.readHomogeneousList(input(root), "positions", Tag.TAG_INT, positive, -1).equals(List.of(1, -1, 2)), "invalid element retains its slot");
        check(ValueIoHelpers.readHomogeneousList(input(root), "mixed", Tag.TAG_INT, positive, -1).isEmpty(), "mixed list never compacted");

        final TagValueOutput out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        out.putLong("tick", 1234567890123L);
        out.putFloat("temperature", 720.5F);
        out.putShort("positions", (short) -1);
        out.putBoolean("invalid", true);
        out.child("inventory").child("inventory").putInt("Size", 9);
        // This checks ValueOutput nesting, not registry-backed fluid serialization.
        out.child("tank").child("Fluid").putString("id", "minecraft:water");
        out.child("strawItems").putString("test", "straw");
        out.child("logItems").putString("test", "log");
        final CompoundTag stored = out.buildResult();
        check(stored.getLongOr("tick", 0) == 1234567890123L, "write long");
        check(stored.getFloatOr("temperature", 0) == 720.5F, "write temperature");
        check(NbtHelpers.hasTag(stored, "positions", Tag.TAG_SHORT), "keep short storage type");
        check(stored.getBooleanOr("invalid", false), "write boolean");
        check(stored.getCompoundOrEmpty("inventory").getCompoundOrEmpty("inventory").getIntOr("Size", 0) == 9, "retain composite nesting");
        check(stored.getCompoundOrEmpty("tank").getCompoundOrEmpty("Fluid").getStringOr("id", "").equals("minecraft:water"), "nested tank fixture");
        check(!stored.getCompoundOrEmpty("tank").contains("id"), "nested fixture is not flattened");
        check(input(stored).childOrEmpty("strawItems").getStringOr("test", "").equals("straw"), "straw inventory child");
        check(input(stored).childOrEmpty("logItems").getStringOr("test", "").equals("log"), "log inventory child");
        check(input(stored).childOrEmpty("absent").getIntOr("Size", 7) == 7, "empty child default");
        System.out.println("PASS: " + checks + " real NBT/ValueIO checks. No gameplay or registry-backed inventory round trip is implied.");
    }
}
