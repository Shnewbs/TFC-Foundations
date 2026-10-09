/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.recipes.ScrapingRecipe;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.NbtHelpers;

import static net.dries007.tfc.TerraFirmaCraft.*;

public class ScrapingBlockEntity extends InventoryBlockEntity<ItemStackHandler>
{
    @Nullable private Identifier inputTexture = null;
    @Nullable private Identifier outputTexture = null;
    private short positions = 0; // essentially a boolean[16]
    @Nullable private DyeColor color1 = null;
    @Nullable private DyeColor color2 = null;

    public ScrapingBlockEntity(BlockPos pos, BlockState state)
    {
        super(TFCBlockEntities.SCRAPING.get(), pos, state, defaultInventory(1));
    }

    @Override
    public ModelData getModelData()
    {
        return super.getModelData().derive().with(BlockEntityModelData.SCRAPING,
            new BlockEntityModelData.Scraping(inputTexture, outputTexture, positions, getColor1(), getColor2())).build();
    }

    public boolean isComplete()
    {
        return positions == -1;
    }

    public short getScrapedPositions()
    {
        return positions;
    }

    public void onClicked(float hitX, float hitZ)
    {
        int xPos = (int) (hitX * 4);
        int zPos = (int) (hitZ * 4);
        positions |= 1 << (xPos + zPos * 4);

        assert level != null;
        if (!level.isClientSide())
        {
            if (isComplete())
            {
                final ItemStack currentItem = inventory.getStackInSlot(0);
                final ScrapingRecipe recipe = ScrapingRecipe.getRecipe(currentItem);
                if (recipe != null)
                {
                    final ItemStack extraDrop = recipe.getExtraDrop().getSingleStack(currentItem);
                    if (!extraDrop.isEmpty())
                    {
                        Helpers.spawnItem(level, worldPosition, extraDrop);
                    }
                    inventory.setStackInSlot(0, recipe.assemble(currentItem));
                }
            }
            markForSync();
        }
        // The existing notification dirties the mesh; refresh its snapshot first.
        if (level.isClientSide()) requestModelDataUpdate();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public boolean dye(DyeColor color)
    {
        if (color1 == null)
        {
            color1 = color;
            BlockEntityModelData.refresh(this);
            markForSync();
            return true;
        }
        else if (color2 == null)
        {
            color2 = color;
            BlockEntityModelData.refresh(this);
            markForSync();
            return true;
        }
        return false;
    }

    public int getColor1()
    {
        return color1 != null ? color1.getTextureDiffuseColor() : -1;
    }

    public int getColor2()
    {
        return color2 != null ? color2.getTextureDiffuseColor() : -1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return ScrapingRecipe.getRecipe(stack) != null;
    }

    @Override
    public void loadAdditional(ValueInput nbt)
    {
        super.loadAdditional(nbt);
        positions = (short) nbt.getShortOr("positions", (short) 0);
        inputTexture = NbtHelpers.hasTag(nbt, "inputTexture", Tag.TAG_STRING) ? Helpers.resourceLocation(nbt.getStringOr("inputTexture", "")) : null;
        outputTexture = NbtHelpers.hasTag(nbt, "outputTexture", Tag.TAG_STRING) ? Helpers.resourceLocation(nbt.getStringOr("outputTexture", "")) : null;
        color1 = NbtHelpers.hasTag(nbt, "color1", Tag.TAG_INT) ? DyeColor.byId(nbt.getIntOr("color1", 0)) : null;
        color2 = NbtHelpers.hasTag(nbt, "color2", Tag.TAG_INT) ? DyeColor.byId(nbt.getIntOr("color2", 0)) : null;
        BlockEntityModelData.refresh(this);
    }

    @Override
    public void saveAdditional(ValueOutput nbt)
    {
        nbt.putShort("positions", positions);
        if (inputTexture != null) nbt.putString("inputTexture", inputTexture.toString());
        if (outputTexture != null) nbt.putString("outputTexture", outputTexture.toString());
        if (color1 != null) nbt.putInt("color1", color1.getId());
        if (color2 != null) nbt.putInt("color2", color2.getId());
        super.saveAdditional(nbt);
    }

    @Nullable
    public Identifier getInputTexture()
    {
        return inputTexture;
    }

    @Nullable
    public Identifier getOutputTexture()
    {
        return outputTexture;
    }

    public void updateDisplayCache()
    {
        if (!isComplete())
        {
            final ItemStack stack = inventory.getStackInSlot(0);
            final ScrapingRecipe recipe = ScrapingRecipe.getRecipe(stack);
            inputTexture = recipe == null ? null : recipe.getInputTexture();
            outputTexture = recipe == null ? null : recipe.getOutputTexture();
            BlockEntityModelData.refresh(this);
        }
    }
}
