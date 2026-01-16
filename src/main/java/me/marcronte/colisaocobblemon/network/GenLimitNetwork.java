package me.marcronte.colisaocobblemon.network;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.client.ClientGenLimit;
import me.marcronte.colisaocobblemon.config.GenerationConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class GenLimitNetwork {

    public static final ResourceLocation GEN_LIMIT_ID = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "gen_limit_sync");

    public record GenLimitPayload(int limit) implements CustomPacketPayload {
        public static final Type<GenLimitPayload> TYPE = new Type<>(GEN_LIMIT_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, GenLimitPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, GenLimitPayload::limit,
                GenLimitPayload::new
        );
        @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerCommon() {
        PayloadTypeRegistry.playS2C().register(GenLimitPayload.TYPE, GenLimitPayload.CODEC);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(GenLimitPayload.TYPE, (payload, context) -> context.client().execute(() -> ClientGenLimit.setMaxGeneration(payload.limit())));
    }

    public static void sendToPlayer(ServerPlayer player) {
        int currentLimit = GenerationConfig.get().max_generation;
        ServerPlayNetworking.send(player, new GenLimitPayload(currentLimit));
    }

    public static void sendToAll(net.minecraft.server.MinecraftServer server) {
        int currentLimit = GenerationConfig.get().max_generation;
        GenLimitPayload payload = new GenLimitPayload(currentLimit);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}