/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import net.minecraft.client.renderer.item.ItemStackRenderState;

/** Shared native layer tint handling for ordinary items and their breaking particles. */
public final class ItemRenderTint
{
    private ItemRenderTint() {}

    public static int firstColor(ItemStackRenderState.LayerRenderState[] layers, int count)
    {
        if (count == 0) return -1;
        final var tints = layers[0].tintLayers();
        return tints.isEmpty() ? -1 : tints.getInt(0);
    }

    public static void tintLastUncoloredLayer(ItemStackRenderState state, ItemStackRenderState.LayerRenderState[] layers, int count, int... colors)
    {
        if (count == 0) return;
        final var tints = layers[count - 1].tintLayers();
        if (tints.isEmpty())
        {
            for (int color : colors)
            {
                tints.add(color);
                state.appendModelIdentityElement(color);
            }
        }
    }
}
