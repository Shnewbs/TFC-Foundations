/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.dries007.tfc.util.NbtHelpers;
import net.dries007.tfc.util.calendar.Calendars;

public class TickingPlantBlockEntity extends TickCounterBlockEntity
{
    public static void reset(Level level, BlockPos pos)
    {
        level.getBlockEntity(pos, TFCBlockEntities.TICK_COUNTING_PLANT.get()).ifPresent(TickCounterBlockEntity::resetCounter);
    }

    public static void addTicks(Level level, BlockPos pos, long ticks)
    {
        level.getBlockEntity(pos, TFCBlockEntities.TICK_COUNTING_PLANT.get()).ifPresent(entity -> entity.increaseCounter(ticks));
    }

    public static void setStemPos(Level level, BlockPos pos, BlockPos stemPos)
    {
        level.getBlockEntity(pos, TFCBlockEntities.TICK_COUNTING_PLANT.get()).ifPresent(entity -> entity.setStemPos(stemPos));
    }

    protected BlockPos stemPos;
    protected long lastPickedTick = Integer.MIN_VALUE;

    public TickingPlantBlockEntity(BlockPos pos, BlockState state)
    {
        this(TFCBlockEntities.TICK_COUNTING_PLANT.get(), pos, state);
    }

    public TickingPlantBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        stemPos = pos;
        resetLastPickedCounter();
    }

    @Override
    public void loadAdditional(ValueInput nbt)
    {
        stemPos = NbtHelpers.hasTag(nbt, "stemPos", CompoundTag.TAG_LONG) ? BlockPos.of(nbt.getLongOr("stemPos", 0L)) : worldPosition;
        lastPickedTick = nbt.getLongOr("lastPickedTick", 0L);
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(ValueOutput nbt)
    {
        nbt.putLong("stemPos", stemPos.asLong());
        nbt.putLong("lastPickedTick", lastPickedTick);
        super.saveAdditional(nbt);
    }

    public void setStemPos(BlockPos stemPos)
    {
        this.stemPos = stemPos;
        setChanged();
    }

    public BlockPos getStemPos()
    {
        return stemPos;
    }

    public void setLastPickedTick(long tick)
    {
        lastPickedTick = tick;
        setChanged();
    }

    public long getLastPickedTick()
    {
        return lastPickedTick;
    }

    public void resetLastPickedCounter()
    {
        lastPickedTick = Calendars.SERVER.getTicks();
        setChanged();
    }
}
