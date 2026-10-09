/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import java.util.EnumMap;
import java.util.Map;
import com.mojang.math.Quadrant;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public class MoldsModelLoader implements UnbakedModelLoader<MoldsModelLoader.MoldModel>
{

    @Override
    public MoldModel read(JsonObject json, JsonDeserializationContext deserializationContext)
            throws JsonParseException
    {
        final JsonArray pattern = GsonHelper.getAsJsonArray(json, "pattern");

        final int height = pattern.size();
        if (height != 14)
        {
            throw new JsonSyntaxException("Invalid pattern: must have 14 rows (has " + height + ")");
        }

        boolean[][] full = new boolean[14][14];

        for (int r = 0; r < 14; ++r)
        {
            String row = GsonHelper.convertToString(pattern.get(r), "pattern[" + r + "]");
            final int width = row.length();
            if (width != 14)
                throw new JsonSyntaxException(
                        "Invalid pattern: must have 14 columns (has " + width + " in row " + r + ")");

            for (int c = 0; c < 14; c++)
            {
                full[r][c] = row.charAt(c) != ' ';
            }
        }

        return new MoldModel(StandardModelParameters.parse(json, deserializationContext), generateBlockElementsFromPattern(full));
    }

    public static List<CuboidModelElement> generateBlockElementsFromPattern(boolean[][] pattern)
    {
        if (pattern == null || pattern.length != 14)
        {
            throw new IllegalArgumentException("Mold pattern must have 14 rows");
        }
        for (boolean[] row : pattern)
        {
            if (row == null || row.length != 14) throw new IllegalArgumentException("Mold rows must have 14 columns");
        }
        ArrayList<CuboidModelElement> elements = new ArrayList<>();

        int from_y = 1;
        int to_y = 2;
        for (int r = 0; r < 14; ++r)
        {
            int from_x = r + 1;
            int to_x = r + 2;
            for (int c = 0; c < 14; c++) {
                if (!pattern[r][c])
                    continue;

                int from_z = c + 1;
                int to_z = c + 2;

                Vector3f from = new Vector3f(from_x, from_y, from_z);
                Vector3f to = new Vector3f(to_x, to_y, to_z);

                final Map<Direction, CuboidFace> faces = new EnumMap<>(Direction.class);
                for (Direction direction : Direction.values())
                {
                    final float[] uv = autoRelativeUV(direction, from, to);
                    faces.put(direction, new CuboidFace(null, -1, "#0", new CuboidFace.UVs(uv[0], uv[1], uv[2], uv[3]), Quadrant.R0));
                }
                elements.add(new CuboidModelElement(from, to, faces, null, true, 0));
            }
        }
        return List.copyOf(elements);
    }

    private static float[] autoRelativeUV(Direction direction, Vector3f from, Vector3f to)
    {
        switch (direction)
        {
            case NORTH:
                return new float[] { 16 - to.x, 16 - to.y, 16 - from.x, 16 - from.y };
            case SOUTH:
                return new float[] { from.x, 16 - to.y, to.x, 16 - from.y };
            case WEST:
                return new float[] { from.z, 16 - to.y, to.z, 16 - from.y };
            case EAST:
                return new float[] { 16 - to.z, 16 - to.y, 16 - from.z, 16 - from.y };
            case UP:
                return new float[] { from.x, from.z, to.x, to.z };
            case DOWN:
                return new float[] { from.x, 16 - to.z, to.x, 16 - from.z };
        }

        return new float[] {};
    }
    public static final class MoldModel extends AbstractUnbakedModel
    {
        private final UnbakedCuboidGeometry geometry;

        private MoldModel(StandardModelParameters parameters, List<CuboidModelElement> elements)
        {
            super(parameters);
            this.geometry = new UnbakedCuboidGeometry(elements);
        }

        @Override
        public UnbakedGeometry geometry()
        {
            return geometry;
        }
    }
}
