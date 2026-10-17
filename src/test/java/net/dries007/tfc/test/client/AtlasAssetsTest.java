/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.repository.PackSource;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.test.TestSetup;

import static org.junit.jupiter.api.Assertions.*;

public class AtlasAssetsTest implements TestSetup
{
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
