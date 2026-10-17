/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.transfer;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/** Component-backed inventories retain auxiliary heat/food changes in the same ItemAccess exchange. */
public final class ItemInventoryResourceHandler implements ResourceHandler<ItemResource>
{
    private final ItemAccess access;
    private final Item item;
    private final Function<ItemStack, ? extends IItemHandler> factory;
    private final UnaryOperator<ItemStack> exposedStack;
    private final int size;

    public ItemInventoryResourceHandler(ItemAccess access, Function<ItemStack, ? extends IItemHandler> factory, UnaryOperator<ItemStack> exposedStack)
    {
        this.access = access;
        this.item = access.getResource().getItem();
        this.factory = factory;
        this.exposedStack = exposedStack;
        this.size = Objects.requireNonNull(factory.apply(access.getResource().toStack())).getSlots();
    }
    private @Nullable IItemHandler copyHandler()
    {
        return access.getAmount() > 0 && access.getResource().is(item) ? factory.apply(access.getResource().toStack()) : null;
    }
    @Override public int size() { return size; }
    @Override public ItemResource getResource(int index)
    {
        Objects.checkIndex(index, size());
        final var handler = copyHandler();
        return handler == null ? ItemResource.EMPTY : ItemResource.of(exposedStack.apply(handler.getStackInSlot(index).copy()));
    }
    @Override public long getAmountAsLong(int index)
    {
        Objects.checkIndex(index, size());
        final var handler = copyHandler();
        return handler == null ? 0 : (long) access.getAmount() * handler.getStackInSlot(index).getCount();
    }
    @Override public long getCapacityAsLong(int index, ItemResource resource)
    {
        Objects.checkIndex(index, size());
        final var handler = copyHandler();
        return handler == null ? 0 : (long) access.getAmount() * (resource.isEmpty() ? handler.getSlotLimit(index) : Math.min(handler.getSlotLimit(index), resource.getMaxStackSize()));
    }
    @Override public boolean isValid(int index, ItemResource resource)
    {
        Objects.checkIndex(index, size());
        final var handler = copyHandler();
        return handler != null && !resource.isEmpty() && handler.isItemValid(index, resource.toStack());
    }
    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction)
    {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferJournal.requireCurrent(transaction);
        final int count = access.getAmount();
        if (count == 0 || !access.getResource().is(item) || amount / count == 0) return 0;
        final ItemStack container = access.getResource().toStack();
        final var handler = factory.apply(container);
        if (handler == null) return 0;
        final int inserted = amount / count - handler.insertItem(index, resource.toStack(amount / count), false).getCount();
        return inserted == 0 ? 0 : inserted * access.exchange(ItemResource.of(container), count, transaction);
    }
    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction)
    {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferJournal.requireCurrent(transaction);
        final int count = access.getAmount();
        if (count == 0 || !access.getResource().is(item) || amount / count == 0 || !resource.equals(getResource(index))) return 0;
        final ItemStack container = access.getResource().toStack();
        final var handler = factory.apply(container);
        if (handler == null) return 0;
        final ItemStack extracted = handler.extractItem(index, amount / count, false);
        return extracted.isEmpty() || !resource.matches(extracted) ? 0 : extracted.getCount() * access.exchange(ItemResource.of(container), count, transaction);
    }
}
