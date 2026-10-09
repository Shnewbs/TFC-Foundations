/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.client.model.DynamicBlockModel;
import net.dries007.tfc.client.model.MoldsModelLoader;
import net.dries007.tfc.client.model.SeasonalBlockStateModel;
import net.dries007.tfc.client.model.SeasonalModelMath;
import net.dries007.tfc.client.model.SeasonalUnbakedModel;

/** Scalar parity, native unbaked geometry and dispatch-contract checks; not a game/atlas/GPU test. */
public final class SeasonalBlockModelSmoke
{
    private static int comparisons;
    private static int assertions;
    private static int moldCells;
    private static int loadedAssets;

    private static void check(boolean value, String message)
    {
        assertions++;
        if (!value) throw new AssertionError(message);
    }

    private static void rejects(Runnable action, String message)
    {
        try { action.run(); }
        catch (IllegalArgumentException | com.google.gson.JsonParseException expected) { assertions++; return; }
        throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception
    {
        scalarParity();
        routing();
        geometry();
        loaderAssets();
        variants();
        System.out.printf("PASS: %d scalar parity comparisons; %d contract assertions; %d native mold cells; %d asset loader checks.%n",
            comparisons, assertions, moldCells, loadedAssets);
        System.out.println("NOT RUN: live climate/world extraction, registered resource reload, atlas baking, GPU rendering or gameplay.");
    }

    private static void scalarParity()
    {
        final Random random = new Random(0x51EA50A1L);
        for (int i = 0; i < 20000; i++)
        {
            final float t = random.nextFloat();
            final float b = random.nextFloat();
            final float be = b + random.nextFloat() * .25f;
            final float se = be + random.nextFloat() * .2f;
            final float de = se + random.nextFloat() * .2f;
            final float doe = de + random.nextFloat() * .3f;
            final float spe = doe + random.nextFloat() * .2f;
            final int start = random.nextInt(24000), end = i % 3 == 0 ? start : random.nextInt(24000);
            final boolean nonDormant = random.nextBoolean(), day = random.nextBoolean();
            for (float time : new float[] {t, b, Math.nextDown(b), Math.nextUp(b), be % 1, se % 1, de % 1, doe % 1, spe % 1})
            {
                final int expected = SeasonalLegacyReference.plant(time, b, be, se, de, doe, spe, start, end, nonDormant, day ? 3 : 2);
                check(SeasonalModelMath.plantStage(time, b, be, se, de, doe, spe, start, end, nonDormant, day) == expected, "plant stage parity");
                comparisons++;
            }
            final float temp = i % 2 == 0 ? 11.65f + random.nextFloat() * 1.2f : random.nextFloat() * 100 - 60;
            final float rv = i % 3 == 0 ? .375f + random.nextFloat() * .05f : random.nextFloat() * 2 - 1;
            final float avgRain = random.nextFloat() * 500;
            final boolean conifer = random.nextBoolean(), north = random.nextBoolean();
            final float offset = random.nextFloat();
            final int climateHash = random.nextInt(128), positionHash = random.nextInt(128);
            final int expected = SeasonalLegacyReference.leaves(conifer, offset, temp, rv, avgRain, t, north, climateHash, positionHash);
            final float lazyRain = SeasonalModelMath.needsAverageRainfall(temp, rv, climateHash) ? avgRain : 0;
            check(SeasonalModelMath.leavesStage(conifer, offset, temp, rv, lazyRain, t, north, climateHash, positionHash) == expected, "leaf stage parity / lazy rainfall");
            comparisons++;
        }
        for (int start : new int[] {0, 6000, 12000, 18000, 23999})
        {
            for (int end : new int[] {0, 6000, 12000, 18000, 23999})
            {
                for (long time : new long[] {0, 1, start - 1, start, start + 1, end - 1, end, end + 1, 23999})
                {
                    final boolean expected = !((end < time && time < start) || (start < end && (time < start || end < time)));
                    check(SeasonalModelMath.isBloomingTime(start, end, time) == expected, "day/night boundary parity");
                    comparisons++;
                }
            }
        }
    }

    /** Synthetic model parts isolate dispatch from atlas setup; no texture/rendering success is claimed. */
    private record Part(int index, Material.Baked particleMaterial) implements BlockStateModelPart
    {
        @Override public List<BakedQuad> getQuads(Direction side) { return List.of(); }
        @Override public boolean useAmbientOcclusion() { return true; }
        @Override public int materialFlags() { return 1 << index; }
    }

    private static final class Selection extends SeasonalBlockStateModel
    {
        private int index;
        private Selection(List<BlockStateModelPart> parts) { super(parts, 6, 3); }
        @Override protected int select(BlockState state, BlockPos pos) { return index; }
    }

    private static void routing()
    {
        final List<BlockStateModelPart> parts = new ArrayList<>();
        for (int i = 0; i < 6; i++) parts.add(new Part(i, new Material.Baked(null, i % 2 != 0)));
        final List<BlockStateModelPart> original = List.copyOf(parts);
        final Selection a = new Selection(parts), b = new Selection(parts);
        parts.clear();
        check(a.materialFlags() == 63, "union must include every material layer");
        check(a.particleMaterial() == original.get(3).particleMaterial(), "fallback particle");
        Object previous = null;
        for (int cycle = 0; cycle < 3; cycle++)
        {
            for (int i = 0; i < 6; i++)
            {
                a.index = i;
                final List<BlockStateModelPart> output = new ArrayList<>();
                a.collectParts(null, null, null, null, output);
                check(output.equals(List.of(original.get(i))), "selected geometry only / defensive copy");
                check(a.materialFlags(null, null, null) == 1 << i, "selected flags");
                check(a.particleMaterial(null, null, null) == original.get(i).particleMaterial(), "selected particle");
                final Object key = a.createGeometryKey(null, null, null, null);
                check(key.equals(a.createGeometryKey(null, null, null, null)), "stable geometry key");
                check(!key.equals(previous), "phase change invalidates key");
                check(!key.equals(b.createGeometryKey(null, null, null, null)), "model-local key");
                previous = key;
            }
        }
        rejects(() -> new Selection(List.of()), "missing alternatives must fail");
    }

    private static void geometry()
    {
        rejects(() -> MoldsModelLoader.generateBlockElementsFromPattern(new boolean[13][14]), "bad rows");
        rejects(() -> MoldsModelLoader.generateBlockElementsFromPattern(new boolean[14][13]), "bad columns");
        check(MoldsModelLoader.generateBlockElementsFromPattern(new boolean[14][14]).isEmpty(), "empty mold");
        for (int pattern = 0; pattern <= 196; pattern++)
        {
            final boolean[][] mask = new boolean[14][14];
            for (int x = 0; x < 14; x++)
                for (int z = 0; z < 14; z++) mask[x][z] = pattern == 196 || x * 14 + z == pattern;
            final List<CuboidModelElement> cells = MoldsModelLoader.generateBlockElementsFromPattern(mask);
            check(cells.size() == (pattern == 196 ? 196 : 1), "occupied cell count");
            for (CuboidModelElement cell : cells)
            {
                moldCells++;
                final int x = (int) cell.from().x(), z = (int) cell.from().z();
                check(mask[x - 1][z - 1], "pattern orientation");
                check(cell.from().y() == 1 && cell.to().y() == 2, "mold recess height");
                check(cell.to().x() == x + 1 && cell.to().z() == z + 1, "unit cell bounds");
                check(cell.faces().size() == 6, "six retained unculled faces");
                check(cell.rotation() == null && cell.shade() && cell.lightEmission() == 0, "legacy rotation/light/shade");
                for (Direction direction : Direction.values())
                {
                    final var face = cell.faces().get(direction);
                    check(face.cullForDirection() == null && face.tintIndex() == -1 && face.texture().equals("#0"), "face texture and cull metadata");
                    final float[] expected = switch (direction)
                    {
                        case NORTH -> new float[] {15 - x, 14, 16 - x, 15};
                        case SOUTH -> new float[] {x, 14, x + 1, 15};
                        case WEST -> new float[] {z, 14, z + 1, 15};
                        case EAST -> new float[] {15 - z, 14, 16 - z, 15};
                        case UP -> new float[] {x, z, x + 1, z + 1};
                        case DOWN -> new float[] {x, 15 - z, x + 1, 16 - z};
                    };
                    check(face.uvs().minU() == expected[0] && face.uvs().minV() == expected[1]
                        && face.uvs().maxU() == expected[2] && face.uvs().maxV() == expected[3], "directional UV parity");
                }
            }
        }
    }

    private static final JsonDeserializationContext CONTEXT = new JsonDeserializationContext()
    {
        @Override public <T> T deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type type)
        {
            return CuboidModel.GSON.fromJson(json, type);
        }
    };

