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
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.network.ScreenButtonPacket;

public class KnappingButton extends Button
{
    public int id;
    private final Identifier texture;
    private final Holder<SoundEvent> sound;

    public KnappingButton(int id, int x, int y, int width, int height, Identifier texture, Holder<SoundEvent> sound)
    {
        this(id, x, y, width, height, texture, sound, button -> {});
    }

    public KnappingButton(int id, int x, int y, int width, int height, Identifier texture, Holder<SoundEvent> sound, OnPress onPress)
    {
        super(x, y, width, height, Component.empty(), onPress, RenderHelpers.NARRATION);
        this.id = id;
        this.texture = texture;
        this.sound = sound;
    }

    @Override
    public void onPress(InputWithModifiers input)
    {
        onPress.onPress(this);
        if (active)
        {
            visible = false;
            ClientPacketDistributor.sendToServer(new ScreenButtonPacket(id));
            playDownSound(Minecraft.getInstance().getSoundManager());
        }
    }

    @Override
    public void playDownSound(SoundManager handler)
    {
        handler.play(SimpleSoundInstance.forUI(sound, 1.0F));
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        if (visible)
        {
            int x = getX();
            int y = getY();
            isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, 16, 16, 16, 16);
        }
    }

    public Identifier getTexture()
    {
        return texture;
    }
}
