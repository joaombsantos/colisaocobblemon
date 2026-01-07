package me.marcronte.colisaocobblemon.features.switchstate;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
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

public class SwitchNetwork {

    public static final ResourceLocation SYNC_STATE_ID = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "sync_switch_state");

    // Payload to send "A" ou "B" to client
    public record SyncStatePayload(String state) implements CustomPacketPayload {
        public static final Type<SyncStatePayload> TYPE = new Type<>(SYNC_STATE_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncStatePayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, SyncStatePayload::state,
                SyncStatePayload::new
        );
        @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerCommon() {
        PayloadTypeRegistry.playS2C().register(SyncStatePayload.TYPE, SyncStatePayload.CODEC);
    }

    public static String CLIENT_STATE = "A";

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncStatePayload.TYPE, (payload, context) -> context.client().execute(() -> CLIENT_STATE = payload.state()));
    }

    public static void sendSync(ServerPlayer player, String state) {
        ServerPlayNetworking.send(player, new SyncStatePayload(state));
    }
}