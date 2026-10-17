/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.SourceFilter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.dries007.tfc.client.TFCItemSpriteSource;
import net.dries007.tfc.test.TestSetup;

import static org.junit.jupiter.api.Assertions.*;

public class AtlasAssetsTest implements TestSetup
{
    @Test
    public void blockUsedItemSpritesResolveExclusivelyToTheBlocksAtlas() throws Exception
    {
        final Path project = Path.of(System.getProperty("tfc.test.projectDir"));
        final Path resources = project.resolve("src/main/resources/assets");
        final var required = new HashSet<Identifier>();
        try (var paths = Files.walk(resources.resolve("tfc/models/block")))
        {
            for (var path : paths.filter(path -> path.toString().endsWith(".json")).toList())
                collectItemSprites(JsonParser.parseString(Files.readString(path)), required);
        }
        final var atlas = JsonParser.parseString(Files.readString(project.resolve("build/resources/main/assets/minecraft/atlases/blocks.json"))).getAsJsonObject();
        final var location = new PackLocationInfo("tfc-test", Component.literal("tfc-test"), PackSource.BUILT_IN, Optional.empty());
        final var output = new SpriteCollector();
        try (var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of(new PathPackResources(location, project.resolve("build/resources/main")))))
        {
            assertTrue(atlas.getAsJsonArray("sources").asList().stream().anyMatch(source -> source.getAsJsonObject().get("type").getAsString().equals("tfc:item_directory")));
            TFCItemSpriteSource.INSTANCE.run(manager, output);
        }
        assertEquals(14, required.size(), "All block-used TFC item images must be covered; vanilla groundcover uses separate sprite aliases");
        for (var sprite : required)
            assertTrue(output.resources.containsKey(sprite), "Native block baking rejects item-atlas sprite " + sprite);

