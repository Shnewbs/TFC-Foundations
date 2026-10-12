/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import net.dries007.tfc.client.model.entity.AnemometerModel;
import net.dries007.tfc.client.model.entity.CalendarClockModel;
import net.dries007.tfc.client.model.entity.VaneModel;
import net.dries007.tfc.client.model.entity.JavelinModel;

/** Exercises native model poses and compares emitted vertices with the former per-part draw path. */
public final class InstrumentModelSmoke
{
    public static void main(String[] args)
    {
        final AnemometerModel anemometer = new AnemometerModel(AnemometerModel.createBodyLayer().bakeRoot());
        final CalendarClockModel clock = new CalendarClockModel(CalendarClockModel.createBodyLayer().bakeRoot());
        final VaneModel vane = new VaneModel(VaneModel.createBodyLayer().bakeRoot());
        final JavelinModel javelin = new JavelinModel(JavelinModel.createBodyLayer().bakeRoot());
        compare(javelin, "bb_main");
        int scenarios = 0;
        for (float angle : new float[] {0, 0.25F, -1.5F, Mth.PI, 2 * Mth.PI})
        {
            anemometer.setupAnim(new AnemometerModel.State(angle));
            equal(anemometer.root().getChild("spinny").yRot, angle);
            compare(anemometer, "post", "base", "spinny");
            final CalendarClockModel.State saved = new CalendarClockModel.State(angle, angle / 2, -angle);
            clock.setupAnim(new CalendarClockModel.State(99, 98, 97));
            clock.setupAnim(saved);
            equal(clock.root().getChild("minute_hand").yRot, angle + Mth.PI);
            equal(clock.root().getChild("hour_hand").yRot, angle / 2 + Mth.PI);
            equal(clock.root().getChild("month_hand").yRot, -angle + Mth.PI);
            compare(clock, "static_parts", "hour_hand", "minute_hand", "month_hand");
            for (boolean base : new boolean[] {true, false, true})
            {
                vane.setupAnim(new VaneModel.State(angle, base));
                equal(vane.root().getChild("spinny").yRot, (float) (angle + Math.PI / 2));
                if (vane.root().getChild("base").visible != base) throw new AssertionError("Vane base leaked between states");
                compare(vane, base ? new String[] {"base", "static", "spinny"} : new String[] {"static", "spinny"});
                scenarios++;
            }
            scenarios += 2;
        }
        System.out.println("PASS: 3 instrument models, " + scenarios + " pose/visibility scenarios, and the javelin model; legacy per-part geometry matches native root rendering.");
        System.out.println("NOT RUN: live block-entity extraction, GPU drawing, resource reload or gameplay.");
    }

    private static void equal(float actual, float expected)
    {
        if (Math.abs(actual - expected) > 0.000001F) throw new AssertionError(actual + " != " + expected);
    }

    private static void compare(Model<?> model, String... parts)
    {
        final Vertices actual = new Vertices();
        model.renderToBuffer(new PoseStack(), actual, 0x00f000f0, 0, -1);
        final Vertices legacy = new Vertices();
        for (String name : parts)
        {
            final ModelPart part = model.root().getChild(name);
            part.render(new PoseStack(), legacy, 0x00f000f0, 0, -1);
        }
        // Child traversal order may change, but the vertex attributes and multiplicity must not.
        Collections.sort(actual.values);
        Collections.sort(legacy.values);
        if (actual.values.isEmpty() || !actual.values.equals(legacy.values)) throw new AssertionError("Instrument geometry changed");
    }

    private static final class Vertices implements VertexConsumer
    {
        final List<String> values = new ArrayList<>();
        private String vertex;
        public VertexConsumer addVertex(float x, float y, float z) { vertex = x + "," + y + "," + z; return this; }
        public VertexConsumer setColor(int argb) { return this; }
        public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        public VertexConsumer setUv(float u, float v) { vertex += "," + u + "," + v; return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { values.add(vertex + "," + x + "," + y + "," + z); return this; }
        public VertexConsumer setLineWidth(float width) { return this; }
    }
}
