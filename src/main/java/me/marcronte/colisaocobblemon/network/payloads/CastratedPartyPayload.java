package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record CastratedPartyPayload(List<UUID> castrated) implements CustomPacketPayload {

    public static final Type<CastratedPartyPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:castrated_party"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CastratedPartyPayload> CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list()), CastratedPartyPayload::castrated,
            CastratedPartyPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}