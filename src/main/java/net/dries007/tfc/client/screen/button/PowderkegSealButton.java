/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.PowderkegScreen;
import net.dries007.tfc.common.blockentities.PowderkegBlockEntity;
import net.dries007.tfc.common.blocks.devices.PowderkegBlock;
import net.dries007.tfc.network.ScreenButtonPacket;

public class PowderkegSealButton extends Button
{
    private final PowderkegBlockEntity powderkeg;

    public PowderkegSealButton(PowderkegBlockEntity powderkeg, int guiLeft, int guiTop, Component tooltip)
    {
        super(guiLeft + 123, guiTop + 35, 20, 20, tooltip, b -> {}, RenderHelpers.NARRATION);
        setTooltip(Tooltip.create(tooltip));
        this.powderkeg = powderkeg;
    }

    @Override
    public void onPress(InputWithModifiers input)
    {
        ClientPacketDistributor.sendToServer(new ScreenButtonPacket(0));
        playDownSound(Minecraft.getInstance().getSoundManager());
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        final int v = powderkeg.getBlockState().getValue(PowderkegBlock.SEALED) ? 0 : 20;
        graphics.blit(RenderPipelines.GUI_TEXTURED, PowderkegScreen.BACKGROUND, getX(), getY(), 236, v, 20, 20, 256, 256);
    }
}
