/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.minecraft.client.model.animal.goat.GoatModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;

public class TFCGoatModel extends QuadrupedModel<TFCAnimalRenderState>
{
    public static LayerDefinition createBodyLayer()
    {
        return GoatModel.createBodyLayer();
    }

    public TFCGoatModel(ModelPart root)
    {
        super(root);
    }

    @Override
    public void setupAnim(TFCAnimalRenderState state)
    {
        super.setupAnim(state);
        head.yRot /= 3F;
        head.getChild("left_horn").visible = !state.isBaby;
        head.getChild("right_horn").visible = !state.isBaby;
        head.getChild("left_horn").y = state.femaleCharacteristics ? 2 : 0;
        head.getChild("right_horn").y = state.femaleCharacteristics ? 2 : 0;
        if (state.isBaby)
        {
            // Preserve the distinct head/body transforms of the old goat model.
            AgeableModelTransforms.scalePart(head, 1.5F / 2.5F, 19, 1);
            AgeableModelTransforms.scalePart(body, 0.5F, 24, 0);
            AgeableModelTransforms.scalePart(rightHindLeg, 0.5F, 24, 0);
            AgeableModelTransforms.scalePart(leftHindLeg, 0.5F, 24, 0);
            AgeableModelTransforms.scalePart(rightFrontLeg, 0.5F, 24, 0);
            AgeableModelTransforms.scalePart(leftFrontLeg, 0.5F, 24, 0);
        }
    }
}
