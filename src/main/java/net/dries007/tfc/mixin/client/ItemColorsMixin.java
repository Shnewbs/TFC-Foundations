/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.client;

import java.util.List;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.client.ItemRenderTint;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.mixin.client.accessor.ItemStackRenderStateAccessor;

/** Native item states carry the tint for all renderers; there is no separate ItemColors/Sodium lookup. */
@Mixin(CuboidItemModelWrapper.class)
public abstract class ItemColorsMixin
{
    @Shadow @Final private List<ItemTintSource> tints;

    @Inject(method = "update", at = @At("RETURN"))
    private void tintUncoloredRottenFood(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
        ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed, CallbackInfo ci)
    {
        if (tints.isEmpty() && FoodCapability.isRotten(stack))
        {
            final ItemStackRenderStateAccessor access = (ItemStackRenderStateAccessor) state;
            final int color = TFCConfig.CLIENT.foodExpiryOverlayColor.get();
            ItemRenderTint.tintLastUncoloredLayer(state, access.tfc$layers(), access.tfc$activeLayerCount(), color);
        }
    }
}
