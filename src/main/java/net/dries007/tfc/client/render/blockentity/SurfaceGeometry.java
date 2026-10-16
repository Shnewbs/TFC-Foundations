/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Immutable fill surface, with UVs captured from the current atlas during extraction. */
public record SurfaceGeometry(float minX, float minZ, float maxX, float maxZ, float y,
    float u0, float u1, float v0, float v1, int color, int overlay, int light)
{
    public static SurfaceGeometry capture(TextureAtlasSprite sprite, int color, float minX, float minZ,
        float maxX, float maxZ, float y, int overlay, int light)
    {
        return new SurfaceGeometry(minX, minZ, maxX, maxZ, y, sprite.getU(minX), sprite.getU(maxX),
            sprite.getV(minZ), sprite.getV(maxZ), color, overlay, light);
    }

    public void draw(PoseStack.Pose pose, VertexConsumer out)
    {
        vertex(pose, out, minX, minZ, u0, v0);
        vertex(pose, out, minX, maxZ, u0, v1);
        vertex(pose, out, maxX, maxZ, u1, v1);
        vertex(pose, out, maxX, minZ, u1, v0);
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer out, float x, float z, float u, float v)
    {
        out.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
    }
}
