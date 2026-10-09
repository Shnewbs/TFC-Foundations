/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.Contract;

import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;

public enum IsMaleCondition implements LootItemCondition
{
    INSTANCE;

    public static final MapCodec<IsMaleCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<IsMaleCondition> codec()
    {
        return CODEC;
    }

    @Override
    public boolean test(LootContext context)
    {
        return context.hasParameter(LootContextParams.THIS_ENTITY) && context.getParameter(LootContextParams.THIS_ENTITY) instanceof TFCAnimalProperties properties && properties.isMale();
    }

    @Contract(pure = true)
    public static LootItemCondition.Builder isMale()
    {
        return () -> INSTANCE;
    }
}
