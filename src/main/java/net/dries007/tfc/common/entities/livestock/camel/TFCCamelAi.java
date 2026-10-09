/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.entities.livestock.camel;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BabyFollowAdult;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomLookAround;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.camel.CamelAi;
import net.minecraft.world.entity.schedule.Activity;

import net.dries007.tfc.common.entities.ai.SetLookTarget;
import net.dries007.tfc.common.entities.ai.TFCBrain;
import net.dries007.tfc.common.entities.ai.livestock.BreedBehavior;
import net.dries007.tfc.common.entities.ai.prey.AvoidPredatorAndRammersBehavior;
import net.dries007.tfc.common.entities.ai.prey.PreyAi;

public class TFCCamelAi
{
    protected static final ImmutableList<SensorType<? extends Sensor<? super Camel>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS,
        SensorType.NEAREST_ADULT, SensorType.HURT_BY, TFCBrain.TEMPTATION_SENSOR.get()
    );

    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.BREED_TARGET,
        MemoryModuleType.TEMPTING_PLAYER,
        MemoryModuleType.NEAREST_VISIBLE_ADULT,
        MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
        MemoryModuleType.IS_TEMPTED,
        MemoryModuleType.AVOID_TARGET,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.HURT_BY,
        MemoryModuleType.IS_PANICKING,
        MemoryModuleType.GAZE_COOLDOWN_TICKS
    );

    public static Brain.Provider<AbstractCamel> brainProvider()
    {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES, TFCCamelAi::createActivities);
    }

    /** Build activity metadata before the provider restores saved memories. */
    public static <E extends AbstractCamel> List<ActivityData<E>> createActivities(E entity)
    {
        return List.of(
            initCoreActivity(),
            initIdleActivity(),
            initRetreatActivity()
        );
    }

    public static <E extends AbstractCamel> Brain<E> makeBrain(Brain<E> brain)
    {
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();

        return brain;
    }

    private static <E extends AbstractCamel> ActivityData<E> initCoreActivity()
    {
        return ActivityData.create(Activity.CORE, 0, ImmutableList.of(
            new Swim<>(0.8F),
            new LookAtTargetSink(45, 90),
            new MoveToTargetSink(),
            new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
            new CountDownCooldownTicks(MemoryModuleType.GAZE_COOLDOWN_TICKS)
        ));
    }

    public static <E extends AbstractCamel> ActivityData<E> initIdleActivity()
    {
        return ActivityData.create(Activity.IDLE, 0, ImmutableList.of(
            SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60)),
            AvoidPredatorAndRammersBehavior.create(true),
            new BreedBehavior<>(2.0F),
            new CamelAi.CamelPanic(4.0F),
            new FollowTemptation(e -> e.isBaby() ? 2.5F : 3.5F),
            BabyFollowAdult.create(UniformInt.of(5, 16), 2.5F),
            new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F),
            createIdleMovementBehaviors()
        ));
    }

    public static RunOne<AbstractCamel> createIdleMovementBehaviors() {
        return new RunOne<>(
            ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
            ImmutableList.of(
                Pair.of(BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), RandomStroll.stroll(2.0F)), 1),
                Pair.of(BehaviorBuilder.triggerIf(Predicate.not(Camel::refuseToMove), SetWalkTargetFromLookTarget.create(2.0F, 3)), 1),
                Pair.of(new CamelAi.RandomSitting(20), 1),
                Pair.of(new DoNothing(30, 60), 1)
            )
        );
    }

    public static <E extends AbstractCamel> ActivityData<E> initRetreatActivity()
    {
        return ActivityData.create(Activity.AVOID, 10, ImmutableList.of(
            SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 3.2F, 15, false),
            new RunOne<>(
                ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                ImmutableList.of( // Same as createIdleMovementBehaviors List, but without RandomSitting
                    Pair.of(RandomStroll.stroll(2.0F), 1),
                    Pair.of(SetWalkTargetFromLookTarget.create(2.0F, 3), 1),
                    Pair.of(new DoNothing(30, 60), 1)
                )
            ),
            SetLookTarget.create(8.0F, UniformInt.of(30, 60)),
            EraseMemoryIf.create(PreyAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)
            ),
            MemoryModuleType.AVOID_TARGET
        );
    }

    public static void updateActivity(AbstractCamel camel)
    {
        camel.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.AVOID, Activity.IDLE));
    }
}
