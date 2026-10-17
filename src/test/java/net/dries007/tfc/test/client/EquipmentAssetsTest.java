/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.test.TestSetup;

import static org.junit.jupiter.api.Assertions.*;

public class EquipmentAssetsTest implements TestSetup
{
    @Test
    public void nativeEquipmentLayersRetainOriginalArmorArtwork() throws Exception
    {
        final Path assets = Path.of(System.getProperty("tfc.test.projectDir")).resolve("build/resources/main/assets/tfc");
        try (var definitions = Files.list(assets.resolve("equipment")))
        {
            final var files = definitions.filter(path -> path.toString().endsWith(".json")).toList();
            assertEquals(9, files.size());
            for (var path : files)
            {
                final String material = path.getFileName().toString().replace(".json", "");
                final var equipment = EquipmentClientInfo.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(path))).getOrThrow();
                for (var type : List.of(EquipmentClientInfo.LayerType.HUMANOID, EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS, EquipmentClientInfo.LayerType.HORSE_BODY))
                {
                    final var layers = equipment.getLayers(type);
                    assertEquals(1, layers.size(), material + "/" + type);
                    final var texture = layers.getFirst().getTextureLocation(type);
                    final String original = switch (type)
                    {
                        case HUMANOID -> "models/armor/" + material + "_layer_1.png";
                        case HUMANOID_LEGGINGS -> "models/armor/" + material + "_layer_2.png";
                        default -> "entity/horse/armor/horse_armor_" + material + ".png";
                    };
                    assertEquals("tfc", texture.getNamespace());
                    assertArrayEquals(Files.readAllBytes(assets.resolve("textures/" + original)),
                        Files.readAllBytes(assets.resolve(texture.getPath())), material + "/" + type);
                }
            }
        }
    }
}
