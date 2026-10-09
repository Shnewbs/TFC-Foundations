/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat;

import java.lang.reflect.InvocationTargetException;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

import net.dries007.tfc.TerraFirmaCraft;

/**
 * Keeps optional adapters out of core class linking. An adapter is available only
 * when this build compiled it and its target mod is installed. A broken included
 * adapter still fails loading; reflection errors must not silently remove features.
 */
public enum OptionalIntegrations
{
    EMI("emi", "emi.EmiIntegration"),
    JADE("jade", "jade.JadeIntegration"),
    THE_ONE_PROBE("theoneprobe", "theoneprobe.TheOneProbeIntegration");

    private final String modId;
    private final String adapterClass;

    OptionalIntegrations(String modId, String adapterClass)
    {
        this.modId = modId;
        this.adapterClass = "net.dries007.tfc.compat." + adapterClass;
    }

    public boolean isAvailable()
    {
        return ModList.get().isLoaded(modId)
            && OptionalIntegrations.class.getClassLoader().getResource(adapterClass.replace('.', '/') + ".class") != null;
    }

    public static void initTheOneProbe(IEventBus bus)
    {
        THE_ONE_PROBE.invoke("init", new Class<?>[] {IEventBus.class}, bus);
    }

    public static void registerJadeToolHandlers()
    {
        JADE.invoke("registerToolHandlers", new Class<?>[0]);
    }

    private void invoke(String method, Class<?>[] parameterTypes, Object... arguments)
    {
        if (!isAvailable())
        {
            if (ModList.get().isLoaded(modId))
            {
                TerraFirmaCraft.LOGGER.info("TFC integration for {} is not included in this build", modId);
            }
            return;
        }
        try
        {
            Class.forName(adapterClass).getMethod(method, parameterTypes).invoke(null, arguments);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("TFC integration for " + modId + " failed to initialize", e.getCause());
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalStateException("TFC integration for " + modId + " has an invalid entry point", e);
        }
    }
}
