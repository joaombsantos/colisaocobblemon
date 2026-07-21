package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class HabitatPayloads {
    public record HabitatActionPayload(int actionId, UUID motherId, UUID fatherId, String berryId) implements CustomPacketPayload {
        public static final Type<HabitatActionPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:habitat_action"));

        public static final StreamCodec<RegistryFriendlyByteBuf, HabitatActionPayload> CODEC = StreamCodec.composite(
                net.minecraft.network.codec.ByteBufCodecs.INT, HabitatActionPayload::actionId,
                net.minecraft.core.UUIDUtil.STREAM_CODEC, HabitatActionPayload::motherId,
                net.minecraft.core.UUIDUtil.STREAM_CODEC, HabitatActionPayload::fatherId,
                net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, HabitatActionPayload::berryId,
                HabitatActionPayload::new
        );

        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public static void registerC2S() {
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(HabitatActionPayload.ID, HabitatActionPayload.CODEC);
    }
}