/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.transfer;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/** One shared journal owns the storage, so every sided view sees the same transactional state. */
public class TransactionalItemStackHandler extends ItemStackHandler
{
    private final TransferJournal<List<SavedStack>> transfers = new TransferJournal<>(this::copyStacks, this::restoreStacks, original -> {
        for (int slot = 0; slot < getSlots(); slot++)
            if (!original.get(slot).matches(getStackInSlot(slot))) onCommittedContentsChanged(slot);
    }, () -> {});

    public TransactionalItemStackHandler(int slots) { super(slots); }

    /** Snapshot the exact patch, without copy() resetting transient food creation dates. */
    private record SavedStack(ItemStack original, net.minecraft.world.item.Item item, int count, net.minecraft.core.component.DataComponentPatch patch)
    {
        SavedStack(ItemStack stack) { this(stack, stack.getItem(), stack.getCount(), stack.getComponentsPatch()); }
        ItemStack restore()
        {
            if (!original.isEmpty() || count > 0)
            {
                original.setCount(count);
                ((net.minecraft.core.component.PatchedDataComponentMap) original.getComponents()).restorePatch(patch);
            }
            return original;
        }
        boolean matches(ItemStack stack)
        {
            return item == stack.getItem() && count == stack.getCount() && patch.equals(stack.getComponentsPatch());
        }
    }

    private List<SavedStack> copyStacks()
    {
        final List<SavedStack> copy = new ArrayList<>(getSlots());
        for (ItemStack stack : stacks) copy.add(new SavedStack(stack));
        return copy;
    }

    private void restoreStacks(List<SavedStack> copy)
    {
        for (int slot = 0; slot < copy.size(); slot++) stacks.set(slot, copy.get(slot).restore());
    }

    /** Fluid/heat capabilities on stored mold items mutate their components directly. */
    public final void prepareExternalMutation()
    {
        transfers.beforeChange();
        transfers.changed();
    }

    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (!simulate) transfers.beforeChange();
        return super.insertItem(slot, stack, simulate);
    }

    @Override public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (!simulate) transfers.beforeChange();
        return super.extractItem(slot, amount, simulate);
    }

    @Override public void setStackInSlot(int slot, ItemStack stack)
    {
        transfers.beforeChange();
        super.setStackInSlot(slot, stack);
    }

    @Override protected final void onContentsChanged(int slot)
    {
        if (transfers.isInTransaction()) transfers.changed();
        else onCommittedContentsChanged(slot);
    }

    protected void onCommittedContentsChanged(int slot) {}
}
