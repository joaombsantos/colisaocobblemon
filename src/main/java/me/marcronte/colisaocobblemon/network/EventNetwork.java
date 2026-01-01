package me.marcronte.colisaocobblemon.network;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.features.eventblock.PokemonBlockadeEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class EventNetwork {

    public static void registerCommon() {
        PayloadTypeRegistry.playC2S().register(SaveBlockadePayload.TYPE, SaveBlockadePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SaveBlockadePayload.TYPE, (payload, context) -> context.server().execute(() -> {
            ServerPlayer player = context.player();
            if (player.level().getBlockEntity(payload.pos()) instanceof PokemonBlockadeEntity tile) {
                tile.setConfig(payload.props(), payload.eventId(), payload.catchable(), payload.checkMessage(), payload.wakeMessage(), payload.hitboxSize());
                player.displayClientMessage(Component.translatable("message.colisao-cobblemon.saved_settings").withStyle(ChatFormatting.GREEN), true);
            }
        }));
    }

    public record SaveBlockadePayload(BlockPos pos, String props, String eventId, boolean catchable, String checkMessage, String wakeMessage, int hitboxSize) implements CustomPacketPayload {

        public static final Type<SaveBlockadePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "save_blockade"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SaveBlockadePayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeBlockPos(payload.pos);
                    buf.writeUtf(payload.props);
                    buf.writeUtf(payload.eventId);
                    buf.writeBoolean(payload.catchable);
                    buf.writeUtf(payload.checkMessage);
                    buf.writeUtf(payload.wakeMessage);
                    buf.writeInt(payload.hitboxSize);
                },
                buf -> new SaveBlockadePayload(
                        buf.readBlockPos(),
                        buf.readUtf(),
                        buf.readUtf(),
                        buf.readBoolean(),
                        buf.readUtf(),
                        buf.readUtf(),
                        buf.readInt()
                )
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}