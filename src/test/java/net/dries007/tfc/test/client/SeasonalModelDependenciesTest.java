/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.client;

import java.lang.reflect.Type;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipFile;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.MissingCuboidModel;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.client.model.SeasonalUnbakedModel;
import net.dries007.tfc.test.TestSetup;

import static org.junit.jupiter.api.Assertions.*;

public class SeasonalModelDependenciesTest implements TestSetup
{
    @Test
    public void nativeDiscoveryResolvesEveryWhiteCedarSeason() throws Exception
    {
        final Path models = Path.of(System.getProperty("tfc.test.projectDir")).resolve("src/main/resources/assets/tfc/models");
        final var id = Identifier.parse("tfc:block/wood/leaves/white_cedar_dynamic");
        final var json = JsonParser.parseString(Files.readString(models.resolve("block/wood/leaves/white_cedar_dynamic.json"))).getAsJsonObject();
        final var names = List.of("dense_leaves", "sparse_leaves", "bare", "blooming");
        final JsonDeserializationContext context = new JsonDeserializationContext()
        {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T deserialize(JsonElement value, Type type)
            {
                return (T) CuboidModel.GSON.fromJson(value, CuboidModel.class);
            }
        };
        final var unbaked = new HashMap<Identifier, UnbakedModel>();
        final var seasonal = new SeasonalUnbakedModel(json, context, names, 0, parts -> null);
        unbaked.put(id, seasonal);
        for (var name : names)
            loadParent(Identifier.parse(json.getAsJsonObject(name).get("parent").getAsString()), models, unbaked);
        final var discovery = new ModelDiscovery(unbaked, MissingCuboidModel.missingModel());
        discovery.addRoot(resolver -> resolver.markDependency(id));
        final var resolved = discovery.resolve();
        for (var name : names)
        {
            final var parent = Identifier.parse(json.getAsJsonObject(name).get("parent").getAsString());
            assertTrue(resolved.containsKey(parent), "Native model discovery omitted " + parent);
            assertNotNull(resolved.get(parent).getTopGeometry());
            assertNotNull(resolved.get(parent).getTopTextureSlots().getMaterial("particle"));
        }
    }

    private static void loadParent(Identifier id, Path models, HashMap<Identifier, UnbakedModel> loaded) throws Exception
    {
        if (loaded.containsKey(id)) return;
        final CuboidModel model;
        if (id.getNamespace().equals("tfc"))
        {
            model = CuboidModel.GSON.fromJson(Files.readString(models.resolve(id.getPath() + ".json")), CuboidModel.class);
        }
        else
        {
            final Path project = Path.of(System.getProperty("tfc.test.projectDir"));
            final var pins = new Properties();
            try (var input = Files.newInputStream(project.resolve("gradle.properties"))) { pins.load(input); }
            final Path game = project.resolve("build/moddev/artifacts/minecraft-patched-" + pins.getProperty("neoForgeVersion") + ".jar");
            try (var archive = new ZipFile(game.toFile()))
            {
                final var entry = archive.getEntry("assets/" + id.getNamespace() + "/models/" + id.getPath() + ".json");
                assertNotNull(entry, id.toString());
                try (var reader = new InputStreamReader(archive.getInputStream(entry), java.nio.charset.StandardCharsets.UTF_8))
                {
                    model = CuboidModel.GSON.fromJson(reader, CuboidModel.class);
                }
            }
        }
        loaded.put(id, model);
        if (model.parent() != null) loadParent(model.parent(), models, loaded);
    }

    @Test
    public void authoredInlineAlternativesDeclareTheirParentModels() throws Exception
    {
        final Path models = Path.of(System.getProperty("tfc.test.projectDir")).resolve("src/main/resources/assets/tfc/models");
        final JsonDeserializationContext context = new JsonDeserializationContext()
        {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T deserialize(JsonElement json, Type type)
            {
                return (T) CuboidModel.GSON.fromJson(json, CuboidModel.class);
            }
        };
        int checked = 0;
        try (var files = Files.walk(models))
        {
            for (var path : files.filter(path -> path.toString().endsWith(".json")).toList())
            {
                final var json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (!json.has("loader")) continue;
                final String loader = json.get("loader").getAsString();
                final List<String> names = switch (loader)
                {
                    case "tfc:leaves" -> List.of("dense_leaves", "sparse_leaves", "bare", "blooming");
                    case "tfc:plant" -> List.of("dormant", "sprouting", "budding", "blooming", "seeding", "dying");
                    default -> List.of();
                };
                if (names.isEmpty()) continue;
                final var model = new SeasonalUnbakedModel(json, context, names, 0, parts -> null);
                final var dependencies = new HashSet<Identifier>();
                model.resolveDependencies(dependencies::add);
                for (var name : names)
                {
                    final var child = CuboidModel.GSON.fromJson(json.getAsJsonObject(name), CuboidModel.class);
                    if (child.parent() != null)
                    {
                        assertTrue(dependencies.contains(child.parent()), path + "/" + name + " omitted " + child.parent());
                        checked++;
                    }
                }
            }
        }
        assertTrue(checked > 100, "Missing authored seasonal model corpus");
    }
}
