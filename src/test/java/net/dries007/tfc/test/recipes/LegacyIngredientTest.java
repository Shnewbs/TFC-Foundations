/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.recipes;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.recipes.LegacyIngredientCodecs;
import net.dries007.tfc.common.recipes.LegacySizedIngredientCodecs;
import net.dries007.tfc.test.TestSetup;

import static org.junit.jupiter.api.Assertions.*;

public class LegacyIngredientTest implements TestSetup
{
    @Test
    public void nativeItemCodecsPreserveLegacyTagsUnionsAndCustomChildren()
    {
        final var ops = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).createSerializationContext(JsonOps.INSTANCE);
        for (String json : new String[]{"{\"item\":\"minecraft:stick\"}", "\"minecraft:stick\"",
            "[{\"item\":\"minecraft:stick\"},{\"item\":\"minecraft:diamond\"}]",
            "{\"type\":\"tfc:and\",\"children\":[{\"item\":\"minecraft:stick\"},{\"type\":\"tfc:not_rotten\"}]}"})
        {
            final var ingredient = LegacyIngredientCodecs.ITEM.parse(ops, JsonParser.parseString(json)).getOrThrow();
            assertTrue(ingredient.test(new ItemStack(Items.STICK)));
            assertFalse(ingredient.test(new ItemStack(Items.DIRT)));
            final var encoded = LegacyIngredientCodecs.ITEM.encodeStart(ops, ingredient).getOrThrow();
            assertEquals(ingredient, LegacyIngredientCodecs.ITEM.parse(ops, encoded).getOrThrow());
        }
        final var tagged = LegacyIngredientCodecs.ITEM.parse(ops, JsonParser.parseString("{\"tag\":\"tfc:rock_knapping\"}")).getOrThrow();
        assertEquals("tfc:rock_knapping", LegacyIngredientCodecs.ITEM.encodeStart(ops, tagged).getOrThrow().getAsJsonObject().get("tag").getAsString());
    }

    @Test
    public void flatSizedNativeIngredientsRetainFieldsAndValidation()
    {
        final var ops = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).createSerializationContext(JsonOps.INSTANCE);
        final var item = LegacySizedIngredientCodecs.FLAT_ITEM.parse(ops, JsonParser.parseString("{\"item\":\"minecraft:stick\",\"count\":2}")).getOrThrow();
        assertEquals(2, item.count());
        final var encoded = LegacySizedIngredientCodecs.FLAT_ITEM.encodeStart(ops, item).getOrThrow().getAsJsonObject();
        assertEquals("minecraft:stick", encoded.get("item").getAsString());
        assertFalse(encoded.has("ingredient"));
        assertEquals(item, LegacySizedIngredientCodecs.FLAT_ITEM.parse(ops, encoded).getOrThrow());
        final var fluid = LegacySizedIngredientCodecs.FLAT_FLUID.parse(ops, JsonParser.parseString("{\"fluid\":\"minecraft:water\",\"amount\":250}")).getOrThrow();
        assertTrue(fluid.test(new FluidStack(Fluids.WATER, 250)));
        assertFalse(fluid.test(new FluidStack(Fluids.WATER, 249)));
        final var fluidEncoded = LegacySizedIngredientCodecs.FLAT_FLUID.encodeStart(ops, fluid).getOrThrow().getAsJsonObject();
        assertEquals("minecraft:water", fluidEncoded.get("fluid").getAsString());
        assertEquals(fluid, LegacySizedIngredientCodecs.FLAT_FLUID.parse(ops, fluidEncoded).getOrThrow());
        assertTrue(LegacySizedIngredientCodecs.FLAT_ITEM.parse(ops, JsonParser.parseString("{\"item\":\"minecraft:stick\",\"count\":0}")).error().isPresent());
    }
}
