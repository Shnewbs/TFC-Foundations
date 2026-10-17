/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.transfer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/** ItemAccess retains replacements such as filled/empty native buckets. */
public final class LegacyFluidItemView extends LegacyFluidView implements IFluidHandlerItem
{
    private final ItemAccess access;
    public LegacyFluidItemView(ResourceHandler<FluidResource> handler, ItemAccess access) { super(handler); this.access = access; }
    @Override public ItemStack getContainer() { return access.getResource().toStack(access.getAmount()); }
}
