/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.common.capabilities.ItemCapabilities;

/** The only legacy/native lookup boundary; native capabilities remain available to other mods. */
public final class TransferAccess
{
    private TransferAccess() {}

    public static @Nullable IFluidHandlerItem fluidHandler(ItemStack stack)
    {
        if (stack.isEmpty()) return null;
        // Single TFC containers retain the direct mutable view expected by legacy gameplay code.
        final var direct = stack.getCount() == 1 ? ItemCapabilities.fluidHandler(stack) : null;
        if (direct != null) return direct;
        final var contents = new SimpleContainer(stack) {
            @Override public void setItem(int slot, ItemStack value, boolean insideTransaction) { getItems().set(slot, value); }
        };
        final var access = ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(contents), 0);
        final var handler = access.getCapability(Capabilities.Fluid.ITEM);
        return handler == null ? null : new LegacyFluidItemView(handler, access);
    }

    public static boolean mightHaveFluidHandler(ItemStack stack) { return fluidHandler(stack.copyWithCount(1)) != null; }
    public static @Nullable IFluidHandler fluidHandler(BlockEntity entity)
    {
        return entity.getLevel() == null ? null : fluidHandler(entity.getLevel(), entity.getBlockPos(), null);
    }
    public static @Nullable IFluidHandler fluidHandler(Level level, BlockPos pos, @Nullable Direction side)
    {
        final var handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        return handler == null ? null : new LegacyFluidView(handler);
    }
    public static @Nullable IItemHandler itemHandler(Level level, BlockPos pos)
    {
        final var handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        return handler == null ? null : new LegacyItemView(handler);
    }
}
