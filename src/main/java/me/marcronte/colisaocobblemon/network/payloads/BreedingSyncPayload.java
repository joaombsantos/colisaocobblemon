package me.marcronte.colisaocobblemon.network.payloads;

import me.marcronte.colisaocobblemon.network.BreedingNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record BreedingSyncPayload(
        @Nullable UUID mother,
        @Nullable UUID father,
        @Nullable String motherSpecies,
        @Nullable String fatherSpecies,
        long startTime,
        long totalDuration,
        boolean active,
        boolean ready
) implements CustomPacketPayload {

    public static final Type<BreedingSyncPayload> ID = new Type<>(BreedingNetwork.SYNC_SCREEN);

    public static final StreamCodec<RegistryFriendlyByteBuf, BreedingSyncPayload> CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeBoolean(value.mother != null);
                if (value.mother != null) buf.writeUUID(value.mother);

                buf.writeBoolean(value.father != null);
                if (value.father != null) buf.writeUUID(value.father);

                buf.writeBoolean(value.motherSpecies != null);
                if (value.motherSpecies != null) buf.writeUtf(value.motherSpecies);

                buf.writeBoolean(value.fatherSpecies != null);
                if (value.fatherSpecies != null) buf.writeUtf(value.fatherSpecies);

                buf.writeLong(value.startTime);
                buf.writeLong(value.totalDuration);
                buf.writeBoolean(value.active);
                buf.writeBoolean(value.ready);
            },
            buf -> {
                boolean hasMother = buf.readBoolean();
                UUID mother = hasMother ? buf.readUUID() : null;

                boolean hasFather = buf.readBoolean();
                UUID father = hasFather ? buf.readUUID() : null;

                boolean hasMotherSpecies = buf.readBoolean();
                String motherSpecies = hasMotherSpecies ? buf.readUtf() : null;

                boolean hasFatherSpecies = buf.readBoolean();
                String fatherSpecies = hasFatherSpecies ? buf.readUtf() : null;

                long startTime = buf.readLong();
                long totalDuration = buf.readLong();
                boolean active = buf.readBoolean();
                boolean ready = buf.readBoolean();

                return new BreedingSyncPayload(mother, father, motherSpecies, fatherSpecies, startTime, totalDuration, active, ready);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}