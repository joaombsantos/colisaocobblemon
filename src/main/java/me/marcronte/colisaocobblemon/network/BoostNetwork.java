package me.marcronte.colisaocobblemon.network;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.client.ColisaoCobblemonClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BoostNetwork {

    public static final ResourceLocation BOOST_STATE_ID = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "boost_state");

    public record BoostStatePayload(boolean boosting) implements CustomPacketPayload {
        public static final Type<BoostStatePayload> TYPE = new Type<>(BOOST_STATE_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, BoostStatePayload> CODEC = StreamCodec.composite(
                StreamCodec.of(RegistryFriendlyByteBuf::writeBoolean, RegistryFriendlyByteBuf::readBoolean),
                BoostStatePayload::boosting,
                BoostStatePayload::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // --- SERVER SIDE ---
    public static void registerCommon() {
        PayloadTypeRegistry.playS2C().register(BoostStatePayload.TYPE, BoostStatePayload.CODEC);
    }

    // --- CLIENT SIDE ---
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(BoostStatePayload.TYPE, (payload, context) -> context.client().execute(() -> ColisaoCobblemonClient.isPlayerBoosting = payload.boosting()));
    }
}