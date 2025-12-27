package me.marcronte.colisaocobblemon.features.pokeloot;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

public class PokeLootNetwork {

    public static final ResourceLocation TOGGLE_VISIBILITY_ID = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "toggle_loot_visibility");

    public record ToggleVisibilityPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<ToggleVisibilityPayload> TYPE = new Type<>(TOGGLE_VISIBILITY_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleVisibilityPayload> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, ToggleVisibilityPayload::pos,
                ToggleVisibilityPayload::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ToggleVisibilityPayload.TYPE, ToggleVisibilityPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ToggleVisibilityPayload.TYPE, (payload, context) -> context.server().execute(() -> {
            ServerLevel level = (ServerLevel) context.player().level();
            if (level.getBlockEntity(payload.pos()) instanceof PokeLootBlockEntity be) {
                be.toggleVisibility();
            }
        }));
    }
}