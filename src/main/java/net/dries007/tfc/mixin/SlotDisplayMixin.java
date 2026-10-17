/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.component.food.FoodCapability;

/** Recipe previews never age; immutable templates and assembled gameplay results remain separate. */
@Mixin(SlotDisplay.ItemStackContentsFactory.class)
public abstract class SlotDisplayMixin
{
    @Inject(method = "forStack(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private void markDisplayFood(ItemStack stack, CallbackInfoReturnable<ItemStack> cir)
    {
        cir.setReturnValue(FoodCapability.setTransientNonDecaying(cir.getReturnValue().copy()));
    }
}
