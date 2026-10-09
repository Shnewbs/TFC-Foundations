/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import net.dries007.tfc.util.NbtHelpers;
import net.dries007.tfc.util.Unchecked;

/** Actual utility tests and target API probes; not a client/server gameplay test. */
public final class CommonApiSmoke
{
    private static int checks;

    private static void check(boolean condition, String message)
    {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args)
    {
        testNbt();
        testProperties();
        testUnchecked();
        System.out.println("PASS: " + checks + " standalone utility/API checks. No gameplay or full-mod compile is implied.");
        System.out.println("NOT RUN: ChunkPos, component patches and integer-provider runtime probes require the real NeoForge bootstrap; their source was compiled only.");
    }

    private static void testNbt()
    {
        CompoundTag nbt = new CompoundTag();
        check(!NbtHelpers.hasTag(nbt, "missing", Tag.TAG_INT), "missing tag");
        check(nbt.getIntOr("missing", 0) == 0, "missing integer default");
        check(nbt.getLongOr("missing", 0L) == 0L, "missing long default");
        check(nbt.getFloatOr("missing", 0f) == 0f, "missing float default");
        check(nbt.getDoubleOr("missing", 0d) == 0d, "missing double default");
        check(nbt.getByteOr("missing", (byte) 0) == 0, "missing byte default");
        check(nbt.getShortOr("missing", (short) 0) == 0, "missing short default");
        check(!nbt.getBooleanOr("missing", false), "missing boolean default");
        check(nbt.getStringOr("missing", "").isEmpty(), "missing string default");
        nbt.putString("number", "bad");
        check(nbt.getIntOr("number", 0) == 0, "mistyped number default");
        check(!NbtHelpers.hasTag(nbt, "number", NbtHelpers.ANY_NUMERIC), "string not numeric");
        nbt.putDouble("number", 12.75);
        check(NbtHelpers.hasTag(nbt, "number", Tag.TAG_DOUBLE), "exact double");
        check(!NbtHelpers.hasTag(nbt, "number", Tag.TAG_INT), "must not accept coerced integer");
        check(NbtHelpers.hasTag(nbt, "number", NbtHelpers.ANY_NUMERIC), "numeric wildcard");
        check(nbt.getIntOr("number", 0) == 12, "legacy numeric coercion");
        nbt.putByte("signed", (byte) -17);
        check(nbt.getByteOr("signed", (byte) 0) == -17, "signed bytes");
        nbt.putLong("ticks", 987654321234L);
        check(nbt.getLongOr("ticks", 0L) == 987654321234L, "long tick precision");
        nbt.putFloat("rain", 17.25f);
        check(Float.floatToIntBits(nbt.getFloatOr("rain", 0f)) == Float.floatToIntBits(17.25f), "float preservation");
        nbt.putBoolean("flag", true);
        check(nbt.getBooleanOr("flag", false), "boolean preservation");
        nbt.putIntArray("heights", new int[] {-64, 0, 319});
        check(Arrays.equals(nbt.getIntArray("heights").orElseGet(() -> new int[0]), new int[] {-64, 0, 319}), "height array");
        check(nbt.getIntArray("absent").orElseGet(() -> new int[0]).length == 0, "missing array");
        check(nbt.getCompoundOrEmpty("absent").isEmpty(), "missing compound");
        check(nbt.getCompoundOrEmpty("number").isEmpty(), "mistyped compound");
        check(!nbt.contains("absent"), "reads must not mutate compound");

        ListTag valid = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.putInt("slot", 7);
        valid.add(entry);
        nbt.put("items", valid);
        check(NbtHelpers.getHomogeneousListOrEmpty(nbt, "items", Tag.TAG_COMPOUND) == valid, "retain valid list identity");
        check(valid.getCompoundOrEmpty(0).getIntOr("slot", 0) == 7, "read compound list entry");
        check(NbtHelpers.getHomogeneousListOrEmpty(nbt, "items", Tag.TAG_INT).isEmpty(), "reject wrong list type");
        ListTag mixed = new ListTag();
        mixed.add(entry);
        mixed.add(StringTag.valueOf("bad"));
        nbt.put("mixed", mixed);
        check(NbtHelpers.getHomogeneousListOrEmpty(nbt, "mixed", Tag.TAG_COMPOUND).isEmpty(), "reject mixed list, not just first entry");
        check(mixed.size() == 2, "validation must not filter or mutate source list");
        ListTag invalid = NbtHelpers.getHomogeneousListOrEmpty(nbt, "missing", Tag.TAG_COMPOUND);
        invalid.add(IntTag.valueOf(1));
        check(NbtHelpers.getHomogeneousListOrEmpty(nbt, "missing", Tag.TAG_COMPOUND).isEmpty(), "fresh fallback list");
        check(NbtHelpers.getHomogeneousListOrEmpty(nbt, "number", Tag.TAG_COMPOUND).isEmpty(), "non-list value");
        ListTag empty = new ListTag();
        nbt.put("empty", empty);
        check(NbtHelpers.getHomogeneousListOrEmpty(nbt, "empty", Tag.TAG_COMPOUND) == empty, "empty lists are valid");
    }

