/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.emi.recipe;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;

public abstract class BasicRecipe<T extends Recipe<?>> extends BasicEmiRecipe implements ComparableRecipe
{

    public BasicRecipe(EmiRecipeCategory category, Identifier id, int width, int height)
    {
        super(category, id, width, height);
    }

    @Override
    public int compareTo(EmiRecipe other)
    {
        Identifier otherId = other.getId();
        if (otherId != null)
        {
            return id.compareTo(other.getId());
        }
        return 0;
    }

}
