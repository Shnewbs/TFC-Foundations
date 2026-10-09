/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import org.joml.Matrix3x2fStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ScreenParticle
{
    private float x;
    private float y;
    private float dx;
    private float dy;
    private float rotation;
    private int lifetime;


    private final Identifier texture;
    private final int width;
    private final int height;
    private final int rotationSign;
    private final float scale;

    public ScreenParticle(Identifier texture, float x, float y, float dx, float dy, int width, int height, RandomSource random)
    {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.rotationSign = random.nextBoolean() ? 1 : -1;
        this.scale = Mth.nextFloat(random, 0.25f, 0.6f);
        this.lifetime = 35;

        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphicsExtractor graphics)
    {
        final Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();

        poseStack.translate(x, y);
        poseStack.rotate(rotation * Mth.DEG_TO_RAD);
        poseStack.scale(scale, scale);

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0, 0, width, height, width, height);

        poseStack.popMatrix();
    }

    public void tick()
    {
        x += dx;
        y += dy;
        dx *= 0.97f;
        dy *= 1.03f;
        rotation += (2f * rotationSign);
        lifetime--;
    }

    public boolean shouldBeRemoved()
    {
        return lifetime <= 0;
    }
}
