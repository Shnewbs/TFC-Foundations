/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.PlacementInfo;

/**
 * A simple set of implementations for {@link Recipe}, that skips some of the more unused methods for non-crafting uses.
 */
public interface ISimpleRecipe<C extends RecipeInput> extends Recipe<C>
{
    // TFC machine recipes are processed by their owning machines, not vanilla's recipe book.
    @Override
    default PlacementInfo placementInfo()
    {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default RecipeBookCategory recipeBookCategory()
    {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    default String group()
    {
        return "";
    }

    @Override
    default boolean showNotification()
    {
        return false;
    }

    // Keep the old registry-aware entry point for machines that still call it directly.
    ItemStack assemble(C input, HolderLookup.Provider registries);

    @Override
    default ItemStack assemble(C input)
    {
        // Every supported TFC machine implementation is registry-independent;
        // the registry-dependent data is already decoded when the recipe is loaded.
        return assemble(input, null);
    }

    /**
     * This is overridden by default for our recipes as vanilla only supports it's own recipe types in the recipe book anyway.
     * There have been forge PRs to try and add support to this, but frankly, nobody cares.
     * This then prevents "Unknown recipe category" log spam for every recipe in {@link net.minecraft.client.ClientRecipeBook}
     */
    @Override
    default boolean isSpecial()
    {
        return true;
    }

    default ItemStack getResultItem(HolderLookup.Provider registries)
    {
        return ItemStack.EMPTY;
    }
}