package me.marcronte.colisaocobblemon.network;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.features.badgecase.BadgeCaseMenu;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BadgeNetwork {

    public static final ResourceLocation RECOVER_BTN_ID = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "recover_badges");

    public record RecoverBadgesPayload() implements CustomPacketPayload {
        public static final Type<RecoverBadgesPayload> TYPE = new Type<>(RECOVER_BTN_ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, RecoverBadgesPayload> CODEC = StreamCodec.unit(new RecoverBadgesPayload());

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(RecoverBadgesPayload.TYPE, RecoverBadgesPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RecoverBadgesPayload.TYPE, (payload, context) -> context.server().execute(() -> {
            if (context.player().containerMenu instanceof BadgeCaseMenu menu) {
                menu.recoverBadges(context.player());
            }
        }));
    }
}