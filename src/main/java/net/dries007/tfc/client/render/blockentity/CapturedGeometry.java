/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** CPU extraction buffer. Submitted snapshots contain values, never the mutable builder or world. */
public final class CapturedGeometry implements VertexConsumer
{
    public record Vertex(float x, float y, float z, int color, float u, float v,
        int overlay, int light, float nx, float ny, float nz) {}

    public record Snapshot(List<Vertex> vertices)
    {
        public Snapshot { vertices = List.copyOf(vertices); }

        public void draw(PoseStack.Pose pose, VertexConsumer out)
        {
            for (Vertex vertex : vertices)
            {
                out.addVertex(pose, vertex.x(), vertex.y(), vertex.z()).setColor(vertex.color())
                    .setUv(vertex.u(), vertex.v()).setOverlay(vertex.overlay()).setLight(vertex.light())
                    .setNormal(pose, vertex.nx(), vertex.ny(), vertex.nz());
            }
        }
    }

    private final List<Vertex> vertices = new ArrayList<>();
    private boolean pending;
    private float x, y, z, u, v, nx, ny, nz;
    private int color, overlay, light;

    private void flush()
    {
        if (pending)
        {
            vertices.add(new Vertex(x, y, z, color, u, v, overlay, light, nx, ny, nz));
            pending = false;
        }
    }

    public Snapshot snapshot()
    {
        flush();
        return new Snapshot(vertices);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z)
    {
        flush();
        this.x = x;
        this.y = y;
        this.z = z;
        color = -1;
        overlay = light = 0;
        u = v = nx = ny = nz = 0;
        pending = true;
        return this;
    }

    @Override public VertexConsumer setColor(int red, int green, int blue, int alpha) { return setColor(alpha << 24 | red << 16 | green << 8 | blue); }
    @Override public VertexConsumer setColor(int color) { this.color = color; return this; }
    @Override public VertexConsumer setUv(float u, float v) { this.u = u; this.v = v; return this; }
    @Override public VertexConsumer setUv1(int u, int v) { overlay = (u & 0xFFFF) | v << 16; return this; }
    @Override public VertexConsumer setUv2(int u, int v) { light = (u & 0xFFFF) | v << 16; return this; }
    @Override public VertexConsumer setNormal(float x, float y, float z) { nx = x; ny = y; nz = z; return this; }
    @Override public VertexConsumer setLineWidth(float width) { throw new UnsupportedOperationException("Block-entity meshes contain textured quads, not lines"); }
}
