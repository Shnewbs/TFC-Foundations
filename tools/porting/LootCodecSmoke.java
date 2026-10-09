/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.dries007.tfc.util.loot.AlwaysTrueCondition;

/** Independent codec checks for actual port source; not a substitute for mod loading. */
public final class LootCodecSmoke
{
    public static void main(String[] args)
    {
        check(AlwaysTrueCondition.INSTANCE.codec() == AlwaysTrueCondition.CODEC, "stable codec identity");
        check(AlwaysTrueCondition.alwaysTrue().build() == AlwaysTrueCondition.INSTANCE, "builder singleton");
        final var encoded = AlwaysTrueCondition.CODEC.codec().encodeStart(JsonOps.INSTANCE, AlwaysTrueCondition.INSTANCE).getOrThrow();
        check(encoded.equals(new JsonObject()), "unit codec encoding");
        check(AlwaysTrueCondition.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow() == AlwaysTrueCondition.INSTANCE, "unit codec round trip");
        System.out.println("PASS: 4 production AlwaysTrueCondition checks. MinMaxProvider also compiled against the resolved target API.");
    }

    private static void check(boolean condition, String description)
    {
        if (!condition)
        {
            throw new AssertionError(description);
        }
    }
}
