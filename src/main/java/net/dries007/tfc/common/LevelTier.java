/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import net.dries007.tfc.common.recipes.RecipeHelpers;

/** TFC's progression level paired with Minecraft 26.x's data-component tool material. */
public interface LevelTier
{
    ToolMaterial material();

    int level();

    default int getUses() { return material().durability(); }
    default float getSpeed() { return material().speed(); }
    default float getAttackDamageBonus() { return material().attackDamageBonus(); }
    default int getEnchantmentValue() { return material().enchantmentValue(); }
    default Ingredient getRepairIngredient() { return RecipeHelpers.ingredient(material().repairItems()); }
}
