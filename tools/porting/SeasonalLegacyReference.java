/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

/** Frozen scalar reference from source 5fbcf662. Game lookups are input parameters only. */
final class SeasonalLegacyReference
{
    static int plant(float timeOfYear, float bloomingStart, float bloomingEnd, float seedingEnd,
        float dyingEnd, float dormantEnd, float sproutingEnd, int startTime, int endTime, boolean nonDormant, int dayStage)
    {
        final float adjustedTimeOfYear = timeOfYear < bloomingStart ? timeOfYear + 1f : timeOfYear;

        if (adjustedTimeOfYear < bloomingEnd)
        {
            if (startTime != endTime)
            {
                return dayStage;
            }
            return 3;
        }
        else if (adjustedTimeOfYear < seedingEnd)
        {
            return 4;
        }
        else if (adjustedTimeOfYear < dyingEnd)
        {
            if (nonDormant)
            {
                return 2;
            }
            return 5;
        }
        if (adjustedTimeOfYear < dormantEnd)
        {
            if (nonDormant)
            {
                if (startTime != endTime)
                {
                    return dayStage;
                }
                return 3;
            }
            return 0;
        }
        else if (adjustedTimeOfYear < sproutingEnd)
        {
            if (nonDormant)
            {
                return 4;
            }
            return 1;
        }
        else
        {
            return 2;
        }
    }
    static int leaves(boolean isConifer, float flowerOffset, float temp, float rainVar, float avgRain,
        float timeOfYear, boolean inNorthernHemisphere, int positionClimateHash, int positionDeltaHash)
    {
        if ((temp > 11.7 && temp < 12.8) || (rainVar > 0.38 && rainVar < 0.42))
        {
            temp += (float) (positionClimateHash - 63) / 4_000f;
            rainVar += (float) (positionClimateHash - 63) / 60_000f;
        }
        final float rainVarAbs = Math.abs(rainVar);

        // Since even trees that do not change foliage color may have a flowering phase,
        // we do need to check time of year earlier than we do for foliage colors

        // See Desmos: https://www.desmos.com/calculator/jw5zkjxtnz
        final float x;
        final boolean inEvergreenClimate;
        float seasonOffset = 0;
        if (temp <= 12f)
        {
            // Numbers chosen to create a 2.5-month summer at -20c avg, and a 12-month "summer" at 15c avg
            x = 1.25f * Math.max(temp, -20f) + 7.6f;
            inEvergreenClimate = false;
            if (!inNorthernHemisphere)
            {
                seasonOffset = 0.5f;
            }
        }
        else
        {
            // For dry-season controlled climates, the minimum rain must be below 120
            final float minRain = avgRain * (1 - rainVarAbs);

            // Small gap in temperature is so that there are small evergreen bands between dry-season controlled areas and winter-controlled areas
            if (rainVarAbs > 0.4 && temp > 12.5f && minRain <= 120)
            {
                if (rainVar < 0)
                {
                    seasonOffset = 0.5f;
                }
                // Numbers chosen to create a 4-month wet season at max rain var & min rain = 0, and a 12-month "wet season" at minimum rain var & min rain = 120
                // Uses multiple variables to ensure smooth transitions, and that biomes that have green grass year-round do not lose leaves
                x = -.2604f * (0.4f - rainVarAbs) * (120f - minRain) + 18.75f + 5.3f;
                inEvergreenClimate = false;
            }
            // If not in any of the above areas, must be in an evergreen area
            else
            {
                if (!inNorthernHemisphere)
                {
                    seasonOffset = 0.5f;
                }
                x = 24.05f;
                inEvergreenClimate = true;
            }

        }

        final float cubedTerm = x * x * x / 4096; // 1 / 16^3
        final float squaredTerm = x * x / 256; // 1 / 16^2

        // Offset the seasons by six months if in southern hemisphere, or if dry season is in the summer
        // Positional hashing to fuzz the time of year per-block
        timeOfYear = (1 + timeOfYear + seasonOffset + ((positionDeltaHash - 63) / 4096f)) % 1;

        final float autumnEnd = (cubedTerm - squaredTerm + 10.5f) / 12f;
        final float springStart = 1f - autumnEnd;
        final float warmSeasonLength = autumnEnd - springStart;
        final float bloomStart = springStart + flowerOffset * warmSeasonLength;

        if (timeOfYear > bloomStart)
        {
            final float bloomEnd = bloomStart + Math.min(0.167f * warmSeasonLength, 0.125f);
            if (timeOfYear < bloomEnd)
            {
                return 3;
            }
        }

        // Now that we've checked it isn't blooming, skip calcs if in an evergreen climate
        if (inEvergreenClimate || isConifer)
        {
            return 0;
        }

        if (timeOfYear > autumnEnd)
        {
            return 2;
        }
        final float autumnStart = (cubedTerm - squaredTerm + 8.5f) / 12f;
        final float autumnMid = 0.5f * (autumnEnd + autumnStart);
        if (timeOfYear > autumnMid)
        {
            return 1;
        }
        final float springMid = 1f - autumnMid;
        if (timeOfYear > springMid)
        {
            return 0;
        }
        if (timeOfYear > springStart)
        {
            return 1;
        }
        return 2;
    }
}
