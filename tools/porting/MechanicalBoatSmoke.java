/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.equine.DonkeyModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.animal.equine.BabyHorseModel;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.object.boat.RaftModel;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.core.Direction;
import net.dries007.tfc.client.model.entity.BoatChestModel;
import net.dries007.tfc.client.model.entity.WaterWheelModel;
import net.dries007.tfc.client.model.entity.WindmillBladeModel;
import net.dries007.tfc.client.model.entity.WindmillBladeLatticeModel;
import net.dries007.tfc.client.model.entity.WindmillBladeRusticModel;
import net.dries007.tfc.client.model.entity.TFCChestedHorseModel;
import net.dries007.tfc.client.render.blockentity.AxleRenderGeometry;
import net.dries007.tfc.client.render.entity.state.TFCChestedHorseRenderState;

/** Actual production models and CPU geometry. No fake Minecraft classes or live-world claims. */
public final class MechanicalBoatSmoke
{
    private static int assertions;
    private static int scenarios;
    private static long vertices;

    public static void main(String[] args)
    {
        testWindmills();
        testWheel();
        testBoats();
        testCarriedChests();
        testHorse();
        testAxles();
        System.out.printf("PASS: %d mechanical/boat/equine scenarios; %d assertions; %d finite CPU vertices.%n", scenarios, assertions, vertices);
        System.out.println("NOT RUN: renderer construction, live extraction, GPU submission, client/server, save/reload or gameplay.");
    }

    private record Draw(WindmillBladeModel model, WindmillBladeModel.BladePose state, int color) {}

    private static void testWindmills()
    {
        final List<WindmillBladeModel> models = List.of(
            new WindmillBladeModel(WindmillBladeModel.createBodyLayer().bakeRoot()),
            new WindmillBladeLatticeModel(WindmillBladeLatticeModel.createBodyLayer().bakeRoot()),
            new WindmillBladeRusticModel(WindmillBladeRusticModel.createBodyLayer().bakeRoot()));
        final int[] opaqueColors = {-1, 0xffff3300, 0xff4455aa, 0xff112233, 0xff77cc22};
        for (WindmillBladeModel model : models)
        {
            require(model.hasExtras() == (model instanceof WindmillBladeRusticModel), "Only rustic blades have structural extras");
            final List<Draw> deferred = new ArrayList<>();
            for (float angle : new float[] {0, 0.37F, -2F, 6.28F})
                for (int slot = 0; slot < 5; slot++)
                    for (WindmillBladeModel.Portion portion : WindmillBladeModel.Portion.values())
                    {
                        final var state = new WindmillBladeModel.BladePose(angle + (float) (Math.PI * 2 / 5 * slot), portion);
                        final int color = portion == WindmillBladeModel.Portion.BLADE ? opaqueColors[slot] : -1;
                        deferred.add(new Draw(model, state, color));
                    }
            // Queue distinct immutable poses for the same cached model; later mutation cannot collapse them into the last blade.
            List<Float> previous = null;
            for (Draw draw : deferred)
            {
                model.setupAnim(draw.state());
                close(model.root().getChild("main").xRot, -draw.state().angle(), "Captured frame angle");
                close(model.root().getChild("blade").xRot, -draw.state().angle(), "Captured sail angle");
                require(model.root().getChild("main").visible == (draw.state().portion() == WindmillBladeModel.Portion.FRAME), "Frame pass visibility");
                require(model.root().getChild("blade").visible == (draw.state().portion() == WindmillBladeModel.Portion.BLADE), "Blade pass visibility");
                if (model.hasExtras())
                {
                    close(model.root().getChild("extras").xRot, -draw.state().angle(), "Extra angle");
                    require(model.root().getChild("extras").visible == (draw.state().portion() == WindmillBladeModel.Portion.EXTRAS), "Extra pass visibility");
                }
                final List<Float> posed = pose(model);
                model.setupAnim(draw.state());
                equal(posed, pose(model), "Reusing a queued windmill pose is idempotent");
                final Counter c = draw(model, draw.color());
                require((c.count > 0) == (draw.state().portion() != WindmillBladeModel.Portion.EXTRAS || model.hasExtras()), "Only real parts produce geometry");
                if (c.count > 0) require(c.colors.stream().allMatch(color -> color == draw.color()), "Each pass retains its own tint");
                previous = posed;
                scenarios++;
            }
            final var first = deferred.getFirst();
            model.setupAnim(first.state());
            final List<Float> reset = pose(model);
            model.setupAnim(deferred.getLast().state());
            model.setupAnim(first.state());
            equal(reset, pose(model), "Last blade and extras cannot leak into next frame");
            require(previous != null, "Nonempty mechanical test queue");
        }
    }

    private static void testWheel()
    {
        final WaterWheelModel model = new WaterWheelModel(WaterWheelModel.createBodyLayer().bakeRoot());
        final WaterWheelModel other = new WaterWheelModel(WaterWheelModel.createBodyLayer().bakeRoot());
        other.setupAnim(0F);
        final List<Float> independent = pose(other);
        for (float angle : new float[] {0, 0.2F, -1.5F, 3.14F, 6.28F})
        {
            model.setupAnim(angle);
            close(model.root().getChild("main").xRot, -angle, "Waterwheel direction");
            final List<Float> p = pose(model);
            model.setupAnim(angle);
            equal(p, pose(model), "Wheel pose reset");
            equal(independent, pose(other), "Wheel roots remain independent");
            require(draw(model, -1).count > 0, "Wheel emits geometry");
            scenarios++;
        }
    }

