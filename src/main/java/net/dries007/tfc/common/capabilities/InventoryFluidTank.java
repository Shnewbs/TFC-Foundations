/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities;

import java.util.function.Predicate;
import net.dries007.tfc.common.capabilities.transfer.TransferJournal;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class InventoryFluidTank extends FluidTank
{
    private final FluidTankCallback callback;
    private final TransferJournal<FluidStack> transfers;

    public InventoryFluidTank(int capacity, FluidTankCallback callback)
    {
        super(capacity);
        this.callback = callback;
        this.transfers = new TransferJournal<>(() -> getFluid().copy(), this::setFluid,
            original -> callback.fluidTankChanged(), callback::fluidTankChanged);
    }

    public InventoryFluidTank(int capacity, Predicate<FluidStack> validator, FluidTankCallback callback)
    {
        super(capacity, validator);
        this.callback = callback;
        this.transfers = new TransferJournal<>(() -> getFluid().copy(), this::setFluid,
            original -> callback.fluidTankChanged(), callback::fluidTankChanged);
    }

    @Override
    protected void onContentsChanged()
    {
        transfers.changed();
    }
    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        if (action.execute()) transfers.beforeChange();
        return super.fill(resource, action);
    }

    @Override
    public FluidStack drain(int amount, FluidAction action)
    {
        if (action.execute()) transfers.beforeChange();
        return super.drain(amount, action);
    }

}
