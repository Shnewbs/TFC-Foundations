/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelPart;


public class AquaticCritterModel extends HierarchicalAnimatedModel
{
    private final AnimationDefinition crawl;
    private final AnimationDefinition calm;
    private final AnimationDefinition swim;
    private final AnimationDefinition damage;

    public AquaticCritterModel(ModelPart root, AnimationDefinition crawl, AnimationDefinition calm, AnimationDefinition swim, AnimationDefinition damage)
    {
        super(root);
        this.crawl = crawl;
        this.calm = calm;
        this.swim = swim;
        this.damage = damage;
    }

    @Override
    public void setupAnim(TFCAnimalRenderState state)
    {
        super.setupAnim(state);
        final float limbSwing = state.walkAnimationPos;
        final float limbSwingAmount = state.walkAnimationSpeed;
        final float ageInTicks = state.ageInTicks;
        final float netHeadYaw = state.yRot;
        final float headPitch = state.xRot;

        if (state.hurtAnimation.isStarted())
        {
            animate(state.hurtAnimation, damage, ageInTicks);
        }
        else if (!state.onGround)
        {
            animateWalk(swim, limbSwing, limbSwingAmount, 1f, 2.5f);
        }
        else
        {
            if (state.idleAnimation.isStarted())
            {
                animate(state.idleAnimation, calm, ageInTicks);
            }
            else
            {
                animateWalk(crawl, limbSwing, limbSwingAmount, 1f, 2.5f);
            }
        }

    }
}
