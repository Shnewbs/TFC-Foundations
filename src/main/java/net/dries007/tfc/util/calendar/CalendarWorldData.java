/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util.calendar;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import static net.dries007.tfc.TerraFirmaCraft.*;

public class CalendarWorldData extends SavedData
{
    private static final String NAME = MOD_ID + "_calendar";
    private static final SavedDataType<CalendarWorldData> TYPE = new SavedDataType<>(
        Identifier.withDefaultNamespace(NAME), CalendarWorldData::new,
        CompoundTag.CODEC.xmap(CalendarWorldData::load, CalendarWorldData::save));

    public static CalendarWorldData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static CalendarWorldData load(CompoundTag nbt)
    {
        final CalendarWorldData data = new CalendarWorldData();
        data.calendar.read(nbt.getCompoundOrEmpty("calendar"));
        return data;
    }

    private final Calendar calendar;

    public CalendarWorldData()
    {
        this.calendar = new Calendar();
    }

    private CompoundTag save()
    {
        final CompoundTag nbt = new CompoundTag();
        nbt.put("calendar", Calendars.SERVER.write());
        return nbt;
    }

    /**
     * Since this updates every tick, and doesn't store a local copy always assume it needs saving to disk
     */
    @Override
    public boolean isDirty()
    {
        return true;
    }

    public Calendar getCalendar()
    {
        return calendar;
    }
}