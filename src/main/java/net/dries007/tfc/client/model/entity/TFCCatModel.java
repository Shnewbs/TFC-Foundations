/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.minecraft.client.model.animal.feline.AdultFelineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import net.dries007.tfc.client.render.entity.state.TFCCatRenderState;

public class TFCCatModel extends AdultFelineModel<TFCCatRenderState>
{
    public static LayerDefinition createBodyLayer(CubeDeformation deformation)
    {
        return LayerDefinition.create(AdultFelineModel.createBodyMesh(deformation), 64, 32);
    }

    public TFCCatModel(ModelPart root)
    {
        super(root);
    }

    @Override
    public void setupAnim(TFCCatRenderState state)
    {
        // The native feline path resets all parts and implements sitting and lying-down poses.
        super.setupAnim(state);
        if (state.isBaby)
        {
            // Retain the adult-layout texture and the legacy enlarged-head kitten proportions.
            AgeableModelTransforms.scalePart(head, 0.75F, 10, 4);
            AgeableModelTransforms.scalePart(body, 0.5F, 24, 0);
            AgeableModelTransforms.scalePart(tail1, 0.5F, 24, 0);
            AgeableModelTransforms.scalePart(tail2, 0.5F, 24, 0);
            AgeableModelTransforms.scalePart(leftHindLeg, 0.5F, 24, 0);
            AgeableModelTransforms.scalePart(rightHindLeg, 0.5F, 24, 0);
            AgeableModelTransforms.scalePart(leftFrontLeg, 0.5F, 24, 0);
            AgeableModelTransforms.scalePart(rightFrontLeg, 0.5F, 24, 0);
        }
    }
}
