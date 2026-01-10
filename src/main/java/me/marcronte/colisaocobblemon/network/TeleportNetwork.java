package me.marcronte.colisaocobblemon.network;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.features.teleportblock.TeleportBlock;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry; // Importante
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TeleportNetwork {

    public static final ResourceLocation SET_TELEPORT_ID = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "set_teleport");

    public record SetTeleportPayload(BlockPos blockPos, int targetX, int targetY, int targetZ, float targetYaw) implements CustomPacketPayload {
        public static final Type<SetTeleportPayload> TYPE = new Type<>(SET_TELEPORT_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, SetTeleportPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, SetTeleportPayload::blockPos,
                ByteBufCodecs.INT, SetTeleportPayload::targetX,
                ByteBufCodecs.INT, SetTeleportPayload::targetY,
                ByteBufCodecs.INT, SetTeleportPayload::targetZ,
                ByteBufCodecs.FLOAT, SetTeleportPayload::targetYaw,
                SetTeleportPayload::new
        );
        @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerCommon() {
        PayloadTypeRegistry.playC2S().register(SetTeleportPayload.TYPE, SetTeleportPayload.CODEC);
    }

    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(SetTeleportPayload.TYPE, (payload, context) -> context.server().execute(() -> {
            ServerPlayer player = context.player();
            if (player.isCreative()) {
                BlockState state = player.level().getBlockState(payload.blockPos());
                if (state.getBlock() instanceof TeleportBlock) {

                    int count = TeleportBlock.propagateSettings(
                            player.level(),
                            payload.blockPos(),
                            new BlockPos(payload.targetX(), payload.targetY(), payload.targetZ()),
                            payload.targetYaw(),
                            0f,
                            new java.util.HashSet<>()
                    );

                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.colisao-cobblemon.updated_teleport"), true);
                }
            }
        }));
    }
}