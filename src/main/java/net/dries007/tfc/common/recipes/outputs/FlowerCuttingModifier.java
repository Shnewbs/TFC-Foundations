/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes.outputs;

import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;

import net.dries007.tfc.common.blocks.plant.PlantBlock;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.item.ItemComponent;
import net.dries007.tfc.common.items.FlowerCuttingItem;
import net.dries007.tfc.util.Helpers;

public enum FlowerCuttingModifier implements ItemStackModifier
{
    INSTANCE;

    @Override
    public ItemStack apply(ItemStack stack, ItemStack input, Context context)
    {
        final ItemStack plant = input.getOrDefault(TFCComponents.PLANT, ItemComponent.EMPTY).stack();
        return plant.isEmpty() ? ItemStack.EMPTY : plant.copyWithCount(2);
    }

    @Override
    public boolean dependsOnInput()
    {
        return true;
    }

    @Override
    public List<ItemStack> displayInputs(ItemStack input)
    {
        if (input.has(TFCComponents.PLANT))
        {
            return List.of(input);
        }
        // Cuttings are only ever obtained with a plant attached, from shearing a flower, so display one cutting per flower
        return Helpers.allBlocks(BlockTags.FLOWERS)
            .filter(block -> block instanceof PlantBlock)
            .map(block -> FlowerCuttingItem.of(new ItemStack(block)))
            .toList();
    }

    @Override
    public ItemStackModifierType<?> type()
    {
        return ItemStackModifiers.FLOWER_CUTTING.get();
    }
}
