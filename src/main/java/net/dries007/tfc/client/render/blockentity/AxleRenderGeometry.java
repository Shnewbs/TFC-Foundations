/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/** Captured atlas coordinates and the unchanged axle/blade cuboid geometry. */
public final class AxleRenderGeometry
{
    public record Texture(float u0, float u1, float v0, float v1)
    {
        public static Texture capture(TextureAtlasSprite sprite)
        {
            return new Texture(sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
        }
    }

    private static final float[][][] AXLE = faces(6F / 16, 6F / 16, 0, 10F / 16, 10F / 16, 1);
    private static final float[][][] BLADE = faces(7F / 16, 10F / 16, 6F / 16, 9F / 16, 17.5F / 16, 10F / 16);

    private AxleRenderGeometry() {}

    public static void applyRotation(PoseStack poses, Direction.Axis axis, float angle)
    {
        poses.translate(0.5F, 0.5F, 0.5F);
        switch (axis)
        {
            case X -> poses.mulPose(Axis.YP.rotationDegrees(90));
            case Y -> poses.mulPose(Axis.XP.rotationDegrees(-90));
            case Z -> {}
        }
        poses.mulPose(Axis.ZP.rotation(angle));
        poses.translate(-0.5F, -0.5F, -0.5F);
    }

    public static void drawAxle(PoseStack.Pose pose, VertexConsumer out, Texture texture, int light, int overlay)
    {
        draw(pose, out, texture, light, overlay, AXLE, 0.25F, 0.25F, 1);
    }

    public static void drawBlade(PoseStack.Pose pose, VertexConsumer out, Texture texture, int light, int overlay)
    {
        draw(pose, out, texture, light, overlay, BLADE, 0.125F, 7.5F / 16, 0.25F);
    }

    private static void draw(PoseStack.Pose pose, VertexConsumer out, Texture texture, int light, int overlay,
        float[][][] faces, float width, float height, float depth)
    {
        for (int axis = 0; axis < 3; axis++)
        {
            final float uSize = axis == 2 ? width : depth;
            final float vSize = axis == 1 ? width : height;
            for (float[] v : faces[axis])
            {
                out.addVertex(pose.pose(), v[0], v[1], v[2]).setColor(-1)
                    .setUv(texture.u0() + (texture.u1() - texture.u0()) * v[3] * uSize,
                        texture.v0() + (texture.v1() - texture.v0()) * v[4] * vSize)
                    .setLight(light).setOverlay(overlay)
                    .setNormal(pose, axis == 0 ? v[5] : 0, axis == 1 ? v[5] : 0, axis == 2 ? v[5] : 0);
            }
        }
    }

    private static float[][][] faces(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
    {
        return new float[][][] {getXVertices(minX, minY, minZ, maxX, maxY, maxZ),
            getYVertices(minX, minY, minZ, maxX, maxY, maxZ), getZVertices(minX, minY, minZ, maxX, maxY, maxZ)};
    }

    private static float[][] getXVertices(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
    {
        return new float[][] {
            {minX, minY, minZ, 0, 1, 1}, // +X
            {minX, minY, maxZ, 1, 1, 1},
            {minX, maxY, maxZ, 1, 0, 1},
            {minX, maxY, minZ, 0, 0, 1},

            {maxX, minY, maxZ, 1, 0, -1}, // -X
            {maxX, minY, minZ, 0, 0, -1},
            {maxX, maxY, minZ, 0, 1, -1},
            {maxX, maxY, maxZ, 1, 1, -1}
        };
    }

    private static float[][] getYVertices(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
    {
        return new float[][] {
            {minX, maxY, minZ, 0, 1, 1}, // +Y
            {minX, maxY, maxZ, 1, 1, 1},
            {maxX, maxY, maxZ, 1, 0, 1},
            {maxX, maxY, minZ, 0, 0, 1},

            {minX, minY, maxZ, 1, 0, -1}, // -Y
            {minX, minY, minZ, 0, 0, -1},
            {maxX, minY, minZ, 0, 1, -1},
            {maxX, minY, maxZ, 1, 1, -1}
        };
    }

    private static float[][] getZVertices(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
    {
        return new float[][] {
            {maxX, minY, minZ, 0, 1, 1}, // +Z
            {minX, minY, minZ, 1, 1, 1},
            {minX, maxY, minZ, 1, 0, 1},
            {maxX, maxY, minZ, 0, 0, 1},

            {minX, minY, maxZ, 1, 0, -1}, // -Z
            {maxX, minY, maxZ, 0, 0, -1},
            {maxX, maxY, maxZ, 0, 1, -1},
            {minX, maxY, maxZ, 1, 1, -1}
        };
    }
}
