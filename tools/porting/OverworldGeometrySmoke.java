/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.overworld;

import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.dries007.tfc.client.render.blockentity.CapturedGeometry;

/** CPU snapshots and transforms only; no GPU, client bootstrap or live world. */
public final class OverworldGeometrySmoke
{
    private static int checks;
    private static void check(boolean condition) { checks++; if (!condition) throw new AssertionError("Sky geometry check " + checks); }
    public static void main(String[] args)
    {
        for (float y : new float[] {-16, 16})
        {
            final var disc = SkyGeometry.skyDisc(y);
            check(disc.vertices().size() == 10);
            check(disc.vertices().getFirst().x() == 0 && disc.vertices().getFirst().y() == y);
            for (int i = 1; i < disc.vertices().size(); i++)
            {
                var vertex = disc.vertices().get(i);
                check(Math.abs(Math.hypot(vertex.x(), vertex.z()) - 512) < 0.06);
                check(vertex.y() == y);
            }
            check(Math.abs(disc.vertices().get(1).x() - disc.vertices().get(9).x()) < 0.001);
        }
        for (int zenith = 0; zenith <= 180; zenith += 5) for (int azimuth = -180; azimuth <= 180; azimuth += 5)
        {
            final SkyPos pos = SkyPos.of(Math.toRadians(zenith), Math.toRadians(azimuth));
            final PoseStack stack = new PoseStack();
            stack.translate(2, -4, 7);
            final Matrix4f original = new Matrix4f(stack.last().pose());
            final Matrix4f sun = SkyGeometry.rotateTo(stack, pos);
            check(original.equals(stack.last().pose()));
            final Matrix4f stars = SkyGeometry.rotateSkyTo(stack, pos);
            check(original.equals(stack.last().pose()));
            check(Math.abs(sun.determinant() - 1) < 0.00001);
            check(Math.abs(stars.determinant() - 1) < 0.00001);
            check(sun.isFinite() && stars.isFinite());
        }
        final CapturedGeometry buffer = new CapturedGeometry();
        final RandomSource random = RandomSource.create(42L);
        for (int i = 0; i < 256; i++)
            SkyGeometry.drawStar(buffer, new Star(i / 256f * (float) Math.PI, i * 0.3f, i * 12345 & 0xFFFFFF, -1 + i / 256f * 7), random);
        final var snapshot = buffer.snapshot();
        check(snapshot.vertices().size() == 1024);
        for (var vertex : snapshot.vertices())
        {
            check(Float.isFinite(vertex.x()) && Float.isFinite(vertex.y()) && Float.isFinite(vertex.z()));
            check((vertex.color() >>> 24) == 255);
        }
        // Real native buffer layouts must accept the captured attribute replay for all four environment formats.
        for (var format : List.of(com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION,
            com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR,
            com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX,
            com.mojang.blaze3d.vertex.DefaultVertexFormat.PARTICLE))
        {
            try (var bytes = com.mojang.blaze3d.vertex.ByteBufferBuilder.exactlySized(snapshot.vertices().size() * format.getVertexSize()))
            {
                final var out = new com.mojang.blaze3d.vertex.BufferBuilder(bytes, com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, format);
                snapshot.draw(new PoseStack().last(), out);
                try (var mesh = out.buildOrThrow())
                {
                    check(mesh.drawState().vertexCount() == 1024);
                    check(mesh.drawState().indexCount() == 1536);
                    check(mesh.vertexBuffer().remaining() == 1024 * format.getVertexSize());
                }
            }
        }
        final var replay = new CapturedGeometry();
        snapshot.draw(new PoseStack().last(), replay);
        check(snapshot.vertices().equals(replay.snapshot().vertices()));
        buffer.addVertex(999, 999, 999);
        buffer.snapshot();
        check(snapshot.vertices().size() == 1024);
        try { snapshot.vertices().clear(); throw new AssertionError("Mutable catalog"); }
        catch (UnsupportedOperationException expected) { checks++; }
        System.out.println("PASS " + checks + " sky geometry CPU assertions; no GPU or world validation");
    }
}
