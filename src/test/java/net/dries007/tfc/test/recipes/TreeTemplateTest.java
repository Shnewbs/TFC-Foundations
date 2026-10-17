/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.recipes;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TreeTemplateTest
{
    @Test
    public void explicitDisconnectedBranchPreservesPreviouslyIgnoredNaturalFlag()
    {
        for (String flag : new String[] {"true", "false"})
        {
            final var legacy = new CompoundTag();
            legacy.putString("Name", "tfc:wood/log/chestnut");
            final var properties = new CompoundTag();
            properties.putString("axis", "y");
            properties.putString("natural", flag);
            legacy.put("Properties", properties);
            final var normalized = legacy.copy();
            final var normalizedProperties = normalized.getCompoundOrEmpty("Properties");
            normalizedProperties.remove("natural");
            normalizedProperties.putString("branch_direction", "none");
            assertEquals(NbtUtils.readBlockState(BuiltInRegistries.BLOCK, legacy), NbtUtils.readBlockState(BuiltInRegistries.BLOCK, normalized));
        }
    }

    @Test
    public void everyShippedTreeLoadsWithNativePaletteProperties() throws Exception
    {
        final Path directory = Path.of(System.getProperty("tfc.test.projectDir"), "src/main/resources/data/tfc/structure");
        try (var paths = Files.walk(directory))
        {
            final var trees = paths.filter(path -> path.toString().endsWith(".nbt")).toList();
            assertEquals(539, trees.size());
            for (Path path : trees)
            {
                final CompoundTag tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
                assertEquals(SharedConstants.WORLD_VERSION, tag.getIntOr("DataVersion", 0), path.toString());
                assertTrue(tag.getListOrEmpty("entities").isEmpty(), path.toString());
                for (CompoundTag palette : tag.getListOrEmpty("palette").compoundStream().toList())
                {
                    final var id = Identifier.parse(palette.getStringOr("Name", ""));
                    final var block = BuiltInRegistries.BLOCK.getOptional(id).orElseThrow(() -> new AssertionError(path + ": missing block " + id));
                    final var properties = palette.getCompoundOrEmpty("Properties");
                    for (String key : properties.keySet())
                    {
                        final var property = block.getStateDefinition().getProperty(key);
                        assertNotNull(property, path + ": missing property " + id + "[" + key + "]");
                        assertTrue(property.getValue(properties.getStringOr(key, "")).isPresent(), path + ": invalid value for " + id + "[" + key + "]");
                    }
                }
                final var template = new StructureTemplate();
                template.load(BuiltInRegistries.BLOCK, tag);
                final CompoundTag saved = template.save(new CompoundTag());
                assertEquals(tag.getListOrEmpty("size"), saved.getListOrEmpty("size"), path.toString());
                assertEquals(tag.getListOrEmpty("blocks").size(), saved.getListOrEmpty("blocks").size(), path.toString());
            }
        }
    }
}
