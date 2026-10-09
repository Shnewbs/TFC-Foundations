/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class WindmillBladeLatticeModel extends WindmillBladeModel
{
    public WindmillBladeLatticeModel(ModelPart root)
    {
        super(root);
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.5F, -94.5F, 4.0F, 3.0F, 96.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition blade = partdefinition.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(98, 29).addBox(-1.0F, 1.5F, -12.5F, 2.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(17, 16).addBox(-1.0F, 13.5F, -93.5F, 2.0F, 1.0F, 81.0F, new CubeDeformation(0.0F))
            .texOffs(98, 29).addBox(-1.0F, 1.5F, -94.5F, 2.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(0, 163).addBox(-1.0F, 1.5F, -93.5F, 1.0F, 12.0F, 81.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }
}
