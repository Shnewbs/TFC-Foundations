/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.item;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.capabilities.InventoryFluidTank;
import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.dries007.tfc.common.capabilities.transfer.BlockFluidResourceHandler;
import net.dries007.tfc.common.capabilities.transfer.BlockItemResourceHandler;
import net.dries007.tfc.common.capabilities.transfer.TransactionalItemStackHandler;
import net.dries007.tfc.common.component.mold.IMold;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.test.TestSetup;
import net.dries007.tfc.util.Metal;

import static org.junit.jupiter.api.Assertions.*;

/** Actual registered containers and storage owners, using NeoForge's native transactions. */
public class NativeTransferTest implements TestSetup
{
    @Test
    public void vesselAlloyChildCommitRollsBackWithParent()
    {
        final ItemStack vessel = new ItemStack(TFCItems.VESSEL);
        IMold.get(vessel).setTemperature(1500);
        final var before = vessel.getComponentsPatch();
        final var fluid = ItemAccess.forStack(vessel).getCapability(Capabilities.Fluid.ITEM);
        assertNotNull(fluid);
        final var copper = FluidResource.of(TFCFluids.METALS.get(Metal.COPPER).getSource());
        final var tin = FluidResource.of(TFCFluids.METALS.get(Metal.TIN).getSource());
        final var bronze = FluidResource.of(TFCFluids.METALS.get(Metal.BRONZE).getSource());
        try (var parent = Transaction.openRoot())
        {
            assertEquals(90, fluid.insert(0, copper, 90, parent));
            try (var child = Transaction.open(parent))
            {
                assertEquals(10, fluid.insert(0, tin, 10, child));
                assertEquals(bronze, fluid.getResource(0));
                assertEquals(20, fluid.extract(0, bronze, 20, child));
                child.commit();
            }
            assertEquals(80, fluid.getAmountAsLong(0));
        }
        assertEquals(before, vessel.getComponentsPatch());
        assertEquals(0, fluid.getAmountAsLong(0));
    }

    @Test
    public void vesselFoodExtractionUsesUnpreservedResourceAndRollsBack()
    {
        final var food = new ItemStack(TFCItems.FOOD.get(net.dries007.tfc.common.items.Food.BANANA));
        final long expiry = net.dries007.tfc.common.component.food.FoodCapability.get(food).getRottenDate();
        final var resource = ItemResource.of(food);
        final var vessel = new ItemStack(TFCItems.VESSEL);
        final var items = ItemAccess.forStack(vessel).getCapability(Capabilities.Item.ITEM);
        assertNotNull(items);
        try (var transaction = Transaction.openRoot())
        {
            assertEquals(3, items.insert(0, resource, 3, transaction));
            transaction.commit();
        }
        final var filled = vessel.getComponentsPatch();
        assertEquals(resource, items.getResource(0));
        assertEquals(expiry, net.dries007.tfc.common.component.food.FoodCapability.get(items.getResource(0).toStack()).getRottenDate());
        try (var transaction = Transaction.openRoot())
        {
            assertEquals(2, items.extract(0, resource, 2, transaction));
            assertEquals(1, items.getAmountAsLong(0));
        }
        assertEquals(filled, vessel.getComponentsPatch());
        assertEquals(3, items.getAmountAsLong(0));
    }

    @Test
    public void bucketNestedCommitStillRollsBackWithParent()
    {
        final ItemStack bucket = new ItemStack(TFCItems.WOODEN_BUCKET);
        final var before = bucket.getComponentsPatch();
        final var fluid = ItemAccess.forStack(bucket).getCapability(Capabilities.Fluid.ITEM);
        assertNotNull(fluid);
        final var water = FluidResource.of(Fluids.WATER);
        try (var parent = Transaction.openRoot())
        {
            assertEquals(300, fluid.insert(0, water, 300, parent));
            try (var child = Transaction.open(parent))
            {
                assertEquals(100, fluid.extract(0, water, 100, child));
                child.commit();
            }
            assertEquals(200, fluid.getAmountAsLong(0));
        }
        assertEquals(before, bucket.getComponentsPatch());
        assertEquals(0, fluid.getAmountAsLong(0));
        try (var transaction = Transaction.openRoot())
        {
            assertEquals(300, fluid.insert(0, water, 300, transaction));
            transaction.commit();
        }
        assertEquals(300, fluid.getAmountAsLong(0));
    }

