/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.function.Function;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import org.jspecify.annotations.Nullable;

/** Preserves the translucent item passes and retextures mask edge quads as well as front/back faces. */
public final class ItemLayerGeometry
{
    private ItemLayerGeometry() {}

    public static QuadCollection translucent(QuadCollection quads, @Nullable TextureAtlasSprite replacement, boolean emissive)
    {
        return remap(quads, replacement, emissive, sprite -> {
            final boolean blocks = sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS);
            return emissive ? NeoForgeRenderTypes.getItemTranslucentUnlit(sprite.atlasLocation())
                : blocks ? Sheets.translucentBlockItemSheet() : Sheets.translucentItemSheet();
        });
    }

    // Pipeline selection stays at the client boundary; quad math is also exercised without game bootstrap.
    static QuadCollection remap(QuadCollection quads, @Nullable TextureAtlasSprite replacement, boolean emissive,
        Function<TextureAtlasSprite, RenderType> renderTypes)
    {
        final QuadCollection.Builder result = new QuadCollection.Builder();
        for (BakedQuad quad : quads.getAll())
        {
            final var original = quad.materialInfo();
            final var sprite = replacement == null ? original.sprite() : replacement;
            final var renderType = renderTypes.apply(sprite);
            final var material = new BakedQuad.MaterialInfo(sprite, ChunkSectionLayer.TRANSLUCENT, renderType,
                replacement == null ? original.tintIndex() : 0, !emissive && original.shade(), emissive ? 15 : original.lightEmission(), !emissive && original.ambientOcclusion());
            result.addUnculledFace(new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                uv(quad.packedUV0(), original.sprite(), sprite), uv(quad.packedUV1(), original.sprite(), sprite),
                uv(quad.packedUV2(), original.sprite(), sprite), uv(quad.packedUV3(), original.sprite(), sprite),
                quad.direction(), material, quad.bakedNormals(), quad.bakedColors()));
        }
        return result.build();
    }

    private static long uv(long packed, TextureAtlasSprite from, TextureAtlasSprite to)
    {
        if (from == to) return packed;
        final float u = (UVPair.unpackU(packed) - from.getU0()) / (from.getU1() - from.getU0());
        final float v = (UVPair.unpackV(packed) - from.getV0()) / (from.getV1() - from.getV0());
        return UVPair.pack(to.getU(u), to.getV(v));
    }
}
