/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.recipes;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.component.food.FoodDefinition;
import net.dries007.tfc.common.component.heat.HeatDefinition;
import net.dries007.tfc.common.recipes.ingredients.EmptyIngredient;
import net.dries007.tfc.common.recipes.ingredients.TFCIngredients;

import static org.junit.jupiter.api.Assertions.*;

public class EmptyIngredientTest implements net.dries007.tfc.test.TestSetup
{
    @Test
    public void preservesLegacyEmptySlotSemanticsAndBothCodecs()
    {
        final Ingredient ingredient = EmptyIngredient.INSTANCE.toVanilla();
        assertTrue(ingredient.test(ItemStack.EMPTY));
        assertFalse(ingredient.test(new ItemStack(Items.STICK)));
        assertTrue(ingredient.isEmpty());
        assertEquals(0, ingredient.items().count());
        assertTrue(FoodDefinition.DEFAULT.ingredient().test(ItemStack.EMPTY));
        assertTrue(HeatDefinition.DEFAULT.ingredient().test(ItemStack.EMPTY));
        assertTrue(TFCIngredients.EMPTY_ITEM.ingredient().test(ItemStack.EMPTY));
        final var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        final var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        final var json = Ingredient.CODEC.encodeStart(ops, ingredient).getOrThrow();
        final var decoded = Ingredient.CODEC.parse(ops, json).getOrThrow();
        assertEquals(ingredient, decoded);
        final var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries, net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
        try
        {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            assertEquals(ingredient, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            assertEquals(0, buffer.readableBytes());
        }
        finally
        {
            buffer.release();
        }
    }
}
