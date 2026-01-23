package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record SaveRoutePayload(String routeName) implements CustomPacketPayload {

    public static final Type<SaveRoutePayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("colisao_cobblemon", "save_route"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SaveRoutePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SaveRoutePayload::routeName,
            SaveRoutePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}