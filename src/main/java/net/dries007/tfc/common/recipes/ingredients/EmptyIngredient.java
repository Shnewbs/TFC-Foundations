/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes.ingredients;

import java.util.stream.Stream;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** The removed legacy empty ingredient: matches an empty slot and has no item alternatives. */
public enum EmptyIngredient implements ICustomIngredient
{
    INSTANCE;

    public static final MapCodec<EmptyIngredient> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, EmptyIngredient> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override public boolean test(ItemStack stack) { return stack.isEmpty(); }
    @Override public Stream<Holder<Item>> items() { return Stream.empty(); }
    // An empty slot cannot be represented by native item membership or the vanilla ingredient stream codec.
    @Override public boolean isSimple() { return false; }
    @Override public SlotDisplay display() { return SlotDisplay.Empty.INSTANCE; }
    @Override public IngredientType<?> getType() { return TFCIngredients.EMPTY.get(); }
}
