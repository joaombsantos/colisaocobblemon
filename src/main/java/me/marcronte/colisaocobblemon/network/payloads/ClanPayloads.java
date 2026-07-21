package me.marcronte.colisaocobblemon.network.payloads;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClanPayloads {

    public static final StreamCodec<RegistryFriendlyByteBuf, List<String>> STRING_LIST_CODEC = StreamCodec.of(
            (buf, list) -> {
                buf.writeInt(list.size());
                for (String s : list) buf.writeUtf(s);
            },
            buf -> {
                int size = buf.readInt();
                List<String> list = new ArrayList<>();
                for (int i = 0; i < size; i++) list.add(buf.readUtf());
                return list;
            }
    );

    public record OpenClanCreationPayload() implements CustomPacketPayload {
        public static final Type<OpenClanCreationPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:open_clan_creation"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenClanCreationPayload> CODEC = StreamCodec.unit(new OpenClanCreationPayload());

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public record OpenClanMenuPayload(String clanName, int clanLevel, int clanXp, int clanXpNeeded, List<String> members, List<String> perks, List<String> missions, String timeRemaining, long nextResetTimestamp, boolean isManager) implements CustomPacketPayload {
        public static final Type<OpenClanMenuPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:open_clan_menu"));

        public static final StreamCodec<RegistryFriendlyByteBuf, OpenClanMenuPayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeUtf(payload.clanName());
                    buf.writeInt(payload.clanLevel());
                    buf.writeInt(payload.clanXp());
                    buf.writeInt(payload.clanXpNeeded());
                    STRING_LIST_CODEC.encode(buf, payload.members());
                    STRING_LIST_CODEC.encode(buf, payload.perks());
                    STRING_LIST_CODEC.encode(buf, payload.missions());
                    buf.writeUtf(payload.timeRemaining());
                    buf.writeLong(payload.nextResetTimestamp());
                    buf.writeBoolean(payload.isManager());
                },
                buf -> new OpenClanMenuPayload(
                        buf.readUtf(),
                        buf.readInt(),
                        buf.readInt(),
                        buf.readInt(),
                        STRING_LIST_CODEC.decode(buf),
                        STRING_LIST_CODEC.decode(buf),
                        STRING_LIST_CODEC.decode(buf),
                        buf.readUtf(),
                        buf.readLong(),
                        buf.readBoolean()
                )
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public record CreateClanPayload(String name, String tag, String colorStr, String type1, String type2) implements CustomPacketPayload {
        public static final Type<CreateClanPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:create_clan"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CreateClanPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, CreateClanPayload::name,
                ByteBufCodecs.STRING_UTF8, CreateClanPayload::tag,
                ByteBufCodecs.STRING_UTF8, CreateClanPayload::colorStr,
                ByteBufCodecs.STRING_UTF8, CreateClanPayload::type1,
                ByteBufCodecs.STRING_UTF8, CreateClanPayload::type2,
                CreateClanPayload::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(OpenClanCreationPayload.ID, OpenClanCreationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenClanMenuPayload.ID, OpenClanMenuPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CreateClanPayload.ID, CreateClanPayload.CODEC);
    }
}