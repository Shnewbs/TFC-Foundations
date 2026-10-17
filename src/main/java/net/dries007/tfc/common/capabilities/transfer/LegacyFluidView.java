/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.transfer;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/** Legacy callers join an existing native transaction through children; simulation always aborts. */
public class LegacyFluidView implements IFluidHandler
{
    protected final ResourceHandler<FluidResource> handler;
    public LegacyFluidView(ResourceHandler<FluidResource> handler) { this.handler = handler; }
    @Override public int getTanks() { return handler.size(); }
    @Override public FluidStack getFluidInTank(int tank) { return handler.getResource(tank).toStack(handler.getAmountAsInt(tank)); }
    @Override public int getTankCapacity(int tank) { return handler.getCapacityAsInt(tank, handler.getResource(tank)); }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return !stack.isEmpty() && handler.isValid(tank, FluidResource.of(stack)); }
    @Override public int fill(FluidStack stack, FluidAction action)
    {
        if (stack.isEmpty()) return 0;
        try (var transaction = TransferJournal.openLegacyTransaction())
        {
            final int inserted = handler.insert(FluidResource.of(stack), stack.getAmount(), transaction);
            if (action.execute()) transaction.commit();
            return inserted;
        }
    }
    @Override public FluidStack drain(FluidStack stack, FluidAction action)
    {
        if (stack.isEmpty()) return FluidStack.EMPTY;
        try (var transaction = TransferJournal.openLegacyTransaction())
        {
            final int extracted = handler.extract(FluidResource.of(stack), stack.getAmount(), transaction);
            if (action.execute()) transaction.commit();
            return stack.copyWithAmount(extracted);
        }
    }
    @Override public FluidStack drain(int amount, FluidAction action)
    {
        if (amount <= 0) return FluidStack.EMPTY;
        try (var transaction = TransferJournal.openLegacyTransaction())
        {
            for (int index = 0; index < handler.size(); index++)
            {
                final FluidResource resource = handler.getResource(index);
                if (resource.isEmpty()) continue;
                final int extracted = handler.extract(index, resource, amount, transaction);
                if (extracted > 0)
                {
                    if (action.execute()) transaction.commit();
                    return resource.toStack(extracted);
                }
            }
            return FluidStack.EMPTY;
        }
    }
}
