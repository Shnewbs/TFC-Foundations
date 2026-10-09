/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import java.util.EnumMap;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.equine.BabyHorseModel;
import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Variant;

public class TFCHorseRenderer extends AbstractHorseRenderer<Horse, HorseRenderState, HorseModel>
{
    private record Textures(Identifier adult, Identifier baby) {}
    private static final Map<Variant, Textures> TEXTURES = new EnumMap<>(Variant.class);
    static
    {
        for (Variant variant : Variant.values())
        {
            final String name = switch (variant)
            {
                case WHITE -> "white";
                case CREAMY -> "creamy";
                case CHESTNUT -> "chestnut";
                case BROWN -> "brown";
                case BLACK -> "black";
                case GRAY -> "gray";
                case DARK_BROWN -> "darkbrown";
            };
            final String base = "textures/entity/horse/horse_" + name;
            TEXTURES.put(variant, new Textures(Identifier.withDefaultNamespace(base + ".png"), Identifier.withDefaultNamespace(base + "_baby.png")));
        }
    }

    public TFCHorseRenderer(EntityRendererProvider.Context context)
    {
        super(context, new HorseModel(context.bakeLayer(ModelLayers.HORSE)), new BabyHorseModel(context.bakeLayer(ModelLayers.HORSE_BABY)));
        addLayer(new HorseMarkingLayer(this));
        addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.HORSE_BODY,
            state -> state.bodyArmorItem, new HorseModel(context.bakeLayer(ModelLayers.HORSE_ARMOR)), null, 2));
        addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.HORSE_SADDLE,
            state -> state.saddle, new EquineSaddleModel(context.bakeLayer(ModelLayers.HORSE_SADDLE)), null, 2));
    }

    @Override
    public HorseRenderState createRenderState() { return new HorseRenderState(); }

    @Override
    public void extractRenderState(Horse horse, HorseRenderState state, float partialTick)
    {
        super.extractRenderState(horse, state, partialTick);
        state.variant = horse.getVariant();
        state.markings = horse.getMarkings();
    }

    @Override
    public Identifier getTextureLocation(HorseRenderState state)
    {
        final Textures textures = TEXTURES.get(state.variant);
        return state.isBaby ? textures.baby() : textures.adult();
    }

    @Override
    protected void scale(HorseRenderState state, PoseStack poses) { poses.scale(1.1F, 1.1F, 1.1F); }
}
