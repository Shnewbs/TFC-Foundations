/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.player.IPlayerInfo;

/** Native consumption still supplies the original stack before it is consumed. */
@Mixin(FoodProperties.class)
public abstract class FoodPropertiesMixin
{
    @Inject(method = "onConsume", at = @At("HEAD"))
    private void eatWithMoreInfo(Level level, LivingEntity user, ItemStack food, Consumable consumable, CallbackInfo ci)
    {
        if (user instanceof Player player) IPlayerInfo.get(player).eat(food);
    }
}
