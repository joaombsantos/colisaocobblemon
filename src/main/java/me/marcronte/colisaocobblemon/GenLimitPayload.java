package me.marcronte.colisaocobblemon;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record GenLimitPayload(int limit) implements CustomPacketPayload {

    public static final Type<GenLimitPayload> ID = new Type<>(ColisaoCobblemon.GEN_LIMIT_PACKET_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, GenLimitPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, GenLimitPayload::limit,
                    GenLimitPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}