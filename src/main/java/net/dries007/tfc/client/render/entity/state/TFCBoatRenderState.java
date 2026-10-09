/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** Boat attachments captured on the client tick side, never a live inventory. */
public final class TFCBoatRenderState extends BoatRenderState
{
    public @Nullable Identifier chestTexture;
}
