/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.camel.AdultCamelModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.CamelRenderState;

import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;

/** Feeds captured TFC camel timelines into the native camel animation model. */
public final class DromedaryCamelModel extends EntityModel<TFCAnimalRenderState>
{
    private final AdultCamelModel model;
    private final CamelRenderState camel = new CamelRenderState();

    public DromedaryCamelModel(ModelPart root)
    {
        super(root);
        model = new AdultCamelModel(root);
    }

    @Override
    public void setupAnim(TFCAnimalRenderState state)
    {
        camel.yRot = state.yRot;
        camel.xRot = state.xRot;
        camel.ageInTicks = state.ageInTicks;
        camel.walkAnimationPos = state.walkAnimationPos;
        camel.walkAnimationSpeed = state.walkAnimationSpeed;
        camel.jumpCooldown = state.camelJumpCooldown;
        camel.sitAnimationState.copyFrom(state.sitAnimationState);
        camel.sitPoseAnimationState.copyFrom(state.sitPoseAnimationState);
        camel.sitUpAnimationState.copyFrom(state.sitUpAnimationState);
        camel.idleAnimationState.copyFrom(state.idleAnimationState);
        camel.dashAnimationState.copyFrom(state.dashAnimationState);
        model.setupAnim(camel);
    }
}
