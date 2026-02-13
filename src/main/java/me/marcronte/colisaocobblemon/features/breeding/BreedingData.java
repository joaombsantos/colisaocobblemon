package me.marcronte.colisaocobblemon.features.breeding;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BreedingData extends SavedData {

    private final Map<UUID, PlayerBreedingInfo> breedingMap = new HashMap<>();

    public static class PlayerBreedingInfo {
        public UUID motherId;
        public UUID fatherId;

        public CompoundTag motherData;
        public CompoundTag fatherData;

        public long startTime = 0;
        public boolean isActive = false;

        // 10000 = 10s | 5400000 = 1h30m
        public static final long BREEDING_DURATION = 5400000;

        public boolean isReady() {
            return isActive && startTime > 0 && (System.currentTimeMillis() - startTime >= BREEDING_DURATION);
        }
    }

    public static BreedingData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(BreedingData::new, BreedingData::load, null),
                "ColisaoBreeding"
        );
    }

    public PlayerBreedingInfo getPlayerInfo(UUID playerUuid) {
        return breedingMap.computeIfAbsent(playerUuid, k -> new PlayerBreedingInfo());
    }

    public Pokemon getPokemon(ServerPlayer owner, UUID pokemonUuid) {
        if (pokemonUuid == null) return null;

        PartyStore party = Cobblemon.INSTANCE.getStorage().getParty(owner);
        Pokemon p = party.get(pokemonUuid);
        if (p != null) return p;

        PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(owner);
        p = pc.get(pokemonUuid);

        return p;
    }

    public void clear(UUID playerUuid) {
        breedingMap.remove(playerUuid);
        setDirty();
    }

    public static BreedingData load(CompoundTag tag, HolderLookup.Provider provider) {
        BreedingData data = new BreedingData();
        CompoundTag mapTag = tag.getCompound("BreedingMap");

        for (String key : mapTag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            CompoundTag infoTag = mapTag.getCompound(key);

            PlayerBreedingInfo info = new PlayerBreedingInfo();

            if (infoTag.hasUUID("Mother")) info.motherId = infoTag.getUUID("Mother");
            if (infoTag.hasUUID("Father")) info.fatherId = infoTag.getUUID("Father");

            if (infoTag.contains("MotherData")) info.motherData = infoTag.getCompound("MotherData");
            if (infoTag.contains("FatherData")) info.fatherData = infoTag.getCompound("FatherData");

            info.startTime = infoTag.getLong("StartTime");
            info.isActive = infoTag.getBoolean("IsActive");

            data.breedingMap.put(uuid, info);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag mapTag = new CompoundTag();

        breedingMap.forEach((uuid, info) -> {
            CompoundTag infoTag = new CompoundTag();

            if (info.motherId != null) infoTag.putUUID("Mother", info.motherId);
            if (info.fatherId != null) infoTag.putUUID("Father", info.fatherId);

            if (info.motherData != null) infoTag.put("MotherData", info.motherData);
            if (info.fatherData != null) infoTag.put("FatherData", info.fatherData);

            infoTag.putLong("StartTime", info.startTime);
            infoTag.putBoolean("IsActive", info.isActive);

            mapTag.put(uuid.toString(), infoTag);
        });

        tag.put("BreedingMap", mapTag);
        return tag;
    }
}