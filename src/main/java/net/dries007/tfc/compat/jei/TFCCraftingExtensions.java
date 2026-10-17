/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.RecipeHolder;

import net.dries007.tfc.common.recipes.AdvancedShapelessRecipe;
import net.dries007.tfc.common.recipes.AdvancedShapedRecipe;
import net.dries007.tfc.common.recipes.RecipeHelpers;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.common.recipes.outputs.ExtraProductModifier;

public final class TFCCraftingExtensions
{
    // NeoForge 26.1 exposes item holders; JEI still displays ItemStacks.
    // Retain TFC's component-aware previews for precise and composite ingredients.
    private static List<ItemStack> previewItems(Ingredient ingredient)
    {
        if (ingredient.getCustomIngredient() instanceof net.dries007.tfc.common.recipes.ingredients.AndIngredient and)
        {
            return and.getItems().toList();
        }
        if (ingredient.getCustomIngredient() instanceof net.dries007.tfc.common.recipes.ingredients.PreciseIngredient precise)
        {
            return precise.getItems().toList();
        }
        return ingredient.items().map(holder -> holder.value().getDefaultInstance()).toList();
    }

    public static void register(IVanillaCategoryExtensionRegistration registry)
    {
        registry.getCraftingCategory().addExtension(AdvancedShapedRecipe.class, new ICraftingCategoryExtension<>()
        {
            @Override
            public List<SlotDisplay> getIngredients(RecipeHolder<AdvancedShapedRecipe> holder)
            {
                return holder.value().getIngredients().stream().map(i -> i.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList();
            }

            @Override
            public int getWidth(RecipeHolder<AdvancedShapedRecipe> holder) { return holder.value().getWidth(); }

            @Override
            public int getHeight(RecipeHolder<AdvancedShapedRecipe> holder) { return holder.value().getHeight(); }

            @Override
            public void setRecipe(RecipeHolder<AdvancedShapedRecipe> holder, IRecipeLayoutBuilder builder, ICraftingGridHelper helper, IFocusGroup focuses)
            {
                final AdvancedShapedRecipe recipe = holder.value();
                TFCCraftingExtensions.setRecipe(builder, helper, recipe.getIngredients().stream().map(i -> i.map(TFCCraftingExtensions::previewItems).orElse(List.of())).toList(),
                    recipe.getResult(), recipe.getRemainder(), recipe.getInputSlot(), recipe.getWidth(), recipe.getHeight());
            }
        });
        registry.getCraftingCategory().addExtension(AdvancedShapelessRecipe.class, new ICraftingCategoryExtension<>()
        {
            @Override
            public List<SlotDisplay> getIngredients(RecipeHolder<AdvancedShapelessRecipe> holder)
            {
                return holder.value().getIngredients().stream().map(Ingredient::display).toList();
            }

            @Override
            public void setRecipe(RecipeHolder<AdvancedShapelessRecipe> holder, IRecipeLayoutBuilder builder, ICraftingGridHelper helper, IFocusGroup focuses)
            {
                final AdvancedShapelessRecipe recipe = holder.value();
                final List<List<ItemStack>> inputs = recipe.getIngredients().stream().map(TFCCraftingExtensions::previewItems).toList();
                final Optional<Ingredient> primary = recipe.getPrimaryIngredient();
                int primaryIndex = -1;
                if (primary.isPresent())
                {
                    final List<ItemStack> primaryItems = previewItems(primary.get());
                    for (int i = 0; i < inputs.size(); i++)
                    {
                        final List<ItemStack> items = inputs.get(i);
                        if (items.size() == primaryItems.size() && java.util.stream.IntStream.range(0, items.size())
                            .allMatch(j -> ItemStack.isSameItemSameComponents(items.get(j), primaryItems.get(j))))
                        {
                            primaryIndex = i;
                            break;
                        }
                    }
                }
                TFCCraftingExtensions.setRecipe(builder, helper, inputs, recipe.getResult(), recipe.getRemainder(), primaryIndex, 0, 0);
            }
        });
    }

    private static void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper helper, List<List<ItemStack>> inputs,
        ItemStackProvider result, Optional<ItemStackProvider> remainder, int primaryIndex, int width, int height)
    {
        final List<IRecipeSlotBuilder> inputSlots = helper.createAndSetInputs(builder, JEIIntegration.ITEM_STACK, inputs, width, height);
        final int gridWidth = width != 0 ? width : inputs.size() == 1 ? 1 : inputs.size() <= 4 ? 2 : 3;
        final int gridHeight = height != 0 ? height : gridWidth;
        final List<ItemStack> outputItems = new ArrayList<>();
        if (!result.dependsOnInput())
        {
            final ItemStack output = result.getEmptyStack();
            if (!output.isEmpty()) outputItems.add(output);
        }
        else if (primaryIndex >= 0 && primaryIndex < inputs.size())
        {
            final List<ItemStack> grid = new ArrayList<>();
            for (int i = 0; i < gridWidth * gridHeight; i++)
            {
                grid.add(i < inputs.size() && !inputs.get(i).isEmpty() ? inputs.get(i).getFirst().copy() : ItemStack.EMPTY);
            }
            for (ItemStack item : inputs.get(primaryIndex))
            {
                for (ItemStack displayItem : result.displayInputs(item))
                {
                    grid.set(primaryIndex, displayItem.copy());
                    RecipeHelpers.setCraftingInput(CraftingInput.of(gridWidth, gridHeight, List.copyOf(grid)));
                    try
                    {
                        final ItemStack output = result.getSingleStackDisplayOnly(displayItem);
                        if (!output.isEmpty()) outputItems.add(output);
                    }
                    finally
                    {
                        RecipeHelpers.clearCraftingInput();
                    }
                }
            }
        }
        if (!outputItems.isEmpty())
        {
            final IRecipeSlotBuilder outputSlot = helper.createAndSetOutputs(builder, JEIIntegration.ITEM_STACK, outputItems);
            // Focus links require a one-to-one sequence, including any component-aware display expansion.
            if (result.dependsOnInput() && primaryIndex >= 0 && outputItems.size() == inputs.get(primaryIndex).size())
            {
                builder.createFocusLink(inputSlots.get(helper.getCraftingIndex(primaryIndex, gridWidth, gridHeight)), outputSlot);
            }
        }
        remainder.ifPresent(r -> r.modifiers().forEach(mod -> {
            if (mod instanceof ExtraProductModifier(ItemStack extra)) builder.addOutputSlot(60, 0).addItemStack(extra);
        }));
    }
}
