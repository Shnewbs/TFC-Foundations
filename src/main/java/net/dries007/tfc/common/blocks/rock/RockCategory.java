/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks.rock;

import java.util.Locale;
import java.util.function.Function;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

import net.dries007.tfc.common.LevelTier;
import net.dries007.tfc.common.Lore;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.TFCTiers;
import net.dries007.tfc.common.items.HammerItem;
import net.dries007.tfc.common.items.JavelinItem;
import net.dries007.tfc.common.items.TFCHoeItem;
import net.dries007.tfc.common.items.ToolItem;

public enum RockCategory implements StringRepresentable
{
    IGNEOUS_EXTRUSIVE(TFCTiers.IGNEOUS_EXTRUSIVE, 0f),
    IGNEOUS_INTRUSIVE(TFCTiers.IGNEOUS_INTRUSIVE, 0.2f),
    METAMORPHIC(TFCTiers.METAMORPHIC, -0.2f),
    SEDIMENTARY(TFCTiers.SEDIMENTARY, -0.4f);

    private final String serializedName;
    private final LevelTier itemTier;
    private final float hardnessModifier;

    RockCategory(LevelTier itemTier, float hardnessModifier)
    {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.itemTier = itemTier;
        this.hardnessModifier = hardnessModifier;
    }

    public LevelTier tier()
    {
        return itemTier;
    }

    public float hardness(float base)
    {
        return base + hardnessModifier;
    }

    @Override
    public String getSerializedName()
    {
        return serializedName;
    }

    public enum ItemType
    {
        AXE(rock -> new AxeItem(rock.tier().material(), ToolItem.baseAttackDamage(rock.tier(), 1.5f), -3.2f, base(rock))),
        AXE_HEAD,
        HAMMER(rock -> new HammerItem(rock.tier(), base(rock))),
        HAMMER_HEAD,
        HOE(rock -> new TFCHoeItem(rock.tier(), -3.0f, base(rock))),
        HOE_HEAD,
        JAVELIN(rock -> new JavelinItem(rock.tier(), -2.2f, base(rock))),
        JAVELIN_HEAD,
        KNIFE(rock -> new ToolItem(rock.tier(), TFCTags.Blocks.MINEABLE_WITH_KNIFE, 0.6f, -2.0f, base(rock))),
        KNIFE_HEAD,
        SHOVEL(rock -> new ShovelItem(rock.tier().material(), ToolItem.baseAttackDamage(rock.tier(), 0.875f), -3.0f, base(rock))),
        SHOVEL_HEAD;

        private static Item.Properties base(RockCategory rock)
        {
            return new Item.Properties().component(Lore.TYPE, Lore.ROCK_CATEGORIES.get(rock));
        }

        private final Function<RockCategory, Item> itemFactory;

        ItemType()
        {
            this(rock -> new Item(base(rock)));
        }

        ItemType(Function<RockCategory, Item> itemFactory)
        {
            this.itemFactory = itemFactory;
        }

        public Item create(RockCategory category)
        {
            return itemFactory.apply(category);
        }
    }
}