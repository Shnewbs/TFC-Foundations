/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes;

import java.util.Optional;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.VisibleForTesting;

import com.mojang.serialization.DataResult;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;

/**
 * An advanced recipe that takes advantage of two additional mechanisms to provide a rich set of behaviors.
 * <ul>
 *     <li>It uses an {@link ItemStackProvider} as an output, identifying the input via the {@code primary_ingredient}</li>
 *     <li>It uses an {@link ItemStackProvider} to compute the remaining items, allowing damaging inputs or other functions</li>
 * </ul>
 */
public class AdvancedShapelessRecipe extends NormalCraftingRecipe
{
    public static final MapCodec<AdvancedShapelessRecipe> CODEC = RecordCodecBuilder.<AdvancedShapelessRecipe>mapCodec(i -> i.group(
        // This part of the codec is identical to the shapeless recipe codec
        net.dries007.tfc.common.recipes.LegacyIngredientCodecs.ITEM
            .listOf()
            .fieldOf("ingredients")
            .flatXmap(list -> {
                final Ingredient[] values = list.toArray(Ingredient[]::new); // Neo skip the empty check and immediately create the array.
                final int length = ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth();
                return values.length == 0
                    ? DataResult.error(() -> "No ingredients for shapeless recipe")
                    : values.length > length
                        ? DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(length))
                        : DataResult.success(NonNullList.copyOf(list));
            }, DataResult::success)
            .forGetter(AdvancedShapelessRecipe::getIngredients),
        ItemStackProvider.CODEC.fieldOf("result").forGetter(c -> c.result),
        ItemStackProvider.CODEC.optionalFieldOf("remainder").forGetter(c -> c.remainder),
        net.dries007.tfc.common.recipes.LegacyIngredientCodecs.ITEM.optionalFieldOf("primary_ingredient").forGetter(c -> c.primaryIngredient)
    ).apply(i, AdvancedShapelessRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(NonNullList::copyOf, Function.identity()), AdvancedShapelessRecipe::getIngredients,
        ItemStackProvider.STREAM_CODEC, c -> c.result,
        ByteBufCodecs.optional(ItemStackProvider.STREAM_CODEC), c -> c.remainder,
        ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), c -> c.primaryIngredient,
        AdvancedShapelessRecipe::new
    );

    private final NonNullList<Ingredient> originalIngredients;
    private final boolean isSimple;
    private final ItemStackProvider result;
    private final Optional<ItemStackProvider> remainder;
    private final Optional<Ingredient> primaryIngredient;

    public AdvancedShapelessRecipe(NonNullList<Ingredient> ingredients, ItemStackProvider result, Optional<ItemStackProvider> remainder, Optional<Ingredient> primaryIngredient)
    {
        super(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""));

        this.originalIngredients = NonNullList.copyOf(ingredients);
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);

        this.result = result;
        this.remainder = remainder;
        this.primaryIngredient = primaryIngredient;
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        RecipeHelpers.setCraftingInput(input);
        try
        {
            return result.getSingleStack(getPrimaryInput(input).copy());
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
                    return RecipeHelpers.getRemainderItemsWithProvider(input, remainder, getPrimaryInput(input));
                }
                finally
                {
                    RecipeHelpers.clearCraftingInput();
                }
            })
            .orElseGet(() -> super.getRemainingItems(input));
    }

    // The native ShapelessRecipe no longer exposes the original ingredient list.
    // Preserve it for TFC transfer UIs and our custom codec.
    public NonNullList<Ingredient> getIngredients()
    {
        return NonNullList.copyOf(originalIngredients);
    }

    private ItemStack getPrimaryInput(CraftingInput input)
    {
        final Ingredient primaryInput = primaryIngredient.orElseThrow();
        for (int i = 0; i < input.size(); i++)
        {
            final ItemStack item = input.getItem(i);
            if (primaryInput.test(item))
            {
                return item;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected PlacementInfo createPlacementInfo()
    {
        return PlacementInfo.create(originalIngredients);
    }

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        if (input.ingredientCount() != originalIngredients.size()) return false;
        if (!isSimple)
        {
            final var nonEmptyItems = new java.util.ArrayList<ItemStack>(input.ingredientCount());
            for (ItemStack item : input.items()) if (!item.isEmpty()) nonEmptyItems.add(item);
            return net.neoforged.neoforge.common.util.RecipeMatcher.findMatches(nonEmptyItems, originalIngredients) != null;
        }
        return input.size() == 1 && originalIngredients.size() == 1
            ? originalIngredients.getFirst().test(input.getItem(0))
            : input.stackedContents().canCraft(this, null);
    }

    @Override
    public List<RecipeDisplay> display()
    {
        return List.of(new ShapelessCraftingRecipeDisplay(originalIngredients.stream().map(Ingredient::display).toList(),
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
    public RecipeSerializer<AdvancedShapelessRecipe> getSerializer()
    {
        return TFCRecipeSerializers.ADVANCED_SHAPELESS_CRAFTING.get();
    }

    public ItemStackProvider getResult()
    {
        return result;
    }

    @VisibleForTesting
    public Optional<ItemStackProvider> getRemainder()
    {
        return remainder;
    }

    @VisibleForTesting
    public Optional<Ingredient> getPrimaryIngredient()
    {
        return primaryIngredient;
    }
}
