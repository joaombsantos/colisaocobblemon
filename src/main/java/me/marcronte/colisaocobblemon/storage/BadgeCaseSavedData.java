package me.marcronte.colisaocobblemon.storage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BadgeCaseSavedData extends SavedData {

    private final Map<UUID, ListTag> playerCases = new HashMap<>();

    public ListTag getPlayerCase(UUID uuid) {
        return playerCases.getOrDefault(uuid, new ListTag());
    }

    public void setPlayerCase(UUID uuid, ListTag listTag) {
        playerCases.put(uuid, listTag);
        this.setDirty();
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, ListTag> entry : playerCases.entrySet()) {
            playersTag.put(entry.getKey().toString(), entry.getValue());
        }
        tag.put("PlayerCases", playersTag);
        return tag;
    }

    public static BadgeCaseSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        BadgeCaseSavedData data = new BadgeCaseSavedData();
        if (tag.contains("PlayerCases")) {
            CompoundTag playersTag = tag.getCompound("PlayerCases");
            for (String key : playersTag.getAllKeys()) {
                try {
                    data.playerCases.put(UUID.fromString(key), playersTag.getList(key, Tag.TAG_COMPOUND));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return data;
    }

    public static BadgeCaseSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(BadgeCaseSavedData::new, BadgeCaseSavedData::load, null),
                "colisao_badge_cases"
        );
    }
}