package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record LensRequestPayload(int entityId) implements CustomPacketPayload {
    public static final Type<LensRequestPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:lens_req"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LensRequestPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeInt(payload.entityId()),
            buf -> new LensRequestPayload(buf.readInt())
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
}