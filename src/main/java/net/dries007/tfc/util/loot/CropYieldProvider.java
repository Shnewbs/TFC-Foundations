/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import net.dries007.tfc.common.blockentities.CropBlockEntity;

public class CropYieldProvider extends MinMaxProvider
{
    public static final MapCodec<CropYieldProvider> CODEC = MinMaxProvider.codec(CropYieldProvider::new);

    public CropYieldProvider(NumberProvider min, NumberProvider max)
    {
        super(min, max);
    }

    @Override
    public float getFloat(LootContext context)
    {
        final BlockEntity entity = context.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (entity instanceof CropBlockEntity crop)
        {
            return Mth.lerp(crop.getYield(), min.getFloat(context), max.getFloat(context));
        }
        return min.getFloat(context);
    }

    @Override
    public MapCodec<CropYieldProvider> codec()
    {
        return CODEC;
    }
}
