/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.client;

import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.client.ClientEventHandler;
import net.dries007.tfc.client.ColorMapReloadListener;
import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.test.TestSetup;
import net.dries007.tfc.util.Helpers;

import static org.junit.jupiter.api.Assertions.*;

public class ClientReloadListenersTest implements TestSetup
{
    @Test
    @SuppressWarnings("deprecation")
    public void sharedFoliageTextureHasIndependentReloadRegistrations()
    {
        // Sharing a texture is intentional; its consumers still need distinct event keys.
        assertEquals(TFCColors.FOLIAGE_COLORS_LOCATION, TFCColors.FOLIAGE_SUMMER_COLORS_LOCATION);
        for (int initialization = 0; initialization < 2; initialization++)
        {
            try (var manager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES))
            {
                manager.registerReloadListener(new GrassColorReloadListener());
                final var event = new AddClientReloadListenersEvent(manager);
                assertDoesNotThrow(() -> ClientEventHandler.registerClientReloadListeners(event));
                final var registry = event.getRegistry();
                assertEquals(11, registry.size()); // Vanilla grass, all nine TFC maps and the star catalog.
                assertEquals(9, registry.values().stream().filter(ColorMapReloadListener.class::isInstance).count());
                final var foliage = registry.get(TFCColors.FOLIAGE_COLORS_LOCATION);
                final var summer = registry.get(Helpers.identifier("colormap/foliage_summer"));
                assertInstanceOf(ColorMapReloadListener.class, foliage);
                assertInstanceOf(ColorMapReloadListener.class, summer);
                assertNotSame(foliage, summer);
                assertDoesNotThrow(() -> manager.updateListenersFrom(event));
                assertEquals(11, manager.getListeners().size());
            }
        }
    }
}
