/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import java.util.function.Function;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;
import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderStateExtractor;

public class SimpleMobRenderer<T extends Mob, M extends EntityModel<? super TFCAnimalRenderState>> extends MobRenderer<T, TFCAnimalRenderState, M>
{
    private final Identifier texture;
    @Nullable
    private final Identifier babyTexture;
    private final Function<T, Identifier> textureGetter;
    private final boolean doesFlop;
    private final float scale;

    public SimpleMobRenderer(EntityRendererProvider.Context ctx, M model, String name, float shadow, boolean flop, float scale, boolean hasBabyTexture, boolean itemInMouth, @Nullable Function<T, Identifier> textureGetter)
    {
        super(ctx, model, shadow);
        doesFlop = flop;
        texture = RenderHelpers.animalTexture(name);
        babyTexture = hasBabyTexture ? RenderHelpers.animalTexture(name + "_young") : null;
        this.textureGetter = textureGetter != null ? textureGetter : e -> babyTexture != null && e.isBaby() ? babyTexture : texture;
        this.scale = scale;
        // todo: re-add item in mouth layer when i can figure out how the heck to render it right.
    }

    @Override
    public TFCAnimalRenderState createRenderState()
    {
        return new TFCAnimalRenderState();
    }

    @Override
    public void extractRenderState(T entity, TFCAnimalRenderState state, float partialTick)
    {
        super.extractRenderState(entity, state, partialTick);
        TFCAnimalRenderStateExtractor.extract(entity, state, partialTick);
        state.texture = textureGetter.apply(entity);
    }

    @Override
    protected void setupRotations(TFCAnimalRenderState state, PoseStack poseStack, float bodyRot, float scale)
    {
        super.setupRotations(state, poseStack, bodyRot, scale);
        if (doesFlop)
        {
            // Preserve the field-guide origin exemption.
            if (state.atGuideOrigin)
                return;
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(0.6F * state.ageInTicks)));
            if (!state.isInWater)
            {
                poseStack.translate(0.1f, 0.1f, -0.1f);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
            }
        }
    }

    @Override
    protected void scale(TFCAnimalRenderState state, PoseStack poseStack)
    {
        float amount = state.isBaby ? this.scale * 0.7f : this.scale;
        poseStack.scale(amount, amount, amount);
        super.scale(state, poseStack);
    }

    @Override
    public Identifier getTextureLocation(TFCAnimalRenderState state)
    {
        return state.texture != null ? state.texture : texture;
    }

    public static class Builder<T extends Mob, M extends EntityModel<? super TFCAnimalRenderState>>
    {
        private final EntityRendererProvider.Context ctx;
        private final Function<ModelPart, M> model;
        private final String name;

        private float shadow = 0.3f;
        private boolean flop = false;
        private float scale = 1f;
        private boolean hasBabyTexture = false;
        private boolean itemInMouth = false;
        @Nullable private Function<T, Identifier> textureGetter = null;

        public Builder(EntityRendererProvider.Context ctx, Function<ModelPart, M> model, String name)
        {
            this.ctx = ctx;
            this.model = model;
            this.name = name;
        }

        public Builder<T, M> flops()
        {
            this.flop = true;
            return this;
        }

        public Builder<T, M> shadow(float size)
        {
            this.shadow = size;
            return this;
        }

        public Builder<T, M> scale(float scale)
        {
            this.scale = scale;
            return this;
        }

        public Builder<T, M> hasBabyTexture()
        {
            this.hasBabyTexture = true;
            return this;
        }

        public Builder<T, M> mouthy()
        {
            this.itemInMouth = true;
            return this;
        }

        public Builder<T, M> texture(Function<T, Identifier> getter)
        {
            this.textureGetter = getter;
            return this;
        }

        public SimpleMobRenderer<T, M> build()
        {
            return new SimpleMobRenderer<>(ctx, model.apply(RenderHelpers.bakeSimple(ctx, name)), name, shadow, flop, scale, hasBabyTexture, itemInMouth, textureGetter);
        }
    }

}
