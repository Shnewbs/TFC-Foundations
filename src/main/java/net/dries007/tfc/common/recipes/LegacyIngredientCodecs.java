/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

/** Preserve TFC's ingredient data format while using the native ingredient implementations. */
public final class LegacyIngredientCodecs
{
    public static final Codec<Ingredient> ITEM = compatible(Codec.lazyInitialized(() -> Ingredient.CODEC), "item");
    public static final Codec<FluidIngredient> FLUID = compatible(Codec.lazyInitialized(() -> FluidIngredient.CODEC), "fluid");
    private static final Set<String> CHILDREN = Set.of("children", "ingredients", "base", "subtracted", "ingredient");

    public static final MapCodec<ShapedRecipePattern> SHAPED_PATTERN = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) { return ShapedRecipePattern.MAP_CODEC.keys(ops); }

        @Override
        public <T> DataResult<ShapedRecipePattern> decode(DynamicOps<T> ops, MapLike<T> input)
        {
            final T adapted = transformPattern(ops, ops.createMap(input.entries()), true);
            return ops.getMap(adapted).flatMap(map -> ShapedRecipePattern.MAP_CODEC.decode(ops, map));
        }

        @Override
        public <T> RecordBuilder<T> encode(ShapedRecipePattern pattern, DynamicOps<T> ops, RecordBuilder<T> builder)
        {
            final var encoded = ShapedRecipePattern.MAP_CODEC.codec().encodeStart(ops, pattern)
                .map(value -> transformPattern(ops, value, false)).flatMap(ops::getMap);
            builder.withErrorsFrom(encoded);
            encoded.result().ifPresent(map -> map.entries().forEach(pair -> builder.add(pair.getFirst(), pair.getSecond())));
            return builder;
        }
    };

    private static <T> T transformPattern(DynamicOps<T> ops, T input, boolean nativeSyntax)
    {
        return ops.createMap(ops.getMap(input).result().orElseThrow().entries().map(pair -> {
            if (!ops.getStringValue(pair.getFirst()).result().orElse("").equals("key")) return pair;
            final var keyMap = ops.getMap(pair.getSecond()).result();
            if (keyMap.isEmpty()) return pair;
            final T key = ops.createMap(keyMap.get().entries().map(entry ->
                Pair.of(entry.getFirst(), nativeSyntax ? toNative(ops, entry.getSecond(), "item") : toLegacy(ops, entry.getSecond(), "item"))));
            return Pair.of(pair.getFirst(), key);
        }));
    }

    private static <A> Codec<A> compatible(Codec<A> nativeCodec, String itemKey)
    {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input)
            {
                return nativeCodec.parse(ops, toNative(ops, input, itemKey)).map(value -> Pair.of(value, ops.empty()));
            }

            @Override
            public <T> DataResult<T> encode(A value, DynamicOps<T> ops, T prefix)
            {
                return nativeCodec.encodeStart(ops, value).flatMap(encoded -> {
                    final T legacy = toLegacy(ops, encoded, itemKey);
                    if (ops.getMap(legacy).result().isPresent()) return ops.getMap(legacy).flatMap(map -> ops.mergeToMap(prefix, map));
                    if (ops.getStream(legacy).result().isPresent()) return ops.getStream(legacy).flatMap(stream -> ops.mergeToList(prefix, stream.toList()));
                    return ops.mergeToPrimitive(prefix, legacy);
                });
            }
        };
    }

    private static <T> T toNative(DynamicOps<T> ops, T input, String itemKey)
    {
        final var list = ops.getStream(input).result();
        if (list.isPresent())
        {
            final List<T> children = list.get().map(child -> toNative(ops, child, itemKey)).toList();
            // Native holder lists cannot contain tags or custom ingredients. The registered union retains their OR semantics.
            return ops.createMap(Stream.of(
                Pair.of(ops.createString("neoforge:ingredient_type"), ops.createString("neoforge:compound")),
                Pair.of(ops.createString("children"), ops.createList(children.stream()))));
        }
        final var map = ops.getMap(input).result();
        if (map.isEmpty()) return input;
        final var entries = map.get().entries().toList();
        if (entries.size() == 1)
        {
            final var item = map.get().get(itemKey);
            if (item != null) return item;
            final var tag = map.get().get("tag");
            if (tag != null && ops.getStringValue(tag).result().isPresent())
                return ops.createString("#" + ops.getStringValue(tag).result().orElseThrow());
        }
        return ops.createMap(entries.stream().map(entry -> {
            final String key = ops.getStringValue(entry.getFirst()).result().orElse("");
            final T value = CHILDREN.contains(key)
                ? transformChildren(ops, entry.getSecond(), itemKey, true) : entry.getSecond();
            return Pair.of(ops.createString(key.equals("type") ? "neoforge:ingredient_type" : key), value);
        }));
    }

    private static <T> T toLegacy(DynamicOps<T> ops, T input, String itemKey)
    {
        final var string = ops.getStringValue(input).result();
        if (string.isPresent())
        {
            final String value = string.get();
            return ops.createMap(Stream.of(Pair.of(ops.createString(value.startsWith("#") ? "tag" : itemKey),
                ops.createString(value.startsWith("#") ? value.substring(1) : value))));
        }
        final var list = ops.getStream(input).result();
        if (list.isPresent()) return ops.createMap(Stream.of(
            Pair.of(ops.createString("type"), ops.createString("neoforge:compound")),
            Pair.of(ops.createString("children"), ops.createList(list.get().map(child -> toLegacy(ops, child, itemKey))))));
        final var map = ops.getMap(input).result();
        if (map.isEmpty()) return input;
        return ops.createMap(map.get().entries().map(entry -> {
            final String key = ops.getStringValue(entry.getFirst()).result().orElse("");
            final T value = CHILDREN.contains(key)
                ? transformChildren(ops, entry.getSecond(), itemKey, false) : entry.getSecond();
            return Pair.of(ops.createString(key.equals("neoforge:ingredient_type") ? "type" : key), value);
        }));
    }

    private static <T> T transformChildren(DynamicOps<T> ops, T value, String itemKey, boolean nativeSyntax)
    {
        final var list = ops.getStream(value).result();
        if (list.isPresent()) return ops.createList(list.get().map(child -> nativeSyntax ? toNative(ops, child, itemKey) : toLegacy(ops, child, itemKey)));
        return nativeSyntax ? toNative(ops, value, itemKey) : toLegacy(ops, value, itemKey);
    }

    private LegacyIngredientCodecs() {}
}
