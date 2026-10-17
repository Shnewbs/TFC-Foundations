/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.capabilities;

import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.transfer.BlockFluidResourceHandler;
import net.dries007.tfc.common.capabilities.transfer.BlockItemResourceHandler;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blockentities.BlastFurnaceBlockEntity;
import net.dries007.tfc.common.blockentities.CrucibleBlockEntity;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blockentities.LampBlockEntity;
import net.dries007.tfc.common.blockentities.MoldTableBlockEntity;
import net.dries007.tfc.common.blockentities.PotBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.dries007.tfc.common.component.heat.IHeatConsumer;
import net.dries007.tfc.util.Helpers;

public final class BlockCapabilities
{
    public static final BlockCapability<ResourceHandler<ItemResource>, @Nullable Direction> ITEM = Capabilities.Item.BLOCK;
    public static final BlockCapability<ResourceHandler<FluidResource>, @Nullable Direction> FLUID = Capabilities.Fluid.BLOCK;

    /**
     * A simple capability exposed on blocks which allow them to receive heat.
     */
    public static final BlockCapability<IHeatConsumer, @Nullable Direction> HEAT = BlockCapability.create(Helpers.identifier("heat"), IHeatConsumer.class, Direction.class);

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(ITEM, TFCBlockEntities.ANVIL.get(), (object, context) -> new BlockItemResourceHandler(object.getInventory(), stack -> {
            net.dries007.tfc.common.component.forge.ForgingCapability.clearRecipeIfNotWorked(stack);
            return stack;
        }));
        registerInventory(event, TFCBlockEntities.BARREL);
        event.registerBlockEntity(FLUID, TFCBlockEntities.BARREL.get(), (object, context) -> fluids(object.getSidedFluidInventory(context)));
        registerInventory(event, TFCBlockEntities.BLAST_FURNACE);
        event.registerBlockEntity(FLUID, TFCBlockEntities.BLAST_FURNACE.get(), (object, context) -> fluids(object.getSidedFluidInventory(context)));
        registerInventory(event, TFCBlockEntities.BOWL);
        registerInventory(event, TFCBlockEntities.CHARCOAL_FORGE);
        registerInventory(event, TFCBlockEntities.COMPOSTER);
        registerInventory(event, TFCBlockEntities.CRATE);
        registerInventory(event, TFCBlockEntities.CRUCIBLE);
        registerInventory(event, TFCBlockEntities.FIREBOX);
        event.registerBlockEntity(FLUID, TFCBlockEntities.CRUCIBLE.get(), (object, context) -> fluids(object.getSidedFluidInventory(context)));
        event.registerBlockEntity(HEAT, TFCBlockEntities.CRUCIBLE.get(), (object, context) -> object.getInventory());
        registerInventory(event, TFCBlockEntities.FIREPIT);
        registerInventory(event, TFCBlockEntities.GRILL);
        event.registerBlockEntity(FLUID, TFCBlockEntities.LAMP.get(), (object, context) -> fluids(object.getSidedFluidInventory(context)));
        registerInventory(event, TFCBlockEntities.LARGE_VESSEL);
        registerInventory(event, TFCBlockEntities.LOBSTER_TRAP);
        registerInventory(event, TFCBlockEntities.LOOM);
        registerInventory(event, TFCBlockEntities.POWER_LOOM);
        registerInventory(event, TFCBlockEntities.NEST_BOX);
        registerInventory(event, TFCBlockEntities.POT);
        event.registerBlockEntity(FLUID, TFCBlockEntities.POT.get(), (object, context) -> fluids(object.getSidedFluidInventory(context)));
        registerInventory(event, TFCBlockEntities.POWDERKEG);
        registerInventory(event, TFCBlockEntities.QUERN);
        event.registerBlockEntity(ITEM, TFCBlockEntities.CHEST.get(), TFCChestBlockEntity::getResourceHandler);
        event.registerBlockEntity(ITEM, TFCBlockEntities.TRAPPED_CHEST.get(), TFCChestBlockEntity::getResourceHandler);
        registerInventory(event, TFCBlockEntities.MOLD_TABLE);
        event.registerBlockEntity(FLUID, TFCBlockEntities.MOLD_TABLE.get(), (object, context) -> {
            final IFluidHandler handler = object.getSidedFluidInventory(context);
            return handler == null ? null : new BlockFluidResourceHandler(handler, object.getInventory()::prepareFluidMutation);
        });
    }

    private static void registerInventory(RegisterCapabilitiesEvent event, Supplier<? extends BlockEntityType<? extends InventoryBlockEntity<?>>> type)
    {
        event.registerBlockEntity(ITEM, type.get(), (object, context) -> items(object.getSidedInventory(context)));
    }
    private static @Nullable ResourceHandler<ItemResource> items(@Nullable IItemHandler handler)
    {
        return handler == null ? null : new BlockItemResourceHandler(handler);
    }

    private static @Nullable ResourceHandler<FluidResource> fluids(@Nullable IFluidHandler handler)
    {
        return handler == null ? null : new BlockFluidResourceHandler(handler);
    }

}
