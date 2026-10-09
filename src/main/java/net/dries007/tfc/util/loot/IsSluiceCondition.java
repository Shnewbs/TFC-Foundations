/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.Contract;

public enum IsSluiceCondition implements LootItemCondition
{
    INSTANCE;

    public static final MapCodec<IsSluiceCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<IsSluiceCondition> codec()
    {
        return CODEC;
    }

    @Override
    public boolean test(LootContext lootContext)
    {
        return lootContext.hasParameter(TFCLoot.SLUICE);
    }

    @Contract(pure = true)
    public static LootItemCondition.Builder isSluice()
    {
        return () -> INSTANCE;
    }
}