    private static void testChunks()
    {
        int[] coordinates = {Integer.MIN_VALUE, -1_875_000, -17, -1, 0, 1, 17, 1_875_000, Integer.MAX_VALUE};
        for (int x : coordinates)
        {
            for (int z : coordinates)
            {
                ChunkPos pos = new ChunkPos(x, z);
                long packed = ((long) x & 0xffffffffL) | (((long) z & 0xffffffffL) << 32);
                check(pos.pack() == packed && ChunkPos.pack(x, z) == packed, "packed coordinate layout");
                check(ChunkPos.unpack(packed).equals(pos), "signed coordinate round trip");
            }
        }
        for (int x : new int[] {-33, -17, -16, -15, -1, 0, 1, 15, 16, 17, 33})
        {
            BlockPos block = new BlockPos(x, 80, -x);
            ChunkPos chunk = ChunkPos.containing(block);
            check(chunk.x() == Math.floorDiv(x, 16) && chunk.z() == Math.floorDiv(-x, 16), "negative block-to-chunk floor");
            check(ChunkPos.pack(block) == chunk.pack(), "block packing factory");
        }
    }

    private static void testProperties()
    {
        EnumProperty<Direction> facing = EnumProperty.create("facing", Direction.class, direction -> direction != Direction.DOWN);
        check(facing.getName().equals("facing"), "unchanged serialized property ID");
        check(facing.getPossibleValues().size() == 5 && !facing.getPossibleValues().contains(Direction.DOWN), "five allowed directions");
        for (Direction direction : facing.getPossibleValues())
            check(facing.getValue(facing.getName(direction)).orElseThrow() == direction, "facing name round trip");
        check(facing.getValue("down").isEmpty(), "disallowed serialized direction");
        check(Arrays.stream(ContainerInput.values()).map(Enum::name).toList().equals(List.of("PICKUP", "QUICK_MOVE", "SWAP", "CLONE", "THROW", "QUICK_CRAFT", "PICKUP_ALL")), "all inventory click actions retained");
    }

    // Invoke only from a real NeoForge-bootstrapped test harness.
    public static void runBootstrappedChecks()
    {
        testChunks();
        var constant = ConstantInt.of(3);
        check(constant.minInclusive() == 3 && constant.maxInclusive() == 3, "integer provider bounds");
        var json = IntProviders.CODEC.encodeStart(JsonOps.INSTANCE, constant).getOrThrow();
        check(IntProviders.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().sample(RandomSource.create(1L)) == 3, "integer provider codec");
        DataComponentType<String> type = DataComponentType.<String>builder().persistent(Codec.STRING).build();
        check(DataComponentPatch.EMPTY.getPatch(type) == null, "absent patch is distinct");
        check(DataComponentPatch.builder().remove(type).build().getPatch(type).equals(Optional.empty()), "explicit component removal");
        check(DataComponentPatch.builder().set(type, "value").build().getPatch(type).equals(Optional.of("value")), "explicit component value");
    }

    private static void testUnchecked()
    {
        AtomicInteger calls = new AtomicInteger();
        String value = Unchecked.get(() -> { calls.incrementAndGet(); return "ok"; });
        check(value.equals("ok") && calls.get() == 1, "supplier executes exactly once");
        check(Unchecked.get(() -> null) == null, "null result preserved");
        Unchecked.run(calls::incrementAndGet);
        check(calls.get() == 2, "runnable executes exactly once");
        for (Throwable expected : List.of(new IOException("checked"), new IllegalStateException("runtime"), new AssertionError("error")))
        {
            try { Unchecked.get(() -> { throw expected; }); throw new AssertionError("not thrown"); }
            catch (Throwable actual) { check(actual == expected, "supplier preserves exact throwable identity"); }
            try { Unchecked.run(() -> { throw expected; }); throw new AssertionError("not thrown"); }
            catch (Throwable actual) { check(actual == expected, "runnable preserves exact throwable identity"); }
        }
        float[] numbers = {1f, 2f};
        float[] reflected = Unchecked.get(() -> (Object) numbers);
        check(reflected == numbers, "caller-selected reflective cast preserved");
    }

    // Compilation probes for the exact Level API. No mock Level is used.
    static boolean client(Level level) { return level.isClientSide(); }
    static RandomSource random(Level level) { return level.getRandom(); }
}