    private static void loaderAssets() throws Exception
    {
        try (var paths = Files.walk(Path.of("src/main/resources/assets")))
        {
            for (Path path : paths.filter(p -> p.toString().contains("/models/") && p.toString().endsWith(".json")).toList())
            {
                final JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (!json.has("loader")) continue;
                final String loader = json.get("loader").getAsString(), before = json.toString();
                if (loader.equals("tfc:plant") || loader.equals("tfc:leaves"))
                {
                    final List<String> names = loader.equals("tfc:plant") ? List.of("dormant", "sprouting", "budding", "blooming", "seeding", "dying")
                        : List.of("dense_leaves", "sparse_leaves", "bare", "blooming");
                    final SeasonalUnbakedModel model = new SeasonalUnbakedModel(json, CONTEXT, names, 0, ignored -> null);
                    final Set<Identifier> dependencies = new HashSet<>();
                    model.resolveDependencies(dependencies::add);
                    final Set<Identifier> expected = new HashSet<>();
                    if (json.has("parent")) expected.add(Identifier.parse(json.get("parent").getAsString()));
                    for (String name : names)
                    {
                        UnbakedModel child = CONTEXT.deserialize(json.get(name), UnbakedModel.class);
                        child.resolveDependencies(expected::add);
                    }
                    check(dependencies.equals(expected), "all seasonal dependencies / parent references");
                    check(model.geometry() != null, "ordinary-consumer geometry fallback");
                    final JsonObject bad = json.deepCopy(); bad.remove(names.getFirst());
                    rejects(() -> new SeasonalUnbakedModel(bad, CONTEXT, names, 0, ignored -> null), "missing stage must fail");
                    loadedAssets++;
                }
                else if (loader.equals("tfc:mold"))
                {
                    final var model = new MoldsModelLoader().read(json, CONTEXT);
                    final UnbakedGeometry geometry = model.geometry();
                    check(geometry instanceof net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry, "native mold geometry");
                    int occupied = 0;
                    for (var row : json.getAsJsonArray("pattern"))
                        for (char c : row.getAsString().toCharArray()) if (c != ' ') occupied++;
                    check(((net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry) geometry).elements().size() == occupied, "actual mold pattern cells");
                    loadedAssets++;
                }
                check(before.equals(json.toString()), "loader must not mutate caller JSON");
            }
        }
        final JsonObject invalid = JsonParser.parseString("{\"pattern\":[]}").getAsJsonObject();
        rejects(() -> new MoldsModelLoader().read(invalid, CONTEXT), "empty pattern data must fail");
    }

    private static void variants()
    {
        for (int x : new int[] {0, 90, 180, 270})
            for (int y : new int[] {0, 90, 180, 270})
                for (boolean uvlock : new boolean[] {false, true})
                {
                    final JsonObject json = new JsonObject();
                    json.addProperty("model", "tfc:block/wood/leaves/oak_dynamic");
                    json.addProperty("x", x); json.addProperty("y", y); json.addProperty("uvlock", uvlock);
                    final var decoded = DynamicBlockModel.Unbaked.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
                    final var encoded = DynamicBlockModel.Unbaked.CODEC.codec().encodeStart(JsonOps.INSTANCE, decoded).getOrThrow();
                    check(decoded.equals(DynamicBlockModel.Unbaked.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow()), "variant round trip");
                    final Set<Identifier> deps = new HashSet<>(); decoded.resolveDependencies(deps::add);
                    check(deps.equals(Set.of(Identifier.parse("tfc:block/wood/leaves/oak_dynamic"))), "variant dependency");
                    check(decoded.variant().modelState().uvLock() == uvlock, "UV lock retained");
                    check(decoded.variant().modelState().asModelState().transformation().getMatrix().isFinite(), "finite native transformation");
                }
    }
}
