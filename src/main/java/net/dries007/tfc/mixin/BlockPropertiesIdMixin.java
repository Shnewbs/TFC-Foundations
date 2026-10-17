/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin;

import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.util.registry.RegistrationContext;

@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesIdMixin
{
    @Inject(method = "effectiveDrops", at = @At("HEAD"))
    private void bindDeferredKey(CallbackInfoReturnable<Optional<ResourceKey<LootTable>>> cir)
    {
        RegistrationContext.bind((BlockBehaviour.Properties) (Object) this);
    }
}
