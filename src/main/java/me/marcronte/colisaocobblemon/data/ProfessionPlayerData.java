package me.marcronte.colisaocobblemon.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfessionPlayerData extends SavedData {
    private final Map<UUID, PlayerProf> players = new HashMap<>();

    public static class PlayerProf {
        public String profession = "nenhuma";
        public String rank = "rank_e";
        public int progress = 0;
        public Map<String, Long> cooldowns = new HashMap<>();
        public Map<Integer, String> plantedBerries = new HashMap<>();
        public Map<Integer, Long> plantTimes = new HashMap<>();
    }

    public PlayerProf getPlayer(UUID uuid) {
        return players.computeIfAbsent(uuid, k -> new PlayerProf());
    }

    public void resetPlayer(UUID uuid) {
        players.put(uuid, new PlayerProf());
        this.setDirty();
    }

    public static ProfessionPlayerData load(CompoundTag tag, HolderLookup.Provider provider) {
        ProfessionPlayerData data = new ProfessionPlayerData();
        CompoundTag playersTag = tag.getCompound("Players");

        for (String key : playersTag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            CompoundTag pTag = playersTag.getCompound(key);

            PlayerProf prof = new PlayerProf();
            prof.profession = pTag.getString("Profession");
            prof.rank = pTag.getString("Rank");
            prof.progress = pTag.getInt("Progress");

            CompoundTag cds = pTag.getCompound("Cooldowns");
            for (String cdKey : cds.getAllKeys()) {
                prof.cooldowns.put(cdKey, cds.getLong(cdKey));
            }
            CompoundTag pBerries = pTag.getCompound("PlantedBerries");
            for (String slotKey : pBerries.getAllKeys()) {
                prof.plantedBerries.put(Integer.parseInt(slotKey), pBerries.getString(slotKey));
            }

            CompoundTag pTimes = pTag.getCompound("PlantTimes");
            for (String slotKey : pTimes.getAllKeys()) {
                prof.plantTimes.put(Integer.parseInt(slotKey), pTimes.getLong(slotKey));
            }
            data.players.put(uuid, prof);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerProf> entry : players.entrySet()) {
            CompoundTag pTag = new CompoundTag();
            pTag.putString("Profession", entry.getValue().profession);
            pTag.putString("Rank", entry.getValue().rank);
            pTag.putInt("Progress", entry.getValue().progress);

            CompoundTag cds = new CompoundTag();
            entry.getValue().cooldowns.forEach(cds::putLong);
            pTag.put("Cooldowns", cds);
            CompoundTag pBerries = new CompoundTag();
            entry.getValue().plantedBerries.forEach((slot, berry) -> pBerries.putString(slot.toString(), berry));
            pTag.put("PlantedBerries", pBerries);

            CompoundTag pTimes = new CompoundTag();
            entry.getValue().plantTimes.forEach((slot, time) -> pTimes.putLong(slot.toString(), time));
            pTag.put("PlantTimes", pTimes);

            playersTag.put(entry.getKey().toString(), pTag);
        }
        tag.put("Players", playersTag);
        return tag;
    }

    public static ProfessionPlayerData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(ProfessionPlayerData::new, ProfessionPlayerData::load, null), "colisao_professions");
    }
}