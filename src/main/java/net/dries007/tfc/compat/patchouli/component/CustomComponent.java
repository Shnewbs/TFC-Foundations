/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.patchouli.component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import com.mojang.logging.LogUtils;
import net.minecraft.IdentifierException;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.slf4j.Logger;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.client.book.gui.GuiBookEntry;
import vazkii.patchouli.client.book.page.PageMultiblock;
import vazkii.patchouli.common.multiblock.SerializedMultiblock;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.tooltip.Tooltips;

public abstract class CustomComponent implements ICustomComponent
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Field MULTIBLOCK_OBJ = Helpers.uncheck(() -> {
        final Field field = PageMultiblock.class.getDeclaredField("multiblockObj");
        field.setAccessible(true);
        return field;
    });

    protected transient int x, y;
    protected transient HolderLookup.Provider provider;

    /**
     * First pass, allows component/template variables to be looked up as page variables.
     */
    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup, HolderLookup.Provider provider)
    {
        this.provider = provider;
    }

    /**
     * Second pass, builds the actual page and all bits and pieces.
     */
    @Override
    public void build(int componentX, int componentY, int pageNum)
    {
        this.x = componentX;
        this.y = componentY;
    }

    @Override
    public abstract void extractRenderState(GuiGraphicsExtractor graphics, IComponentRenderContext context, float partialTicks, int mouseX, int mouseY);

    protected void renderSetup(GuiGraphicsExtractor graphics)
    {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
    }

    /**
     * Same code flow as {@link IComponentRenderContext#renderIngredient(GuiGraphicsExtractor, int, int, int, int, Ingredient)} but with a {@code List<ItemStack>} instead.
     */
    protected void renderItemStacks(IComponentRenderContext context, GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, List<ItemStack> stacks)
    {
        if (!stacks.isEmpty())
        {
            context.renderItemStack(graphics, x, y, mouseX, mouseY, stacks.get((context.getTicksInBook() / 20) % stacks.size()));
        }
    }

    protected void renderFluidStacks(IComponentRenderContext context, GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, List<FluidStack> fluids)
    {
        if (!fluids.isEmpty())
        {
            renderFluidStack(context, graphics, x, y, mouseX, mouseY, fluids.get((context.getTicksInBook() / 20) % fluids.size()));
        }
    }

    protected void renderFluidStack(IComponentRenderContext context, GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, FluidStack fluid)
    {
        if (!fluid.isEmpty())
        {
            final TextureAtlasSprite sprite = RenderHelpers.getFluidSprite(fluid);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16,
                RenderHelpers.getFluidColor(fluid));

            if (context.isAreaHovered(mouseX, mouseY, x, y, 16, 16))
            {
                final List<Component> tooltip = new ArrayList<>(1);
                tooltip.add(Tooltips.fluidUnitsOf(fluid));
                context.setHoverTooltipComponents(tooltip);
            }
        }
    }

    protected List<ItemStack> unpackItemStackIngredient(SizedIngredient ingredient)
    {
        return ingredient.ingredient().items().map(item -> new ItemStack(item, ingredient.count())).toList();
    }

    protected List<FluidStack> unpackFluidStackIngredient(SizedFluidIngredient ingredient)
    {
        return ingredient.ingredient().fluids().stream().map(fluid -> new FluidStack(fluid, ingredient.amount())).toList();
    }

    protected <V, T> Optional<T> asJson(V value, Function<V, T> parse)
    {
        try
        {
            return Optional.of(parse.apply(value));
        }
        catch (JsonSyntaxException e)
        {
            LOGGER.error("Parsing {}: {}", value, e);
            return Optional.empty();
        }
    }

    protected Optional<Component> asTextComponent(JsonElement json)
    {
        final Component text;
        try
        {
            text = ComponentSerialization.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow(JsonSyntaxException::new);
        }
        catch (JsonSyntaxException e)
        {
            LOGGER.error("Cannot load text component from json {}: {}", json, e);
            return Optional.empty();
        }
        if (text == null)
        {
            LOGGER.error("Cannot load text component from json {}", json);
            return Optional.empty();
        }
        return Optional.of(text);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Recipe<?>> Optional<T> asRecipe(String variable, RecipeType<T> type)
    {
        return asResourceLocation(variable).flatMap(id -> {
            final RecipeMap recipes = ClientHelpers.tryGetSafeRecipeMap();
            final RecipeHolder<?> holder = recipes == null ? null : recipes.byKey(ResourceKey.create(Registries.RECIPE, id));
            if (holder == null)
            {
                LOGGER.error("No synchronized recipe of type {} named {}", BuiltInRegistries.RECIPE_TYPE.getKey(type), id);
                return Optional.empty();
            }
            if (holder.value().getType() != type)
            {
                LOGGER.error("The recipe {} of type {} is not of type {}", id, BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType()), type);
                return Optional.empty();
            }
            return Optional.of((T) holder.value());
        });
    }

    protected Optional<Identifier> asResourceLocation(String variable)
    {
        try
        {
            return Optional.of(Helpers.resourceLocation(variable));
        }
        catch (IdentifierException e)
        {
            LOGGER.error(e.getMessage());
            return Optional.empty();
        }
    }

    protected Optional<Supplier<IMultiblock>> asMultiblock(IVariable variable)
    {
        try
        {
            if (variable.unwrap().isJsonPrimitive())
            {
                return asResourceLocation(GsonHelper.convertToString(variable.unwrap(), "multiblock id"))
                    .flatMap(id -> {
                        final IMultiblock multiblock = PatchouliAPI.get().getMultiblock(id);
                        if (multiblock != null)
                        {
                            return Optional.of(() -> multiblock);
                        }
                        LOGGER.error("No multiblock by id: {}", id);
                        return Optional.empty();
                    });
            }
            final SerializedMultiblock serialized = GSON.fromJson(variable.unwrap(), SerializedMultiblock.class);
            return Optional.of(serialized::toMultiblock);
        }
        catch (JsonSyntaxException e)
        {
            LOGGER.error("Deserializing multiblock: ", e);
            return Optional.empty();
        }
    }

    protected Optional<MultiblockRenderer> asMultiblockRenderer(Supplier<IMultiblock> resolved)
    {
        try
        {
            final IMultiblock multiblock = resolved.get();
            final MultiblockRenderer stub = new MultiblockRenderer();
            MULTIBLOCK_OBJ.set(stub, /* (AbstractMultiblock) - not an API class */ multiblock);
            return Optional.of(stub);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            LOGGER.error("Building multiblock: ", e);
            return Optional.empty();
        }
    }

    public static class MultiblockRenderer extends PageMultiblock
    {
        public void render(IComponentRenderContext context, GuiGraphicsExtractor poseStack, int mouseX, int mouseY, float partialTicks)
        {
            if (context instanceof GuiBookEntry entryContext)
            {
                mc = Minecraft.getInstance();
                parent = entryContext;
                book = entryContext.book;
                extractRenderState(poseStack, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean shouldRenderText()
        {
            return false;
        }
    }
}
