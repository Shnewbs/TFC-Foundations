/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.function.BiFunction;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.heat.Heat;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.items.TFCFishingRodItem;
import net.dries007.tfc.util.Helpers;

/** Native numeric properties retain the original threshold and owner semantics. */
public enum TFCItemProperties implements RangeSelectItemModelProperty
{
    CAST((stack, entity) -> entity instanceof Player player && TFCFishingRodItem.isThisTheHeldRod(player, stack) && player.fishing != null ? 1f : 0f),
    BLOCKING((stack, entity) -> entity instanceof Player && entity.isUsingItem() && entity.getUseItem() == stack ? 1f : 0f),
    THROWING((stack, entity) -> entity != null && ((entity.isUsingItem() && entity.getUseItem() == stack) || (entity instanceof Monster monster && monster.isAggressive())) ? 1f : 0f),
    DAMAGED((stack, entity) -> stack.getDamageValue() > stack.getMaxDamage() - 10 ? 1f : 0f),
    SEALED((stack, entity) -> stack.has(TFCComponents.CONTENTS) || (stack.has(TFCComponents.BARREL) && !stack.get(TFCComponents.BARREL).isEmpty()) ? 1f : 0f),
    HEAT((stack, entity) -> Mth.clamp(HeatCapability.getTemperature(stack) / Heat.maxVisibleTemperature(), 0, 1));

    private final BiFunction<ItemStack, @Nullable LivingEntity, Float> getter;
    private final MapCodec<TFCItemProperties> codec;

    TFCItemProperties(BiFunction<ItemStack, @Nullable LivingEntity, Float> getter)
    {
        this.getter = getter;
        this.codec = MapCodec.unit(this);
    }

    @Override public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed)
    {
        return getter.apply(stack, owner == null ? null : owner.asLivingEntity());
    }

    @Override public MapCodec<TFCItemProperties> type() { return codec; }

    public static void register(RegisterRangeSelectItemModelPropertyEvent event)
    {
        for (TFCItemProperties property : values()) event.register(Helpers.identifier(property.name().toLowerCase(java.util.Locale.ROOT)), property.codec);
    }
}
