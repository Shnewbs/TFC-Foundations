/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;

import net.dries007.tfc.client.model.entity.HorseChestLayer;
import net.dries007.tfc.client.model.entity.TFCChestedHorseModel;
import net.dries007.tfc.client.render.entity.state.TFCChestedHorseRenderState;
import net.dries007.tfc.common.entities.livestock.horse.TFCChestedHorse;

public class TFCChestedHorseRenderer<T extends TFCChestedHorse> extends AbstractHorseRenderer<T, TFCChestedHorseRenderState, TFCChestedHorseModel>
{
    public static ModelLayerLocation saddleLayer(ModelLayerLocation layer)
    {
        return new ModelLayerLocation(layer.model().withSuffix("_saddle"), layer.layer());
    }

    private final Identifier texture;
    private final float size;

    public TFCChestedHorseRenderer(EntityRendererProvider.Context context, float scale, ModelLayerLocation layer, String name)
    {
        this(context, scale, layer, Identifier.withDefaultNamespace("textures/entity/horse/" + name + ".png"));
    }

    public TFCChestedHorseRenderer(EntityRendererProvider.Context context, float scale, ModelLayerLocation layer, Identifier texture)
    {
        super(context, new TFCChestedHorseModel(context.bakeLayer(layer), false),
            new TFCChestedHorseModel(context.bakeLayer(layer), false));
        addLayer(new HorseChestLayer(this, new TFCChestedHorseModel(context.bakeLayer(layer), true)));
        addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(),
            layer.model().getPath().endsWith("mule") ? EquipmentClientInfo.LayerType.MULE_SADDLE : EquipmentClientInfo.LayerType.DONKEY_SADDLE,
            state -> state.saddle, new EquineSaddleModel(context.bakeLayer(saddleLayer(layer))), null));
        this.texture = texture;
        size = scale;
    }

    @Override
    public TFCChestedHorseRenderState createRenderState() { return new TFCChestedHorseRenderState(); }

    @Override
    public void extractRenderState(T horse, TFCChestedHorseRenderState state, float partialTick)
    {
        super.extractRenderState(horse, state, partialTick);
        state.chestTexture = HorseChestLayer.textureFor(horse.getChestItem());
        // TFC can carry barrels and other registered items, not only wooden chests.
        state.hasChest = state.chestTexture != null;
    }

    @Override
    public Identifier getTextureLocation(TFCChestedHorseRenderState state) { return texture; }

    @Override
    protected void scale(TFCChestedHorseRenderState state, PoseStack poses) { poses.scale(size, size, size); }
}