    private static void testBoats()
    {
        for (boolean raft : new boolean[] {false, true})
        {
            final Model<BoatRenderState> hull = raft ? new RaftModel(RaftModel.createRaftModel().bakeRoot()) : new BoatModel(BoatModel.createBoatModel().bakeRoot());
            final ModelPart nativeRoot = (raft ? RaftModel.createChestRaftModel() : BoatModel.createChestBoatModel()).bakeRoot();
            final BoatChestModel attachment = new BoatChestModel((raft ? RaftModel.createChestRaftModel() : BoatModel.createChestBoatModel()).bakeRoot());
            for (int i = 0; i < 8; i++)
            {
                final BoatRenderState state = new BoatRenderState();
                state.rowingTimeLeft = i * 0.35F;
                state.rowingTimeRight = (7 - i) * 0.4F;
                state.isUnderWater = (i & 1) != 0;
                hull.setupAnim(state);
                attachment.setupAnim(state);
                final Counter nativeChest = new Counter();
                final PoseStack poses = new PoseStack();
                for (String part : List.of("chest_bottom", "chest_lid", "chest_lock"))
                    nativeRoot.getChild(part).render(poses, nativeChest, 0x00f000f0, 0, -1);
                final Counter actualChest = draw(attachment, -1);
                require(actualChest.count == 72, "Exactly three chest cuboids, without a duplicate hull");
                equal(nativeChest.positions, actualChest.positions, "Native chest shape alignment");
                equal(nativeChest.uvs, actualChest.uvs, "Native chest UV alignment");
                final List<Float> before = pose(attachment);
                attachment.setupAnim(state);
                equal(before, pose(attachment), "Boat chest pose reuse");
                require(draw(hull, -1).count > 0, "Original boat/raft hull remains present");
                scenarios++;
            }
        }
    }

    private static void testCarriedChests()
    {
        final TFCChestedHorseModel body = new TFCChestedHorseModel(DonkeyModel.createBodyLayer(1F).bakeRoot(), false);
        final TFCChestedHorseModel carried = new TFCChestedHorseModel(DonkeyModel.createBodyLayer(1F).bakeRoot(), true);
        for (int mask = 0; mask < 32; mask++)
        {
            final TFCChestedHorseRenderState state = new TFCChestedHorseRenderState();
            state.isBaby = (mask & 1) != 0;
            state.hasChest = (mask & 2) != 0;
            state.eatAnimation = (mask & 4) != 0 ? 0.8F : 0;
            state.standAnimation = (mask & 8) != 0 ? 0.9F : 0;
            state.isInWater = (mask & 16) != 0;
            state.ageScale = state.isBaby ? 0.5F : 1;
            state.xRot = 19;
            state.yRot = 28;
            state.walkAnimationPos = 2.5F;
            state.walkAnimationSpeed = 0.7F;
            state.ageInTicks = 47;
            body.setupAnim(state);
            carried.setupAnim(state);
            close(state.xRot, 19, "Chested model must not mutate shared head-pitch state");
            equalMatrices(body.root(), carried.root(), "Body/carried juvenile root alignment");
            equalMatrices(body.root().getChild("body"), carried.root().getChild("body"), "Carried body animation alignment");
            for (String side : List.of("left_chest", "right_chest"))
                equalMatrices(body.root().getChild("body").getChild(side), carried.root().getChild("body").getChild(side), "Carried side alignment");
            require(!body.root().getChild("body").getChild("left_chest").visible, "Base pass excludes luggage");
            close(body.root().xScale, state.isBaby ? 0.5F : 1, "Adult-layout juvenile scale");
            final Counter c = draw(carried, -1);
            require(c.count == (state.hasChest ? 48 : 0), "Carried pass emits only two chests, even for juveniles");
            final List<Float> first = pose(body);
            body.setupAnim(state);
            equal(first, pose(body), "Chested horse body pose is reusable");
            require(draw(body, -1).count > 0, "Donkey/mule body retained");
            scenarios++;
        }
    }

    private static void testHorse()
    {
        for (boolean baby : new boolean[] {false, true})
        {
            final HorseModel model = baby
                ? new BabyHorseModel(LayerDefinition.create(BabyHorseModel.createBabyMesh(CubeDeformation.NONE), 64, 64).bakeRoot())
                : new HorseModel(LayerDefinition.create(AbstractEquineModel.createBodyMesh(CubeDeformation.NONE), 64, 64).bakeRoot());
            for (int mask = 0; mask < 8; mask++)
            {
                final HorseRenderState state = new HorseRenderState();
                state.isBaby = baby;
                state.ageScale = baby ? 0.5F : 1;
                state.eatAnimation = (mask & 1) != 0 ? 0.8F : 0;
                state.standAnimation = (mask & 2) != 0 ? 0.9F : 0;
                state.animateTail = (mask & 4) != 0;
                state.xRot = 18;
                state.yRot = 22;
                state.ageInTicks = 41;
                model.setupAnim(state);
                final List<Float> initial = pose(model);
                model.setupAnim(state);
                equal(initial, pose(model), "Native horse pose reuse");
                close(state.xRot, 18, "Horse input pose remains unchanged");
                require(draw(model, -1).count > 0, "Native horse body geometry");
                scenarios++;
            }
        }
    }

