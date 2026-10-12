/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class AnemometerModel extends Model<AnemometerModel.State>
{
    private final ModelPart spinny;

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition post = partdefinition.addOrReplaceChild("post", CubeListBuilder.create().texOffs(16, 15).addBox(-1.0F, -7.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 21).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition spinny = partdefinition.addOrReplaceChild("spinny", CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -11.001F, -5.5F, 11.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
            .texOffs(6, 18).addBox(-7.75F, -11.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 28.0F, 0.0F));

        PartDefinition cube_r1 = spinny.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(6, 18).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, 6.5F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r2 = spinny.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(6, 18).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, -6.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r3 = spinny.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(6, 18).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, -9.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public AnemometerModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.spinny = root.getChild("spinny");
    }

    @Override
    public void setupAnim(State state)
    {
        super.setupAnim(state);
        spinny.yRot = state.angle();
    }

    public record State(float angle)
    {
    }
}
