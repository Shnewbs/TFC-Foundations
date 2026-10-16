/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.joml.Matrix4f;

/** CPU fixtures subclass the real native render state; no game classes are replaced. */
public final class ItemDisplaySmoke
{
    private static int checks;

    private static void check(boolean value)
    {
        checks++;
        if (!value) throw new AssertionError("Item display check " + checks);
    }

    private static final class RecordingItem extends ItemStackRenderState
    {
        Matrix4f pose;
        int count;
        int light;
        int overlay;

        @Override
        public void submit(PoseStack poses, SubmitNodeCollector collector, int light, int overlay, int outline)
        {
            pose = new Matrix4f(poses.last().pose());
            count++;
            this.light = light;
            this.overlay = overlay;
            check(outline == 0);
        }
    }

    private static final class RecordingVertices implements VertexConsumer
    {
        final List<float[]> positions = new ArrayList<>();
        final List<float[]> uv = new ArrayList<>();
        final List<Integer> colors = new ArrayList<>();
        final List<Integer> lights = new ArrayList<>();
        final List<Integer> overlays = new ArrayList<>();
        final List<Vector3f> normals = new ArrayList<>();

        @Override public VertexConsumer addVertex(float x, float y, float z) { positions.add(new float[] {x, y, z}); return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return setColor(a << 24 | r << 16 | g << 8 | b); }
        @Override public VertexConsumer setColor(int color) { colors.add(color); return this; }
        @Override public VertexConsumer setUv(float u, float v) { uv.add(new float[] {u, v}); return this; }
        @Override public VertexConsumer setUv1(int u, int v) { overlays.add(u | v << 16); return this; }
        @Override public VertexConsumer setUv2(int u, int v) { lights.add(u | v << 16); return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { normals.add(new Vector3f(x, y, z)); return this; }
        @Override public VertexConsumer setLineWidth(float width) { throw new AssertionError("Unexpected line vertex"); }
    }

    private static void surfaces()
    {
        // Barrel boundaries, the low-fill inset transition, and rectangular bounds.
        for (float fill : new float[] {0, 0.01f, 0.03f, 0.125f, 0.5f, 1})
        {
            final float inset = fill > 0.03f ? 0 : (0.03f - fill) * 7;
            final float min = 0.1875f + inset;
            final float max = 0.8125f - inset;
            final float height = 0.140625f + (0.75f - 0.015625f) * fill;
            final SurfaceGeometry surface = new SurfaceGeometry(min, min, max, max, height,
                0.125f, 0.25f, 0.375f, 0.5f, 0x7FAABBCC, 0x123456, 0x00F000A0);
            final PoseStack poses = new PoseStack();
            poses.translate(11, -3, 7);
            poses.mulPose(Axis.YP.rotationDegrees(90));
            final RecordingVertices out = new RecordingVertices();
            surface.draw(poses.last(), out);
            check(out.positions.size() == 4);
            final float[][] coords = {{min, height, min}, {min, height, max}, {max, height, max}, {max, height, min}};
            final float[][] uv = {{0.125f, 0.375f}, {0.125f, 0.5f}, {0.25f, 0.5f}, {0.25f, 0.375f}};
            for (int i = 0; i < 4; i++)
            {
                final Vector3f expected = poses.last().pose().transformPosition(new Vector3f(coords[i]));
                check(expected.equals(new Vector3f(out.positions.get(i)), 0.00001f));
                check(out.uv.get(i)[0] == uv[i][0] && out.uv.get(i)[1] == uv[i][1]);
                check(out.colors.get(i) == 0x7FAABBCC);
                check(out.lights.get(i) == 0x00F000A0 && out.overlays.get(i) == 0x123456);
                check(out.normals.get(i).equals(new Vector3f(0, 1, 0), 0.00001f));
            }
        }
    }

    public static void main(String[] args)
    {
        final SubmitNodeCollector collector = (SubmitNodeCollector) Proxy.newProxyInstance(
            ItemDisplaySmoke.class.getClassLoader(), new Class<?>[] {SubmitNodeCollector.class},
            (proxy, method, values) -> { throw new AssertionError("Unexpected GPU collection: " + method); });
        final ItemDisplayBlockEntityRenderer.Displays state = new ItemDisplayBlockEntityRenderer.Displays();
        final PoseStack local = new PoseStack();
        local.translate(0.5, 0.878125, 0.5);
        local.scale(0.3f, 0.3f, 0.3f);
        local.mulPose(Axis.XP.rotationDegrees(90));
        local.mulPose(Axis.ZP.rotationDegrees(270));
        local.translate(-0.4f, 0, 0);
        final Matrix4f captured = new Matrix4f(local.last().pose());
        final RecordingItem hammer = new RecordingItem();
        state.add(hammer, local, 0x123456, 0x654321);
        // Subsequent slot transforms must not mutate the hammer snapshot.
        local.translate(1.15f, 0, 0);
        final Matrix4f second = new Matrix4f(local.last().pose());
        final RecordingItem input = new RecordingItem();
        state.add(input, local, 0xf000f0, 7);
        local.setIdentity();

        final PoseStack world = new PoseStack();
        world.translate(13, -4, 9);
        final Matrix4f parent = new Matrix4f(world.last().pose());
        state.submitItems(world, collector);
        check(hammer.pose.equals(new Matrix4f(parent).mul(captured), 0.00001f));
        check(input.pose.equals(new Matrix4f(parent).mul(second), 0.00001f));
        check(world.last().pose().equals(parent, 0.00001f));
        check(hammer.light == 0x123456 && hammer.overlay == 0x654321);
        check(input.light == 0xf000f0 && input.overlay == 7);
        check(hammer.count == 1 && input.count == 1);
        state.clearItems();
        state.submitItems(world, collector);
        check(hammer.count == 1 && input.count == 1);
        final RecordingItem nextFrame = new RecordingItem();
        state.add(nextFrame, local, 8, 9);
        state.submitItems(world, collector);
        check(hammer.count == 1 && input.count == 1 && nextFrame.count == 1);
        check(nextFrame.pose.equals(parent, 0.00001f));
        surfaces();
        System.out.println("PASS " + checks + " display/surface transform, UV, tint, light and reuse checks (CPU only)");
    }
}
