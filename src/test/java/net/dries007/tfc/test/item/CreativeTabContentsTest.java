/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.TFCCreativeTabs;
import net.dries007.tfc.test.TestSetup;

import static org.junit.jupiter.api.Assertions.*;

public class CreativeTabContentsTest implements TestSetup
{
    @Test
    public void allTenTabsBuildThroughTheNativeDuplicateCheckingOutput()
    {
        final var parameters = new CreativeModeTab.ItemDisplayParameters(FeatureFlags.VANILLA_SET, true, REGISTRIES.get());
        for (var holder : TFCCreativeTabs.CREATIVE_TABS.getEntries())
        {
            assertDoesNotThrow(() -> holder.get().buildContents(parameters), holder.getId().toString());
            assertFalse(holder.get().getDisplayItems().isEmpty());
        }
    }

    @Test
    public void removingExactDuplicatesRetainsComponentVariantsAndBothVisibilities()
    {
        final var variant = new ItemStack(Items.DIAMOND);
        variant.set(DataComponents.CUSTOM_MODEL_DATA, new net.minecraft.world.item.component.CustomModelData(List.of(1f), List.of(), List.of(), List.of()));
        final List<ItemStack> emitted = new ArrayList<>();
        final List<CreativeModeTab.TabVisibility> visibility = new ArrayList<>();
        TFCCreativeTabs.uniqueDisplayItems((parameters, output) -> {
            output.accept(new ItemStack(Items.DIAMOND), CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
            output.accept(new ItemStack(Items.DIAMOND), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            output.accept(new ItemStack(Items.DIAMOND), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            output.accept(variant);
        }).accept(null, (stack, v) -> { emitted.add(stack); visibility.add(v); });
        assertEquals(3, emitted.size());
        assertEquals(List.of(CreativeModeTab.TabVisibility.PARENT_TAB_ONLY, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS), visibility);
        assertEquals(variant.getComponents(), emitted.getLast().getComponents());
    }
}
