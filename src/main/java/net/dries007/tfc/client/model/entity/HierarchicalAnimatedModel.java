/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model.entity;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.AnimationState;

import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;

/** Shared pose reset and per-model baked animation ownership. */
public abstract class HierarchicalAnimatedModel extends EntityModel<TFCAnimalRenderState>
{
    // A definition must be baked against this model's bones, never shared roots.
    // Definitions are static or constructor-owned; each is baked at most once.
    private final Map<AnimationDefinition, KeyframeAnimation> animations = new IdentityHashMap<>();

    private final Function<String, ModelPart> partLookup;

    public HierarchicalAnimatedModel(ModelPart root)
    {
        super(root);
        partLookup = root.createPartLookup();
    }

    protected final KeyframeAnimation animation(AnimationDefinition definition)
    {
        return animations.computeIfAbsent(definition, this::bakeAnimation);
    }

    /**
     * Explicit omissions in animation sets shared with a differently shaped model.
     * Legacy animation lookup skipped absent bones; do not silently accept new
     * typos or remove an entire animation to make the target's strict bake pass.
     */
    protected Set<String> optionalAnimationBones()
    {
        return Set.of();
    }

    private KeyframeAnimation bakeAnimation(AnimationDefinition definition)
    {
        final Map<String, List<AnimationChannel>> channels = new LinkedHashMap<>();
        for (var entry : definition.boneAnimations().entrySet())
        {
            if (partLookup.apply(entry.getKey()) != null)
            {
                channels.put(entry.getKey(), entry.getValue());
            }
            else if (!optionalAnimationBones().contains(entry.getKey()))
            {
                throw new IllegalArgumentException("Unknown animation bone " + entry.getKey() + " in " + getClass().getSimpleName());
            }
        }
        if (!definition.boneAnimations().isEmpty() && channels.isEmpty())
        {
            throw new IllegalArgumentException("Animation has no compatible bones in " + getClass().getSimpleName());
        }
        return new AnimationDefinition(definition.lengthInSeconds(), definition.looping(), channels).bake(root());
    }

    protected final void animateWalk(AnimationDefinition definition, float position, float speed, float speedMultiplier, float amplitudeMultiplier)
    {
        animation(definition).applyWalk(position, speed, speedMultiplier, amplitudeMultiplier);
    }

    protected final void animate(AnimationState timeline, AnimationDefinition definition, float ageInTicks)
    {
        animation(definition).apply(timeline, ageInTicks);
    }

    protected final void animate(AnimationState timeline, AnimationDefinition definition, float ageInTicks, float speed)
    {
        animation(definition).apply(timeline, ageInTicks, speed);
    }

    public float getAdjustedLandSpeed(TFCAnimalRenderState state)
    {
        return getAdjustedLandSpeed(state, 80f, 8);
    }

    public float getAdjustedLandSpeed(TFCAnimalRenderState state, float scale, float max)
    {
        return Math.min((float) state.movementLengthSqr * scale, max);
    }

    @Override
    public void setupAnim(TFCAnimalRenderState state)
    {
        super.setupAnim(state);
    }
}
