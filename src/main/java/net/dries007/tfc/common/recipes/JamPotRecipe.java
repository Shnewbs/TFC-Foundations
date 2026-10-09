/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.IPotInventory;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.outputs.PotOutput;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.tooltip.BlockEntityTooltip;
import net.dries007.tfc.util.tooltip.BlockEntityTooltips;

public class JamPotRecipe extends PotRecipe
{
    public static final MapCodec<JamPotRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        PotRecipe.CODEC.forGetter(c -> c),
        ItemStack.CODEC.fieldOf("unsealed_result").forGetter(c -> c.jarredStack),
        ItemStack.CODEC.fieldOf("sealed_result").forGetter(c -> c.jarredStackWithLid),
        Identifier.CODEC.fieldOf("texture").forGetter(c -> c.texture)
    ).apply(i, JamPotRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, JamPotRecipe> STREAM_CODEC = StreamCodec.composite(
        PotRecipe.STREAM_CODEC, c -> c,
        ItemStack.STREAM_CODEC, c -> c.jarredStack,
        ItemStack.STREAM_CODEC, c -> c.jarredStackWithLid,
        Identifier.STREAM_CODEC, c -> c.texture,
        JamPotRecipe::new
    );

    public static final PotOutput.OutputType OUTPUT_TYPE = nbt -> {
        ItemStack stack = nbt.read("unsealed_result", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        ItemStack stack2 = nbt.read("sealed_result", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        Identifier texture = Helpers.resourceLocation(nbt.getStringOr("texture", ""));
        return new JamPotRecipe.JamOutput(stack, stack2, texture);
    };

    private final ItemStack jarredStack;
    private final ItemStack jarredStackWithLid;
    private final Identifier texture;

    public JamPotRecipe(PotRecipe base, ItemStack jarredStack, ItemStack jarredStackWithLid, Identifier texture)
    {
        super(base);
        this.jarredStack = jarredStack;
        this.jarredStackWithLid = jarredStackWithLid;
        this.texture = texture;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries)
    {
        return jarredStackWithLid;
    }

    public Identifier getTexture()
    {
        return texture;
    }

    @Override
    public PotOutput getOutput(IPotInventory inventory)
    {
        inventory.clearFluid();
        return new JamOutput(jarredStack.copy(), jarredStackWithLid.copy(), texture);
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return TFCRecipeSerializers.POT_JAM.get();
    }

    public record JamOutput(ItemStack unsealedStack, ItemStack sealedStack, Identifier texture) implements PotOutput
    {
        @Override
        public boolean isEmpty()
        {
            return unsealedStack.isEmpty() || sealedStack.isEmpty();
        }

        @Override
        public InteractionResult onInteract(IPotInventory entity, Player player, ItemStack clickedWith)
        {
            if (Helpers.isItem(clickedWith, TFCItems.EMPTY_JAR) && !unsealedStack.isEmpty())
            {
                // take the player's empty jar
                clickedWith.shrink(1);
                sealedStack.shrink(1);
                ItemHandlerHelper.giveItemToPlayer(player, unsealedStack.split(1));
                return (player.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
            }
            if (Helpers.isItem(clickedWith, TFCTags.Items.EMPTY_JARS_WITH_LID) && !sealedStack.isEmpty())
            {
                // take the player's empty jar
                clickedWith.shrink(1);
                unsealedStack.shrink(1);
                ItemHandlerHelper.giveItemToPlayer(player, sealedStack.split(1));
                return (player.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
            }
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        @Override
        public Identifier getRenderTexture()
        {
            return texture;
        }

        @Override
        public float getFluidYLevel()
        {
            return Mth.clampedMap(unsealedStack.getCount(), 0, 4, 7f / 16, 10f / 16);
        }

        @Override
        public void write(ValueOutput nbt)
        {
            nbt.store("unsealed_result", ItemStack.OPTIONAL_CODEC, unsealedStack);
            nbt.store("sealed_result", ItemStack.OPTIONAL_CODEC, sealedStack);
            nbt.putString("texture", texture.toString());
        }

        @Override
        public OutputType getType()
        {
            return JamPotRecipe.OUTPUT_TYPE;
        }

        @Override
        public BlockEntityTooltip getTooltip()
        {
            return ((level, state, pos, entity, tooltip) -> {
                BlockEntityTooltips.itemWithCount(tooltip, sealedStack);
                FoodCapability.addTooltipInfo(sealedStack, tooltip);
            });
        }
    }
}
