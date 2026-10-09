/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util;

import org.jspecify.annotations.Nullable;

/** Exception-transparent operations for the existing reflection call sites. */
public final class Unchecked
{
    private Unchecked() {}

    @FunctionalInterface
    public interface ThrowingSupplier<T extends @Nullable Object>
    {
        T get() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowingRunnable
    {
        void run() throws Throwable;
    }

    /** Retains the historical caller-selected cast used by reflective field reads. */
    @SuppressWarnings("unchecked")
    public static <T extends @Nullable Object> T get(ThrowingSupplier<?> action)
    {
        try
        {
            return (T) action.get();
        }
        catch (Throwable error)
        {
            return rethrow(error);
        }
    }

    public static void run(ThrowingRunnable action)
    {
        try
        {
            action.run();
        }
        catch (Throwable error)
        {
            rethrow(error);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends @Nullable Object, E extends Throwable> T rethrow(Throwable error) throws E
    {
        throw (E) error;
    }
}
