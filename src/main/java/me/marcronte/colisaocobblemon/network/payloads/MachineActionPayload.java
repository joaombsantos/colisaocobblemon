package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record MachineActionPayload(int actionId, BlockPos pos, @Nullable UUID pokemonId) implements CustomPacketPayload {

    public static final Type<MachineActionPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:machine_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineActionPayload> CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeInt(value.actionId);
                buf.writeBlockPos(value.pos);

                buf.writeBoolean(value.pokemonId != null);
                if (value.pokemonId != null) buf.writeUUID(value.pokemonId);
            },
            buf -> {
                int action = buf.readInt();
                BlockPos pos = buf.readBlockPos();
                UUID uuid = buf.readBoolean() ? buf.readUUID() : null;
                return new MachineActionPayload(action, pos, uuid);
            }
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
}