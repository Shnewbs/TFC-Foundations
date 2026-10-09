/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AnimationState;

import net.dries007.tfc.client.model.entity.*;
import net.dries007.tfc.client.render.entity.state.TFCAnimalRenderState;

/** Headless tests of real production model construction, poses and CPU vertices. */
public final class ModelRenderSmoke
{
    private static int checks;
    private static long vertices;
    private static final int SCENARIOS = 8;

    public static void main(String[] names) throws Exception
    {
        for (String name : names)
        {
            final Class<?> type = Class.forName("net.dries007.tfc.client.model.entity." + name);
            final LayerDefinition definition = (LayerDefinition) type.getMethod("createBodyLayer").invoke(null);
            final ModelPart root = definition.bakeRoot();
            final HierarchicalAnimatedModel model = (HierarchicalAnimatedModel) type.getConstructor(ModelPart.class).newInstance(root);
            require(model.root() == root, name + " retains its baked root");
            require(!model.allParts().isEmpty(), name + " has bones");
            final TFCAnimalRenderState baseline = state(0);
            model.setupAnim(baseline);
            final List<Float> resting = pose(model);
            // A second instance must not share the first model's baked animations.
            final HierarchicalAnimatedModel other = (HierarchicalAnimatedModel) type.getConstructor(ModelPart.class).newInstance(definition.bakeRoot());
            other.setupAnim(baseline);
            final List<Float> otherResting = pose(other);
            for (int mode = 0; mode < SCENARIOS; mode++)
            {
                final TFCAnimalRenderState state = state(mode);
                model.setupAnim(state);
                final List<Float> first = pose(model);
                model.setupAnim(state);
                equalPose(first, pose(model), name + " repeated pose " + mode);
                equalPose(otherResting, pose(other), name + " cross-instance isolation " + mode);
                final CountingVertices output = new CountingVertices();
                model.renderToBuffer(new PoseStack(), output, 0x00f000f0, 0, -1);
                require(output.vertices > 0, name + " emits CPU geometry " + mode);
                vertices += output.vertices;
                model.setupAnim(baseline);
                equalPose(resting, pose(model), name + " reset after pose " + mode);
            }
            System.out.println("PASS model: " + name + " (" + model.allParts().size() + " bones, " + SCENARIOS + " scenarios)");
        }
        testCustomStateReset();
        testSpecificPoses();
        testAnimationBinding();
        System.out.println("PASS: " + names.length + " concrete production models; " + (names.length * SCENARIOS)
            + " model/scenario combinations; " + checks + " assertions; " + vertices + " CPU vertices checked.");
        System.out.println("NOT RUN: live entity extraction, GPU drawing, in-game appearance, client/server startup or gameplay.");
    }

    private static TFCAnimalRenderState state(int mode) throws IllegalAccessException
    {
        final TFCAnimalRenderState state = new TFCAnimalRenderState();
        state.onGround = true;
        state.ageInTicks = 40;
        state.xRot = 12;
        state.yRot = 23;
        state.scale = state.ageScale = 1;
        if (mode > 0)
        {
            state.walkAnimationPos = 2;
            state.walkAnimationSpeed = 0.65F;
            state.movementLengthSqr = 0.04;
            state.movingOnLand = true;
        }
        if (mode == 1) state.aggressive = true;
        if (mode == 2)
        {
            state.onGround = state.movingOnLand = false;
            state.isInWater = state.inWaterOrBubble = true;
        }
        if (mode == 3)
        {
            state.sleeping = state.sitting = true;
            state.sleepingAnimation.start(0);
        }
        if (mode == 4)
        {
            state.telegraphingAttack = state.maleCharacteristics = state.isMale = true;
            state.telegraphAttackTick = 12;
            state.attackingAnimation.start(0);
        }
        if (mode == 5)
        {
            state.playingDead = state.isBaby = state.hasProduct = state.bearCrawlsOn = true;
            state.hurtByEntity = true;
        }
        if (mode == 6)
        {
            state.onGround = false;
            state.fallSeason = state.climbing = true;
            state.headRollAngle = 0.25F;
            state.hurtAnimation.start(0);
        }
        if (mode == 7)
        {
            for (Field field : TFCAnimalRenderState.class.getDeclaredFields())
            {
                if (field.getType() == AnimationState.class) ((AnimationState) field.get(state)).start(3);
            }
            state.sitting = state.hasProduct = true;
        }
        return state;
    }

