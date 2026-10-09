/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.List;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.client.ClimateRenderCache;
import net.dries007.tfc.client.overworld.SolarCalculator;
import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

public final class LeavesBlockModel extends SeasonalBlockStateModel
{
    public LeavesBlockModel(List<BlockStateModelPart> parts)
    {
        super(parts, 4, 3);
    }

    @Override
    protected int select(@Nullable BlockState state, @Nullable BlockPos pos)
    {
        // Fast graphics should disable this behavior entirely
        if (!ClientHelpers.useFancyGraphics())
        {
            return 0;
        }

        final float flowerOffset;
        final boolean isConifer;
        // Checks whether the tree species has a flowering stage
        if (pos == null)
        {
            pos = BlockPos.ZERO;
        }
        // Default to using same texture all year round (evergreen behavior)
        if (state == null)
        {
            return 0;
        }
        else
        {
            final Block block = state.getBlock();
            if (block instanceof TFCLeavesBlock)
            {
                isConifer = ((TFCLeavesBlock) block).isConifer();
                flowerOffset = ((TFCLeavesBlock) block).getFlowerOffset();
            }
            else
            {
                return 0;
            }
        }

        final Level level = ClientHelpers.getLevel();
        if (level == null)
        {
            return 0;
        }

        // Calculates the seasons based on average temperature
        // Should match the method used in TFCColors

        // Hash climate values based on block positions. This helps with transitional areas
        final BlockPos seaLevelPos = new BlockPos(pos.getX(), SEA_LEVEL_Y, pos.getZ());
        final float temperature = Climate.getAverageTemperature(level, seaLevelPos);
        final float rainfallVariance = Climate.getRainfallVariance(level, pos);
        final int climateHash = Helpers.hash(912381187503828153L, pos) & 127;
        final float averageRainfall = SeasonalModelMath.needsAverageRainfall(temperature, rainfallVariance, climateHash)
            ? Climate.getAverageRainfall(level, seaLevelPos) : 0;
        return SeasonalModelMath.leavesStage(isConifer, flowerOffset, temperature, rainfallVariance,
            averageRainfall, Calendars.CLIENT.getCalendarFractionOfYear(),
            SolarCalculator.getInNorthernHemisphere(pos.getZ(), ClimateRenderCache.INSTANCE.getHemisphereScale()),
            climateHash, Helpers.hash(836494187578334123L, pos) & 127);
    }

    public static final class Loader implements UnbakedModelLoader<SeasonalUnbakedModel>
    {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public SeasonalUnbakedModel read(JsonObject json, JsonDeserializationContext context)
        {
            return new SeasonalUnbakedModel(json, context,
                List.of("dense_leaves", "sparse_leaves", "bare", "blooming"), 0, LeavesBlockModel::new);
        }
    }
}
