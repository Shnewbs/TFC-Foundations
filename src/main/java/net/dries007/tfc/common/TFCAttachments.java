/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common;

import java.util.function.Supplier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.mixin.accessor.ChunkAccessAccessor;
import net.dries007.tfc.util.AttachmentValueIO;
import net.dries007.tfc.util.registry.RegistryHolder;
import net.dries007.tfc.util.tracker.WorldTracker;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataGenerator;

public final class TFCAttachments
{
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TerraFirmaCraft.MOD_ID);

    public static final Id<ChunkData> CHUNK_DATA = register("chunk_data", () -> AttachmentType.builder(holder -> {
            final ChunkAccess chunk = (ChunkAccess) holder;
            final ChunkDataGenerator generator = ((ChunkAccessAccessor) chunk).accessor$getLevelHeightAccessor() instanceof ServerLevel level
                && level.getChunkSource().getGenerator() instanceof ChunkGeneratorExtension ex
                ? ex.chunkDataGenerator()
                : null;
            return new ChunkData(generator, chunk.getPos());
        })
        .serialize(new IAttachmentSerializer<ChunkData>() {
            @Override
            public ChunkData read(IAttachmentHolder holder, ValueInput input)
            {
                final ChunkData data = holder.getData(CHUNK_DATA);
                data.deserializeNBT(AttachmentValueIO.read(input));
                return data;
            }

            @Override
            public boolean write(ChunkData data, ValueOutput output)
            {
                return AttachmentValueIO.write(output, data.serializeNBT());
            }
        })
        .build());

    public static final Id<WorldTracker> WORLD_TRACKER = register("world", () -> AttachmentType.builder(
        holder -> new WorldTracker((Level) holder))
        .serialize(new IAttachmentSerializer<WorldTracker>() {

            @Override
            public WorldTracker read(IAttachmentHolder holder, ValueInput input)
            {
                final WorldTracker tracker = holder.getData(WORLD_TRACKER);
                tracker.deserializeNBT(AttachmentValueIO.read(input));
                return tracker;
            }

            @Override
            public boolean write(WorldTracker tracker, ValueOutput output)
            {
                return AttachmentValueIO.write(output, tracker.serializeNBT());
            }
        })
        .build());

    private static <T> Id<T> register(String name, Supplier<AttachmentType<T>> type)
    {
        return new Id<>(TYPES.register(name, type));
    }

    public record Id<T>(DeferredHolder<AttachmentType<?>, AttachmentType<T>> holder)
        implements RegistryHolder<AttachmentType<?>, AttachmentType<T>> {}
}
