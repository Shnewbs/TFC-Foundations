/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.BoatRenderState;

/** Uses the native chest/raft UV layout without submitting a second hull or paddles. */
public final class BoatChestModel extends EntityModel<BoatRenderState>
{
    private final ModelPart bottom;
    private final ModelPart lid;
    private final ModelPart lock;

    public BoatChestModel(ModelPart root)
    {
        super(root);
        bottom = root.getChild("chest_bottom");
        lid = root.getChild("chest_lid");
        lock = root.getChild("chest_lock");
    }

    @Override
    public void setupAnim(BoatRenderState state)
    {
        super.setupAnim(state);
        for (ModelPart part : allParts())
        {
            // skipDraw retains the parent transform and traversal of its children.
            part.skipDraw = part != bottom && part != lid && part != lock;
        }
    }
}