        final var items = JsonParser.parseString(Files.readString(project.resolve("build/resources/main/assets/minecraft/atlases/items.json"))).getAsJsonObject();
        final var filter = SourceFilter.MAP_CODEC.codec().parse(JsonOps.INSTANCE, items.getAsJsonArray("sources").get(0)).getOrThrow();
        final var vanilla = Identifier.parse("minecraft:item/flint");
        final var other = Identifier.parse("other:item/example");
        output.resources.put(vanilla, null);
        output.resources.put(other, null);
        filter.run(null, output);
        assertEquals(Set.of(vanilla, other), output.resources.keySet(), "Only TFC-owned item sprites leave the items atlas");
    }

    @Test
    public void itemDirectoryKeepsNamespaceAndResourcePackPriority(@TempDir Path temporary) throws Exception
    {
        final Path low = temporary.resolve("low"), high = temporary.resolve("high");
        for (var pack : List.of(low, high))
        {
            for (String namespace : List.of("tfc", "minecraft", "other"))
            {
                final Path path = pack.resolve("assets/" + namespace + "/textures/item/example.png");
                Files.createDirectories(path.getParent());
                Files.writeString(path, pack.getFileName().toString());
            }
        }
        final var location = new PackLocationInfo("test", Component.literal("test"), PackSource.BUILT_IN, Optional.empty());
        try (var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of(new PathPackResources(location, low), new PathPackResources(location, high))))
        {
            final var output = new SpriteCollector();
            TFCItemSpriteSource.INSTANCE.run(manager, output);
            assertEquals(Set.of(Identifier.parse("tfc:item/example")), output.resources.keySet());
            try (var input = output.resources.get(Identifier.parse("tfc:item/example")).open())
            {
                assertEquals("high", new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            }
        }
    }

    @Test
    public void vanillaGroundcoverAliasesRetainOriginalImageAndGeometry() throws Exception
    {
        final Path project = Path.of(System.getProperty("tfc.test.projectDir"));
        final var atlas = JsonParser.parseString(Files.readString(project.resolve("build/resources/main/assets/minecraft/atlases/blocks.json"))).getAsJsonObject();
        final var aliases = new HashMap<String, String>();
        for (var source : atlas.getAsJsonArray("sources"))
        {
            final var entry = source.getAsJsonObject();
            if (entry.get("type").getAsString().equals("single")) aliases.put(entry.get("sprite").getAsString(), entry.get("resource").getAsString());
        }
        assertEquals(3, aliases.size());
        final var location = new PackLocationInfo("vanilla", Component.literal("vanilla"), PackSource.BUILT_IN, Optional.empty());
        try (var vanilla = new VanillaPackResourcesBuilder().pushJarResources().exposeNamespace("minecraft").build(location))
        {
            for (var name : List.of("flint", "feather", "rotten_flesh"))
            {
                final String sprite = "tfc:block/groundcover/" + name;
                assertEquals("minecraft:item/" + name, aliases.get(sprite));
                assertNotNull(vanilla.getResource(PackType.CLIENT_RESOURCES, Identifier.parse("minecraft:textures/item/" + name + ".png")));
                final Path model = project.resolve("src/main/resources/assets/tfc/models/block/groundcover/" + name + ".json");
                final var json = JsonParser.parseString(Files.readString(model)).getAsJsonObject();
                assertEquals(sprite, json.getAsJsonObject("textures").get("0").getAsString());
                assertEquals(sprite, json.getAsJsonObject("textures").get("particle").getAsString());
                assertFalse(json.getAsJsonArray("elements").isEmpty());
            }
        }
    }

    private static class SpriteCollector implements SpriteSource.Output
    {
        final HashMap<Identifier, Resource> resources = new HashMap<>();
        @Override public void add(Identifier id, Resource resource) { resources.put(id, resource); }
        @Override public void add(Identifier id, SpriteSource.DiscardableLoader loader) { fail("Expected original resource mapping"); }
        @Override public void removeAll(Predicate<Identifier> predicate) { resources.keySet().removeIf(predicate); }
    }

    private static void collectItemSprites(JsonElement value, Set<Identifier> result)
    {
        if (value.isJsonObject())
        {
            final var object = value.getAsJsonObject();
            if (object.has("textures"))
            {
                for (var texture : object.getAsJsonObject("textures").entrySet())
                {
                    if (!texture.getValue().isJsonPrimitive()) continue;
                    final String name = texture.getValue().getAsString();
                    if (name.startsWith("#")) continue;
                    final var id = Identifier.parse(name);
                    if (id.getPath().startsWith("item/")) result.add(id);
                }
            }
            for (var entry : object.entrySet()) collectItemSprites(entry.getValue(), result);
        }
        else if (value.isJsonArray())
        {
            for (var child : value.getAsJsonArray()) collectItemSprites(child, result);
        }
    }

    @Test
    public void everyNativeArmorTrimPaletteInputExists() throws Exception
    {
        final Path atlas = Path.of(System.getProperty("tfc.test.projectDir")).resolve("build/resources/main/assets/minecraft/atlases/armor_trims.json");
        final var definition = JsonParser.parseString(Files.readString(atlas)).getAsJsonObject();
        final var location = new PackLocationInfo("vanilla", Component.literal("vanilla"), PackSource.BUILT_IN, Optional.empty());
        try (var vanilla = new VanillaPackResourcesBuilder().pushJarResources().exposeNamespace("minecraft").build(location))
        {
            int checked = 0;
            for (var source : definition.getAsJsonArray("sources"))
            {
                for (var texture : source.getAsJsonObject().getAsJsonArray("textures"))
                {
                    final var id = Identifier.parse(texture.getAsString()).withPath(path -> "textures/" + path + ".png");
                    assertNotNull(vanilla.getResource(net.minecraft.server.packs.PackType.CLIENT_RESOURCES, id), id.toString());
                    checked++;
                }
            }
            assertEquals(36, checked); // All 18 patterns, with both humanoid and leggings layers.
        }
    }
}
