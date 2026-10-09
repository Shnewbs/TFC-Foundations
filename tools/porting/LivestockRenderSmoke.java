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
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import net.dries007.tfc.client.model.entity.*;
import net.dries007.tfc.client.render.entity.state.*;

/** Executes real livestock/cat models and snapshot helpers; no game or entity stubs. */
public final class LivestockRenderSmoke
{
    private static int assertions;
    private static int scenarios;
    private static long vertices;

    @SuppressWarnings("unchecked")
    public static void main(String[] names) throws Exception
    {
        for (String name : names)
        {
            final Class<?> type = Class.forName("net.dries007.tfc.client.model.entity." + name);
            final LayerDefinition definition = name.equals("TFCPigModel")
                ? TFCPigModel.createTFCBodyLayer(CubeDeformation.NONE)
                : (LayerDefinition) type.getMethod("createBodyLayer").invoke(null);
            final EntityModel<TFCAnimalRenderState> model = (EntityModel<TFCAnimalRenderState>) type.getConstructor(ModelPart.class).newInstance(definition.bakeRoot());
            final EntityModel<TFCAnimalRenderState> other = (EntityModel<TFCAnimalRenderState>) type.getConstructor(ModelPart.class).newInstance(definition.bakeRoot());
            final TFCAnimalRenderState baseline = state(0);
            model.setupAnim(baseline);
            other.setupAnim(baseline);
            final List<Float> resting = pose(model);
            final List<Float> independent = pose(other);
            require(model.root() != other.root(), name + " has independently baked roots");
            for (int mode = 0; mode < 32; mode++)
            {
                final TFCAnimalRenderState state = state(mode);
                final List<Object> input = scalars(state);
                model.setupAnim(state);
                final List<Float> first = pose(model);
                assertVisibility(name, model.root(), state);
                assertJuvenileTransform(name, model.root(), state);
                model.setupAnim(state);
                equalPose(first, pose(model), name + " idempotent pose " + mode);
                equalPose(independent, pose(other), name + " independent root " + mode);
                require(input.equals(scalars(state)), name + " does not mutate submitted state " + mode);
                draw(model, name);
                model.setupAnim(baseline);
                equalPose(resting, pose(model), name + " resets pose/visibility " + mode);
                scenarios++;
            }
            System.out.println("PASS livestock model: " + name);
        }
        testCat();
        testTransforms();
        testSnapshotMath();
        testGuideCapture();
        System.out.printf("PASS: %d livestock/cat models; %d scenarios; %d assertions; %d finite CPU vertices.%n",
            names.length + 1, scenarios, assertions, vertices);
        System.out.println("NOT RUN: live entity extraction, renderer construction, GPU drawing, client/server or gameplay.");
    }

    private static TFCAnimalRenderState state(int mask)
    {
        final TFCAnimalRenderState state = new TFCAnimalRenderState();
        state.isBaby = (mask & 1) != 0;
        state.isMale = (mask & 2) != 0;
        state.maleCharacteristics = state.isMale && !state.isBaby;
        state.femaleCharacteristics = !state.isMale && !state.isBaby;
        state.hasProduct = (mask & 4) != 0;
        state.isInWater = state.inWaterOrBubble = (mask & 8) != 0;
        state.onGround = (mask & 16) == 0;
        state.walkAnimationPos = 3.25F;
        state.walkAnimationSpeed = 0.65F;
        state.xRot = 18;
        state.yRot = 33;
        state.ageInTicks = 61;
        state.scale = 1;
        state.ageScale = state.isBaby ? 0.5F : 1;
        state.geneticSizeScale = LivestockRenderStateMath.geneticScale(17);
        state.wingFlap = LivestockRenderStateMath.wingFlap(0.2F, 0.8F, 0.4F, 0.9F, 0.5F);
        return state;
    }

