/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.util.function.Consumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.model.entity.JavelinModel;

/** Reload-owned native special renderer with the original TFC geometry and material. */
public record JavelinItemRenderer(JavelinModel model, Identifier texture) implements NoDataSpecialModelRenderer
{
    @Override
    public void submit(PoseStack poses, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline)
    {
        poses.pushPose();
        poses.scale(1, -1, -1);
        collector.submitModelPart(model.root(), poses, model.renderType(texture), light, overlay, null, false, foil, -1, null, outline);
        poses.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output)
    {
        final PoseStack poses = new PoseStack();
        poses.scale(1, -1, -1);
        model.root().getExtentsForGui(poses, output);
    }

    public record Unbaked(Identifier texture) implements NoDataSpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = Identifier.CODEC.fieldOf("texture").xmap(Unbaked::new, Unbaked::texture);
        @Override public MapCodec<Unbaked> type() { return CODEC; }
        @Override public JavelinItemRenderer bake(SpecialModelRenderer.BakingContext context)
        {
            return new JavelinItemRenderer(new JavelinModel(context.entityModelSet().bakeLayer(RenderHelpers.layerId("javelin"))), texture);
        }
    }
}
