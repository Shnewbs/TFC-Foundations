/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes;

import java.util.Optional;
import java.util.List;

import org.jetbrains.annotations.VisibleForTesting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;

/**
 * A shaped recipe type which uses {@link ItemStackProvider} as it's output mechanism
 * It also requires that the recipe specify which (of the crafting grid) inputs is responsible for the item stack provider's "input" stack.
 */
public class AdvancedShapedRecipe extends NormalCraftingRecipe
{
    public static final MapCodec<AdvancedShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        // Copied from ShapedRecipe.Serializer.CODEC, as we want to avoid the "result" field as a strict item stack
        LegacyIngredientCodecs.SHAPED_PATTERN.forGetter(c -> c.pattern),
        Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(AdvancedShapedRecipe::showNotification),
        ItemStackProvider.CODEC.fieldOf("result").forGetter(c -> c.result),
        ItemStackProvider.CODEC.optionalFieldOf("remainder").forGetter(c -> c.remainder),
        Codec.INT.optionalFieldOf("input_row", 0).forGetter(c -> c.inputRow),
        Codec.INT.optionalFieldOf("input_column", 0).forGetter(c -> c.inputColumn)
    ).apply(i, AdvancedShapedRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedShapedRecipe> STREAM_CODEC = StreamCodec.composite(
        ShapedRecipePattern.STREAM_CODEC, c -> c.pattern,
        ByteBufCodecs.BOOL, AdvancedShapedRecipe::showNotification,
        ItemStackProvider.STREAM_CODEC, c -> c.result,
        ByteBufCodecs.optional(ItemStackProvider.STREAM_CODEC), c -> c.remainder,
        ByteBufCodecs.VAR_INT, c -> c.inputRow,
        ByteBufCodecs.VAR_INT, c -> c.inputColumn,
        AdvancedShapedRecipe::new
    );

    public final ShapedRecipePattern pattern;
    private final ItemStackProvider result;
    private final Optional<ItemStackProvider> remainder;
    private final int inputSlot, inputRow, inputColumn;

    public AdvancedShapedRecipe(ShapedRecipePattern pattern, boolean showNotification, ItemStackProvider result, Optional<ItemStackProvider> remainder, int inputRow, int inputColumn)
    {
        super(new Recipe.CommonInfo(showNotification), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""));
        this.pattern = pattern;

        this.result = result;
        this.remainder = remainder;
        this.inputSlot = RecipeHelpers.dissolveRowColumn(inputRow, inputColumn, pattern.width());
        this.inputRow = inputRow;
        this.inputColumn = inputColumn;
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        RecipeHelpers.setCraftingInput(input);
        try
        {
            final int matchSlot = RecipeHelpers.translateMatch(this, inputSlot, input);
            final ItemStack inputStack = matchSlot != -1 ? input.getItem(matchSlot).copy() : ItemStack.EMPTY;
            return result.getSingleStack(inputStack);
        }
        finally
        {
            RecipeHelpers.clearCraftingInput();
        }
    }

    public ItemStack getResultItem(HolderLookup.Provider registries)
    {
        return result.getEmptyStack();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input)
    {
        return remainder.map(remainder -> {
                RecipeHelpers.setCraftingInput(input);
                try
                {
                    final int matchSlot = RecipeHelpers.translateMatch(this, inputSlot, input);
                    final ItemStack inputStack = matchSlot != -1 ? input.getItem(matchSlot).copy() : ItemStack.EMPTY;
                    return RecipeHelpers.getRemainderItemsWithProvider(input, remainder, inputStack);
                }
                finally
                {
                    RecipeHelpers.clearCraftingInput();
                }
            })
            .orElseGet(() -> super.getRemainingItems(input));
    }

    public int getWidth() { return pattern.width(); }

    public int getHeight() { return pattern.height(); }

    public List<Optional<Ingredient>> getIngredients() { return pattern.ingredients(); }

    public ItemStackProvider getResult() { return result; }

    public int getInputSlot() { return inputSlot; }

    @Override
    protected PlacementInfo createPlacementInfo()
    {
        return PlacementInfo.createFromOptionals(pattern.ingredients());
    }

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        return pattern.matches(input);
    }

    @Override
    public List<RecipeDisplay> display()
    {
        return List.of(new ShapedCraftingRecipeDisplay(getWidth(), getHeight(),
            pattern.ingredients().stream().map(i -> i.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(),
            resultDisplay(), new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    private SlotDisplay resultDisplay()
    {
        // Preserve the provider's existing empty-input preview for guide pages. JEI also supplies input-aware alternatives.
        final ItemStack output = result.getEmptyStack();
        return output.isEmpty() ? SlotDisplay.Empty.INSTANCE
            : new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(output));
    }

    @Override
    public boolean isSpecial()
    {
        return result.dependsOnInput();
    }

    @Override
    public RecipeSerializer<AdvancedShapedRecipe> getSerializer()
    {
        return TFCRecipeSerializers.ADVANCED_SHAPED_CRAFTING.get();
    }

    @VisibleForTesting
    public Optional<ItemStackProvider> getRemainder()
    {
        return remainder;
    }
}
