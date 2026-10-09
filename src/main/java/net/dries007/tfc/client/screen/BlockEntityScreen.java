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

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;

public class BlockEntityScreen<T extends InventoryBlockEntity<?>, C extends BlockEntityContainer<T>> extends TFCContainerScreen<C>
{
    protected final T blockEntity;
    private int disabledSlotStart = -1;
    private int disabledSlotEnd = -1;

    public BlockEntityScreen(C container, Inventory playerInventory, Component name, Identifier texture)
    {
        this(container, playerInventory, name, texture, 176, 166);
    }

    public BlockEntityScreen(C container, Inventory playerInventory, Component name, Identifier texture, int imageWidth, int imageHeight)
    {
        super(container, playerInventory, name, texture, imageWidth, imageHeight);
        this.blockEntity = container.getBlockEntity();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        disabledSlotStart = disabledSlotEnd = -1;
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        super.extractSlots(graphics, mouseX, mouseY);
        // Container labels are extracted before slots in 26.x; tint disabled slots after their items.
        if (disabledSlotStart >= 0)
        {
            menu.slots.stream()
                .filter(slot -> slot.index <= disabledSlotEnd && slot.index >= disabledSlotStart)
                .forEach(slot -> graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x80FFFFFF));
        }
    }

    protected void highlightDisabledSlots(int start, int end)
    {
        disabledSlotStart = start;
        disabledSlotEnd = end;
    }
}
