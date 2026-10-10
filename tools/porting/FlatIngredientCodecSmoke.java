/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.dries007.tfc.common.recipes.FlatIngredientCodec;

public final class FlatIngredientCodecSmoke {
    private record Sized(String ingredient, int count) {}
    private static final Codec<Sized> ITEM = FlatIngredientCodec.flat(
        Codec.STRING.fieldOf("item").codec(), Sized::ingredient, Sized::count, Sized::new, "count", 1);
    private static final Codec<Sized> TAG = FlatIngredientCodec.flat(
        Codec.STRING.fieldOf("tag").codec(), Sized::ingredient, Sized::count, Sized::new, "count", 1);
    private static final Codec<Sized> FLUID = FlatIngredientCodec.flat(
        Codec.STRING.fieldOf("fluid").codec(), Sized::ingredient, Sized::count, Sized::new, "amount", 1000);
    private static int tests;

    private static void require(boolean yes, String why) {
        tests++;
        if (!yes) throw new AssertionError(why);
    }
    private static Sized parse(Codec<Sized> codec, String json) {
        return codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    }
    private static String write(Codec<Sized> codec, Sized value) {
        return codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow().toString();
    }
    private static boolean invalid(Codec<Sized> codec, String json) {
        return codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).error().isPresent();
    }
    public static void main(String[] args) {
        require(parse(ITEM, "{\"item\":\"minecraft:stick\",\"count\":3}").equals(new Sized("minecraft:stick",3)), "legacy item schema");
        require(parse(TAG, "{\"tag\":\"tfc:wool\",\"count\":5}").equals(new Sized("tfc:wool",5)), "legacy tag schema");
        require(parse(FLUID, "{\"fluid\":\"minecraft:water\",\"amount\":250}").equals(new Sized("minecraft:water",250)), "legacy fluid schema");
        require(parse(ITEM, "{\"item\":\"minecraft:stick\"}").count()==1, "default item count");
        require(parse(FLUID, "{\"fluid\":\"minecraft:water\"}").count()==1000, "default fluid amount");
        require(write(ITEM, new Sized("minecraft:stick",2)).contains("\"count\":2"), "count encode");
        require(write(ITEM, new Sized("minecraft:stick",2)).contains("\"item\":\"minecraft:stick\""), "flat item encode");
        require(!write(ITEM,new Sized("minecraft:stick",2)).contains("\"ingredient\""), "not nested");
        require(write(FLUID,new Sized("minecraft:water",300)).contains("\"amount\":300"), "amount encode");
        require(invalid(ITEM,"{\"item\":\"minecraft:stick\",\"count\":0}"),"reject zero");
        require(invalid(FLUID,"{\"fluid\":\"minecraft:water\",\"amount\":-1}"),"reject negative");
        require(invalid(ITEM,"{\"item\":\"minecraft:stick\",\"count\":\"four\"}"),"reject string count");
        require(invalid(FLUID,"{\"fluid\":\"minecraft:water\",\"amount\":\"many\"}"),"reject string amount");
        require(invalid(ITEM,"{\"count\":2}"),"missing item fails");
        require(!write(TAG,new Sized("tfc:wool",2)).contains("\"ingredient\""),"flat tag encode");
        require(!write(FLUID,new Sized("minecraft:water",400)).contains("\"ingredient\""),"flat fluid encode");
        System.out.println("PASS: "+tests+" legacy flat ingredient codec checks");
    }
}
