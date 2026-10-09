/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.resources.Identifier;

/** Detached TFC pet appearance plus the native feline pose inputs. */
public final class TFCCatRenderState extends FelineRenderState
{
    public Identifier texture = Identifier.withDefaultNamespace("textures/entity/cat/cat_tabby.png");
    public boolean hasOwner;
    public boolean sleeping;
    public int collarColor = -1;
}
