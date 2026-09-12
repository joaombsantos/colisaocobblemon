package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record LensResponsePayload(int entityId, String ability, String nature, int hp, int atk, int def, int spa, int spd, int spe) implements CustomPacketPayload {
    public static final Type<LensResponsePayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:lens_res"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LensResponsePayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeInt(p.entityId());
                buf.writeUtf(p.ability()); buf.writeUtf(p.nature());
                buf.writeInt(p.hp()); buf.writeInt(p.atk()); buf.writeInt(p.def()); buf.writeInt(p.spa()); buf.writeInt(p.spd()); buf.writeInt(p.spe());
            },
            buf -> new LensResponsePayload(buf.readInt(), buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt())
    );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
}