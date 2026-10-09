/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.Map;
import java.util.HashMap;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3f;

/** Bakes snapshot geometry with the materials belonging to this resource reload, not a global atlas cache. */
public final class StaticBlockMeshBaker
{
    private final MaterialBaker materials;
    private final ModelDebugName debug;
    private final ModelState state;
    private final boolean ambientOcclusion;

    public StaticBlockMeshBaker(MaterialBaker materials, ModelDebugName debug, ModelState state, boolean ambientOcclusion)
    {
        this.materials = materials;
        final String name = debug.debugName();
        this.debug = () -> name;
        this.state = state;
        this.ambientOcclusion = ambientOcclusion;
    }

    private Material.Baked material(Identifier id, Map<Identifier, Material.Baked> resolved)
    {
        return resolved.computeIfAbsent(id, key -> {
            // Missing-texture reporters in a MaterialBaker need not be thread-safe.
            synchronized (materials) { return materials.get(new Material(key), debug); }
        });
    }

    public BlockStateModelPart bake(StaticBlockMesh.Mesh mesh)
    {
        final QuadCollection.Builder quads = new QuadCollection.Builder();
        final Map<Identifier, Material.Baked> resolved = new HashMap<>();
        for (StaticBlockMesh.Face original : mesh.faces())
        {
            final StaticBlockMesh.Face face = StaticBlockMesh.transform(original, state);
            final Material.Baked material = material(face.texture(), resolved);
            final var sprite = material.sprite();
            final Vector3f[] positions = new Vector3f[4];
            final long[] uv = new long[4];
            float minU = Float.POSITIVE_INFINITY, minV = Float.POSITIVE_INFINITY;
            float maxU = Float.NEGATIVE_INFINITY, maxV = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < 4; i++)
            {
                final StaticBlockMesh.Vertex v = face.vertices().get(i);
                positions[i] = new Vector3f(v.x(), v.y(), v.z());
                uv[i] = UVPair.pack(sprite.getU(v.u()), sprite.getV(v.v()));
                minU = Math.min(minU, v.u()); minV = Math.min(minV, v.v());
                maxU = Math.max(maxU, v.u()); maxV = Math.max(maxV, v.v());
            }
            final var transparency = sprite.contents().computeTransparency(minU, minV, maxU, maxV);
            final BakedQuad.MaterialInfo info = BakedQuad.MaterialInfo.of(material, transparency, -1, true, 0, ambientOcclusion);
            // Chunk lighting is supplied by the native renderer; do not bake stale light or double-apply directional shade.
            quads.addUnculledFace(new BakedQuad(positions[0], positions[1], positions[2], positions[3], uv[0], uv[1], uv[2], uv[3],
                face.direction(), info, BakedNormals.UNSPECIFIED, BakedColors.of(face.color())));
        }
        return new SimpleModelWrapper(quads.build(), ambientOcclusion, material(mesh.particle(), resolved));
    }
}
