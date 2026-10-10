/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes;


import com.mojang.serialization.Codec;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * The new NeoForge sized-ingredient codecs nest the ingredient under an
 * {@code ingredient} key. TFC's existing, generated recipes use the legacy flat
 * form: {@code {"fluid":"minecraft:water","amount":250}} or
 * {@code {"tag":"tfc:wool","count":2}}.
 *
 * Keep the original on-disk schema and validation instead of rewriting or
 * silently invalidating thousands of existing recipes.
 */
public final class LegacySizedIngredientCodecs
{
    public static final Codec<SizedIngredient> FLAT_ITEM = FlatIngredientCodec.flat(
        Ingredient.CODEC, SizedIngredient::ingredient, SizedIngredient::count,
        SizedIngredient::new, "count", 1);
    public static final Codec<SizedFluidIngredient> FLAT_FLUID = FlatIngredientCodec.flat(
        FluidIngredient.CODEC, SizedFluidIngredient::ingredient, SizedFluidIngredient::amount,
        SizedFluidIngredient::new, "amount", 1000);

    private LegacySizedIngredientCodecs() {}

}
