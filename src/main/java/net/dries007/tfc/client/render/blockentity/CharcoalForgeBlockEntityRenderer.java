/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blockentities.CharcoalForgeBlockEntity;

public class CharcoalForgeBlockEntityRenderer extends ItemDisplayBlockEntityRenderer<CharcoalForgeBlockEntity>
{
    public CharcoalForgeBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    private static final Vec2[] POSITIONS = {
        new Vec2(0.25f, 0.25f),
        new Vec2(0.25f, 0.75f),
        new Vec2(0.75f, 0.25f),
        new Vec2(0.75f, 0.75f),
        new Vec2(0.5f, 0.5f),
        new Vec2(0.8f, 0.9f),
        new Vec2(0.6f, 0.9f),
        new Vec2(0.4f, 0.9f),
        new Vec2(0.2f, 0.9f),
    };

    @Override
    protected void extractItems(CharcoalForgeBlockEntity forge, float partialTicks, PoseStack poseStack, State output, int combinedLight, int combinedOverlay)
    {
        if (forge.getLevel() == null)
        {
            return;
        }


        for (int i = CharcoalForgeBlockEntity.SLOT_INPUT_MIN; i <= CharcoalForgeBlockEntity.SLOT_EXTRA_MAX; i++)
        {
            final ItemStack stack = forge.getInventory().getStackInSlot(i);
            if (stack.isEmpty())
                continue;
            final Vec2 pos = POSITIONS[i - CharcoalForgeBlockEntity.SLOT_INPUT_MIN];

            poseStack.pushPose();
            poseStack.translate(pos.x, 15f / 16f, pos.y);
            poseStack.scale(0.33f, 0.33f, 0.33f);
            poseStack.mulPose(Axis.YP.rotationDegrees(RenderHelpers.itemTimeRotation()));
            addItem(output, stack, ItemDisplayContext.FIXED, RenderHelpers.getHeatedBrightness(stack, combinedLight), combinedOverlay, poseStack, forge.getLevel(), 0);
            poseStack.popPose();
        }
    }
}
