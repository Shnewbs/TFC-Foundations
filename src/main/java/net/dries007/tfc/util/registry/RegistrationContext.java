/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Carries the actual deferred entry key through legacy supplier factories, including shared properties. */
public final class RegistrationContext
{
    private static final ScopedValue<Entry<Block>> BLOCK = ScopedValue.newInstance();
    private static final ScopedValue<Entry<Item>> ITEM = ScopedValue.newInstance();

    private RegistrationContext() {}

    public static <T extends Block> T block(Identifier id, Supplier<T> factory)
    {
        return ScopedValue.where(BLOCK, new Entry<>(ResourceKey.create(Registries.BLOCK, id))).call(factory::get);
    }

    public static <T extends Item> T item(Identifier id, Supplier<T> factory)
    {
        return ScopedValue.where(ITEM, new Entry<>(ResourceKey.create(Registries.ITEM, id))).call(factory::get);
    }

    /** Called before native properties first derive a loot-table key. Outside these factories, do nothing. */
    public static void bind(BlockBehaviour.Properties properties)
    {
        if (BLOCK.isBound()) properties.setId(BLOCK.get().claim(properties));
    }

    /** Called before native properties first derive description/model/component-initializer keys. */
    public static void bind(Item.Properties properties)
    {
        if (ITEM.isBound()) properties.setId(ITEM.get().claim(properties));
    }

    private static final class Entry<T>
    {
        private final ResourceKey<T> key;
        private Object properties;

        private Entry(ResourceKey<T> key) { this.key = key; }

        private ResourceKey<T> claim(Object properties)
        {
            if (this.properties != null && this.properties != properties)
                throw new IllegalStateException("One registry factory constructed multiple entries for " + key);
            this.properties = properties;
            return key;
        }
    }
}
