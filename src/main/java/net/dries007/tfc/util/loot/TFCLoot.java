/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.util.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryHolder;

import static net.dries007.tfc.TerraFirmaCraft.*;

public class TFCLoot
{
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MOD_ID);
    public static final DeferredRegister<MapCodec<? extends NumberProvider>> NUMBER_PROVIDERS = DeferredRegister.create(Registries.LOOT_NUMBER_PROVIDER_TYPE, MOD_ID);
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MOD_ID);

    public static final ContextKey<Boolean> ISOLATED = new ContextKey<>(Helpers.identifier("isolated"));
    public static final ContextKey<Boolean> BURNT_OUT = new ContextKey<>(Helpers.identifier("burnt_out"));
    public static final ContextKey<Boolean> SLUICE = new ContextKey<>(Helpers.identifier("sluice"));

    public static final Id<LootItemCondition> IS_ISOLATED = lootCondition("is_isolated", IsIsolatedCondition.CODEC);
    public static final Id<LootItemCondition> IS_BURNT_OUT = lootCondition("is_burnt_out", IsBurntOutCondition.CODEC);
    public static final Id<LootItemCondition> IS_SLUICE = lootCondition("is_sluice", IsSluiceCondition.CODEC);
    public static final Id<LootItemCondition> IS_MALE = lootCondition("is_male", IsMaleCondition.CODEC);
    public static final Id<LootItemCondition> ALWAYS_TRUE = lootCondition("always_true", AlwaysTrueCondition.CODEC);
    public static final Id<LootItemCondition> NOT_PREDATED = lootCondition("not_predated", NotPredatedCondition.CODEC);
    public static final Id<NumberProvider> CROP_YIELD = numberProvider("crop_yield_uniform", CropYieldProvider.CODEC);
    public static final Id<NumberProvider> ANIMAL_YIELD = numberProvider("animal_yield", AnimalYieldProvider.CODEC);
    public static final LootFunctionId<CopyFluidFunction> COPY_FLUID = lootFunction("copy_fluid", CopyFluidFunction.CODEC);
    public static final LootFunctionId<RottenFunction> ROTTEN = lootFunction("rotten", RottenFunction.CODEC);
    public static final LootFunctionId<ApplyStackSizeFunction> APPLY_STACK_SIZE = lootFunction("apply_stack_size", ApplyStackSizeFunction.CODEC);

    private static <T extends LootItemFunction> LootFunctionId<T> lootFunction(String id, MapCodec<T> codec)
    {
        return new LootFunctionId<>(LOOT_FUNCTIONS.register(id, () -> codec));
    }

    private static Id<LootItemCondition> lootCondition(String id, MapCodec<? extends LootItemCondition> codec)
    {
        return new Id<>(CONDITIONS.register(id, () -> codec));
    }

    private static Id<NumberProvider> numberProvider(String id, MapCodec<? extends NumberProvider> codec)
    {
        return new Id<>(NUMBER_PROVIDERS.register(id, () -> codec));
    }

    public record Id<T>(DeferredHolder<MapCodec<? extends T>, MapCodec<? extends T>> holder)
        implements RegistryHolder<MapCodec<? extends T>, MapCodec<? extends T>> {}
    public record LootFunctionId<T extends LootItemFunction>(DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<T>> holder)
        implements RegistryHolder<MapCodec<? extends LootItemFunction>, MapCodec<T>> {}
}
