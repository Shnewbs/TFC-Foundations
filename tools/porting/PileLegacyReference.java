/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Vector3f;

/** Frozen pre-migration pile geometry (bf289ca2); only the original drawing sink is replaced with captured vertices. */
public final class PileLegacyReference
{
    public static List<float[]> vertices(int ingots, boolean doubled)
    {
        final List<float[]> vertices = new ArrayList<>();
        final PoseStack poseStack = new PoseStack();
        for (int i = 0; i < ingots; i++)
        {
            if (doubled) doubleIngot(vertices, poseStack, i);
            else singleIngot(vertices, poseStack, i);
        }
        return vertices;
    }

    private static void singleIngot(List<float[]> vertices, PoseStack poseStack, int i)
    {
            final int layer = (i + 8) / 8;
            final boolean oddLayer = (layer % 2) == 1;
            final float x = (i % 4) * 0.25f;
            final float y = (layer - 1) * 0.125f;
            final float z = i % 8 >= 4 ? 0.5f : 0;

            poseStack.pushPose();
            if (oddLayer)
            {
                // Rotate 90 degrees every other layer
                poseStack.translate(0.5f, 0f, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(90f));
                poseStack.translate(-0.5f, 0f, -0.5f);
            }

            poseStack.translate(x, y, z);

            final float scale = 0.0625f / 2f;
            final float minX = scale * 0.5f;
            final float minY = scale * 0f;
            final float minZ = scale * 0.5f;
            final float maxX = scale * (minX + 7);
            final float maxY = scale * (minY + 4);
            final float maxZ = scale * (minZ + 15);

            capture(vertices, poseStack, minX, maxX, minZ, maxZ, minX + scale, maxX - scale, minZ + scale, maxZ - scale, minY, maxY, 7, 4, 15, oddLayer);

            poseStack.popPose();
    }

    private static void doubleIngot(List<float[]> vertices, PoseStack poseStack, int i)
    {
            final int layer = (i + 6) / 6;
            final boolean oddLayer = (layer % 2) == 1;
            final float x = (i % 3) * 0.33f;
            final float y = (layer - 1) * 1f / 6;
            final float z = i % 6 >= 3 ? 0.5f : 0;

            poseStack.pushPose();
            if (oddLayer)
            {
                // Rotate 90 degrees every other layer
                poseStack.translate(0.5f, 0f, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(90f));
                poseStack.translate(-0.5f, 0f, -0.5f);
            }

            poseStack.translate(x, y, z);

            final float scale = 0.0625f / 2f;
            final float minX = scale * 0.5f;
            final float minY = scale * 0f;
            final float minZ = scale * 0.5f;
            final float maxX = scale * (minX + 10);
            final float maxY = scale * (minY + 5);
            final float maxZ = scale * (minZ + 15);

            capture(vertices, poseStack, minX, maxX, minZ, maxZ, minX + scale, maxX - scale, minZ + scale, maxZ - scale, minY, maxY, 10, 5, 15, oddLayer);

            poseStack.popPose();
    }

    private static void capture(List<float[]> output, PoseStack pose, float pMinX, float pMaxX, float pMinZ, float pMaxZ, float qMinX, float qMaxX, float qMinZ, float qMaxZ, float minY, float maxY, float xPixels, float yPixels, float zPixels, boolean invertNormal)
    {
        captureFaces(output, pose, getTrapezoidalCuboidXVertices(pMinX,pMaxX,pMinZ,pMaxZ,qMinX,qMaxX,qMinZ,qMaxZ,minY,maxY), zPixels, yPixels);
        captureFaces(output, pose, getTrapezoidalCuboidYVertices(pMinX,pMaxX,pMinZ,pMaxZ,qMinX,qMaxX,qMinZ,qMaxZ,minY,maxY), zPixels, xPixels);
        captureFaces(output, pose, getTrapezoidalCuboidZVertices(pMinX,pMaxX,pMinZ,pMaxZ,qMinX,qMaxX,qMinZ,qMaxZ,minY,maxY), xPixels, yPixels);
    }
    private static void captureFaces(List<float[]> output, PoseStack pose, float[][] faces, float u, float v)
    {
        for (float[] vertex : faces)
        {
            final Vector3f p = new Vector3f(vertex[0], vertex[1], vertex[2]).mulPosition(pose.last().pose());
            output.add(new float[] {p.x, p.y, p.z, vertex[3] * u * 1f / 16f, vertex[4] * v * 1f / 16f});
        }
    }

    public static float[][] getTrapezoidalCuboidXVertices(float pMinX, float pMaxX, float pMinZ, float pMaxZ, float qMinX, float qMaxX, float qMinZ, float qMaxZ, float minY, float maxY)
    {
        return new float[][] {
            {pMinX, minY, pMinZ, 0, 1, 1}, // +X
            {pMinX, minY, pMaxZ, 1, 1, 1},
            {qMinX, maxY, qMaxZ, 1, 0, 1},
            {qMinX, maxY, qMinZ, 0, 0, 1},

            {pMaxX, minY, pMaxZ, 1, 0, -1}, // -X
            {pMaxX, minY, pMinZ, 0, 0, -1},
            {qMaxX, maxY, qMinZ, 0, 1, -1},
            {qMaxX, maxY, qMaxZ, 1, 1, -1},
        };
    }

    public static float[][] getTrapezoidalCuboidYVertices(float pMinX, float pMaxX, float pMinZ, float pMaxZ, float qMinX, float qMaxX, float qMinZ, float qMaxZ, float minY, float maxY)
    {
        return new float[][] {
            {qMinX, maxY, qMinZ, 0, 1, 1}, // +Y
            {qMinX, maxY, qMaxZ, 1, 1, 1},
            {qMaxX, maxY, qMaxZ, 1, 0, 1},
            {qMaxX, maxY, qMinZ, 0, 0, 1},

            {pMinX, minY, pMaxZ, 1, 0, -1}, // -Y
            {pMinX, minY, pMinZ, 0, 0, -1},
            {pMaxX, minY, pMinZ, 0, 1, -1},
            {pMaxX, minY, pMaxZ, 1, 1, -1},
        };
    }

    public static float[][] getTrapezoidalCuboidZVertices(float pMinX, float pMaxX, float pMinZ, float pMaxZ, float qMinX, float qMaxX, float qMinZ, float qMaxZ, float minY, float maxY)
    {
        return new float[][] {
            {pMaxX, minY, pMinZ, 0, 1, 1}, // +Z
            {pMinX, minY, pMinZ, 1, 1, 1},
            {qMinX, maxY, qMinZ, 1, 0, 1},
            {qMaxX, maxY, qMinZ, 0, 0, 1},

            {pMinX, minY, pMaxZ, 1, 0, -1}, // -Z
            {pMaxX, minY, pMaxZ, 0, 0, -1},
            {qMaxX, maxY, qMaxZ, 0, 1, -1},
            {qMinX, maxY, qMaxZ, 1, 1, -1}
        };
    }
}
