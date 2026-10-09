/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.List;
import java.util.Random;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.client.overworld.ClientSolarCalculatorBridge;
import net.dries007.tfc.common.blocks.plant.BodyPlantBlock;
import net.dries007.tfc.common.blocks.plant.PlantBlock;
import net.dries007.tfc.common.blocks.plant.TopPlantBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.registry.RegistryPlant;

public final class PlantBlockModel extends SeasonalBlockStateModel
{
    public PlantBlockModel(List<BlockStateModelPart> parts)
    {
        super(parts, 6, 3);
    }

    @Override
    protected int select(@Nullable BlockState state, @Nullable BlockPos pos)
    {
        if (pos == null)
        {
            pos = BlockPos.ZERO;
        }
        if (state == null)
        {
            return getModelFromCalendar();
        }
        final Block block = state.getBlock();
        final RegistryPlant plant;
        switch (block)
        {
            case PlantBlock plantBlock -> plant = plantBlock.getPlant();
            case BodyPlantBlock bodyPlantBlock -> plant = bodyPlantBlock.getPlant();
            case TopPlantBlock topPlantBlock -> plant = topPlantBlock.getPlant();
            default ->
            {
                return getModelFromCalendar();
            }
        }
        float start = plant.getBloomOffset();
        final Random random = new Random();
        final Level level = ClientHelpers.getLevel();
        final BlockPos posXZ = new BlockPos(pos.getX(), 0, pos.getZ());
        final float randomScale;

        random.setSeed(Helpers.hash(836494186029734123L, posXZ));
        if (level == null) return getModelFromCalendar();
        if (plant.isWetSeasonBlooming())
        {
            final float rainVariance = Climate.getRainfallVariance(level, pos);
            randomScale = Mth.clampedMap(Math.abs(rainVariance), 0.0f, 0.3f, 0.5f, 0.03f);
            start = start + (rainVariance < 0f ? 1f : 1.5f);
        }
        else
        {
            randomScale = Mth.clampedMap(Climate.getAverageTemperature(level, pos), 16f, 26f, 0.03f, 0.5f);

            // This handles differences between hemispheres
            if (ClientHelpers.inNorthernHemisphere())
            {
                start = start + 1.5f;
            }
            else
            {
                start = start + 1f;
            }
        }
        start = (start + random.nextFloat(-randomScale, randomScale)) % 1;
        return getModelFromCalendar(start, start + plant.getBloomingEnd(), start + plant.getSeedingEnd(), start + plant.getDyingEnd(),
            start + plant.getDormantEnd(), start + plant.getSproutingEnd(), plant.getStartTime(), plant.getEndTime(), randomScale > 0.25f);
    }

    private int getModelFromCalendar()
    {
        return getModelFromCalendar(0.4f, 0.6f, 0.75f, 0.9f, 1.1f, 1.25f, 0, 0, false);
    }

    private int getModelFromCalendar(float bloomingStart, float bloomingEnd, float seedingEnd, float dyingEnd, float dormantEnd, float sproutingEnd, int startTime, int endTime, boolean nonDormant)
    {
        final int stage = SeasonalModelMath.plantStage(Calendars.CLIENT.getCalendarFractionOfYear(), bloomingStart, bloomingEnd,
            seedingEnd, dyingEnd, dormantEnd, sproutingEnd, startTime, endTime, nonDormant, true);
        return stage == 3 && startTime != endTime ? getModelByDayTime(startTime, endTime) : stage;
    }

    private int getModelByDayTime(int startTime, int endTime)
    {
        final Level level = ClientHelpers.getLevel();
        if (level != null)
        {
            final long dayTime = ClientSolarCalculatorBridge.getDayTime(level) % ICalendar.CALENDAR_TICKS_IN_DAY;
            if (!SeasonalModelMath.isBloomingTime(startTime, endTime, dayTime))
            {
                return 2;
            }
        }
        return 3;
    }

    public static final class Loader implements UnbakedModelLoader<SeasonalUnbakedModel>
    {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public SeasonalUnbakedModel read(JsonObject json, JsonDeserializationContext context)
        {
            return new SeasonalUnbakedModel(json, context,
                List.of("dormant", "sprouting", "budding", "blooming", "seeding", "dying"), 3, PlantBlockModel::new);
        }
    }
}
