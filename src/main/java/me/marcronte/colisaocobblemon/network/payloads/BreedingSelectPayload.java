package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record BreedingSelectPayload(int slot, UUID pokemonUuid) implements CustomPacketPayload {

    public static final Type<BreedingSelectPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:breeding_select"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BreedingSelectPayload> CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeInt(value.slot);
                buf.writeUUID(value.pokemonUuid);
            },
            buf -> new BreedingSelectPayload(buf.readInt(), buf.readUUID())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}