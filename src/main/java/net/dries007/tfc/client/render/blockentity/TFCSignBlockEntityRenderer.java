/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;

/** Native standing/wall geometry and both text faces use the already registered TFC WoodTypes. */
public class TFCSignBlockEntityRenderer extends StandingSignRenderer
{
    public TFCSignBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }
}
