/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

/** Real native images, sprites, quads and codecs; no Minecraft classes are replaced. No GPU or mod bootstrap. */
public final class ItemLayerSmoke
{
    private static int checks;
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("Item layer assertion " + checks); }

    private static class Sprite extends TextureAtlasSprite
    {
        Sprite(Identifier atlas, int x, int y)
        {
            super(atlas, createContents(), 128, 128, x, y, 0);
        }
        private static SpriteContents createContents()
        {
            final NativeImage image = new NativeImage(16, 16, true);
            for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) image.setPixel(x, y, 0xFFFFFFFF);
            return new SpriteContents(Identifier.fromNamespaceAndPath("tfc", "cpu_fixture"), new FrameSize(16, 16), image);
        }
    }

    private static void geometry()
    {
        final Sprite mask = new Sprite(TextureAtlas.LOCATION_ITEMS, 16, 32);
        final Sprite fluid = new Sprite(TextureAtlas.LOCATION_BLOCKS, 64, 80);
        try
        {
            for (Direction direction : Direction.values())
            {
                final var material = new BakedQuad.MaterialInfo(mask, ChunkSectionLayer.SOLID, null, -1, true, 0, true);
                final var original = new BakedQuad(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), new Vector3f(1, 1, 0), new Vector3f(1, 0, 0),
                    UVPair.pack(mask.getU(0), mask.getV(0)), UVPair.pack(mask.getU(0), mask.getV(1)),
                    UVPair.pack(mask.getU(1), mask.getV(1)), UVPair.pack(mask.getU(1), mask.getV(0)), direction, material);
                final QuadCollection source = new QuadCollection.Builder().addUnculledFace(original).build();
                for (boolean emissive : new boolean[] {false, true})
                {
                    final var quads = ItemLayerGeometry.remap(source, fluid, emissive, sprite -> null).getAll();
                    check(quads.size() == 1);
                    final var quad = quads.getFirst();
                    check(quad.materialInfo().sprite() == fluid && quad.materialInfo().tintIndex() == 0);
                    check(quad.materialInfo().layer() == ChunkSectionLayer.TRANSLUCENT);
                    check(quad.materialInfo().lightEmission() == (emissive ? 15 : 0));
                    check(quad.materialInfo().shade() != emissive && quad.materialInfo().ambientOcclusion() != emissive);
                    check(quad.direction() == direction && quad.bakedColors() == original.bakedColors() && quad.bakedNormals() == original.bakedNormals());
                    for (int index = 0; index < 4; index++)
                    {
                        check(quad.position(index).equals(original.position(index)));
                        final float expectedU = index >= 2 ? fluid.getU(1) : fluid.getU(0);
                        final float expectedV = index == 1 || index == 2 ? fluid.getV(1) : fluid.getV(0);
                        check(Math.abs(UVPair.unpackU(quad.packedUV(index)) - expectedU) < 0.00001f);
                        check(Math.abs(UVPair.unpackV(quad.packedUV(index)) - expectedV) < 0.00001f);
                    }
                    check(source.getAll().getFirst() == original && original.materialInfo().sprite() == mask);
                }
                final var translucent = ItemLayerGeometry.remap(source, null, false, sprite -> null).getAll().getFirst();
                check(translucent.materialInfo().sprite() == mask && translucent.materialInfo().tintIndex() == -1);
                for (int index = 0; index < 4; index++) check(translucent.packedUV(index) == original.packedUV(index));
            }
        }
        finally { mask.contents().close(); fluid.contents().close(); }
        final var matrix = ContainedFluidModel.FLUID_TRANSFORM.getMatrix();
        check(matrix.getTranslation(new Vector3f()).equals(new Vector3f(0.001f, 0.001f, 0), 0.000001f));
        check(matrix.getScale(new Vector3f()).equals(new Vector3f(1, 1, 1.002f), 0.000001f));
        check(ContainedFluidModel.COVER_TRANSFORM.getMatrix().getScale(new Vector3f()).equals(new Vector3f(1, 1, 1.004f), 0.000001f));
    }

    private static void codecs() throws Exception
    {
        int fluids = 0, trims = 0;
        try (var paths = Files.walk(Path.of("src/main/resources/assets")))
        {
            for (Path file : paths.filter(p -> p.toString().contains("/models/item/") && p.toString().endsWith(".json")).toList())
            {
                final var json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                final String loader = json.has("loader") ? json.get("loader").getAsString() : "";
                final List<Identifier> dependencies = new ArrayList<>();
                if (loader.equals("tfc:fluid_container"))
                {
                    final var model = ContainedFluidModel.Unbaked.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
                    check(model.textures().base().orElseThrow().sprite().toString().equals(json.getAsJsonObject("textures").get("base").getAsString()));
                    check(model.textures().fluid().orElseThrow().sprite().toString().equals(json.getAsJsonObject("textures").get("fluid").getAsString()));
                    check(model.type() == ContainedFluidModel.Unbaked.CODEC);
                    model.resolveDependencies(dependencies::add); fluids++;
                }
                else if (loader.equals("tfc:trim"))
                {
                    final var model = TrimmedItemModel.Unbaked.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
                    check(model.textures().armor().sprite().toString().equals(json.getAsJsonObject("textures").get("armor").getAsString()));
                    check(model.textures().trim().sprite().toString().equals(json.getAsJsonObject("textures").get("trim").getAsString()));
                    check(model.textures().overlay().isPresent() == json.getAsJsonObject("textures").has("overlay"));
                    check(model.tints().isEmpty());
                    model.resolveDependencies(dependencies::add); trims++;
                }
                else continue;
                check(dependencies.equals(List.of(ContainedFluidModel.DEFAULT_PARENT)));
            }
        }
        check(fluids == 24 && trims == 44);
        check(TrimmedItemModel.materialSuffix(null).isEmpty());
        for (String suffix : List.of("copper", "iron", "tfc_bismuth"))
        {
            final var assets = net.minecraft.world.item.equipment.trim.MaterialAssetGroup.create(suffix);
            final var material = new net.minecraft.world.item.equipment.trim.TrimMaterial(assets, net.minecraft.network.chat.Component.literal(suffix));
            final var pattern = new net.minecraft.world.item.equipment.trim.TrimPattern(Identifier.fromNamespaceAndPath("tfc", "fixture"), net.minecraft.network.chat.Component.literal("fixture"), false);
            final var trim = new net.minecraft.world.item.equipment.trim.ArmorTrim(net.minecraft.core.Holder.direct(material), net.minecraft.core.Holder.direct(pattern));
            check(TrimmedItemModel.materialSuffix(trim).equals(suffix));
        }
        check(ContainedFluidModel.Unbaked.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString("{\"textures\":{}} ")).error().isPresent());
        check(TrimmedItemModel.Unbaked.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString("{\"textures\":{\"armor\":\"tfc:a\"}} ")).error().isPresent());
    }

    public static void main(String[] args) throws Exception
    {
        codecs(); geometry();
        System.out.println("PASS " + checks + " native item-layer codec/geometry/UV/material checks; 24 fluid and 44 trim definitions. CPU only; no atlas reload, live item resolution or gameplay.");
    }
}
