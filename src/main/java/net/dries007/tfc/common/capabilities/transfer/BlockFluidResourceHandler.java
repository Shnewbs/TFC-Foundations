/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.transfer;

import java.util.Objects;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** TFC blocks expose one fluid port; mold tables may consume fluid through an instant recipe. */
public record BlockFluidResourceHandler(IFluidHandler handler, Runnable beforeMutation) implements ResourceHandler<FluidResource>
{
    public BlockFluidResourceHandler(IFluidHandler handler) { this(handler, () -> {}); }
    @Override public int size() { return Math.max(1, handler.getTanks()); }
    @Override public FluidResource getResource(int index)
    {
        Objects.checkIndex(index, size());
        return index < handler.getTanks() ? FluidResource.of(handler.getFluidInTank(index)) : FluidResource.EMPTY;
    }
    @Override public long getAmountAsLong(int index)
    {
        Objects.checkIndex(index, size());
        return index < handler.getTanks() ? handler.getFluidInTank(index).getAmount() : 0;
    }
    @Override public long getCapacityAsLong(int index, FluidResource resource)
    {
        Objects.checkIndex(index, size());
        return index < handler.getTanks() ? handler.getTankCapacity(index) : Integer.MAX_VALUE;
    }
    @Override public boolean isValid(int index, FluidResource resource)
    {
        Objects.checkIndex(index, size());
        return !resource.isEmpty() && ((index < handler.getTanks() && handler.isFluidValid(index, resource.toStack(1)))
            || handler.fill(resource.toStack(Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE) > 0);
    }
    @Override public int insert(int index, FluidResource resource, int amount, TransactionContext transaction)
    {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferJournal.requireCurrent(transaction);
        return index == 0 ? insert(resource, amount, transaction) : 0;
    }
    @Override public int insert(FluidResource resource, int amount, TransactionContext transaction)
    {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferJournal.requireCurrent(transaction);
        if (amount == 0) return 0;
        beforeMutation.run();
        return handler.fill(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
    }
    @Override public int extract(int index, FluidResource resource, int amount, TransactionContext transaction)
    {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferJournal.requireCurrent(transaction);
        return index == 0 ? extract(resource, amount, transaction) : 0;
    }
    @Override public int extract(FluidResource resource, int amount, TransactionContext transaction)
    {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferJournal.requireCurrent(transaction);
        if (amount == 0) return 0;
        beforeMutation.run();
        return handler.drain(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE).getAmount();
    }
}
