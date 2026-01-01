package me.marcronte.colisaocobblemon.features.eventblock;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EventBlockData extends SavedData {
    private static final String ID = "colisao_event_blocks";

    private final Map<UUID, Set<String>> completedEvents = new HashMap<>();

    public boolean isCompleted(UUID playerUuid, String eventId) {
        return completedEvents.containsKey(playerUuid) && completedEvents.get(playerUuid).contains(eventId);
    }

    public void completeEvent(UUID playerUuid, String eventId) {
        completedEvents.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(eventId);
        setDirty();
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        CompoundTag playersTag = new CompoundTag();
        completedEvents.forEach((uuid, events) -> {
            ListTag list = new ListTag();
            events.forEach(e -> list.add(StringTag.valueOf(e)));
            playersTag.put(uuid.toString(), list);
        });
        nbt.put("CompletedEvents", playersTag);
        return nbt;
    }

    public static EventBlockData load(CompoundTag tag, HolderLookup.Provider provider) {
        EventBlockData data = new EventBlockData();
        if (tag.contains("CompletedEvents")) {
            CompoundTag playersTag = tag.getCompound("CompletedEvents");
            for (String key : playersTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    ListTag list = playersTag.getList(key, Tag.TAG_STRING);
                    Set<String> events = new HashSet<>();
                    for (Tag t : list) events.add(t.getAsString());
                    data.completedEvents.put(uuid, events);
                } catch (Exception ignored) {}
            }
        }
        return data;
    }

    public static EventBlockData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(EventBlockData::new, EventBlockData::load, null), ID);
    }
}