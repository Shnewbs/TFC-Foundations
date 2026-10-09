/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen.button;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.AnvilScreen;
import net.dries007.tfc.common.component.forge.ForgeStep;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.dries007.tfc.util.Helpers;

public class AnvilStepButton extends Button
{
    private final ForgeStep step;

    public AnvilStepButton(ForgeStep step, int guiLeft, int guiTop)
    {
        super(guiLeft + step.buttonX(), guiTop + step.buttonY(), 16, 16, Helpers.translateEnum(step), button -> {
            ClientPacketDistributor.sendToServer(new ScreenButtonPacket(step.ordinal()));
        }, RenderHelpers.NARRATION);
        setTooltip(Tooltip.create(Helpers.translateEnum(step)));

        this.step = step;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, AnvilScreen.BACKGROUND, getX(), getY(), step.iconX(), step.iconY(), 16, 16, 16, 16, 256, 256);
    }
}
