/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common;

import java.util.EnumMap;
import net.minecraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.HolderHolder;

public final class TFCArmorMaterials
{
    public static final Id COPPER = register("copper", TFCSounds.COPPER_EQUIP, 160, 200, 215, 150, 1, 3, 4, 1, 9, 0f, 0f);
    public static final Id BISMUTH_BRONZE = register("bismuth_bronze", TFCSounds.BISMUTH_BRONZE_EQUIP,  250, 288, 311, 240, 1, 4, 4, 1, 9, 0f, 0f);
    public static final Id BLACK_BRONZE = register("black_bronze", TFCSounds.BLACK_BRONZE_EQUIP, 285, 340, 336, 262, 1, 4, 4, 1, 9, 0f, 0f);
    public static final Id BRONZE = register("bronze", TFCSounds.BRONZE_EQUIP, 270, 315, 323, 251, 1, 4, 4, 1, 9, 0f, 0f);
    public static final Id WROUGHT_IRON = register("wrought_iron", TFCSounds.WROUGHT_IRON_EQUIP, 429, 495, 528, 370, 1, 4, 5, 2, 12, 0f, 0f);
    public static final Id STEEL = register("steel", TFCSounds.STEEL_EQUIP, 520, 600, 640, 440, 2, 5, 6, 2, 12, 1f, 0f);
    public static final Id BLACK_STEEL = register("black_steel", TFCSounds.BLACK_STEEL_EQUIP, 650, 750, 800, 550, 2, 5, 6, 2, 17, 2f, 0.05f);
    public static final Id BLUE_STEEL = register("blue_steel", TFCSounds.BLUE_STEEL_EQUIP, 860, 960, 1088, 748, 3, 6, 8, 3, 23, 3f, 0.1f);
    public static final Id RED_STEEL = register("red_steel", TFCSounds.RED_STEEL_EQUIP, 884, 1020, 1010, 715, 3, 6, 8, 3, 23, 3f, 0.1f);

    private static Id register(
        String name,
        HolderHolder<SoundEvent> equipSound,
        int feetDamage, int legDamage, int chestDamage, int headDamage,
        int feetReduction, int legReduction, int chestReduction, int headReduction,
        int enchantability, float toughness, float knockbackResistance
    ) {
        final TagKey<Item> repairTag = TagKey.create(Registries.ITEM, Helpers.identifier("repairs/" + name + "_armor"));
        final ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Helpers.identifier(name));
        return new Id(Holder.direct(new ArmorMaterial(
            1, // Per-slot durability is preserved on the registered item properties below.
            Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, feetReduction);
                map.put(ArmorType.LEGGINGS, legReduction);
                map.put(ArmorType.CHESTPLATE, chestReduction);
                map.put(ArmorType.HELMET, headReduction);
                map.put(ArmorType.BODY, chestReduction);
            }),
            enchantability,
            equipSound.holder(),
            toughness,
            knockbackResistance,
            repairTag,
            asset
        )), feetDamage, legDamage, chestDamage, headDamage);
    }

    public record Id(
        Holder<ArmorMaterial> holder,
        int feetDamage,
        int legDamage,
        int chestDamage,
        int headDamage
    ) {}
}