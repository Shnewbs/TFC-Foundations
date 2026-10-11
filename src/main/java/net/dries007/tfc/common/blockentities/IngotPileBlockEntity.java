/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.model.data.ModelData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.devices.IngotPileBlock;
import net.dries007.tfc.util.MetalItem;
import net.dries007.tfc.util.ValueIoHelpers;


// TODO: Log and ingot piles should share a unified "pile" block entity, since at this point the only differences are the model, and the stacking behavior
//  and ingots piles could benefit from using the log stacking behavior. However, this change would break existing ingot piles, unless we take additional
//  steps to avoid that, so let's remember this for porting time
public class IngotPileBlockEntity extends TFCBlockEntity
{
    private final List<Entry> entries;

    public IngotPileBlockEntity(BlockPos pos, BlockState state)
    {
        super(TFCBlockEntities.INGOT_PILE.get(), pos, state);

        entries = new ArrayList<>();
    }

    @Override
    public ModelData getModelData()
    {
        final List<Identifier> textures = new ArrayList<>(Math.min(entries.size(), 64));
        for (int i = 0; i < Math.min(entries.size(), 64); i++) textures.add(getOrCacheMetal(i).softTextureId());
        return super.getModelData().derive().with(BlockEntityModelData.PILE,
            new BlockEntityModelData.Pile(textures, MetalItem.unknown().softTextureId())).build();
    }

    public void addIngot(ItemStack stack)
    {
        entries.add(new Entry(stack));
        BlockEntityModelData.refresh(this);
        markForSync();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        // Preserve the old pile-drop behavior before the block entity disappears.
        // Creative-mode removal already voids the pile contents before this hook.
        if (level != null && !level.isClientSide() && state.getBlock() instanceof IngotPileBlock)
        {
            removeAllIngots(stack -> Block.popResource(level, pos, stack));
        }
        super.preRemoveSideEffects(pos, state);
    }

    public void removeAllIngots(Consumer<ItemStack> ingotConsumer)
    {
        for (Entry entry : this.entries)
        {
            ingotConsumer.accept(entry.stack);
        }
        this.entries.clear();
        BlockEntityModelData.refresh(this);
        markForSync();
    }

    public ItemStack removeIngot()
    {
        if (!entries.isEmpty())
        {
            final Entry entry = entries.remove(entries.size() - 1);
            BlockEntityModelData.refresh(this);
            markForSync();
            return entry.stack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns a cached metal for the given side, if present, otherwise grabs from the cache.
     * The metal is defined by checking what metal the stack would melt into if heated.
     * Any other items turn into {@link MetalItem#unknown()}.
     */
    public MetalItem getOrCacheMetal(int index)
    {
        if (index >= entries.size())
        {
            return MetalItem.unknown();
        }

        final Entry entry;
        try
        {
            entry = entries.get(index);
        }
        catch (IndexOutOfBoundsException e)
        {
            // This is terrible, but it's a threadsafety issue. `entries` might be updated between the bounds check above, and this query
            return MetalItem.unknown();
        }

        if (entry.metal == null)
        {
            entry.metal = MetalItem.getOrUnknown(entry.stack);
        }
        return entry.metal;
    }

    @Override
    protected void saveAdditional(ValueOutput tag)
    {
        final ValueOutput.TypedOutputList<ItemStack> stacks = tag.list("stacks", ItemStack.OPTIONAL_CODEC);
        for (final Entry entry : entries)
        {
            stacks.add(entry.stack);
        }
        super.saveAdditional(tag);
    }

    @Override
    protected void loadAdditional(ValueInput tag)
    {
        entries.clear();
        for (ItemStack stack : ValueIoHelpers.readItemStacks(tag, "stacks"))
        {
            entries.add(new Entry(stack));
        }
        super.loadAdditional(tag);
        BlockEntityModelData.refresh(this);
    }

    public void fillTooltip(Consumer<Component> tooltip)
    {
        class Counter
        {
            final ItemStack stack;
            int count = 0;

            Counter(ItemStack stack) {this.stack = stack;}
        }

        final Map<MetalItem, Counter> counts = new LinkedHashMap<>(); // Deterministic iteration order
        for (Entry entry : entries)
        {
            if (entry.metal != null)
            {
                counts.compute(entry.metal, (key, old) -> {
                    if (old == null) old = new Counter(entry.stack);
                    old.count++;
                    return old;
                });
            }
        }
        for (Counter value : counts.values())
        {
            tooltip.accept(Component.literal(value.count + "x ").append(value.stack.getHoverName()));
        }
    }

    public ItemStack getPickedItemStack()
    {
        return entries.isEmpty() ? ItemStack.EMPTY : entries.get(0).stack.copy();
    }

    static class Entry
    {
        final ItemStack stack;
        @Nullable MetalItem metal;

        Entry(ItemStack stack)
        {
            this.stack = stack;
        }
    }
}