    private static void assertVisibility(String name, ModelPart root, TFCAnimalRenderState state)
    {
        final ModelPart body = root.getChild("body");
        switch (name)
        {
            case "TFCCowModel" -> {
                require(body.getChild("udder").visible == state.femaleCharacteristics, "Cow udder visibility");
                require(body.getChild("head").getChild("hornR").getChild("hornR2").visible == state.maleCharacteristics, "Cow bull horn visibility");
            }
            case "YakModel" -> require(body.getChild("neck").getChild("head").getChild("hornL1").visible == !state.isBaby, "Yak juvenile horns");
            case "TFCSheepModel" -> {
                require(body.getChild("woolBody").visible == state.hasProduct, "Sheep shearing visibility");
                require(body.getChild("head").getChild("leftHorn").visible == state.maleCharacteristics, "Sheep ram horns");
            }
            case "AlpacaModel" -> require(body.getChild("wool_body_f").visible == state.hasProduct, "Alpaca shearing visibility");
            case "MuskOxModel" -> {
                require(body.getChild("quiviut").visible == state.hasProduct, "Musk ox coat visibility");
                require(body.getChild("neck").getChild("head").getChild("hornL1").visible == state.maleCharacteristics, "Musk ox horn visibility");
            }
            case "TFCPigModel" -> require(root.getChild("head").getChild("tusk1").visible == state.maleCharacteristics, "Pig tusk visibility");
            case "TFCGoatModel" -> {
                require(root.getChild("head").getChild("left_horn").visible == !state.isBaby, "Goat juvenile horns");
                close(root.getChild("head").getChild("left_horn").y, state.femaleCharacteristics ? 2 : 0, "Goat female horn offset");
                close(root.getChild("head").yRot, state.yRot * Mth.DEG_TO_RAD / 3F, "Goat reduced head yaw");
            }
            case "DuckModel", "QuailModel", "TFCChickenModel" -> {
                final boolean flap = !state.onGround && (!name.equals("DuckModel") || !state.isInWater);
                close(body.getChild("wingR").zRot, flap ? state.wingFlap : 0, name + " right-wing flap");
                close(body.getChild("wingL").zRot, flap ? -state.wingFlap : 0, name + " left-wing flap");
                if (state.isInWater && !name.equals("TFCChickenModel"))
                    close(body.zRot, body.getInitialPose().zRot(), name + " water clears old walking sway");
            }
            default -> throw new AssertionError("Uncovered model: " + name);
        }
    }

    private static void assertJuvenileTransform(String name, ModelPart root, TFCAnimalRenderState state)
    {
        if (name.equals("TFCGoatModel"))
        {
            close(root.getChild("head").xScale, state.isBaby ? 0.6F : 1, "Goat separate juvenile head scale");
            close(root.getChild("body").xScale, state.isBaby ? 0.5F : 1, "Goat separate juvenile body scale");
            return;
        }
        final boolean pig = name.equals("TFCPigModel");
        final float scale = pig ? 0.5F : 1F / 1.8F;
        final float offset = pig ? 24 : switch (name) {
            case "TFCSheepModel", "DuckModel", "QuailModel", "TFCChickenModel" -> 18;
            default -> 19;
        };
        close(root.xScale, state.isBaby ? scale : 1, name + " juvenile root scale");
        close(root.y, state.isBaby ? offset * scale : 0, name + " scale-then-translate offset");
    }

