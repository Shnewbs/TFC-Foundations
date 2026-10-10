/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Real 26.1.2 vanilla hunger I/O with TFC's existing nested storage keys. */
public final class PlayerFoodValueIOSmoke
{
    public static void main(String[] args)
    {
        final HolderLookup.Provider lookup = HolderLookup.Provider.create(Stream.empty());
        final FoodData food = new FoodData();
        food.setFoodLevel(12);
        food.setSaturation(3.5f);
        food.addExhaustion(1.25f);
        final CompoundTag tfc = new CompoundTag();
        tfc.putLong("lastDrinkTick", 123456789L);
        tfc.putFloat("thirst", 53.25f);
        tfc.putString("chiselMode", "tfc:slab");
        CompoundTag nutrition = new CompoundTag();
        nutrition.putInt("example", 7);
        tfc.put("nutrition", nutrition);
        tfc.putLong("intoxication", 98765L);

        TagValueOutput writer = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lookup);
        food.addAdditionalSaveData(writer);
        writer.store("tfc:food", CompoundTag.CODEC, tfc);
        CompoundTag root = writer.buildResult();
        if (!root.getCompoundOrEmpty("tfc:food").equals(tfc)) throw new AssertionError("Changed TFC food save keys");
        if (root.getIntOr("foodLevel", -1) != 12 || root.getFloatOr("foodSaturationLevel", -1) != 3.5f)
            throw new AssertionError("Vanilla food keys not serialized at root");
        var input = TagValueInput.create(ProblemReporter.DISCARDING, lookup, root);
        FoodData restored = new FoodData();
        restored.readAdditionalSaveData(input);
        if (restored.getFoodLevel() != 12 || restored.getSaturationLevel() != 3.5f)
            throw new AssertionError("Vanilla food values not restored");
        CompoundTag tfcRead = input.read("tfc:food", CompoundTag.CODEC).orElseThrow();
        if (!tfcRead.equals(tfc)) throw new AssertionError("TFC keys not restored");
        if (tfcRead.getCompoundOrEmpty("nutrition").getIntOr("example", -1) != 7)
            throw new AssertionError("Nested nutrition keys lost");
        System.out.println("PASS: Vanilla 26.1.2 hunger codec and all five original TFC food save keys round trip without moving root fields.");
    }
}
