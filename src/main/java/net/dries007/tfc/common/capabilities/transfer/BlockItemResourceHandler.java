/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.transfer;

import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Native interface over TFC storage whose backing handlers own their journals. */
public record BlockItemResourceHandler(IItemHandler handler, UnaryOperator<ItemStack> exposedStack) implements ResourceHandler<ItemResource>
{
    public BlockItemResourceHandler(IItemHandler handler) { this(handler, UnaryOperator.identity()); }
    @Override public int size() { return handler.getSlots(); }
    @Override public ItemResource getResource(int index) { Objects.checkIndex(index, size()); return ItemResource.of(exposedStack.apply(handler.getStackInSlot(index).copy())); }
    @Override public long getAmountAsLong(int index) { Objects.checkIndex(index, size()); return handler.getStackInSlot(index).getCount(); }
    @Override public long getCapacityAsLong(int index, ItemResource resource)
    {
        Objects.checkIndex(index, size());
        return resource.isEmpty() ? handler.getSlotLimit(index) : Math.min(handler.getSlotLimit(index), resource.getMaxStackSize());
    }
    @Override public boolean isValid(int index, ItemResource resource)
    {
        Objects.checkIndex(index, size());
        return !resource.isEmpty() && handler.isItemValid(index, resource.toStack());
    }
    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction)
    {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferJournal.requireCurrent(transaction);
        return amount == 0 ? 0 : amount - handler.insertItem(index, resource.toStack(amount), false).getCount();
    }
    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction)
    {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferJournal.requireCurrent(transaction);
        if (amount == 0 || !resource.equals(getResource(index))) return 0;
        try (var child = Transaction.open(transaction))
        {
            final ItemStack extracted = handler.extractItem(index, amount, false);
            if (extracted.isEmpty() || !resource.matches(extracted)) return 0;
            child.commit();
            return extracted.getCount();
        }
    }
}
