/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.overworld;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.dries007.tfc.client.render.blockentity.CapturedGeometry;

/** CPU sky geometry, separate from GPU resources and live level extraction. */
final class SkyGeometry
{
    static CapturedGeometry.Snapshot skyDisc(float y)
    {
        final CapturedGeometry out = new CapturedGeometry();
        out.addVertex(0, y, 0);
        for (int i = -180; i <= 180; i += 45)
            out.addVertex(Math.signum(y) * 512 * Mth.cos(i * Mth.DEG_TO_RAD), y, 512 * Mth.sin(i * Mth.DEG_TO_RAD));
        return out.snapshot();
    }

    static CapturedGeometry.Snapshot celestial(TextureAtlasSprite sprite, boolean moon)
    {
        final CapturedGeometry out = new CapturedGeometry();
        final float v0 = moon ? sprite.getV1() : sprite.getV0();
        final float v1 = moon ? sprite.getV0() : sprite.getV1();
        out.addVertex(-30, 100, -30).setUv(sprite.getU0(), v0);
        out.addVertex(30, 100, -30).setUv(sprite.getU1(), v0);
        out.addVertex(30, 100, 30).setUv(sprite.getU1(), v1);
        out.addVertex(-30, 100, 30).setUv(sprite.getU0(), v1);
        return out.snapshot();
    }

    static void drawStar(VertexConsumer buffer, Star star, RandomSource random)
    {
        final float zenith = star.zenith();
        final float azimuth = star.azimuth();
        final int color = 0xff000000 | star.color();
        final float size = Mth.clampedMap(star.apparentMagnitude(), -1, 6, 0.4f, 0.1f);

        final float sinZenith = Mth.sin(zenith);
        final float x = 100f * sinZenith * Mth.cos(azimuth);
        final float z = 100f * sinZenith * Mth.sin(azimuth);
        final float y = 100f * Mth.cos(zenith);

        final Vector3f pos = new Vector3f(x, y, z);
        final Quaternionf q = new Quaternionf()
            .rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), pos)
            .rotateZ(random.nextFloat() * Mth.TWO_PI);

        buffer.addVertex(pos.add(new Vector3f(size, -size, 0).rotate(q))).setColor(color);
        buffer.addVertex(pos.add(new Vector3f(size, size, 0).rotate(q))).setColor(color);
        buffer.addVertex(pos.add(new Vector3f(-size, size, 0).rotate(q))).setColor(color);
        buffer.addVertex(pos.add(new Vector3f(-size, -size, 0).rotate(q))).setColor(color);
    }

    static Matrix4f rotateTo(PoseStack stack, SkyPos pos)
    {
        stack.pushPose();
        stack.mulPose(Axis.YP.rotation(pos.azimuth()));
        stack.mulPose(Axis.XN.rotation(pos.zenith()));
        final Matrix4f pose = new Matrix4f(stack.last().pose());
        stack.popPose();
        return pose;
    }

    static Matrix4f rotateSkyTo(PoseStack stack, SkyPos pos)
    {
        stack.pushPose();
        stack.mulPose(Axis.XN.rotation(pos.zenith()));
        stack.mulPose(Axis.YN.rotation(pos.azimuth()));
        final Matrix4f pose = new Matrix4f(stack.last().pose());
        stack.popPose();
        return pose;
    }
}
