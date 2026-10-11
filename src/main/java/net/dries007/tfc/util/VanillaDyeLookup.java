/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;

/** Gets vanilla dye items by their stable registry identities in Minecraft 26.1.2. */
public final class VanillaDyeLookup
{
    private VanillaDyeLookup() {}

    public static DyeItem byColor(DyeColor color)
    {
        return (DyeItem) BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getSerializedName() + "_dye"));
    }
}
