/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.startup.StartupArgs;

/** Applies real client-side Mixins without constructing a game window or claiming gameplay validation. */
public final class ClientMixinLoadSmoke
{
    public static void main(String[] args) throws Exception
    {
        final var startup = new StartupArgs(Path.of(args[0]), true, Dist.CLIENT, true,
            new String[0], Set.of(), List.of(), Thread.currentThread().getContextClassLoader());
        try (var loader = FMLLoader.create(startup))
        {
            if (loader.getLoadingModList().hasErrors())
                throw new AssertionError(loader.getLoadingModList().getModLoadingIssues());
            final var gameLoader = loader.getCurrentClassLoader();
            Thread.currentThread().setContextClassLoader(gameLoader);
            final var screen = Class.forName("net.minecraft.client.gui.screens.worldselection.CreateWorldScreen", false, gameLoader);
            if (Arrays.stream(screen.getDeclaredMethods()).noneMatch(method -> method.getName().contains("changeDefaultWorldType")))
                throw new AssertionError("Required TFC preset Mixin did not transform the native client screen");
            System.out.println("PASS real CLIENT FML/Mixin transformation of CreateWorldScreen; no game window/world was opened.");
            for (int i = 1; i < args.length; i++)
            {
                if (args[i].startsWith("net.caffeinemc.mods.sodium.") && loader.getLoadingModList().getModFileById("sodium") == null)
                {
                    System.out.println("NOT RUN optional Sodium target: dependency absent");
                    continue;
                }
                Class.forName(args[i], false, gameLoader).getDeclaredMethods();
                System.out.println("PASS client Mixin target " + args[i]);
            }
        }
    }
}
