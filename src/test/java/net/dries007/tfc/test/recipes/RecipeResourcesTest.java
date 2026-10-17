/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.recipes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Instruments;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.test.TestSetup;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeResourcesTest implements TestSetup
{
    @Test
    public void allGeneratedRecipeFilesDecodeWithNativeRegistries() throws Exception
    {
        final var root = Path.of(System.getProperty("tfc.test.projectDir")).resolve("src/generated/resources/data");
        final var ops = new ConditionalOps<>(REGISTRIES.get().createSerializationContext(JsonOps.INSTANCE), ICondition.IContext.EMPTY);
        final List<String> failures = new ArrayList<>();
        int decoded = 0, disabled = 0;
        try (var paths = Files.walk(root))
        {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json") && root.relativize(p).getName(1).toString().equals("recipe")).sorted().toList())
            {
                final var parsed = Recipe.CONDITIONAL_CODEC.parse(ops, JsonParser.parseString(Files.readString(path)));
                if (parsed.error().isPresent()) failures.add(root.relativize(path) + ": " + parsed.error().get().message());
                else if (parsed.getOrThrow().isEmpty()) disabled++;
                else
                {
                    decoded++;
                    // The native prototype supplies ponder; omitting the redundant component must retain the instrument.
                    if (path.getFileName().toString().equals("ponder_goat_horn_goat_horn.json"))
                    {
                        final var recipe = parsed.getOrThrow().orElseThrow().carrier();
                        final var result = ((net.dries007.tfc.common.recipes.ISimpleRecipe<?>) recipe).getResultItem(REGISTRIES.get());
                        assertTrue(result.get(DataComponents.INSTRUMENT).instrument().is(Instruments.PONDER_GOAT_HORN));
                    }
                }
            }
        }
        assertTrue(failures.isEmpty(), () -> String.join("\n", failures));
        assertTrue(decoded > 5000, "Missing generated recipe corpus: " + decoded);
        assertTrue(disabled > 100, "Missing disabled vanilla recipe conditions: " + disabled);
        System.out.println("Native recipe resources: " + decoded + " decoded, " + disabled + " conditionally disabled");
    }
}
