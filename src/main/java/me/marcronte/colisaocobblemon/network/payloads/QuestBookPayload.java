package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record QuestBookPayload(List<QuestEntry> quests) implements CustomPacketPayload {
    public static final Type<QuestBookPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:quest_book"));

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestBookPayload> CODEC = StreamCodec.of(
            (buf, payload) -> payload.write(buf),
            QuestBookPayload::new
    );

    private QuestBookPayload(RegistryFriendlyByteBuf buf) {
        this(readQuests(buf));
    }

    private static List<QuestEntry> readQuests(RegistryFriendlyByteBuf buf) {
        int size = buf.readInt();
        List<QuestEntry> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new QuestEntry(
                    buf.readUtf(), buf.readInt(), buf.readInt(), buf.readUtf(), buf.readUtf()
            ));
        }
        return list;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(quests.size());
        for (QuestEntry q : quests) {
            buf.writeUtf(q.id());
            buf.writeInt(q.category()); // 0 = Daily, 1 = Normal, 2 = Questline
            buf.writeInt(q.status());   // 0 = Unknown (???), 1 = In Progress, 2 = Concluded
            buf.writeUtf(q.title());
            buf.writeUtf(q.description());
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public record QuestEntry(String id, int category, int status, String title, String description) {}
}