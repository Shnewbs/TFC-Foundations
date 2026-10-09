/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.DonkeyRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** Resolved carried-item appearance; no entity or mutable inventory reference. */
public final class TFCChestedHorseRenderState extends DonkeyRenderState
{
    public @Nullable Identifier chestTexture;
}
