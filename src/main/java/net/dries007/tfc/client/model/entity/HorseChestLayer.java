/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import java.util.HashMap;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.render.entity.state.TFCChestedHorseRenderState;

public class HorseChestLayer extends RenderLayer<TFCChestedHorseRenderState, TFCChestedHorseModel>
{
    private static final Map<Item, Identifier> MAP = new HashMap<>();
    private static final Identifier DEFAULT_CHEST_TEXTURE = Identifier.fromNamespaceAndPath("tfc", "textures/entity/chest/horse/oak.png");

    public static void registerChest(Item item, Identifier location) { MAP.put(item, location); }

    public static @Nullable Identifier textureFor(ItemStack stack)
    {
        return stack.isEmpty() ? null : MAP.getOrDefault(stack.getItem(), DEFAULT_CHEST_TEXTURE);
    }

    private final TFCChestedHorseModel model;

    public HorseChestLayer(RenderLayerParent<TFCChestedHorseRenderState, TFCChestedHorseModel> parent, TFCChestedHorseModel model)
    {
        super(parent);
        this.model = model;
    }

    @Override
    public void submit(PoseStack poses, SubmitNodeCollector collector, int light, TFCChestedHorseRenderState state, float yaw, float pitch)
    {
        if (state.chestTexture != null && state.hasChest)
        {
            collector.order(1).submitModel(model, state, poses, state.chestTexture, light,
                OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }
}
