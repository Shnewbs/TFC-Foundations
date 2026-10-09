/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.minecraft.client.model.animal.equine.DonkeyModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.DonkeyRenderState;

/** Adult-layout body and independently posed carried-item layer. */
public class TFCChestedHorseModel extends DonkeyModel
{
    private final ModelPart leftChest;
    private final ModelPart rightChest;
    private final boolean chestOnly;

    public TFCChestedHorseModel(ModelPart root, boolean chestOnly)
    {
        super(root);
        leftChest = body.getChild("left_chest");
        rightChest = body.getChild("right_chest");
        this.chestOnly = chestOnly;
    }

    @Override
    public void setupAnim(DonkeyRenderState state)
    {
        super.setupAnim(state);
        leftChest.visible = rightChest.visible = chestOnly && state.hasChest;
        for (ModelPart part : allParts())
            part.skipDraw = chestOnly && part != leftChest && part != rightChest;
        // Keep the adult texture layout for TFC carried items, including on juveniles.
        // A single root transform also keeps the two independent passes aligned.
        if (state.isBaby) AgeableModelTransforms.scalePart(root(), 0.5F, 24, 0);
    }
}
