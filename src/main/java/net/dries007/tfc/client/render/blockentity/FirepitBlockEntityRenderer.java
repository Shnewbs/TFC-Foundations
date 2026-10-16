/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blockentities.AbstractFirepitBlockEntity;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.FirepitBlock;
import net.dries007.tfc.util.Helpers;

public class FirepitBlockEntityRenderer<T extends AbstractFirepitBlockEntity<?>> extends ItemDisplayBlockEntityRenderer<T>
{
    public FirepitBlockEntityRenderer(BlockEntityRendererProvider.Context context) { super(context); }

    public static final Map<AbstractFirepitBlockEntity.BurnStage, List<Identifier>> BURN_STAGE_MODELS = Helpers.mapOf(AbstractFirepitBlockEntity.BurnStage.class, e -> {
        final String name = e.name().toLowerCase(Locale.ROOT);
        return List.of(
            RenderHelpers.modelId("block/firepit_log_1_" + name),
            RenderHelpers.modelId("block/firepit_log_2_" + name),
            RenderHelpers.modelId("block/firepit_log_3_" + name),
            RenderHelpers.modelId("block/firepit_log_4_" + name)
        );
    });

    @Override
    protected void extractItems(T firepit, float partialTick, PoseStack poseStack, State outputState, int combinedLight, int combinedOverlay)
    {
        if (firepit.getLevel() == null)
        {
            return;
        }

        for (int i = AbstractFirepitBlockEntity.SLOT_FUEL_CONSUME; i <= AbstractFirepitBlockEntity.SLOT_FUEL_INPUT; i++)
        {
            final ItemStack stack = firepit.getInventory().getStackInSlot(i);
            if (!stack.isEmpty())
            {
                poseStack.pushPose();
                if (firepit.getBlockState().getValue(FirepitBlock.AXIS) == Direction.Axis.Z)
                {
                    poseStack.translate(0.5f, 0.5f, 0.5f);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90f));
                    poseStack.translate(-0.5f, -0.5f, -0.5f);
                }

                if (firepit.getBlockState().is(TFCBlocks.STOVE.get()) || firepit.getBlockState().is(TFCBlocks.STOVE_POT.get()))
                {
                    poseStack.translate(0, 0.1875d, 0);
                }

                AbstractFirepitBlockEntity.BurnStage stage = firepit.getBurnStage(i);
                addBlockModel(outputState, poseStack, BURN_STAGE_MODELS.get(stage).get(i), firepit.getLevel(), firepit.getBlockPos(), firepit.getBlockState());
                poseStack.popPose();
            }
        }
    }
}
