/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.dries007.tfc.util.AttachmentValueIO;

/** Standalone native ValueInput / ValueOutput smoke test; no Minecraft server stubs. */
public final class AttachmentValueIOSmoke
{
    private static int checks;
    private static final HolderLookup.Provider LOOKUP = HolderLookup.Provider.create(Stream.empty());

    private static void roundTrip(CompoundTag original)
    {
        final TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, LOOKUP);
        if (!AttachmentValueIO.write(output, original)) throw new AssertionError("Serializer rejected payload");
        final CompoundTag serialized = output.buildResult();
        if (!original.equals(serialized)) throw new AssertionError("Changed attachment keys, root layout or tag types: " + serialized);
        final CompoundTag restored = AttachmentValueIO.read(TagValueInput.create(ProblemReporter.DISCARDING, LOOKUP, serialized));
        if (!original.equals(restored)) throw new AssertionError("Lost attachment keys or tag types on load: " + restored);
        checks += 2;
    }

    public static void main(String[] args)
    {
        // Both attachment root layouts are the same as existing worlds: no wrapper key.
        final CompoundTag chunk = new CompoundTag();
        chunk.putByte("status", (byte) 2);
        chunk.putIntArray("surfaceHeight", new int[] {0, 30, -8, 104});
        chunk.putIntArray("aquiferSurfaceHeight", new int[] {63, 48, -30});
        for (String key : new String[] {"rainfall", "rainVariance", "baseGroundwater", "temperature"})
        {
            CompoundTag layer = new CompoundTag();
            layer.putFloat("center", 9.25f);
            layer.putLongArray("levels", new long[] {Long.MIN_VALUE, 0L, Long.MAX_VALUE});
            chunk.put(key, layer);
        }
        chunk.putByte("forestType", (byte) 3);
        chunk.putLong("lastRandomTick", 9240000L);
        chunk.putByte("nextSnowPosition", (byte) 17);
        roundTrip(chunk);

        final CompoundTag world = new CompoundTag();
        ListTag landslides = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.putLong("pos", -32L);
        entry.putInt("delay", 2);
        landslides.add(entry);
        world.put("landslideTicks", landslides);
        world.putLongArray("isolatedPositions", new long[] {0L, -1L, 34359738368L});
        ListTag collapses = new ListTag();
        CompoundTag collapse = new CompoundTag();
        collapse.putDouble("radius", 8.5d);
        collapses.add(collapse);
        world.put("collapsesInProgress", collapses);
        world.putBoolean("weatherEnabled", false);
        roundTrip(world);
        roundTrip(new CompoundTag());

        // All primitive NBT types, nested compounds, lists, numeric arrays and random edge values.
        final Random rng = new Random(0x26_1_2L);
        for (int i = 0; i < 400; ++i)
        {
            final CompoundTag tag = new CompoundTag();
            tag.putInt("int", rng.nextInt());
            tag.putLong("long", rng.nextLong());
            tag.putFloat("float", rng.nextFloat());
            tag.putDouble("double", rng.nextDouble());
            tag.putBoolean("bool", rng.nextBoolean());
            tag.putByte("byte", (byte) rng.nextInt());
            tag.putShort("short", (short) rng.nextInt());
            tag.putString("text", "entry." + i);
            tag.putIntArray("int_array", new int[] {rng.nextInt(), rng.nextInt()});
            tag.putLongArray("long_array", new long[] {rng.nextLong(), rng.nextLong()});
            tag.putByteArray("byte_array", new byte[] {(byte) rng.nextInt(), (byte) rng.nextInt()});
            final CompoundTag nested = new CompoundTag();
            nested.putString("child_key", "retained");
            nested.putInt("child_number", i);
            tag.put("nested", nested);
            final ListTag list = new ListTag();
            list.add(nested.copy());
            tag.put("list", list);
            roundTrip(tag);
        }
        System.out.println("PASS: " + checks + " live native attachment NBT shape/value checks (legacy-compatible root, all primitive types, arrays, nested compounds and lists).");
    }
}
