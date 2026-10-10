/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.items;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ShieldItem;

import net.dries007.tfc.common.LevelTier;

public class TFCShieldItem extends ShieldItem
{
    private final LevelTier tier;

    public TFCShieldItem(LevelTier tier, Properties builder)
    {
        super(builder.durability(tier.getUses()).enchantable(tier.getEnchantmentValue()).repairable(tier.material().repairItems()));
        this.tier = tier;
    }

    public float getDamageBlocked()
    {
        return Mth.clampedMap(tier.getAttackDamageBonus(), 0f, 12f, 0.25f, 1f);
    }

    public LevelTier getTier()
    {
        return tier;
    }
}