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
import net.minecraft.util.Mth;

public class CalendarClockModel extends Model<CalendarClockModel.State>
{
    private final ModelPart month_hand;
    private final ModelPart hour_hand;
    private final ModelPart minute_hand;

    public CalendarClockModel(ModelPart root)
    {
        super(root, RenderTypes::entityCutout);
        this.month_hand = root.getChild("month_hand");
        this.hour_hand = root.getChild("hour_hand");
        this.minute_hand = root.getChild("minute_hand");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition static_parts = partdefinition.addOrReplaceChild("static_parts", CubeListBuilder.create().texOffs(0, 15).addBox(-15.0F, -1.0F, 1.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0).addBox(-15.0F, -2.0F, 1.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition month_hand = partdefinition.addOrReplaceChild("month_hand", CubeListBuilder.create().texOffs(15, 30).addBox(-0.5F, -2.49F, -6.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.25F, 0.0F));

        PartDefinition hour_hand = partdefinition.addOrReplaceChild("hour_hand", CubeListBuilder.create().texOffs(29, 30).addBox(-0.5F, -1.51F, -4.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.25F, 0.0F));

        PartDefinition minute_hand = partdefinition.addOrReplaceChild("minute_hand", CubeListBuilder.create().texOffs(0, 30).addBox(-0.5F, -2.52F, -6.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 25.25F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void setupAnim(State state)
    {
        super.setupAnim(state);
        minute_hand.yRot = state.minuteAngle() + Mth.PI;
        hour_hand.yRot = state.hourAngle() + Mth.PI;
        month_hand.yRot = state.monthAngle() + Mth.PI;
    }

    public record State(float minuteAngle, float hourAngle, float monthAngle)
    {
    }
}
