package me.marcronte.colisaocobblemon.features.npcs.quest;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestCounterData extends SavedData {

    // UUID -> (QuestID -> Counter)
    private final Map<UUID, Map<String, Integer>> playerCounters = new HashMap<>();

    public static QuestCounterData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(QuestCounterData::new, QuestCounterData::load, null),
                "ColisaoQuestCounters"
        );
    }

    public int getCount(UUID playerUuid, String questId) {
        return playerCounters.computeIfAbsent(playerUuid, k -> new HashMap<>()).getOrDefault(questId, 0);
    }

    public void incrementCount(UUID playerUuid, String questId, int amount) {
        Map<String, Integer> counters = playerCounters.computeIfAbsent(playerUuid, k -> new HashMap<>());
        counters.put(questId, counters.getOrDefault(questId, 0) + amount);
        setDirty();
    }

    public void clearCount(UUID playerUuid, String questId) {
        if (playerCounters.containsKey(playerUuid)) {
            playerCounters.get(playerUuid).remove(questId);
            setDirty();
        }
    }

    public static QuestCounterData load(CompoundTag tag, HolderLookup.Provider provider) {
        QuestCounterData data = new QuestCounterData();
        CompoundTag playersTag = tag.getCompound("Players");

        for (String uuidStr : playersTag.getAllKeys()) {
            UUID uuid = UUID.fromString(uuidStr);
            CompoundTag questsTag = playersTag.getCompound(uuidStr);
            Map<String, Integer> counters = new HashMap<>();

            for (String questId : questsTag.getAllKeys()) {
                counters.put(questId, questsTag.getInt(questId));
            }
            data.playerCounters.put(uuid, counters);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag playersTag = new CompoundTag();

        playerCounters.forEach((uuid, counters) -> {
            CompoundTag questsTag = new CompoundTag();
            counters.forEach(questsTag::putInt);
            playersTag.put(uuid.toString(), questsTag);
        });

        tag.put("Players", playersTag);
        return tag;
    }
}