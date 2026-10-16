/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.util.function.Function;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blockentities.HotPouredGlassBlockEntity;
import net.dries007.tfc.util.Helpers;

public class HotPouredGlassBlockEntityRenderer extends ItemDisplayBlockEntityRenderer<HotPouredGlassBlockEntity>
{
    public HotPouredGlassBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    private static final Identifier VERY_VERY_HOT = Helpers.identifier("block/glass/3");
    private static final Identifier HOT = Helpers.identifier("block/glass/1");

    @Override
    protected void extractItems(HotPouredGlassBlockEntity glass, float partialTicks, PoseStack poseStack, State outputState, int combinedLight, int combinedOverlay)
    {
        final float ticks = (40f - glass.getAnimationTicks()) / 40f;
        final Function<Identifier, TextureAtlasSprite> textureAtlas = RenderHelpers::blockTexture;
        final VertexConsumer buffer = captureGeometry(outputState, Sheets.cutoutBlockSheet());

        if (glass.isInitialTransition())
        {
            final TextureAtlasSprite sprite = textureAtlas.apply(VERY_VERY_HOT);
            float hLerp = Mth.lerp(!glass.isInitialized() ? 0f : ticks, 4f, 0f) / 16f;
            float yLerp = Mth.lerp(!glass.isInitialized() ? 0f : ticks, 8f, 1f) / 16f;
            RenderHelpers.renderTexturedCuboid(poseStack, buffer, sprite, combinedLight, combinedOverlay,
                hLerp, 0f, hLerp, 1f - hLerp, yLerp, 1f - hLerp
            );
        }
        else
        {
            if (ticks > 0f)
            {
                final TextureAtlasSprite sprite = textureAtlas.apply(HOT);
                RenderHelpers.renderTexturedCuboid(poseStack, buffer, sprite, combinedLight, combinedOverlay,
                    0f, 0f, 0f, 1f, Mth.lerp(ticks, 0.025f, 1f / 16f), 1f
                );
            }
        }
    }

}
