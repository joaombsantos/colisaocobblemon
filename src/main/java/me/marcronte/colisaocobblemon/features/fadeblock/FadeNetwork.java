package me.marcronte.colisaocobblemon.features.fadeblock;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.client.ColisaoCobblemonClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs; // IMPORTANTE
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FadeNetwork {

    public static final ResourceLocation SYNC_UNLOCKED_ID = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "sync_fade_blocks");

    public record SyncUnlockedPayload(List<BlockPos> positions) implements CustomPacketPayload {
        public static final Type<SyncUnlockedPayload> TYPE = new Type<>(SYNC_UNLOCKED_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, SyncUnlockedPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncUnlockedPayload::positions,
                SyncUnlockedPayload::new
        );

        @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerCommon() {
        PayloadTypeRegistry.playS2C().register(SyncUnlockedPayload.TYPE, SyncUnlockedPayload.CODEC);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncUnlockedPayload.TYPE, (payload, context) -> context.client().execute(() -> ColisaoCobblemonClient.updateUnlockedBlocks(payload.positions())));
    }

    public static void sendSync(ServerPlayer player, List<BlockPos> positions) {
        ServerPlayNetworking.send(player, new SyncUnlockedPayload(positions));
    }
}