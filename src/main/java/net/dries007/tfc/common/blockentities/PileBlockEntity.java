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
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.GroundcoverBlockType;
import net.dries007.tfc.common.blocks.ISpecialPile;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.NbtHelpers;

public class PileBlockEntity extends TFCBlockEntity
{
    private BlockState internalState;
    @Nullable private BlockState aboveState;

    public PileBlockEntity(BlockPos pos, BlockState state)
    {
        super(TFCBlockEntities.PILE.get(), pos, state);

        internalState = Blocks.AIR.defaultBlockState();
        aboveState = null;
    }

    public void setHiddenStates(BlockState internalState, @Nullable BlockState aboveState, boolean byPlayer)
    {
        if (Helpers.isBlock(internalState, TFCTags.Blocks.CONVERTS_TO_HUMUS) && !byPlayer)
        {
            this.internalState = TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.HUMUS).get().defaultBlockState();
        }
        else if (internalState.getBlock() instanceof ISpecialPile special)
        {
            this.internalState = special.getHiddenState(internalState, byPlayer);
        }
        else
        {
            this.internalState = internalState;
        }

        if (aboveState != null && aboveState.getBlock() instanceof ISpecialPile special)
        {
            this.aboveState = special.getHiddenStateAbove(aboveState, byPlayer);
        }
        else
        {
            this.aboveState = aboveState;
        }
    }

    public BlockState getInternalState()
    {
        return internalState;
    }

    @Nullable
    public BlockState getAboveState()
    {
        return aboveState;
    }

    @Override
    protected void loadAdditional(ValueInput tag)
    {
        internalState = NbtUtils.readBlockState(tag.lookup().lookupOrThrow(Registries.BLOCK), tag.read("internalState", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        aboveState = NbtHelpers.hasTag(tag, "aboveState", Tag.TAG_COMPOUND) ? NbtUtils.readBlockState(tag.lookup().lookupOrThrow(Registries.BLOCK), tag.read("aboveState", CompoundTag.CODEC).orElseGet(CompoundTag::new)) : null;
        super.loadAdditional(tag);
    }

    @Override
    protected void saveAdditional(ValueOutput tag)
    {
        tag.store("internalState", CompoundTag.CODEC, NbtUtils.writeBlockState(internalState));
        if (aboveState != null)
        {
            tag.store("aboveState", CompoundTag.CODEC, NbtUtils.writeBlockState(aboveState));
        }
        super.saveAdditional(tag);
    }
}
