/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.item;

import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.test.TestSetup;

import static org.junit.jupiter.api.Assertions.*;

public class ItemDescriptionsTest implements TestSetup
{
    @Test
    public void allRegisteredItemsResolveAnExistingAuthoredName() throws Exception
    {
        final var language = JsonParser.parseString(Files.readString(Path.of(System.getProperty("tfc.test.projectDir"), "src/main/resources/assets/tfc/lang/en_us.json"))).getAsJsonObject();
        final var missing = TFCItems.ITEMS.getEntries().stream().map(h -> h.get().getDescriptionId()).filter(k -> !language.has(k)).sorted().toList();
        assertTrue(missing.isEmpty(), () -> "Missing item names: " + missing);
    }
}
