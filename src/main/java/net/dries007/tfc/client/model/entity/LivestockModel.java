/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;

/** Whole-body juvenile transform for livestock whose heads belong to the body tree. */
public abstract class LivestockModel extends EntityModel<TFCAnimalRenderState>
{
    private final float babyBodyScale;
    private final float babyBodyYOffset;

    protected LivestockModel(ModelPart root, float babyBodyScale, float babyBodyYOffset)
    {
        super(root);
        if (!(babyBodyScale > 0) || !Float.isFinite(babyBodyScale) || !Float.isFinite(babyBodyYOffset))
        {
            throw new IllegalArgumentException("Juvenile model transform must be finite with a positive scale");
        }
        this.babyBodyScale = babyBodyScale;
        this.babyBodyYOffset = babyBodyYOffset;
    }

    @Override
    public void setupAnim(TFCAnimalRenderState state)
    {
        super.setupAnim(state);
        if (state.isBaby)
        {
            AgeableModelTransforms.scalePart(root, 1F / babyBodyScale, babyBodyYOffset, 0);
        }
    }
}
