/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import net.dries007.tfc.util.NbtHelpers;

/** Real save-utility and target API checks; does not create a world or fake a loader. */
public final class EntitySaveSmoke
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
        final CompoundTag tag = new CompoundTag();
        tag.putInt("i", 17);
        tag.putLong("l", 9007199254740993L);
        tag.putFloat("f", 0.375F);
        tag.putString("s", "minecraft:black");
        tag.putShort("short", (short) 17);
        tag.putDouble("double", 0.375D);
        final ValueInput in = input(tag);
        check(NbtHelpers.getIntOrDefault(tag, "i", -1) == 17, "compound exact int");
        check(NbtHelpers.getIntOrDefault(in, "i", -1) == 17, "value exact int");
        check(NbtHelpers.getLongOrDefault(tag, "l", -1) == 9007199254740993L, "compound long precision");
        check(NbtHelpers.getLongOrDefault(in, "l", -1) == 9007199254740993L, "value long precision");
        check(NbtHelpers.getFloatOrDefault(tag, "f", -1) == 0.375F, "compound exact float");
        check(NbtHelpers.getFloatOrDefault(in, "f", -1) == 0.375F, "value exact float");
        check(NbtHelpers.getStringOrDefault(tag, "s", "fallback").equals("minecraft:black"), "compound exact string");
        check(NbtHelpers.getStringOrDefault(in, "s", "fallback").equals("minecraft:black"), "value exact string");
        for (String wrong : List.of("missing", "l", "f", "s", "short", "double"))
        {
            check(NbtHelpers.getIntOrDefault(tag, wrong, -17) == -17, "compound rejects non-int " + wrong);
            check(NbtHelpers.getIntOrDefault(in, wrong, -17) == -17, "value rejects non-int " + wrong);
        }
        for (String wrong : List.of("missing", "i", "f", "s", "short", "double"))
        {
            check(NbtHelpers.getLongOrDefault(tag, wrong, Long.MIN_VALUE) == Long.MIN_VALUE, "compound cooldown sentinel " + wrong);
            check(NbtHelpers.getLongOrDefault(in, wrong, Long.MIN_VALUE) == Long.MIN_VALUE, "value cooldown sentinel " + wrong);
        }
        for (String wrong : List.of("missing", "i", "l", "s", "short", "double"))
        {
            check(NbtHelpers.getFloatOrDefault(tag, wrong, -0.25F) == -0.25F, "compound rejects non-float " + wrong);
            check(NbtHelpers.getFloatOrDefault(in, wrong, -0.25F) == -0.25F, "value rejects non-float " + wrong);
        }
        check(NbtHelpers.getStringOrDefault(tag, "i", "fallback").equals("fallback"), "compound rejects numeric name");
        check(NbtHelpers.getStringOrDefault(in, "i", "fallback").equals("fallback"), "value rejects numeric name");
        check(NbtHelpers.getStringOrDefault(tag, "missing", "minecraft:black").equals("minecraft:black"), "compound missing variant");
        check(NbtHelpers.getStringOrDefault(in, "missing", "minecraft:black").equals("minecraft:black"), "value missing variant");

        // Check the exact representation used by persisted owners and inherited genes.
        final UUID owner = UUID.fromString("12345678-9abc-def0-fedc-ba9876543210");
        final Tag encoded = UUIDUtil.CODEC.encodeStart(NbtOps.INSTANCE, owner).getOrThrow();
        check(encoded instanceof IntArrayTag, "owner uses legacy NBT int array");
        check(java.util.Arrays.equals(((IntArrayTag) encoded).getAsIntArray(), new int[] {0x12345678, 0x9abcdef0, 0xfedcba98, 0x76543210}), "owner preserves all 128 bits and word order");
        check(UUIDUtil.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow().equals(owner), "owner UUID codec round trip");
        check(UUIDUtil.CODEC.parse(NbtOps.INSTANCE, new IntArrayTag(new int[] {1, 2, 3})).result().isEmpty(), "reject truncated owner");
        final CompoundTag genes = new CompoundTag();
        genes.putInt("size", 23);
        genes.putBoolean("runt", true);
        genes.putString("variant", "minecraft:black");
        genes.store("owner", UUIDUtil.CODEC, owner);
        final TagValueOutput out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        out.store("Owner", UUIDUtil.CODEC, owner);
        out.store("genes", CompoundTag.CODEC, genes);
        out.putLong("plucked", Long.MIN_VALUE);
        out.putLong("pregnant", 9876543210L);
        final CompoundTag saved = out.buildResult();
        final ValueInput savedIn = input(saved);
        check(savedIn.read("Owner", UUIDUtil.CODEC).orElseThrow().equals(owner), "capitalized persistent owner key");
        check(savedIn.read("genes", CompoundTag.CODEC).orElseThrow().equals(genes), "nested genes round trip");
        check(savedIn.childOrEmpty("genes").read("owner", UUIDUtil.CODEC).orElseThrow().equals(owner), "lowercase inherited owner key");
        check(NbtHelpers.getLongOrDefault(savedIn, "plucked", 0) == Long.MIN_VALUE, "unplucked sentinel survives write and read");
        check(savedIn.getLongOr("pregnant", 0L) == 9876543210L, "pregnancy tick precision");
        check(input(new CompoundTag()).read("Owner", UUIDUtil.CODEC).orElse(null) == null, "missing owner can clear old reference");
        check(input(new CompoundTag()).read("genes", CompoundTag.CODEC).orElse(null) == null, "missing genes can clear old data");
        saved.putString("genes", "not a compound");
        saved.putInt("Owner", 4);
        check(input(saved).read("genes", CompoundTag.CODEC).orElse(null) == null, "reject malformed genes");
        check(input(saved).read("Owner", UUIDUtil.CODEC).orElse(null) == null, "reject malformed owner");
        check(genes.getIntOr("size", 0) == 23 && genes.getBooleanOr("runt", false), "reading does not mutate source genes");

        final EntityReference<LivingEntity> reference = EntityReference.of(owner);
        check(reference.getUUID().equals(owner), "native owner reference UUID identity");
        final Tag refNbt = EntityReference.<LivingEntity>codec().encodeStart(NbtOps.INSTANCE, reference).getOrThrow();
        check(refNbt.equals(encoded), "native reference and legacy owner codec representation agree");
        check(EntityReference.<LivingEntity>codec().parse(NbtOps.INSTANCE, refNbt).getOrThrow().getUUID().equals(owner), "native owner reference codec round trip");

        // Use actual vanilla behaviors. No substitute Brain, Entity, or registry is supplied.
        final DoNothing first = new DoNothing(5, 10);
        final DoNothing second = new DoNothing(11, 20);
        final DoNothing third = new DoNothing(21, 30);
        final var activities = ActivityData.<LivingEntity>createPriorityPairs(7, ImmutableList.of(first, second, third));
        check(activities.size() == 3, "activity factory retains behavior count");
        check(activities.get(0).getFirst() == 7 && activities.get(1).getFirst() == 8 && activities.get(2).getFirst() == 9, "activity factory preserves priority increments");
        check(activities.get(0).getSecond() == first && activities.get(1).getSecond() == second && activities.get(2).getSecond() == third, "activity factory preserves behavior identity and order");
        check(ActivityData.<LivingEntity>createPriorityPairs(5, ImmutableList.of()).isEmpty(), "empty activity priorities");
        System.out.println("PASS: " + checks + " standalone entity-save utility and target API checks.");
        System.out.println("NOT RUN: entity construction, AI ticks, synchronized ownership, variant spawning, and world save/reload.");
    }
}
