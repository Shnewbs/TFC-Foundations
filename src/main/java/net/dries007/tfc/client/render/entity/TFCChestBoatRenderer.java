/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;

import net.dries007.tfc.client.model.entity.BoatChestModel;
import net.dries007.tfc.client.render.entity.state.TFCBoatRenderState;
import net.dries007.tfc.common.entities.misc.TFCChestBoat;
import net.dries007.tfc.common.items.ChestBlockItem;

public class TFCChestBoatRenderer extends TFCBoatRenderer
{
    public static ModelLayerLocation chestBoatName(String name)
    {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath("tfc", "chest_boat/" + name), "main");
    }

    private static final Identifier DEFAULT_TEXTURE = Identifier.fromNamespaceAndPath("tfc", "textures/entity/chest_boat/oak.png");
    private final BoatChestModel chestModel;

    public TFCChestBoatRenderer(EntityRendererProvider.Context context, String name)
    {
        super(context, name);
        chestModel = new BoatChestModel(context.bakeLayer(chestBoatName(name)));
    }

    public TFCChestBoatRenderer(EntityRendererProvider.Context context, Pair<Identifier, EntityModel<BoatRenderState>> hull, BoatModel model)
    {
        super(context, hull);
        chestModel = new BoatChestModel(model.root());
    }

    @Override
    public void extractRenderState(AbstractBoat boat, BoatRenderState state, float partialTick)
    {
        super.extractRenderState(boat, state, partialTick);
        if (boat instanceof TFCChestBoat chest)
        {
            ((TFCBoatRenderState) state).chestTexture = getChestTexture(chest);
        }
    }

    @Override
    protected void submitTypeAdditions(BoatRenderState state, PoseStack poses, SubmitNodeCollector collector, int light)
    {
        super.submitTypeAdditions(state, poses, collector, light);
        final Identifier texture = ((TFCBoatRenderState) state).chestTexture;
        if (texture != null)
        {
            // The native base has already applied yaw, damage and bubble transforms.
            collector.submitModel(chestModel, state, poses, texture, light,
                OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }

    protected Identifier getChestTexture(TFCChestBoat chest)
    {
        final ItemStack stack = chest.getChestItem();
        return stack.getItem() instanceof ChestBlockItem item ? item.getBoatTexture() : DEFAULT_TEXTURE;
    }
}
