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
import org.jspecify.annotations.Nullable;

/** Independent, immutable animation input for each deferred frame/blade/extra draw. */
public class WindmillBladeModel extends Model<WindmillBladeModel.BladePose>
{
    public enum Portion { FRAME, BLADE, EXTRAS }
    public record BladePose(float angle, Portion portion) {}

    private final ModelPart blade;
    private final ModelPart main;
    private final @Nullable ModelPart extras;

    public WindmillBladeModel(ModelPart root)
    {
        super(root, RenderTypes::entityCutout);
        blade = root.getChild("blade");
        main = root.getChild("main");
        extras = root.hasChild("extras") ? root.getChild("extras") : null;
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition blade = partdefinition.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(0, 99).addBox(-1.0F, 1.5F, -94.5F, 2.0F, 13.0F, 80.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -1.5F, -94.5F, 4.0F, 3.0F, 96.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    public final boolean hasExtras()
    {
        return extras != null;
    }

    @Override
    public void setupAnim(BladePose state)
    {
        super.setupAnim(state);
        main.xRot = blade.xRot = -state.angle();
        main.visible = state.portion() == Portion.FRAME;
        blade.visible = state.portion() == Portion.BLADE;
        if (extras != null)
        {
            extras.xRot = -state.angle();
            extras.visible = state.portion() == Portion.EXTRAS;
        }
    }
}
