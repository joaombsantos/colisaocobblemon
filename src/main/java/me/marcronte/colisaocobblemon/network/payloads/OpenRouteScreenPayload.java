package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record OpenRouteScreenPayload() implements CustomPacketPayload {

    public static final Type<OpenRouteScreenPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("colisao_cobblemon", "open_route_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRouteScreenPayload> CODEC = StreamCodec.unit(new OpenRouteScreenPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}