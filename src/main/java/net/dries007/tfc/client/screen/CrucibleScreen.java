/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.blockentities.CrucibleBlockEntity;
import net.dries007.tfc.common.component.heat.Heat;
import net.dries007.tfc.common.component.mold.IMold;
import net.dries007.tfc.common.container.CrucibleContainer;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.network.PourFasterPacket;
import net.dries007.tfc.util.FluidAlloy;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.tooltip.Tooltips;

public class CrucibleScreen extends BlockEntityScreen<CrucibleBlockEntity, CrucibleContainer>
{
    private static final Identifier BACKGROUND = Helpers.identifier("textures/gui/crucible.png");
    private static final int MAX_ELEMENTS = 3;

    private int scrollPos;
    private boolean scrollPress;
    private int pourFasterDecayTicks = 0;

    public CrucibleScreen(CrucibleContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, BACKGROUND, 176, 221);

        inventoryLabelY = 127;

        scrollPos = 0;
        scrollPress = false;
    }

    @Override
    protected void containerTick()
    {
        if (pourFasterDecayTicks <= 0 && InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT))
        {
            if (hoveredSlot != null)
            {
                final IMold mold = IMold.get(hoveredSlot.getItem());
                if (mold != null)
                {
                    ClientPacketDistributor.sendToServer(new PourFasterPacket(blockEntity.getBlockPos(), hoveredSlot.index));
                    pourFasterDecayTicks = 10;
                }
            }
        }
        else
        {
            pourFasterDecayTicks--;
        }
        super.containerTick();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor stack, int mouseX, int mouseY)
    {
        // No-op - this screen basically doesn't have room for the inventory labels... how sad
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        final double mouseX = event.x();
        final double mouseY = event.y();
        final int button = event.button();
        if (mouseX >= leftPos + 154 && mouseX <= leftPos + 165 && mouseY >= topPos + 11 + scrollPos && mouseY <= topPos + 26 + scrollPos)
        {
            scrollPress = true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY)
    {
        final double mouseX = event.x();
        final double mouseY = event.y();
        final int button = event.button();
        if (scrollPress)
        {
            scrollPos = Math.min(Math.max((int) mouseY - topPos - 18, 0), 49);
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event)
    {
        final int button = event.button();
        if (scrollPress && button == 0)
        {
            scrollPress = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        // Draw the temperature indicator
        int temperature = Heat.scaleTemperatureForGui(blockEntity.getTemperature());
        if (temperature > 0)
        {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 7, topPos + 131 - Math.min(temperature, 51), 176, 0, 15, 5, 256, 256);
        }

        // Draw the scroll bar
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 154, topPos + 11 + scrollPos, 176, 7, 12, 15, 256, 256);

        // Draw the fluid + detailed content
        final FluidAlloy alloy = blockEntity.getAlloy();
        final FluidStack alloyResult = blockEntity.getAlloyResult();
        if (alloy.getAmount() > 0 && !alloyResult.isEmpty())
        {
            final TextureAtlasSprite sprite = RenderHelpers.getFluidSprite(alloyResult);
            final int fillHeight = (int) Math.ceil((float) 31 * alloy.getAmount() / blockEntity.containerInfo().fluidCapacity());

            RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + 97, topPos + 124 - fillHeight, 36, fillHeight, 16, 16, RenderHelpers.getFluidColor(alloyResult));


            // Draw Title:
            final Component resultText = alloyResult.getFluidType().getDescription().copy().withStyle(ChatFormatting.UNDERLINE);
            graphics.text(font, resultText, leftPos + 10, topPos + 11, 0xFF000000, false);

            int startElement = Math.max(0, (int) Math.floor(((alloy.getContent().size() - MAX_ELEMENTS) / 49D) * (scrollPos + 1)));

            // Draw Components
            int yPos = topPos + 22;
            int index = -1; // So the first +1 = 0
            for (Object2DoubleMap.Entry<Fluid> entry : alloy.getContent().object2DoubleEntrySet())
            {
                index++;
                if (index < startElement)
                {
                    continue;
                }
                if (index > startElement - 1 + MAX_ELEMENTS)
                {
                    break;
                }

                // Draw the content, format:
                // Metal name:
                //   XXX units (YY.Y%)
                // Metal 2 name:
                //   ZZZ units (WW.W%)

                final String metalName = font.plainSubstrByWidth(entry.getKey().getFluidType().getDescription().getString(), 141) + ":";
                // %s units (%s %)
                final MutableComponent content = Component.translatable("tfc.tooltip.crucible_content_line", Tooltips.fluidUnits(entry.getDoubleValue()), String.format("%2.1f", Math.round(1000 * entry.getDoubleValue() / alloy.getAmount()) / 10f));

                graphics.text(font, metalName, leftPos + 10, yPos, 0xFF404040, false);
                graphics.text(font, content, leftPos + 10, yPos + 9, 0xFF404040, false);
                yPos += 18;
            }
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (RenderHelpers.isInside(mouseX, mouseY, leftPos + 7, topPos + 131 - 51, 15, 51))
        {
            final var text = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(blockEntity.getTemperature());
            if (text != null)
            {
                graphics.setTooltipForNextFrame(font, text, mouseX, mouseY);
            }
        }
    }
}
