/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.lang.reflect.Proxy;
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
        System.out.println("PASS " + checks + " item-display snapshot/pose/light/reuse checks (CPU only)");
    }
}
