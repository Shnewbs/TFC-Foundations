/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.transfer;

import java.util.Objects;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/** Execute the real container logic on copies, then exchange every changed component atomically. */
public final class ItemFluidResourceHandler implements ResourceHandler<FluidResource>
{
    private final ItemAccess access;
    private final Item item;
    private final Function<ItemStack, ? extends IFluidHandlerItem> factory;

    public ItemFluidResourceHandler(ItemAccess access, Function<ItemStack, ? extends IFluidHandlerItem> factory)
    {
        this.access = access;
        this.item = access.getResource().getItem();
        this.factory = factory;
    }

    private @Nullable IFluidHandlerItem copyHandler()
    {
        return access.getAmount() > 0 && access.getResource().is(item) ? factory.apply(access.getResource().toStack()) : null;
    }

    @Override public int size() { return 1; }
    @Override public FluidResource getResource(int index)
    {
        Objects.checkIndex(index, size());
        final var handler = copyHandler();
        return handler == null ? FluidResource.EMPTY : FluidResource.of(handler.getFluidInTank(0));
    }
    @Override public long getAmountAsLong(int index)
    {
        Objects.checkIndex(index, size());
        final var handler = copyHandler();
        return handler == null ? 0 : (long) access.getAmount() * handler.getFluidInTank(0).getAmount();
    }
    @Override public long getCapacityAsLong(int index, FluidResource resource)
    {
        Objects.checkIndex(index, size());
        final var handler = copyHandler();
        return handler == null ? 0 : (long) access.getAmount() * handler.getTankCapacity(0);
    }
    @Override public boolean isValid(int index, FluidResource resource)
    {
        Objects.checkIndex(index, size());
        final var handler = copyHandler();
        return handler != null && !resource.isEmpty() && handler.isFluidValid(0, resource.toStack(1));
    }
    private int exchange(IFluidHandlerItem handler, int amount, TransactionContext transaction)
    {
        final ItemResource replacement = ItemResource.of(handler.getContainer());
        return replacement.isEmpty() ? access.extract(access.getResource(), amount, transaction) : access.exchange(replacement, amount, transaction);
    }
    @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction)
    {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferJournal.requireCurrent(transaction);
        final int count = access.getAmount();
        final var handler = copyHandler();
        if (handler == null || amount / count == 0) return 0;
        final int filled = handler.fill(resource.toStack(amount / count), IFluidHandler.FluidAction.EXECUTE);
        return filled == 0 ? 0 : filled * exchange(handler, count, transaction);
    }
    @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction)
    {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferJournal.requireCurrent(transaction);
        final int count = access.getAmount();
        final var handler = copyHandler();
        if (handler == null || amount / count == 0) return 0;
        final var drained = handler.drain(resource.toStack(amount / count), IFluidHandler.FluidAction.EXECUTE);
        return drained.isEmpty() || !resource.matches(drained) ? 0 : drained.getAmount() * exchange(handler, count, transaction);
    }
}