    private static List<Float> pose(HierarchicalAnimatedModel model)
    {
        final List<Float> pose = new ArrayList<>();
        for (ModelPart part : model.allParts())
        {
            final float[] values = {part.x, part.y, part.z, part.xRot, part.yRot, part.zRot,
                part.xScale, part.yScale, part.zScale, part.visible ? 1 : 0};
            for (float value : values)
            {
                if (!Float.isFinite(value)) throw new AssertionError("Non-finite model pose");
                pose.add(value);
            }
        }
        return pose;
    }

    private static void equalPose(List<Float> expected, List<Float> actual, String label)
    {
        if (expected.size() != actual.size()) throw new AssertionError(label + ": bone count changed");
        for (int i = 0; i < expected.size(); i++)
        {
            if (Math.abs(expected.get(i) - actual.get(i)) > 0.00001F)
                throw new AssertionError(label + ": pose field " + i + ", expected " + expected.get(i) + ", got " + actual.get(i));
        }
        checks++;
    }

    private static void testCustomStateReset() throws Exception
    {
        final TFCAnimalRenderState state = state(7);
        for (Field field : TFCAnimalRenderState.class.getDeclaredFields())
        {
            require(field.getType().isPrimitive() || field.getType() == AnimationState.class || field.getType() == Identifier.class, "Only detached values in state: " + field.getName());
            if (field.getType() == boolean.class) field.setBoolean(state, true);
        }
        state.texture = Identifier.withDefaultNamespace("test");
        state.headRollAngle = 2;
        state.collarColor = 123;
        final AnimationState source = new AnimationState();
        source.start(7);
        state.walkingAnimation.copyFrom(source);
        final long time = state.walkingAnimation.getTimeInMillis(40);
        source.start(99);
        require(state.walkingAnimation.getTimeInMillis(40) == time, "Copied animation timing is independent");
        state.resetCustomState();
        for (Field field : TFCAnimalRenderState.class.getDeclaredFields())
        {
            if (field.getType() == boolean.class) require(!field.getBoolean(state), "Reset " + field.getName());
            if (field.getType() == AnimationState.class) require(!((AnimationState) field.get(state)).isStarted(), "Stop " + field.getName());
        }
        require(state.texture == null && state.headRollAngle == 0 && state.collarColor == -1, "Reset optional appearance");
        require(state.ageInTicks == 40 && state.xRot == 12 && state.yRot == 23, "Keep native extracted fields");
    }

    private static void testSpecificPoses() throws Exception
    {
        final TFCAnimalRenderState state = state(0);
        final BisonModel bison = new BisonModel(BisonModel.createBodyLayer().bakeRoot());
        state.telegraphingAttack = true;
        state.telegraphAttackTick = 10;
        bison.setupAnim(state);
        require(Math.abs(bison.root().getChild("whole_body").getChild("body").getChild("head").xRot + 10F * ((float) Math.PI / 180)) < 0.00001F, "Bison charge windup angle");
        state.movementLengthSqr = 1;
        require(bison.getAdjustedLandSpeed(state) == 8, "Movement speed cap");
        state.movementLengthSqr = 0;
        require(bison.getAdjustedLandSpeed(state) == 0, "Stationary speed");
        final BactrianCamelModel camel = new BactrianCamelModel(BactrianCamelModel.createBodyLayer().bakeRoot());
        state.isBaby = true;
        camel.setupAnim(state);
        require(camel.root().xScale == 0.45F && camel.root().yScale == 0.45F && camel.root().zScale == 0.45F, "Baby camel scale");
        require(Math.abs(camel.root().y - 0.45F * 1.834375F * 16F) < 0.00001F, "Baby camel transform order");
        state.isBaby = false;
        camel.setupAnim(state);
        require(camel.root().xScale == 1 && camel.root().y == 0, "Adult camel pose resets baby transform");
        final DogModel dog = new DogModel(DogModel.createBodyLayer().bakeRoot());
        state.sitting = true;
        state.headRollAngle = 0.3F;
        dog.setupAnim(state);
        require(dog.root().getChild("upper_body").y == 16, "Dog sitting body placement");
        require(dog.root().getChild("head").getChild("real_head").zRot == 0.3F, "Dog interpolated head roll");
        state.sitting = false;
        dog.setupAnim(state);
        require(dog.root().getChild("upper_body").y == 14, "Dog standing body placement");
    }