    @Test
    public void moldRollbackPreservesHeatAndColdDrainLock()
    {
        final ItemStack mold = new ItemStack(TFCItems.MOLDS.get(Metal.ItemType.INGOT));
        final var before = mold.getComponentsPatch();
        final var fluid = ItemAccess.forStack(mold).getCapability(Capabilities.Fluid.ITEM);
        assertNotNull(fluid);
        final var copper = FluidResource.of(TFCFluids.METALS.get(Metal.COPPER).getSource());
        try (var transaction = Transaction.openRoot())
        {
            assertEquals(100, fluid.insert(0, copper, 100, transaction));
            assertEquals(0, fluid.extract(0, copper, 100, transaction));
        }
        assertEquals(before, mold.getComponentsPatch());
        IMold.get(mold).setTemperature(1500);
        final var hot = mold.getComponentsPatch();
        try (var transaction = Transaction.openRoot())
        {
            assertEquals(100, fluid.insert(0, copper, 100, transaction));
            assertEquals(100, fluid.extract(0, copper, 100, transaction));
        }
        assertEquals(hot, mold.getComponentsPatch());
    }

    @Test
    public void vesselRollsBackAllCoupledComponents()
    {
        final ItemStack vessel = new ItemStack(TFCItems.VESSEL);
        final var before = vessel.getComponentsPatch();
        final var items = ItemAccess.forStack(vessel).getCapability(Capabilities.Item.ITEM);
        assertNotNull(items);
        final var rock = ItemResource.of(TFCBlocks.ROCK_BLOCKS.get(Rock.CHERT).get(Rock.BlockType.LOOSE).asItem());
        try (var parent = Transaction.openRoot())
        {
            assertEquals(5, items.insert(0, rock, 5, parent));
            try (var child = Transaction.open(parent))
            {
                assertEquals(3, items.extract(0, items.getResource(0), 3, child));
                child.commit();
            }
            assertEquals(2, items.getAmountAsLong(0));
        }
        assertEquals(before, vessel.getComponentsPatch());
        assertEquals(0, items.getAmountAsLong(0));
    }

    @Test
    public void sharedSidedItemViewsDeferCallbacksAndRestoreStoredReference()
    {
        final var callbacks = new AtomicInteger();
        final var storage = new TransactionalItemStackHandler(1) {
            @Override protected void onCommittedContentsChanged(int slot) { callbacks.incrementAndGet(); }
        };
        final ItemStack original = new ItemStack(Items.STICK, 8);
        storage.setStackInSlot(0, original);
        callbacks.set(0);
        final var first = new BlockItemResourceHandler(storage);
        final var second = new BlockItemResourceHandler(storage);
        try (var parent = Transaction.openRoot())
        {
            assertEquals(3, first.extract(0, ItemResource.of(Items.STICK), 3, parent));
            try (var child = Transaction.open(parent))
            {
                assertEquals(5, second.extract(0, ItemResource.of(Items.STICK), 5, child));
                child.commit();
            }
            assertTrue(storage.getStackInSlot(0).isEmpty());
            assertEquals(0, callbacks.get());
        }
        assertSame(original, storage.getStackInSlot(0));
        assertEquals(8, original.getCount());
        assertEquals(0, callbacks.get());
        try (var transaction = Transaction.openRoot())
        {
            assertEquals(2, first.extract(0, ItemResource.of(Items.STICK), 2, transaction));
            transaction.commit();
        }
        assertEquals(6, storage.getStackInSlot(0).getCount());
        assertEquals(1, callbacks.get());
    }

    @Test
    public void blockTankAndItemStorageAbortAsOneTransaction()
    {
        final var notifications = new AtomicInteger();
        final var tank = new InventoryFluidTank(1000, new FluidTankCallback() {
            @Override public void fluidTankChanged() { notifications.incrementAndGet(); }
        });
        final var fluid = new BlockFluidResourceHandler(tank);
        final var storage = new TransactionalItemStackHandler(1);
        final var items = new BlockItemResourceHandler(storage);
        try (var transaction = Transaction.openRoot())
        {
            assertEquals(300, fluid.insert(0, FluidResource.of(Fluids.WATER), 300, transaction));
            assertEquals(4, items.insert(0, ItemResource.of(Items.STICK), 4, transaction));
            assertEquals(0, notifications.get());
        }
        assertEquals(0, fluid.getAmountAsLong(0));
        assertTrue(storage.getStackInSlot(0).isEmpty());
        assertEquals(0, notifications.get());
    }
}
