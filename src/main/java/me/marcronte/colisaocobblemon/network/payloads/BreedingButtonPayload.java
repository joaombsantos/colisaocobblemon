package me.marcronte.colisaocobblemon.network.payloads;

import me.marcronte.colisaocobblemon.network.BreedingNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record BreedingButtonPayload(int buttonId) implements CustomPacketPayload {

    public static final Type<BreedingButtonPayload> ID = new Type<>(BreedingNetwork.CLICK_BUTTON);

    public static final StreamCodec<RegistryFriendlyByteBuf, BreedingButtonPayload> CODEC = StreamCodec.of(
            (buf, value) -> buf.writeInt(value.buttonId),
            buf -> new BreedingButtonPayload(buf.readInt())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}