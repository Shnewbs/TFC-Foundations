/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

import net.dries007.tfc.util.Helpers;

public final class TFCTiers
{
    public static final LevelTier IGNEOUS_INTRUSIVE = create("igneous_intrusive", BlockTags.INCORRECT_FOR_WOODEN_TOOL, 0, 60, 4.7f, 2.0f, 5);
    public static final LevelTier IGNEOUS_EXTRUSIVE = create("igneous_extrusive", BlockTags.INCORRECT_FOR_WOODEN_TOOL, 0, 70, 4.7f, 2.0f, 5);
    public static final LevelTier SEDIMENTARY = create("sedimentary", BlockTags.INCORRECT_FOR_WOODEN_TOOL, 0, 50, 4.0f, 2.0f, 5);
    public static final LevelTier METAMORPHIC = create("metamorphic", BlockTags.INCORRECT_FOR_WOODEN_TOOL, 0, 55, 4.35f, 2.0f, 5);
    public static final LevelTier OBSIDIAN = create("obsidian", BlockTags.INCORRECT_FOR_WOODEN_TOOL, 0, 60, 5.0f, 2.5f, 8);
    public static final LevelTier COPPER = create("copper", BlockTags.INCORRECT_FOR_STONE_TOOL, 1, 600, 5.25f, 3.25f, 8);
    public static final LevelTier BRONZE = create("bronze", BlockTags.INCORRECT_FOR_IRON_TOOL, 2, 1300, 7.3f, 4.0f, 13);
    public static final LevelTier BISMUTH_BRONZE = create("bismuth_bronze", BlockTags.INCORRECT_FOR_IRON_TOOL, 2, 1200, 6.65f, 4.0f, 10);
    public static final LevelTier BLACK_BRONZE = create("black_bronze", BlockTags.INCORRECT_FOR_IRON_TOOL, 2, 1460, 6.0f, 4.25f, 10);
    public static final LevelTier WROUGHT_IRON = create("wrought_iron", BlockTags.INCORRECT_FOR_IRON_TOOL, 3, 2200, 8.0f, 4.75f, 12);
    public static final LevelTier STEEL = create("steel", BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 4, 3300, 9.5f, 5.75f, 12);
    public static final LevelTier BLACK_STEEL = create("black_steel", BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 5, 4200, 11.0f, 7.0f, 17);
    public static final LevelTier BLUE_STEEL = create("blue_steel", BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 6, 6500, 12.0f, 9.0f, 22);
    public static final LevelTier RED_STEEL = create("red_steel", BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 6, 6500, 12.0f, 9.0f, 22);

    private static LevelTier create(String name, TagKey<Block> incorrect, int level, int uses, float speed, float damage, int enchantmentValue)
    {
        final TagKey<Item> noRepairs = TagKey.create(Registries.ITEM, Helpers.identifier("repairs/" + name + "_tools"));
        return new LeveledTier(new ToolMaterial(incorrect, uses, speed, damage, enchantmentValue, noRepairs), level, name);
    }

    record LeveledTier(ToolMaterial material, int level, String name) implements LevelTier
    {
        @Override public String toString() { return name; }
    }
}
