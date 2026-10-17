/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin;

import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.util.SelfTests;

/** Recipe decoding moved to the shared JSON scanner; retain self-test failures for all parse errors. */
@Mixin(SimpleJsonResourceReloadListener.class)
public abstract class JsonResourceReloadMixin
{
    @Inject(method = "*", at = {
        @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;[Ljava/lang/Object;)V", remap = false),
        @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false)
    })
    private static void setStateToErrored(CallbackInfo ci)
    {
        SelfTests.reportExternalError();
    }
}
