/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelPart;


public class FelinePredatorModel extends HierarchicalAnimatedModel
{
    private final AnimationDefinition sleep;
    private final AnimationDefinition walk;
    private final AnimationDefinition run;
    private final AnimationDefinition attack;

    public FelinePredatorModel(ModelPart root, AnimationDefinition sleep, AnimationDefinition walk, AnimationDefinition run, AnimationDefinition attack)
    {
        super(root);
        this.sleep = sleep;
        this.walk = walk;
        this.run = run;
        this.attack = attack;
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


        if (state.sleepingAnimation.isStarted())
        {
            setupSleeping();
            this.animate(state.sleepingAnimation, sleep, ageInTicks);
        }
        else
        {
            // swimming is animated as walking. animations can be swapped with no consequences!
            if (state.inWaterOrBubble || !state.aggressive || !state.movingOnLand)
            {
                this.animateWalk(walk, limbSwing, limbSwingAmount, 2.5f, 2.5f);
            }
            else
            {
                this.animateWalk(run, limbSwing, limbSwingAmount, 1f, 2.5f);
            }
            this.animate(state.attackingAnimation, attack, ageInTicks);
            setupHeadRotations(netHeadYaw, headPitch);
        }
    }

    public void setupHeadRotations(float yaw, float pitch)
    {

    }

    public void setupSleeping()
    {

    }
}
