/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.dries007.tfc.common.blocks.BloomBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.Helpers;

public class BloomBlockEntity extends TFCBlockEntity
{
    public static final int TOTAL_LAYERS = 8;

    private ItemStack item;
    private int count;
    private int maxCount;

    public BloomBlockEntity(BlockPos pos, BlockState state)
    {
        super(TFCBlockEntities.BLOOM.get(), pos, state);
        item = ItemStack.EMPTY;
        count = 0;
    }

    @Override
    protected void saveAdditional(ValueOutput tag)
    {
        super.saveAdditional(tag);
        if (!item.isEmpty())
        {
            tag.store("item", ItemStack.OPTIONAL_CODEC, item);
        }
        tag.putInt("count", count);
        tag.putInt("maxCount", maxCount);
    }

    @Override
    protected void loadAdditional(ValueInput tag)
    {
        super.loadAdditional(tag);
        item = tag.read("item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        count = tag.getIntOr("count", 0);
        maxCount = tag.getIntOr("maxCount", 0);
    }

    public void setBloom(ItemStack item, int count)
    {
        if (count > 0)
        {
            assert level != null;
            this.item = item;
            this.count = count;
            this.maxCount = count;
            level.setBlockAndUpdate(worldPosition, getState());
        }
    }

    public boolean dropBloom()
    {
        assert level != null;

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos dropPos = worldPosition;
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            cursor.setWithOffset(worldPosition, direction);
            if (Helpers.isBlock(level.getBlockState(cursor), TFCBlocks.BLOOMERY.get()))
            {
                dropPos = cursor.immutable();
                break;
            }
        }

        count -= 1;
        ItemStack item = this.item.copy();
        item.setCount(1);
        Helpers.spawnItem(level, dropPos, item);
        return level.setBlock(worldPosition, getState(), level.isClientSide() ? 11 : 3);
    }

    public BlockState getState()
    {
        assert level != null;
        if (count <= 0)
        {
            return Blocks.AIR.defaultBlockState();
        }
        final int layers = maxCount <= TOTAL_LAYERS
            ? count // Must be in [1, TOTAL_LAYERS], so we use the count directly
            : Mth.clamp(TOTAL_LAYERS * count / maxCount, 1, TOTAL_LAYERS); // Otherwise, scale based on the max count to discrete layers

        return getBlockState().setValue(BloomBlock.LAYERS, layers);
    }

    public ItemStack getItem()
    {
        return item;
    }

    public int getCount()
    {
        return count;
    }
}
