/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import net.dries007.tfc.common.blockentities.BlockEntityModelData;
import net.dries007.tfc.client.model.*;

public final class SnapshotBlockSmoke
{
    private static int assertions, vertices, pileScenarios, masks;
    private static final Identifier A = Identifier.fromNamespaceAndPath("tfc", "block/metal/soft/copper");
    private static final Identifier B = Identifier.fromNamespaceAndPath("tfc", "block/metal/soft/bismuth");
    private static final Identifier UNKNOWN = Identifier.fromNamespaceAndPath("tfc", "block/metal/soft/unknown");
    private static void check(boolean value, String message)
    {
        assertions++;
        if (!value) throw new AssertionError(message);
    }
    private static void near(float a, float b, String message) { check(Float.isFinite(a) && Math.abs(a - b) < 0.000002f, message + ": " + a + " != " + b); }
    private static void rejected(Runnable action)
    {
        try { action.run(); throw new AssertionError("Expected rejection"); }
        catch (IllegalArgumentException | UnsupportedOperationException expected) { assertions++; }
    }
    private static void pileParity()
    {
        final List<Identifier> source = new ArrayList<>(List.of(A, B, A));
        final var data = new BlockEntityModelData.Pile(source, UNKNOWN);
        source.clear();
        check(data.textures().size() == 3, "defensive snapshot copy");
        rejected(() -> data.textures().clear());
        for (boolean doubled : new boolean[] {false, true})
        {
            for (int count = 0; count <= (doubled ? 36 : 64); count++)
            {
                final var mesh = StaticBlockMesh.pile(data, count, doubled);
                final List<float[]> legacy = PileLegacyReference.vertices(count, doubled);
                check(mesh.faces().size() == 6 * count, "six faces per ingot");
                check(mesh.particle().equals(count == 0 ? UNKNOWN : data.texture(count - 1)), "top ingot particle");
                int index = 0;
                for (var face : mesh.faces())
                {
                    check(face.texture().equals(data.texture(index / 24)), "per-ingot material");
                    check(face.color() == -1, "native shading must start from white");
                    for (var v : face.vertices())
                    {
                        final float[] old = legacy.get(index++);
                        near(v.x(), old[0], "legacy x"); near(v.y(), old[1], "legacy y"); near(v.z(), old[2], "legacy z");
                        near(v.u(), old[3], "legacy u"); near(v.v(), old[4], "legacy v");
                        check(v.x() >= -0.000002f && v.x() <= 1.000002f && v.y() >= 0 && v.y() <= 1 && v.z() >= -0.000002f && v.z() <= 1.000002f, "pile bounds");
                        vertices++;
                    }
                }
                check(index == legacy.size(), "all legacy vertices emitted");
                pileScenarios++;
            }
        }
        rejected(() -> StaticBlockMesh.pile(data, -1, false));
        rejected(() -> StaticBlockMesh.pile(data, 65, false));
        rejected(() -> StaticBlockMesh.pile(data, 37, true));
    }
    private static void scraping()
    {
        // Every possible 4x4 bit mask is checked for exactly one input/output tile per position.
        for (int mask = 0; mask <= 0xffff; mask++)
        {
            var data = new BlockEntityModelData.Scraping(A, B, (short) mask, 0x123456, 0x987654);
            var mesh = StaticBlockMesh.scraping(data, UNKNOWN);
            if (mesh.faces().size() != 16) throw new AssertionError("Missing/duplicate scraping tile");
            int seen = 0, outputs = 0;
            for (var face : mesh.faces())
            {
                var first = face.vertices().get(0);
                int x = Math.round(first.x() * 4), z = Math.round(first.z() * 4), bit = 1 << (x + 4 * z);
                if ((seen & bit) != 0) throw new AssertionError("Duplicate scraping cell");
                seen |= bit;
                boolean output = (mask & bit) != 0;
                if (!face.texture().equals(output ? B : A) || face.color() != (output ? 0xff987654 : 0xff123456)) throw new AssertionError("Scraping material/color mismatch");
                if (output) outputs++;
                for (var v : face.vertices()) if (v.y() != 0.01f || v.u() != v.x() || v.v() != v.z()) throw new AssertionError("Scraping height/UV mismatch");
            }
            if (seen != 0xffff || outputs != Integer.bitCount(mask)) throw new AssertionError("Wrong mask coverage");
            masks++;
        }
        check(StaticBlockMesh.scraping(new BlockEntityModelData.Scraping(null, B, (short) 0, -1, -1), UNKNOWN).faces().isEmpty(), "missing input stays empty");
        check(StaticBlockMesh.scraping(new BlockEntityModelData.Scraping(A, null, (short) 0, -1, -1), UNKNOWN).particle().equals(UNKNOWN), "missing output particle");
        final var face = StaticBlockMesh.scraping(new BlockEntityModelData.Scraping(A, B, (short) 0, -1, -1), UNKNOWN).faces().get(0);
        check(face.direction() == Direction.UP, "horizontal scraping surface normal");
        rejected(() -> face.vertices().clear());
    }
    private static void transformations()
    {
        var face = StaticBlockMesh.pile(new BlockEntityModelData.Pile(List.of(A), UNKNOWN), 1, false).faces().get(0);
        final Matrix4f geometry = new Matrix4f().rotateY((float) Math.PI / 2).translate(0.1f, 0.2f, -0.1f).scale(0.75f, 0.5f, 0.8f);
        final Matrix4f uv = new Matrix4f().rotateZ((float) Math.PI / 2);
        final ModelState state = new ModelState() {
            @Override public Transformation transformation() { return new Transformation(geometry); }
            @Override public org.joml.Matrix4fc inverseFaceTransformation(Direction ignored) { return uv; }
        };
        var transformed = StaticBlockMesh.transform(face, state);
        for (int i = 0; i < 4; i++)
        {
            var v = face.vertices().get(i); var t = transformed.vertices().get(i);
            var expected = new Matrix4f().translate(0.5f,0.5f,0.5f).mul(geometry).translate(-0.5f,-0.5f,-0.5f).transformPosition(new Vector3f(v.x(),v.y(),v.z()));
            near(t.x(),expected.x,"transformed x"); near(t.y(),expected.y,"transformed y"); near(t.z(),expected.z,"transformed z");
            var expectedUv = new Matrix4f().translate(0.5f,0.5f,0).mul(uv).translate(-0.5f,-0.5f,0).transformPosition(new Vector3f(v.u(),v.v(),0));
            near(t.u(),expectedUv.x,"locked u"); near(t.v(),expectedUv.y,"locked v");
        }
        var identity = StaticBlockMesh.transform(face, BlockModelRotation.IDENTITY);
        for (int i=0; i<4; i++) { near(identity.vertices().get(i).x(),face.vertices().get(i).x(),"identity"); }
    }
    // Model parts and the level below are controlled dispatch fixtures, not atlas-baked game models.
    private record Part(int flags, int weight) implements BlockStateModelPart
    {
        @Override public List<BakedQuad> getQuads(Direction direction) { return direction == null ? Collections.nCopies(weight, null) : List.of(); }
        @Override public boolean useAmbientOcclusion() { return true; }
        @Override public Material.Baked particleMaterial() { return new Material.Baked(null, false); }
        @Override public int materialFlags() { return flags; }
    }
    private static void cacheAndDispatch() throws Exception
    {
        final ModelProperty<String> property = new ModelProperty<>();
        final AtomicInteger generated = new AtomicInteger();
        final var empty = new Part(0, 0);
        final var model = new SnapshotBlockStateModel<String>((data,state)->data.get(property), value -> { generated.incrementAndGet(); return new Part(3, 256); }, empty);
        final var other = new SnapshotBlockStateModel<String>((data,state)->data.get(property), value -> new Part(3,256), empty);
        check(model.part(null) == empty, "empty snapshot");
        check(model.geometryKey("a").equals(model.geometryKey(new String("a"))), "equivalent snapshots reuse keys");
        check(!model.geometryKey("a").equals(model.geometryKey("b")), "different snapshots cannot alias");
        check(!model.geometryKey("a").equals(other.geometryKey("a")), "model/reload identity in key");
        try (var pool = Executors.newFixedThreadPool(8))
        {
            var tasks = new ArrayList<java.util.concurrent.Future<BlockStateModelPart>>();
            for (int i=0; i<64; i++) tasks.add(pool.submit(()->model.part("a")));
            var first = tasks.getFirst().get();
            for (var task : tasks) check(task.get() == first, "concurrent cache result");
        }
        check(generated.get() == 1, "single load per immutable key");
        final AtomicReference<ModelData> snapshot = new AtomicReference<>(ModelData.of(property,"a"));
        final BlockAndTintGetter level = (BlockAndTintGetter) Proxy.newProxyInstance(SnapshotBlockSmoke.class.getClassLoader(), new Class<?>[] {BlockAndTintGetter.class},
            (proxy, method, args) -> { if (method.getName().equals("getModelData")) return snapshot.get(); throw new AssertionError("Unexpected world read: " + method); });
        final List<BlockStateModelPart> output = new ArrayList<>();
        model.collectParts(level,null,null,null,output);
        check(output.size()==1 && output.getFirst()==model.part("a"), "snapshot dispatch");
        var key = model.createGeometryKey(level,null,null,null);
        snapshot.set(ModelData.EMPTY); output.clear(); model.collectParts(level,null,null,null,output);
        check(output.getFirst()==empty && !key.equals(model.createGeometryKey(level,null,null,null)), "removal clears cached appearance");
        check(model.materialFlags(level,null,null)==0 && model.materialFlags()==3, "context flags and conservative fallback");
        var old = model.part("a");
        for(int i=0;i<128;i++) model.part("other"+i);
        check(model.part("a")!=old,"bounded geometry cache evicts old entries");
    }
    private static final JsonDeserializationContext JSON = new JsonDeserializationContext() {
        @Override public <T> T deserialize(JsonElement json, java.lang.reflect.Type type) { return CuboidModel.GSON.fromJson(json, type); }
    };
    private static void moldsAndLoaders() throws Exception
    {
        final var base = new Part(1,0); final var insert = new Part(2,0);
        final Map<Identifier,BlockStateModelPart> parts = new HashMap<>();parts.put(A,insert);
        final var model = new MoldTableBlockModel(base,parts);parts.clear();
        final List<BlockStateModelPart> output = new ArrayList<>(); model.collect(A,output);
        check(output.equals(List.of(base,insert)), "base and insert exactly once");
        output.clear();model.collect(null,output);check(output.equals(List.of(base)),"removed mold clears appearance");
        output.clear();model.collect(B,output);check(output.equals(List.of(base)),"unknown item has no mold");
        check(model.materialFlags()==3,"table material union");
        check(!model.geometryKey(A).equals(model.geometryKey(null)),"mold selection participates in key");
        check(!model.geometryKey(A).equals(new MoldTableBlockModel(base,Map.of(A,insert)).geometryKey(A)),"table reload isolation");
        var json = JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/tfc/models/block/mold_table_base.json"))).getAsJsonObject();
        var before = json.deepCopy();
        var unbaked = MoldTableBlockModel.Loader.INSTANCE.read(json, JSON);
        check(json.equals(before), "table loader must not mutate caller JSON");
        check(((UnbakedCuboidGeometry) unbaked.geometry()).elements().size()==json.getAsJsonArray("elements").size(),"table geometry retained");
        for(var loader:List.of(IngotPileBlockModel.INSTANCE,DoubleIngotPileBlockModel.INSTANCE,ScrapingBlockModel.INSTANCE))
        {
            var loaded=loader.read(new JsonObject(),JSON);
            check(loaded.geometry()==UnbakedGeometry.EMPTY,"context-free world-only fallback");
            check(loaded instanceof DynamicBlockModel,"world model dispatch contract");
        }
        final List<Identifier> resources = new ArrayList<>();
        try(var files=Files.walk(Path.of("src/main/resources/assets/tfc/models/block/mold")))
        {
            files.filter(p->p.toString().endsWith(".json")).forEach(p->resources.add(Identifier.fromNamespaceAndPath("tfc",p.toString().replace("src/main/resources/assets/tfc/", "").replace('\\','/'))));
        }
        final var extra=Identifier.fromNamespaceAndPath("addon", "models/block/mold/custom/mold.json"); resources.add(extra);
        resources.add(Identifier.fromNamespaceAndPath("addon", "models/block/moldish/not_a_mold.json"));
        final Map<net.neoforged.neoforge.client.model.standalone.StandaloneModelKey<?>,net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel<?>> registered = new IdentityHashMap<>();
        MoldTableBlockModel.registerDiscoveredModels(new ModelEvent.RegisterStandalone(registered), resources);
        check(registered.size()==resources.size()-1,"register all matching namespaces and ignore unrelated files");
        final Set<Identifier> dependencies = new HashSet<>();unbaked.resolveDependencies(dependencies::add);
        check(dependencies.contains(Identifier.fromNamespaceAndPath("addon","block/mold/custom/mold")),"resource-pack molds are resolved");
        check(MoldTableBlockModel.itemForModelResource(extra).equals(Identifier.fromNamespaceAndPath("addon","custom/mold")),"namespace and nested item paths");
        check(MoldTableBlockModel.itemForModelResource(Identifier.fromNamespaceAndPath("tfc","models/block/mold/.json"))==null,"reject empty item path");
        MoldTableBlockModel.registerDiscoveredModels(new ModelEvent.RegisterStandalone(new IdentityHashMap<>()), List.of());
        var next=MoldTableBlockModel.Loader.INSTANCE.read(json,JSON);Set<Identifier> nextDeps=new HashSet<>();next.resolveDependencies(nextDeps::add);
        check(nextDeps.isEmpty(),"reload discovery replaces catalog rather than retaining deleted files");
        output.clear();model.collect(A,output);check(output.equals(List.of(base,insert)),"new discovery cannot mutate a baked table");
    }
    // Compiled, not invoked: catches ambiguous event method references without constructing a client.
    private static void registrationSignature(net.neoforged.bus.api.IEventBus bus)
    {
        bus.addListener(MoldTableBlockModel::registerStandaloneModels);
    }

    public static void main(String[] args) throws Exception
    {
        pileParity(); scraping(); transformations(); cacheAndDispatch(); moldsAndLoaders();
        System.out.println("PASS: " + pileScenarios + " pile-count scenarios, " + vertices + " position/UV vertices compared with the frozen legacy path; all " + masks + " scraping masks checked; " + assertions + " counted assertions (including vertex comparisons).");
        System.out.println("PASS: immutable snapshots, concurrent/evicting cache, model-data dispatch fixtures, native loader parsing and standalone dependency registration.");
        System.out.println("NOT RUN: atlas/material baking, registered whole-resource reload, live block entities, GPU rendering or gameplay.");
    }
}
