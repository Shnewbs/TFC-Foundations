/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

/** Retains item-specific colors for legacy TFC models that have no authored native tint sources. */
public final class ItemTintRegistration
{
    private static final Map<Item, Color> COLORS = new IdentityHashMap<>();

    public void register(Color color, ItemLike... items)
    {
        for (ItemLike item : items) COLORS.put(item.asItem(), color);
    }

    public static @Nullable Color get(ItemStack stack) { return COLORS.get(stack.getItem()); }

    @FunctionalInterface
    public interface Color
    {
        int getColor(ItemStack stack, int tintIndex);
    }
}