    private static void testCat() throws Exception
    {
        final TFCCatModel model = new TFCCatModel(TFCCatModel.createBodyLayer(CubeDeformation.NONE).bakeRoot());
        final TFCCatModel collar = new TFCCatModel(TFCCatModel.createBodyLayer(new CubeDeformation(0.01F)).bakeRoot());
        final TFCCatRenderState baseline = catState(0);
        model.setupAnim(baseline);
        final List<Float> resting = pose(model);
        for (int mode = 0; mode < 16; mode++)
        {
            final TFCCatRenderState state = catState(mode);
            final List<Object> input = scalars(state);
            model.setupAnim(state);
            collar.setupAnim(state);
            final List<Float> first = pose(model);
            equalPose(first, pose(collar), "Cat/collar pose alignment " + mode);
            model.setupAnim(state);
            equalPose(first, pose(model), "Cat repeated pose " + mode);
            require(input.equals(scalars(state)), "Cat keeps render state immutable during poses");
            close(model.root().getChild("head").xScale, state.isBaby ? 0.75F : 1, "Cat juvenile head scale");
            close(model.root().getChild("body").xScale, state.isBaby ? 0.5F : 1, "Cat juvenile body scale");
            if (state.sleeping)
            {
                close(model.root().getChild("head").zRot, -1.2707963F, "Cat sleeping head roll");
                close(model.root().getChild("left_front_leg").xRot, -1.2707963F, "Cat sleeping front leg");
            }
            if (state.isSitting) close(model.root().getChild("body").xRot, Mth.PI / 4, "Cat sitting body angle");
            draw(model, "Cat");
            draw(collar, "Cat collar");
            model.setupAnim(baseline);
            equalPose(resting, pose(model), "Cat standing reset " + mode);
            scenarios++;
        }
        System.out.println("PASS pet model: TFCCatModel and matching collar");
    }

    private static TFCCatRenderState catState(int mask)
    {
        final TFCCatRenderState state = new TFCCatRenderState();
        state.isBaby = (mask & 1) != 0;
        state.isSitting = (mask & 2) != 0;
        state.sleeping = (mask & 4) != 0;
        state.isCrouching = (mask & 8) != 0;
        state.isSprinting = !state.isCrouching && !state.isSitting;
        state.lieDownAmount = state.sleeping ? 1 : 0;
        state.lieDownAmountTail = state.sleeping ? 0.87F : 0;
        state.scale = 1;
        state.ageScale = state.isBaby ? 0.5F : 1;
        state.ageInTicks = 61;
        state.walkAnimationPos = 3.25F;
        state.walkAnimationSpeed = 0.65F;
        state.xRot = 18;
        state.yRot = 33;
        return state;
    }

    private static void testTransforms()
    {
        for (float scale : new float[] {0.5F, 0.6F, 0.75F, 1F / 1.8F})
        {
            final ModelPart original = new ModelPart(List.of(), java.util.Map.of());
            original.setPos(3, 12, -4);
            original.setRotation(0.3F, -0.7F, 0.5F);
            original.xScale = 0.8F;
            original.yScale = 0.9F;
            original.zScale = 1.1F;
            final PoseStack expected = new PoseStack();
            expected.scale(scale, scale, scale);
            expected.translate(0, 19F / 16F, 1F / 16F);
            original.translateAndRotate(expected);
            AgeableModelTransforms.scalePart(original, scale, 19, 1);
            final PoseStack actual = new PoseStack();
            original.translateAndRotate(actual);
            final float[] a = new float[16], b = new float[16];
            expected.last().pose().get(a);
            actual.last().pose().get(b);
            for (int i = 0; i < 16; i++) close(a[i], b[i], "Legacy transform matrix " + scale + ":" + i);
        }
    }

    private static void testSnapshotMath()
    {
        close(LivestockRenderStateMath.geneticScale(Integer.MIN_VALUE), 0.9F, "Minimum size clamped");
        close(LivestockRenderStateMath.geneticScale(Integer.MAX_VALUE), 1.1F, "Maximum size clamped");
        float previous = 0;
        for (int size = 1; size <= 32; size++)
        {
            final float actual = LivestockRenderStateMath.geneticScale(size);
            close(actual, 0.9F + (size - 1F) / 31F * 0.2F, "Genetic size " + size);
            require(actual > previous, "Genetic scale monotonic " + size);
            previous = actual;
        }
        for (float partial : new float[] {0, 0.25F, 0.5F, 0.75F, 1})
            close(LivestockRenderStateMath.wingFlap(0.2F, 0.8F, 0.4F, 0.9F, partial),
                (Mth.sin(0.2F + 0.6F * partial) + 1) * (0.4F + 0.5F * partial), "Interpolated wing phase " + partial);
        final TFCAnimalRenderState state = state(31);
        state.femaleCharacteristics = true;
        state.resetCustomState();
        require(!state.femaleCharacteristics && state.geneticSizeScale == 1 && state.wingFlap == 0, "Reset livestock custom snapshot values");
        require(state.ageInTicks == 61 && state.isBaby && state.isInWater, "Custom reset preserves native fields");
    }

