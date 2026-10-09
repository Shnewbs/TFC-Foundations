/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.screen.button.PowderkegSealButton;
import net.dries007.tfc.common.blockentities.PowderkegBlockEntity;
import net.dries007.tfc.common.blocks.devices.PowderkegBlock;
import net.dries007.tfc.common.container.PowderkegContainer;
import net.dries007.tfc.util.Helpers;

public class PowderkegScreen extends BlockEntityScreen<PowderkegBlockEntity, PowderkegContainer>
{
    private static final Component SEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.seal_barrel");
    private static final Component UNSEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.unseal_barrel");

    public static final Identifier BACKGROUND = Helpers.identifier("textures/gui/powderkeg.png");

    public PowderkegScreen(PowderkegContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, BACKGROUND);
    }

    @Override
    public void init()
    {
        super.init();
        addRenderableWidget(new PowderkegSealButton(blockEntity, leftPos, topPos, isSealed() ? UNSEAL : SEAL));
    }

    private boolean isSealed()
    {
        return blockEntity.getBlockState().getValue(PowderkegBlock.SEALED);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor poseStack, int mouseX, int mouseY)
    {
        super.extractLabels(poseStack, mouseX, mouseY);
        if (isSealed())
        {
            highlightDisabledSlots(0, PowderkegBlockEntity.SLOTS - 1);
        }
    }
}
