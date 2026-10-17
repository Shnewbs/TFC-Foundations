/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.recipes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.recipes.AdvancedShapedRecipe;
import net.dries007.tfc.common.recipes.AdvancedShapelessRecipe;
import net.dries007.tfc.common.recipes.RecipeHelpers;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifierType;
import net.dries007.tfc.common.recipes.outputs.CopyInputModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.test.TestSetup;

import static org.junit.jupiter.api.Assertions.*;

public class AdvancedCraftingTest implements TestSetup
{
    private static AdvancedShapedRecipe shaped(ItemStackProvider result, Optional<ItemStackProvider> remainder)
    {
        return new AdvancedShapedRecipe(ShapedRecipePattern.of(
            Map.of('S', Ingredient.of(Items.STICK), 'D', Ingredient.of(Items.DIAMOND)), List.of("SD")),
            true, result, remainder, 0, 0);
    }

    private static AdvancedShapelessRecipe shapeless(ItemStackProvider result, Optional<ItemStackProvider> remainder)
    {
        return new AdvancedShapelessRecipe(NonNullList.copyOf(List.of(Ingredient.of(Items.STICK), Ingredient.of(Items.DIAMOND))),
            result, remainder, Optional.of(Ingredient.of(Items.STICK)));
    }

    @Test
    public void dynamicShapedOutputTracksMirroringAndKeepsInputs()
    {
        final var recipe = shaped(ItemStackProvider.copyInput(), Optional.of(ItemStackProvider.copyInput()));
        final var diamond = new ItemStack(Items.DIAMOND, 3);
        final var stick = new ItemStack(Items.STICK, 5);
        final var mirrored = CraftingInput.of(2, 1, List.of(diamond, stick));
        assertTrue(recipe.matches(mirrored, null));
        assertEquals(1, RecipeHelpers.translateMatch(recipe, recipe.getInputSlot(), mirrored));
        final var output = recipe.assemble(mirrored);
        assertTrue(output.is(Items.STICK));
        assertEquals(1, output.getCount());
        assertEquals(5, stick.getCount());
        assertTrue(recipe.getRemainingItems(mirrored).get(1).is(Items.STICK));
        assertTrue(recipe.isSpecial());
        assertEquals(SlotDisplay.Empty.INSTANCE, recipe.display().getFirst().result());
        assertEquals(2, recipe.placementInfo().ingredients().size());
        assertFalse(recipe.matches(CraftingInput.of(1, 2, List.of(stick, diamond)), null));
    }

    @Test
    public void shapelessUsesPrimaryInputAndRejectsExtraOrDuplicateIngredients()
    {
        final var recipe = shapeless(ItemStackProvider.copyInput(), Optional.empty());
        assertTrue(recipe.matches(CraftingInput.of(2, 1, List.of(new ItemStack(Items.DIAMOND), new ItemStack(Items.STICK))), null));
        assertTrue(recipe.assemble(CraftingInput.of(2, 1, List.of(new ItemStack(Items.DIAMOND), new ItemStack(Items.STICK)))).is(Items.STICK));
        assertFalse(recipe.matches(CraftingInput.of(2, 1, List.of(new ItemStack(Items.STICK), new ItemStack(Items.STICK))), null));
        assertFalse(recipe.matches(CraftingInput.of(3, 1, List.of(new ItemStack(Items.STICK), new ItemStack(Items.DIAMOND), new ItemStack(Items.DIRT))), null));
    }

    @Test
    public void fixedResultsUseRealNativeDisplaysAndDefaultRemainders()
    {
        final var recipe = new AdvancedShapelessRecipe(NonNullList.copyOf(List.of(Ingredient.of(Items.WATER_BUCKET))),
            ItemStackProvider.of(Items.DIAMOND, 2), Optional.empty(), Optional.of(Ingredient.of(Items.WATER_BUCKET)));
        assertFalse(recipe.isSpecial());
        assertInstanceOf(SlotDisplay.ItemStackSlotDisplay.class, recipe.display().getFirst().result());
        final var input = CraftingInput.of(1, 1, List.of(new ItemStack(Items.WATER_BUCKET)));
        assertTrue(recipe.matches(input, null));
        assertEquals(2, recipe.assemble(input).getCount());
        assertTrue(recipe.getRemainingItems(input).getFirst().is(Items.BUCKET));
        assertInstanceOf(SlotDisplay.ItemStackSlotDisplay.class, shaped(ItemStackProvider.of(Items.DIAMOND), Optional.empty()).display().getFirst().result());
    }

    @Test
    public void bothRegisteredCodecsRetainDynamicProviders()
    {
        final var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        final var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        for (var recipe : List.of(shaped(ItemStackProvider.copyInput(), Optional.empty()), shapeless(ItemStackProvider.copyInput(), Optional.empty())))
        {
            final var encoded = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow();
            assertEquals(recipe.getClass(), Recipe.CODEC.parse(ops, encoded).getOrThrow().getClass());
            final var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries, net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE);
            try
            {
                Recipe.STREAM_CODEC.encode(buffer, recipe);
                final var decoded = Recipe.STREAM_CODEC.decode(buffer);
                assertEquals(recipe.getClass(), decoded.getClass());
                assertTrue(decoded.isSpecial());
                assertEquals(0, buffer.readableBytes());
            }
            finally { buffer.release(); }
        }
    }

    @Test
    public void failingProvidersAlwaysClearCraftingContext()
    {
        final var failure = ItemStackProvider.of(new ItemStackModifier() {
            @Override
            public ItemStack apply(ItemStack stack, ItemStack input, Context context) { throw new IllegalStateException("fixture"); }
            @Override
            public ItemStackModifierType<?> type() { return CopyInputModifier.INSTANCE.type(); }
        });
        final var input = CraftingInput.of(2, 1, List.of(new ItemStack(Items.STICK), new ItemStack(Items.DIAMOND)));
        for (var recipe : List.of(shaped(failure, Optional.of(failure)), shapeless(failure, Optional.of(failure))))
        {
            assertThrows(IllegalStateException.class, () -> recipe.assemble(input));
            assertFalse(RecipeHelpers.getCraftingInput().iterator().hasNext());
            assertThrows(IllegalStateException.class, () -> recipe.getRemainingItems(input));
            assertFalse(RecipeHelpers.getCraftingInput().iterator().hasNext());
        }
    }
}
