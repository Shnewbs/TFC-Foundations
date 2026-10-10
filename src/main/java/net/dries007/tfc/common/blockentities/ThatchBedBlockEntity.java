/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

import net.dries007.tfc.util.Helpers;

import static net.dries007.tfc.TerraFirmaCraft.*;

public class ThatchBedBlockEntity extends InventoryBlockEntity<ItemStackHandler>
{
    private BlockState headState;
    private BlockState footState;

    public ThatchBedBlockEntity(BlockPos pos, BlockState state)
    {
        super(TFCBlockEntities.THATCH_BED.get(), pos, state, defaultInventory(1));
        headState = footState = Blocks.AIR.defaultBlockState();
    }

    public void setBed(BlockState head, BlockState foot, ItemStack top)
    {
        assert level != null;
        headState = head;
        footState = foot;
        inventory.setStackInSlot(0, top);
    }

    public void destroyBed()
    {
        ejectInventory();
        if (level instanceof ServerLevel serverLevel)
        {
            Helpers.dropWithContext(serverLevel, headState, worldPosition, ctx -> {}, true);
            Helpers.dropWithContext(serverLevel, footState, worldPosition, ctx -> {}, true);
        }
    }

    /**
     * Called before the chunk removes its old bed block entity. This is also
     * triggered when the other bed half is broken and vanilla removes the head.
     * The old five-argument block onRemove hook no longer exists in 26.1.2.
     */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState previousState)
    {
        if (previousState.hasProperty(BedBlock.PART) && previousState.getValue(BedBlock.PART) == BedPart.HEAD)
        {
            destroyBed();
        }
        super.preRemoveSideEffects(pos, previousState);
    }

    @Override
    public void saveAdditional(ValueOutput tag)
    {
        tag.store("HeadBlockState", CompoundTag.CODEC, NbtUtils.writeBlockState(headState));
        tag.store("FootBlockState", CompoundTag.CODEC, NbtUtils.writeBlockState(footState));
        super.saveAdditional(tag);
    }

    @Override
    public void loadAdditional(ValueInput tag)
    {
        headState = NbtUtils.readBlockState(tag.lookup().lookupOrThrow(Registries.BLOCK), tag.read("HeadBlockState", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        footState = NbtUtils.readBlockState(tag.lookup().lookupOrThrow(Registries.BLOCK), tag.read("FootBlockState", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        super.loadAdditional(tag);
    }
}
