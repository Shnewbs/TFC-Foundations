/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes.outputs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

/** Recipe data can load before default components bind; materialize stacks only when used. */
public record ItemOutput(@Nullable ItemStackTemplate template)
{
    public static final ItemOutput EMPTY = new ItemOutput(null);
    // Keep the existing JSON representation (including count: 1) while decoding into a native template.
    private static final Codec<ItemStackTemplate> TEMPLATE_CODEC = RecordCodecBuilder.create(i -> i.group(
        Item.CODEC.fieldOf("id").forGetter(ItemStackTemplate::item),
        ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(ItemStackTemplate::count),
        DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStackTemplate::components)
    ).apply(i, ItemStackTemplate::new));
    public static final Codec<ItemOutput> CODEC = TEMPLATE_CODEC.xmap(ItemOutput::new, ItemOutput::requiredTemplate);
    // Preserve the existing recipe network representation, where components are already bound.
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemOutput> STREAM_CODEC = ItemStack.STREAM_CODEC.map(ItemOutput::of, ItemOutput::copy);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemOutput> OPTIONAL_STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.map(ItemOutput::of, ItemOutput::copy);

    public static ItemOutput of(ItemStack stack)
    {
        return stack.isEmpty() ? EMPTY : new ItemOutput(ItemStackTemplate.fromNonEmptyStack(stack));
    }

    private ItemStackTemplate requiredTemplate()
    {
        if (template == null) throw new IllegalStateException("Required recipe output is empty");
        return template;
    }

    public ItemStack copy()
    {
        return template == null ? ItemStack.EMPTY : template.create();
    }

    public boolean isEmpty()
    {
        return template == null;
    }

    public Item getItem()
    {
        return requiredTemplate().item().value();
    }
}