    private static void testAnimationBinding()
    {
        final var channel = new net.minecraft.client.animation.AnimationChannel(
            net.minecraft.client.animation.AnimationChannel.Targets.ROTATION,
            new net.minecraft.client.animation.Keyframe(0, new org.joml.Vector3f(0.5F, 0, 0),
                net.minecraft.client.animation.AnimationChannel.Interpolations.LINEAR));
        final var known = new net.minecraft.client.animation.AnimationDefinition(1, true, java.util.Map.of("known", List.of(channel)));
        final var shared = new net.minecraft.client.animation.AnimationDefinition(1, true,
            java.util.Map.of("known", List.of(channel), "shared_only", List.of(channel)));
        final var missing = new net.minecraft.client.animation.AnimationDefinition(1, true, java.util.Map.of("shared_only", List.of(channel)));
        final BindingProbe strict = new BindingProbe(false);
        require(strict.bind(known) == strict.bind(known), "Animation is cached per definition and model");
        boolean rejected = false;
        try { strict.bind(shared); } catch (IllegalArgumentException expected) { rejected = true; }
        require(rejected, "Unexpected missing bone is rejected");
        final BindingProbe compatible = new BindingProbe(true);
        final var bound = compatible.bind(shared);
        bound.apply(0L, 1F);
        require(Math.abs(compatible.root().getChild("known").xRot - 0.5F) < 0.00001F, "Shared animation keeps existing bone motion");
        require(strict.root().getChild("known").xRot == 0, "Cached animations never cross model roots");
        rejected = false;
        try { compatible.bind(missing); } catch (IllegalArgumentException expected) { rejected = true; }
        require(rejected, "Compatibility must not discard an entire animation");
    }

    private static final class BindingProbe extends HierarchicalAnimatedModel
    {
        private final boolean allowShared;
        BindingProbe(boolean allowShared)
        {
            super(new ModelPart(List.of(), java.util.Map.of("known", new ModelPart(List.of(), java.util.Map.of()))));
            this.allowShared = allowShared;
        }
        protected java.util.Set<String> optionalAnimationBones() { return allowShared ? java.util.Set.of("shared_only") : java.util.Set.of(); }
        net.minecraft.client.animation.KeyframeAnimation bind(net.minecraft.client.animation.AnimationDefinition definition) { return animation(definition); }
    }

    private static void require(boolean condition, String label)
    {
        if (!condition) throw new AssertionError(label);
        checks++;
    }

    private static final class CountingVertices implements VertexConsumer
    {
        int vertices;
        private void finite(float... values)
        {
            for (float value : values) if (!Float.isFinite(value)) throw new AssertionError("Non-finite emitted vertex");
        }
        public VertexConsumer addVertex(float x, float y, float z) { finite(x, y, z); vertices++; return this; }
        public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        public VertexConsumer setColor(int argb) { return this; }
        public VertexConsumer setUv(float u, float v) { finite(u, v); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { finite(x, y, z); return this; }
        public VertexConsumer setLineWidth(float width) { finite(width); return this; }
    }
}
