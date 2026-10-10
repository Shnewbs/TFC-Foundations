/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

/** Generic flattened ingredient codec; independent of Minecraft registries for testing. */
public final class FlatIngredientCodec
{
    private FlatIngredientCodec() {}

    /** A count-bearing ingredient encoded in the same JSON object, not nested. */
    public static <R, A> Codec<R> flat(Codec<A> ingredientCodec, Function<R, A> ingredient,
        ToIntFunction<R> size, BiFunction<A, Integer, R> constructor, String sizeKey, int defaultSize)
    {
        return Codec.of(new Encoder<>()
        {
            @Override
            public <T> DataResult<T> encode(R value, DynamicOps<T> ops, T prefix)
            {
                final int count = size.applyAsInt(value);
                if (count <= 0)
                    return DataResult.error(() -> sizeKey + " must be positive: " + count);
                return ingredientCodec.encodeStart(ops, ingredient.apply(value))
                    .flatMap(encoded -> ops.getMap(encoded))
                    .flatMap(map -> ops.mergeToMap(prefix, map))
                    .flatMap(withIngredient -> ops.mergeToMap(withIngredient, ops.createString(sizeKey), ops.createInt(count)));
            }
        }, new Decoder<>()
        {
            @Override
            public <T> DataResult<Pair<R, T>> decode(DynamicOps<T> ops, T input)
            {
                final DataResult<Integer> countResult = ops.get(input, sizeKey).result().isPresent()
                    ? ops.get(input, sizeKey).flatMap(ops::getNumberValue).map(Number::intValue)
                    : DataResult.success(defaultSize);
                return countResult.flatMap(count -> {
                    if (count <= 0)
                        return DataResult.error(() -> sizeKey + " must be positive: " + count);
                    return ingredientCodec.parse(ops, ops.remove(input, sizeKey))
                        .map(item -> Pair.of(constructor.apply(item, count), ops.empty()));
                });
            }
        }, "tfc:flat_" + sizeKey + "_ingredient");
    }
}
