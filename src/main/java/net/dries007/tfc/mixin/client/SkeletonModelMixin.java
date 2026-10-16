/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.items.JavelinItem;

/** Arm poses live in extracted state; preserve the humanoid spear pose through the skeleton melee override. */
@Mixin(SkeletonModel.class)
public abstract class SkeletonModelMixin
{
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;)V", at = @At("HEAD"))
    private void poseJavelin(SkeletonRenderState state, CallbackInfo ci)
    {
        if (state.isAggressive && state.getMainHandItemStack().getItem() instanceof JavelinItem)
        {
            if (state.mainArm == HumanoidArm.RIGHT) state.rightArmPose = HumanoidModel.ArmPose.THROW_TRIDENT;
            else state.leftArmPose = HumanoidModel.ArmPose.THROW_TRIDENT;
        }
    }

    @ModifyExpressionValue(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;)V",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;isHoldingBow:Z"))
    private boolean preserveJavelinPose(boolean holdingBow, SkeletonRenderState state)
    {
        return holdingBow || state.getMainHandItemStack().getItem() instanceof JavelinItem;
    }
}
