/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.dries007.tfc.common.blockentities.BlockEntityModelData;

/** Position/UV descriptions only: no inventory, level, atlas or mutable model state is retained. */
public final class StaticBlockMesh
{
    private StaticBlockMesh() {}

    public record Vertex(float x, float y, float z, float u, float v) {}

    public record Face(Identifier texture, int color, List<Vertex> vertices)
    {
        public Face
        {
            Objects.requireNonNull(texture);
            vertices = List.copyOf(vertices);
            if (vertices.size() != 4) throw new IllegalArgumentException("A face needs four vertices");
        }

        public Direction direction()
        {
            final Vertex a = vertices.get(0), b = vertices.get(1), c = vertices.get(2);
            final Vector3f normal = new Vector3f(b.x - a.x, b.y - a.y, b.z - a.z)
                .cross(c.x - a.x, c.y - a.y, c.z - a.z);
            return Direction.getApproximateNearest(normal.x, normal.y, normal.z);
        }
    }

    public record Mesh(List<Face> faces, Identifier particle)
    {
        public Mesh
        {
            faces = List.copyOf(faces);
            Objects.requireNonNull(particle);
        }
    }

    public static Mesh pile(BlockEntityModelData.Pile data, int count, boolean doubled)
    {
        if (count < 0 || count > (doubled ? 36 : 64)) throw new IllegalArgumentException("Invalid pile count");
        final List<Face> faces = new ArrayList<>(count * 6);
        for (int i = 0; i < count; i++)
        {
            final int perLayer = doubled ? 6 : 8;
            final int layer = (i + perLayer) / perLayer;
            final boolean oddLayer = (layer % 2) == 1;
            final float x = doubled ? (i % 3) * 0.33f : (i % 4) * 0.25f;
            final float y = doubled ? (layer - 1) * 1f / 6 : (layer - 1) * 0.125f;
            final float z = i % perLayer >= perLayer / 2 ? 0.5f : 0;
            final Matrix4f pose = new Matrix4f();
            if (oddLayer) pose.translate(0.5f, 0, 0.5f).rotate(Axis.YP.rotationDegrees(90f)).translate(-0.5f, 0, -0.5f);
            pose.translate(x, y, z);

            // Keep the original dimensions, including its fractional edge offsets.
            final float scale = 0.0625f / 2f;
            final float minX = scale * 0.5f, minY = scale * 0f, minZ = scale * 0.5f;
            final float maxX = scale * (minX + (doubled ? 10 : 7));
            final float maxY = scale * (minY + (doubled ? 5 : 4));
            final float maxZ = scale * (minZ + 15);
            final float qMinX = minX + scale, qMaxX = maxX - scale, qMinZ = minZ + scale, qMaxZ = maxZ - scale;
            addFaces(faces, data.texture(i), pose, getTrapezoidalCuboidXVertices(minX, maxX, minZ, maxZ, qMinX, qMaxX, qMinZ, qMaxZ, minY, maxY), 15, doubled ? 5 : 4);
            addFaces(faces, data.texture(i), pose, getTrapezoidalCuboidYVertices(minX, maxX, minZ, maxZ, qMinX, qMaxX, qMinZ, qMaxZ, minY, maxY), 15, doubled ? 10 : 7);
            addFaces(faces, data.texture(i), pose, getTrapezoidalCuboidZVertices(minX, maxX, minZ, maxZ, qMinX, qMaxX, qMinZ, qMaxZ, minY, maxY), doubled ? 10 : 7, doubled ? 5 : 4);
        }
        return new Mesh(faces, count == 0 ? data.fallback() : data.texture(count - 1));
    }

    private static void addFaces(List<Face> output, Identifier texture, Matrix4f pose, float[][] vertices, float uSize, float vSize)
    {
        for (int start = 0; start < vertices.length; start += 4)
        {
            final List<Vertex> face = new ArrayList<>(4);
            for (int i = start; i < start + 4; i++)
            {
                final float[] v = vertices[i];
                final Vector3f point = pose.transformPosition(new Vector3f(v[0], v[1], v[2]));
                face.add(new Vertex(point.x, point.y, point.z, v[3] * uSize / 16f, v[4] * vSize / 16f));
            }
            output.add(new Face(texture, -1, face));
        }
    }

    public static Mesh scraping(BlockEntityModelData.Scraping data, Identifier missing)
    {
        final List<Face> faces = new ArrayList<>(16);
        if (data.input() != null && data.output() != null)
        {
            // Retain the original input pass followed by the output pass and the same tile UVs.
            for (int condition = 0; condition < 2; condition++)
            {
                for (int x = 0; x < 4; x++)
                {
                    for (int z = 0; z < 4; z++)
                    {
                        if (((data.positions() >> (x + 4 * z)) & 1) != condition) continue;
                        final float x0 = x / 4f, z0 = z / 4f, x1 = x0 + 0.25f, z1 = z0 + 0.25f;
                        faces.add(new Face(condition == 0 ? data.input() : data.output(), condition == 0 ? data.inputColor() : data.outputColor(), List.of(
                            new Vertex(x0, 0.01f, z0, x0, z0), new Vertex(x0, 0.01f, z1, x0, z1),
                            new Vertex(x1, 0.01f, z1, x1, z1), new Vertex(x1, 0.01f, z0, x1, z0))));
                    }
                }
            }
        }
        return new Mesh(faces, data.output() != null ? data.output() : missing);
    }

    /** Same block-center and UV-center conventions as the target FaceBakery. */
    public static Face transform(Face face, ModelState state)
    {
        final var transform = state.transformation().getMatrix();
        final var uvTransform = state.inverseFaceTransformation(face.direction());
        final List<Vertex> vertices = new ArrayList<>(4);
        for (Vertex v : face.vertices())
        {
            final Vector3f point = transform.transformPosition(new Vector3f(v.x - 0.5f, v.y - 0.5f, v.z - 0.5f)).add(0.5f, 0.5f, 0.5f);
            final Vector3f uv = uvTransform.transformPosition(new Vector3f(v.u - 0.5f, v.v - 0.5f, 0)).add(0.5f, 0.5f, 0);
            vertices.add(new Vertex(point.x, point.y, point.z, uv.x, uv.y));
        }
        return new Face(face.texture(), face.color(), vertices);
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
