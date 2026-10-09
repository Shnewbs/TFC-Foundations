/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import org.joml.Matrix3x2fStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.common.entities.livestock.pet.TamableMammal;
import net.dries007.tfc.network.PetCommandPacket;
import net.dries007.tfc.util.Helpers;

public class PetCommandScreen extends Screen
{
    private final TamableMammal entity;

    public PetCommandScreen(TamableMammal entity)
    {
        super(Component.translatable("tfc.screen.pet_command"));
        this.entity = entity;
    }

    @Override
    protected void init()
    {
        super.init();
        int y = 72;
        for (TamableMammal.Command command : TamableMammal.Command.VALUES)
        {
            if (entity.willListenTo(command, true))
            {
                MutableComponent comp = Helpers.translateEnum(command);
                addRenderableWidget(Button.builder(comp, b -> {
                        ClientPacketDistributor.sendToServer(new PetCommandPacket(entity, command));
                        Minecraft.getInstance().gui.setScreen(null);

                        final Player player = ClientHelpers.getPlayer();
                        if (player != null)
                        {
                            player.containerMenu = player.inventoryMenu;
                        }
                    }).bounds(width / 2 - 100, height / 4 + y, 200, 20).build()
                );
                y += 24;
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        graphics.fillGradient(0, 0, width, height, -1072689136, -804253680);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.scale(2.0F, 2.0F);
        graphics.centeredText(font, title, width / 2 / 2, 30, 0xFFFFFFFF);
        poseStack.popMatrix();

        for (var widget : children())
        {
            if (widget instanceof Button button && button.isHoveredOrFocused())
            {
                graphics.setTooltipForNextFrame(font, button.getMessage(), mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
