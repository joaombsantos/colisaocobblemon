package me.marcronte.colisaocobblemon.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class QuestProgressData extends SavedData {

    private final Map<String, Long> completedQuests = new HashMap<>();

    private final Set<String> activeQuests = new HashSet<>();

    public static QuestProgressData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        QuestProgressData::new,
                        QuestProgressData::load,
                        DataFixTypes.LEVEL
                ),
                "ColisaoQuests"
        );
    }

    public QuestProgressData() {}

    public static QuestProgressData load(CompoundTag tag, HolderLookup.Provider provider) {
        QuestProgressData data = new QuestProgressData();

        if (tag.contains("Quests")) {
            CompoundTag questsTag = tag.getCompound("Quests");
            for (String key : questsTag.getAllKeys()) {
                data.completedQuests.put(key, questsTag.getLong(key));
            }
        }

        if (tag.contains("ActiveQuests")) {
            ListTag list = tag.getList("ActiveQuests", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                data.activeQuests.add(list.getString(i));
            }
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag questsTag = new CompoundTag();
        completedQuests.forEach(questsTag::putLong);
        tag.put("Quests", questsTag);

        ListTag activeList = new ListTag();
        activeQuests.forEach(key -> activeList.add(StringTag.valueOf(key)));
        tag.put("ActiveQuests", activeList);

        return tag;
    }

    public boolean canDoQuest(UUID playerUUID, String npcId, int cooldownHours) {
        String key = playerUUID.toString() + ":" + npcId;
        if (!completedQuests.containsKey(key)) return true;
        if (cooldownHours <= 0) return false;

        long lastCompletion = completedQuests.get(key);
        long cooldownMillis = cooldownHours * 3600000L;
        return System.currentTimeMillis() > (lastCompletion + cooldownMillis);
    }

    public void startQuest(UUID playerUUID, String npcId) {
        String key = playerUUID.toString() + ":" + npcId;
        if (activeQuests.add(key)) {
            setDirty();
        }
    }

    public boolean hasStartedQuest(UUID playerUUID, String npcId) {
        String key = playerUUID.toString() + ":" + npcId;
        return activeQuests.contains(key);
    }

    public void completeQuest(UUID playerUUID, String npcId) {
        String key = playerUUID.toString() + ":" + npcId;
        completedQuests.put(key, System.currentTimeMillis());

        activeQuests.remove(key);

        setDirty();
    }
}