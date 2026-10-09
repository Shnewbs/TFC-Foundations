/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.container.ScribingTableContainer;
import net.dries007.tfc.network.ScribingTablePacket;
import net.dries007.tfc.util.Helpers;

public class ScribingTableScreen extends ItemCombinerScreen<ScribingTableContainer>
{
    private static final Identifier TEXTURE = Helpers.identifier("textures/gui/scribing_table.png");
    // Time in ticks
    private static final float ITEM_ROTATE_TIME = 50f;

    private EditBox name;
    private List<Item> valid;
    private int currentIndex = 0;
    private float currentTime = 0f;

    public ScribingTableScreen(ScribingTableContainer container, Inventory playerInv, Component name)
    {
        super(container, playerInv, name, TEXTURE);
        this.titleLabelX = 60;
        valid = List.of();
    }

    @Override
    protected void subInit()
    {
        name = new EditBox(font, leftPos + 62, topPos + 24, 103, 12, Component.translatable("container.repair"));
        name.setCanLoseFocus(false);
        name.setTextColor(-1);
        name.setTextColorUneditable(-1);
        name.setBordered(false);
        name.setMaxLength(50);
        name.setResponder(this::onNameChanged);
        name.setValue("");
        addRenderableWidget(name);
        setInitialFocus(name);
        name.setEditable(false);
        // Should this be done here?
        valid = Stream.concat(Helpers.allItems(TFCTags.Items.SCRIBING_INK), Helpers.allFluids(TFCTags.Fluids.USABLE_IN_SCRIBING_TABLE).map(Fluid::getBucket)).toList();
    }

    @Override
    public void resize(int width, int height)
    {
        String text = name.getValue();
        init(width, height);
        name.setValue(text);
        if (menu.getSlot(0).hasItem())
        {
            setFocused(name);
            name.setEditable(true);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (event.isEscape())
        {
            minecraft.player.closeContainer();
        }
        return name.keyPressed(event) || name.canConsumeInput() || super.keyPressed(event);
    }

    private void onNameChanged(String text)
    {
        if (!text.isEmpty())
        {
            Slot slot = menu.getSlot(AnvilMenu.INPUT_SLOT);
            if (slot.hasItem() && !slot.getItem().has(DataComponents.CUSTOM_NAME) && text.equals(slot.getItem().getHoverName().getString()))
            {
                text = "";
            }

            menu.setItemName(text);
            ClientPacketDistributor.sendToServer(new ScribingTablePacket(text));
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        super.extractLabels(graphics, mouseX, mouseY);
        if (menu.getSlot(0).hasItem())
        {
            Component component = null;
            if (!menu.getSlot(1).hasItem())
            {
                component = Component.translatable("tfc.tooltip.scribing_table.missing_ink");
            }
            else if (!ScribingTableContainer.isInkInput(menu.getSlot(1).getItem()))
            {
                component = Component.translatable("tfc.tooltip.scribing_table.invalid_ink");
            }
            if (component != null)
            {
                int k = this.imageWidth - 8 - this.font.width(component) - 2;
                graphics.fill(k - 2, 67, this.imageWidth - 8, 79, 1325400064);
                graphics.text(font, component, k, 69, 0xFFFF6060, false);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        this.currentTime += partialTicks;
        if (!valid.isEmpty() && this.currentTime > ITEM_ROTATE_TIME)
        {
            this.currentTime = this.currentTime % ITEM_ROTATE_TIME;
            this.currentIndex = (this.currentIndex + 1) % valid.size();
        }

        Slot itemSlot = menu.getSlot(0);
        Slot inkSlot = menu.getSlot(1);
        if (itemSlot.hasItem())
        {
            //graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXT_FIELD_SPRITE, this.leftPos + 59, this.topPos + 20, 110, 16);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 59, this.topPos + 20, 0, 166, 110, 16, 256, 256);
            if (!ScribingTableContainer.isInkInput(inkSlot.getItem()))
            {
                extractErrorIcon(graphics, mouseX, mouseY);
            }
        }
        else
        {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + 59, this.topPos + 20, 0, 182, 110, 16, 256, 256);
        }

        if (!inkSlot.hasItem() && !valid.isEmpty())
        {
            graphics.fakeItem(valid.get(this.currentIndex).getDefaultInstance(), this.leftPos + 76, this.topPos + 47);
            // Fade the suggestion into the slot without mutating global render state.
            graphics.fill(this.leftPos + 76, this.topPos + 47, this.leftPos + 92, this.topPos + 63, 0xBFC6C6C6);
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu menu, int slot, ItemStack stack)
    {
        if (slot == 0)
        {
            name.setValue(stack.isEmpty() ? "" : stack.getHoverName().getString());
            name.setEditable(!stack.isEmpty());
            setFocused(name);
        }
    }

    @Override
    protected void extractErrorIcon(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(this.menu.getResultSlot()).hasItem())
        {
            // copied from anvil... we may not have the texture?
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 99, topPos + 45, this.imageWidth, 0, 28, 21, 256, 256);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int x, int y)
    {
        super.extractTooltip(graphics, x, y);
        if (hoveredSlot != null && hoveredSlot.index == 1 && !hoveredSlot.hasItem() && !valid.isEmpty())
        {
            ItemStack hintItem = valid.get(this.currentIndex).getDefaultInstance();
            graphics.setTooltipForNextFrame(this.font, hintItem, x, y);
        }
    }
}
