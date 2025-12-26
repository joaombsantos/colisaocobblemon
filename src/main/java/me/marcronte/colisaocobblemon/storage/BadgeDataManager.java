package me.marcronte.colisaocobblemon.storage;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BadgeDataManager extends SavedData {

    private final Map<UUID, Set<String>> playerBadges = new HashMap<>();

    private final Map<UUID, Set<String>> defeatedTrainers = new HashMap<>();

    private static final String ID = "colisao_badges";

    public boolean hasBadge(UUID playerUuid, String badgeId) {
        return playerBadges.containsKey(playerUuid) && playerBadges.get(playerUuid).contains(badgeId);
    }

    public void addBadge(UUID playerUuid, String badgeId) {
        playerBadges.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(badgeId);
        setDirty();
    }

    public void removeBadge(UUID playerUuid, String badgeId) {
        if (playerBadges.containsKey(playerUuid)) {
            playerBadges.get(playerUuid).remove(badgeId);
            setDirty();
        }
    }

    public Set<String> getBadges(UUID playerUuid) {
        return playerBadges.getOrDefault(playerUuid, Collections.emptySet());
    }

    // --- TRAINER'S MECHANICS ---

    public boolean hasDefeated(UUID playerUuid, String trainerName) {
        return defeatedTrainers.containsKey(playerUuid) && defeatedTrainers.get(playerUuid).contains(trainerName);
    }

    public void addDefeatedTrainer(UUID playerUuid, String trainerName) {
        defeatedTrainers.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(trainerName);
        setDirty();
    }

    // --- SAVE AND LOADING (NBT) ---

    @Override
    public @NotNull CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        CompoundTag playersTag = new CompoundTag();

        playerBadges.forEach((uuid, badges) -> {
            ListTag list = new ListTag();
            badges.forEach(badge -> list.add(StringTag.valueOf(badge)));
            playersTag.put(uuid.toString(), list);
        });

        nbt.put("PlayerBadges", playersTag);
        return nbt;
    }

    public static BadgeDataManager createFromNbt(CompoundTag tag, HolderLookup.Provider provider) {
        BadgeDataManager state = new BadgeDataManager();
        CompoundTag playersTag = tag.getCompound("PlayerBadges");

        for (String uuidString : playersTag.getAllKeys()) { // getKeys -> getAllKeys
            try {
                UUID uuid = UUID.fromString(uuidString);
                ListTag list = playersTag.getList(uuidString, Tag.TAG_STRING);
                Set<String> badges = new HashSet<>();

                for (Tag element : list) {
                    badges.add(element.getAsString());
                }

                state.playerBadges.put(uuid, badges);
            } catch (Exception e) {
                ColisaoCobblemon.LOGGER.error("Error when loading badge for UUID: {}", uuidString, e);
            }
        }

        if (tag.contains("DefeatedTrainers")) {
            CompoundTag trainersTag = tag.getCompound("DefeatedTrainers");
            for (String uuidString : trainersTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    ListTag list = trainersTag.getList(uuidString, Tag.TAG_STRING);
                    Set<String> trainers = new HashSet<>();
                    for (Tag element : list) trainers.add(element.getAsString());
                    state.defeatedTrainers.put(uuid, trainers);
                } catch (Exception e) {
                    ColisaoCobblemon.LOGGER.error("Error when loading trainers ", e);
                }
            }
        }

        return state;
    }

    // --- STATIC ACCESS (Singleton) ---

    public static BadgeDataManager getServerState(MinecraftServer server) {
        DimensionDataStorage stateManager = server.overworld().getDataStorage();

        SavedData.Factory<BadgeDataManager> factory = new SavedData.Factory<>(
                BadgeDataManager::new,
                BadgeDataManager::createFromNbt,
                null
        );

        return stateManager.computeIfAbsent(factory, ID);
    }
}