    private static void testGuideCapture()
    {
        final LivingEntityRenderState state = new LivingEntityRenderState();
        require(!GuideRenderState.isAtOrigin(state), "Uncaptured guide state is not exempt");
        for (Vec3 pos : new Vec3[] {Vec3.ZERO, new Vec3(0.009, -0.009, 0.009)})
        {
            GuideRenderState.captureOrigin(state, pos);
            require(GuideRenderState.isAtOrigin(state), "Guide origin inside boundary");
        }
        for (Vec3 pos : new Vec3[] {new Vec3(0.01F, 0, 0), new Vec3(0, -0.01F, 0),
            new Vec3(0, 0, 0.01F), new Vec3(100, 20, 0), new Vec3(Double.NaN, 0, 0)})
        {
            GuideRenderState.captureOrigin(state, Vec3.ZERO);
            GuideRenderState.captureOrigin(state, pos);
            require(!GuideRenderState.isAtOrigin(state), "Guide capture clears stale origin flag");
        }
        GuideRenderState.captureOrigin(state, Vec3.ZERO);
        state.x = 200;
        state.y = 30;
        state.z = 100;
        require(GuideRenderState.isAtOrigin(state), "Guide exemption uses captured position, not interpolated coordinates");
        state.resetRenderData();
        require(!GuideRenderState.isAtOrigin(state), "Native extension reset clears guide flag");
    }

    private static List<Object> scalars(Object state) throws Exception
    {
        final List<Object> result = new ArrayList<>();
        for (Field field : state.getClass().getFields())
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType().isPrimitive()) result.add(field.get(state));
        return result;
    }

    private static List<Float> pose(Model<?> model)
    {
        final List<Float> result = new ArrayList<>();
        for (ModelPart part : model.allParts())
            for (float value : new float[] {part.x, part.y, part.z, part.xRot, part.yRot, part.zRot,
                part.xScale, part.yScale, part.zScale, part.visible ? 1 : 0})
            {
                if (!Float.isFinite(value)) throw new AssertionError("Non-finite pose");
                result.add(value);
            }
        return result;
    }

    private static void equalPose(List<Float> expected, List<Float> actual, String label)
    {
        if (expected.size() != actual.size()) throw new AssertionError(label + ": bone count");
        for (int i = 0; i < expected.size(); i++)
            if (Math.abs(expected.get(i) - actual.get(i)) > 0.00001F)
                throw new AssertionError(label + ": field " + i + ", expected " + expected.get(i) + ", got " + actual.get(i));
        assertions++;
    }

    private static void draw(Model<?> model, String name)
    {
        final CountingVertices counter = new CountingVertices();
        model.renderToBuffer(new PoseStack(), counter, 0x00f000f0, 0, -1);
        require(counter.vertices > 0, name + " emits actual model geometry");
        vertices += counter.vertices;
    }

    private static void close(float actual, float expected, String label)
    {
        require(Float.isFinite(actual) && Math.abs(actual - expected) < 0.00001F, label + ": expected " + expected + ", got " + actual);
    }

    private static void require(boolean condition, String label)
    {
        if (!condition) throw new AssertionError(label);
        assertions++;
    }

    private static final class CountingVertices implements VertexConsumer
    {
        int vertices;
        private void finite(float... values) { for (float value : values) if (!Float.isFinite(value)) throw new AssertionError("Non-finite CPU vertex"); }
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
