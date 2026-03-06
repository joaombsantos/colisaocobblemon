package me.marcronte.colisaocobblemon.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PlantationPayloads {
    public record SyncPayload(int unlockedSlots, List<SlotData> slots, List<String> availableBerries) implements CustomPacketPayload {
        public static final Type<SyncPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:plantation_sync"));

        public static final StreamCodec<RegistryFriendlyByteBuf, SyncPayload> CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), SyncPayload::new
        );

        private SyncPayload(RegistryFriendlyByteBuf buf) {
            this(buf.readInt(), readSlots(buf), readBerries(buf));
        }

        private static List<SlotData> readSlots(RegistryFriendlyByteBuf buf) {
            int size = buf.readInt();
            List<SlotData> list = new ArrayList<>();
            for (int i = 0; i < size; i++) list.add(new SlotData(buf.readInt(), buf.readUtf(), buf.readLong()));
            return list;
        }

        private static List<String> readBerries(RegistryFriendlyByteBuf buf) {
            int size = buf.readInt();
            List<String> list = new ArrayList<>();
            for (int i = 0; i < size; i++) list.add(buf.readUtf());
            return list;
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeInt(unlockedSlots);
            buf.writeInt(slots.size());
            for (SlotData s : slots) { buf.writeInt(s.index()); buf.writeUtf(s.berryId()); buf.writeLong(s.plantTime()); }
            buf.writeInt(availableBerries.size());
            for (String b : availableBerries) buf.writeUtf(b);
        }

        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
        public record SlotData(int index, String berryId, long plantTime) {}
    }

    public record ActionPayload(int slotIndex, String action, String berryId) implements CustomPacketPayload {
        public static final Type<ActionPayload> ID = new Type<>(ResourceLocation.parse("colisao-cobblemon:plantation_action"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ActionPayload> CODEC = StreamCodec.of(
                (buf, payload) -> payload.write(buf), ActionPayload::new
        );

        private ActionPayload(RegistryFriendlyByteBuf buf) {
            this(buf.readInt(), buf.readUtf(), buf.readUtf());
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeInt(slotIndex); buf.writeUtf(action); buf.writeUtf(berryId);
        }

        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }
}