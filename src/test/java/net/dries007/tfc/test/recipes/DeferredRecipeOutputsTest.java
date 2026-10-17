/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.recipes;

import java.util.List;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.recipes.outputs.ItemOutput;
import net.dries007.tfc.common.recipes.outputs.FluidOutput;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.test.TestSetup;
import net.dries007.tfc.util.SelfTests;

import static org.junit.jupiter.api.Assertions.*;

public class DeferredRecipeOutputsTest implements TestSetup
{
    @Test
    public void itemRecipeDataLoadsBeforeComponentsAndCreatesIndependentUpdatedStacks()
    {
        final var holder = Items.DIAMOND.builtInRegistryHolder();
        final var defaults = holder.components();
        final var ops = REGISTRIES.get().createSerializationContext(JsonOps.INSTANCE);
        final var json = JsonParser.parseString("{\"id\":\"minecraft:diamond\",\"count\":3}");
        try
        {
            holder.bindComponents(null); // Reproduce the native reload stage, before default components bind.
            assertFalse(holder.areComponentsBound());
            assertTrue(ItemStack.CODEC.parse(ops, json).isError());
            final var output = ItemOutput.CODEC.parse(ops, json).getOrThrow();
            final var provider = ItemStackProvider.CODEC.parse(ops, json).getOrThrow();
            assertFalse(holder.areComponentsBound(), "Decoding must not bind prototype components");
            holder.bindComponents(defaults);
            assertEquals(3, output.copy().getCount());
            assertTrue(provider.stack().is(Items.DIAMOND));
            final var first = output.copy(); first.setCount(1);
            assertEquals(3, output.copy().getCount());
            holder.bindComponents(DataComponentMap.builder().addAll(defaults).set(DataComponents.MAX_STACK_SIZE, 32).build());
            assertEquals(32, output.copy().get(DataComponents.MAX_STACK_SIZE), "Reloaded defaults must be visible");
            assertEquals(json, ItemOutput.CODEC.encodeStart(ops, output).getOrThrow());
        }
        finally { holder.bindComponents(defaults); }
    }

    @Test
    public void fluidRecipeDataLoadsBeforeComponentsAndPreservesAmount()
    {
        final var holder = Fluids.WATER.builtInRegistryHolder();
        final var defaults = holder.components();
        final var ops = REGISTRIES.get().createSerializationContext(JsonOps.INSTANCE);
        try
        {
            holder.bindComponents(null);
            final var json = JsonParser.parseString("{\"id\":\"minecraft:water\",\"amount\":750}");
            assertTrue(FluidStack.CODEC.parse(ops, json).isError());
            final var output = FluidOutput.CODEC.parse(ops, json).getOrThrow();
            assertFalse(holder.areComponentsBound());
            assertEquals(750, output.getAmount());
            holder.bindComponents(defaults);
            final var first = output.copy(); first.setAmount(1);
            assertEquals(750, output.copy().getAmount());
            assertTrue(output.copy().is(Fluids.WATER));
        }
        finally { holder.bindComponents(defaults); }
    }

    @Test
    public void displayedFoodDoesNotDecayButCraftedCopiesDo()
    {
        final var provider = ItemStackProvider.of(new ItemStack(net.dries007.tfc.common.items.TFCItems.FOOD.get(net.dries007.tfc.common.items.Food.BANANA)));
        final var display = provider.stack();
        assertEquals(net.dries007.tfc.common.component.food.IFood.TRANSIENT_NEVER_DECAY_FLAG, net.dries007.tfc.common.component.food.FoodCapability.get(display).getCreationDate());
        final var first = provider.getEmptyStack();
        final var second = provider.getEmptyStack();
        assertTrue(net.dries007.tfc.common.component.food.FoodCapability.get(first).getCreationDate() >= 0);
        net.dries007.tfc.common.component.food.FoodCapability.setRotten(first);
        assertNotEquals(net.dries007.tfc.common.component.food.IFood.ROTTEN_FLAG, net.dries007.tfc.common.component.food.FoodCapability.get(second).getCreationDate());
        assertEquals(net.dries007.tfc.common.component.food.IFood.TRANSIENT_NEVER_DECAY_FLAG, net.dries007.tfc.common.component.food.FoodCapability.get(provider.stack()).getCreationDate());
    }

    @Test
    public void templateOutputsPreserveTheNativeNetworkBytes()
    {
        final var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        final var oldBytes = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries, ConnectionType.NEOFORGE);
        final var newBytes = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries, ConnectionType.NEOFORGE);
        try
        {
            final var item = new ItemStack(Items.DIAMOND, 3);
            ItemStack.STREAM_CODEC.encode(oldBytes, item);
            ItemOutput.STREAM_CODEC.encode(newBytes, ItemOutput.of(item));
            assertEquals(oldBytes, newBytes);
            assertTrue(ItemStack.matches(item, ItemOutput.STREAM_CODEC.decode(newBytes).copy()));
            oldBytes.clear(); newBytes.clear();
            final var fluid = new FluidStack(Fluids.WATER, 750);
            FluidStack.STREAM_CODEC.encode(oldBytes, fluid);
            FluidOutput.STREAM_CODEC.encode(newBytes, FluidOutput.of(fluid));
            assertEquals(oldBytes, newBytes);
            assertEquals(750, FluidOutput.STREAM_CODEC.decode(newBytes).copy().getAmount());
        }
        finally { oldBytes.release(); newBytes.release(); }
    }

    @Test
    public void noLootIsAllowedButEveryDeclaredLootTableMustExist()
    {
        assertTrue(SelfTests.findMissingBlockLootTables(List.of(), List.of(Blocks.WATER)).isEmpty());
        assertEquals(List.of(Blocks.STONE), SelfTests.findMissingBlockLootTables(List.of(), List.of(Blocks.STONE)));
        assertTrue(SelfTests.findMissingBlockLootTables(List.of(Blocks.STONE.getLootTable().orElseThrow().identifier()), List.of(Blocks.STONE)).isEmpty());
    }
}
