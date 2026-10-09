/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.entities.livestock.pet;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.CatSoundVariant;
import net.minecraft.world.entity.animal.feline.CatSoundVariants;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.feline.CatVariants;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.EntityHelpers;
import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.NbtHelpers;

public class TFCCat extends TamableMammal
{
    public static final EntityDataAccessor<Holder<CatVariant>> DATA_VARIANT = SynchedEntityData.defineId(TFCCat.class, EntityDataSerializers.CAT_VARIANT);


    public TFCCat(EntityType<? extends TamableMammal> type, Level level)
    {
        super(type, level, TFCSounds.CAT, TFCConfig.SERVER.catConfig);
    }

    @Override
    public boolean willListenTo(Command command, boolean isClientSide)
    {
        if (!isClientSide && command == Command.SIT && getRandom().nextFloat() < 0.1f)
        {
            return false;
        }
        return super.willListenTo(command, isClientSide);
    }

    @Override
    public void createGenes(CompoundTag tag, TFCAnimalProperties male)
    {
        super.createGenes(tag, male);
        if (male instanceof TFCCat maleCat)
        {
            final Identifier variant = registryAccess().lookupOrThrow(Registries.CAT_VARIANT).getKey(random.nextBoolean() ? maleCat.getVariant() : getVariant());
            if (variant != null)
                tag.putString("variant", variant.toString());
        }
    }

    @Override
    public void applyGenes(CompoundTag tag, MammalProperties baby)
    {
        super.applyGenes(tag, baby);
        if (baby instanceof TFCCat cat)
        {
            final Identifier variant = Identifier.tryParse(EntityHelpers.getStringOrDefault(tag, "variant", CatVariants.BLACK.identifier().toString()));
            if (variant != null)
                registryAccess().lookupOrThrow(Registries.CAT_VARIANT).get(variant).ifPresent(cat::setVariant);
        }
    }

    @Override
    public void initCommonAnimalData(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason)
    {
        super.initCommonAnimalData(level, difficulty, reason);

        // Native selectors contain the target's full-moon and structure conditions.
        // Select from the level's data-driven registry, including datapack variants.
        VariantUtils.selectVariantToSpawn(SpawnContext.create(level, blockPosition()), Registries.CAT_VARIANT)
            .ifPresent(this::setVariant);

        final ServerLevel serverlevel = level.getLevel();
        if (serverlevel.structureManager().getStructureWithPieceAt(this.blockPosition(), StructureTags.CATS_SPAWN_AS_BLACK).isValid())
        {
            this.setVariant(registryAccess().lookupOrThrow(Registries.CAT_VARIANT).getOrThrow(CatVariants.ALL_BLACK));
            this.setPersistenceRequired();
        }
    }

    @Override
    public boolean canAttack(LivingEntity entity)
    {
        return super.canAttack(entity) && Helpers.isEntity(entity, TFCTags.Entities.HUNTED_BY_CATS);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, VariantUtils.getDefaultOrAny(registryAccess(), CatVariants.BLACK));
    }

    public CatVariant getVariant()
    {
        return this.entityData.get(DATA_VARIANT).value();
    }

    public void setVariant(Holder<CatVariant> type)
    {
        this.entityData.set(DATA_VARIANT, type);
    }

    public Identifier getTextureLocation()
    {
        // TFC scales its existing model for kittens; keep the matching adult UV layout.
        return getVariant().assetInfo(false).texturePath();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag)
    {
        super.addAdditionalSaveData(tag);
        Identifier key = registryAccess().lookupOrThrow(Registries.CAT_VARIANT).getKey(this.getVariant());
        if (key != null)
        {
            tag.putString("variant", key.toString());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag)
    {
        super.readAdditionalSaveData(tag);
        if (NbtHelpers.hasTag(tag, "variant", Tag.TAG_STRING))
        {
            Optional.ofNullable(Identifier.tryParse(tag.getStringOr("variant", "")))
                .flatMap(registryAccess().lookupOrThrow(Registries.CAT_VARIANT)::get)
                .ifPresent(this::setVariant);
        }
    }

    @Override
    public TagKey<Item> getFoodTag()
    {
        return TFCTags.Items.CAT_FOOD;
    }

    @Override
    public void receiveCommand(ServerPlayer player, Command command)
    {
        if (getOwner() != null && getOwner().equals(player))
        {
            final CatSoundVariant sounds = registryAccess().lookupOrThrow(Registries.CAT_SOUND_VARIANT)
                .getOrThrow(CatSoundVariants.CLASSIC).value();
            playSound((isBaby() ? sounds.babySounds() : sounds.adultSounds()).purreowSound().value(), getSoundVolume(), getVoicePitch());
        }
        super.receiveCommand(player, command);
    }
}
