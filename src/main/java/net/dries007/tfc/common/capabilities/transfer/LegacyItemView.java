/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.transfer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record LegacyItemView(ResourceHandler<ItemResource> handler) implements IItemHandler
{
    @Override public int getSlots() { return handler.size(); }
    @Override public ItemStack getStackInSlot(int slot) { return handler.getResource(slot).toStack(handler.getAmountAsInt(slot)); }
    @Override public int getSlotLimit(int slot) { return handler.getCapacityAsInt(slot, ItemResource.EMPTY); }
    @Override public boolean isItemValid(int slot, ItemStack stack) { return !stack.isEmpty() && handler.isValid(slot, ItemResource.of(stack)); }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        try (var transaction = TransferJournal.openLegacyTransaction())
        {
            final int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) transaction.commit();
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (amount <= 0) return ItemStack.EMPTY;
        final var resource = handler.getResource(slot);
        if (resource.isEmpty()) return ItemStack.EMPTY;
        try (var transaction = TransferJournal.openLegacyTransaction())
        {
            final int extracted = handler.extract(slot, resource, amount, transaction);
            if (!simulate) transaction.commit();
            return resource.toStack(extracted);
        }
    }
}