    private static void testAxles()
    {
        final AxleRenderGeometry.Texture texture = new AxleRenderGeometry.Texture(0.25F, 0.5F, 0.625F, 0.875F);
        for (Direction.Axis axis : Direction.Axis.values())
            for (float angle : new float[] {0, 0.2F, -1.5F, 3.14F})
            {
                final PoseStack expected = new PoseStack();
                expected.translate(0.5F, 0.5F, 0.5F);
                if (axis == Direction.Axis.X) expected.mulPose(Axis.YP.rotationDegrees(90));
                if (axis == Direction.Axis.Y) expected.mulPose(Axis.XP.rotationDegrees(-90));
                expected.mulPose(Axis.ZP.rotation(angle));
                expected.translate(-0.5F, -0.5F, -0.5F);
                final PoseStack actual = new PoseStack();
                AxleRenderGeometry.applyRotation(actual, axis, angle);
                require(actual.last().pose().equals(expected.last().pose(), 0.00001F), "Legacy axle axis/rotation matrix");
                final Counter axle = new Counter(), blade = new Counter();
                AxleRenderGeometry.drawAxle(actual.last(), axle, texture, 12345, 54321);
                AxleRenderGeometry.drawBlade(actual.last(), blade, texture, 12345, 54321);
                require(axle.count == 24 && blade.count == 24, "Six axle/blade faces");
                require(axle.colors.stream().allMatch(c -> c == -1) && blade.colors.stream().allMatch(c -> c == -1), "Unshaded white mechanical geometry");
                for (Counter c : List.of(axle, blade))
                {
                    for (int i = 0; i < c.uvs.size(); i += 2)
                    {
                        require(c.uvs.get(i) >= texture.u0() && c.uvs.get(i) <= texture.u1(), "Atlas U bounds");
                        require(c.uvs.get(i + 1) >= texture.v0() && c.uvs.get(i + 1) <= texture.v1(), "Atlas V bounds");
                    }
                    vertices += c.count;
                }
                scenarios++;
            }
    }

    private static void equalMatrices(ModelPart left, ModelPart right, String label)
    {
        final PoseStack a = new PoseStack(), b = new PoseStack();
        left.translateAndRotate(a);
        right.translateAndRotate(b);
        require(a.last().pose().equals(b.last().pose(), 0.00001F), label);
    }

    private static List<Float> pose(Model<?> model)
    {
        final List<Float> result = new ArrayList<>();
        for (ModelPart part : model.allParts())
            for (float value : new float[] {part.x, part.y, part.z, part.xRot, part.yRot, part.zRot, part.xScale, part.yScale, part.zScale,
                part.visible ? 1 : 0, part.skipDraw ? 1 : 0})
            {
                if (!Float.isFinite(value)) throw new AssertionError("Non-finite model pose");
                result.add(value);
            }
        return result;
    }

    private static void equal(List<Float> expected, List<Float> actual, String label)
    {
        require(expected.size() == actual.size(), label + " size");
        for (int i = 0; i < expected.size(); i++)
            if (Math.abs(expected.get(i) - actual.get(i)) > 0.00001F) throw new AssertionError(label + " index " + i);
        assertions++;
    }

    private static Counter draw(Model<?> model, int color)
    {
        final Counter c = new Counter();
        model.renderToBuffer(new PoseStack(), c, 0x00f000f0, 0, color);
        vertices += c.count;
        return c;
    }

    private static void close(float actual, float expected, String label) { require(Float.isFinite(actual) && Math.abs(actual - expected) < 0.00001F, label); }
    private static void require(boolean ok, String label) { if (!ok) throw new AssertionError(label); assertions++; }

    private static final class Counter implements VertexConsumer
    {
        int count;
        final List<Float> positions = new ArrayList<>(), uvs = new ArrayList<>();
        final List<Integer> colors = new ArrayList<>();
        private void finite(float... values) { for (float f : values) if (!Float.isFinite(f)) throw new AssertionError("Non-finite CPU vertex"); }
        public VertexConsumer addVertex(float x, float y, float z) { finite(x, y, z); positions.add(x); positions.add(y); positions.add(z); count++; return this; }
        public VertexConsumer setColor(int r, int g, int b, int a) { colors.add(a << 24 | r << 16 | g << 8 | b); return this; }
        public VertexConsumer setColor(int argb) { colors.add(argb); return this; }
        public VertexConsumer setUv(float u, float v) { finite(u, v); uvs.add(u); uvs.add(v); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { finite(x, y, z); return this; }
        public VertexConsumer setLineWidth(float w) { finite(w); return this; }
    }
}
