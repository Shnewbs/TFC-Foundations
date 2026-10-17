/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.util.registry.RegistrationContext;

@Mixin(Item.Properties.class)
public abstract class ItemPropertiesIdMixin
{
    @Inject(method = "effectiveDescriptionId", at = @At("HEAD"))
    private void bindDeferredKey(CallbackInfoReturnable<String> cir)
    {
        RegistrationContext.bind((Item.Properties) (Object) this);
    }
}
