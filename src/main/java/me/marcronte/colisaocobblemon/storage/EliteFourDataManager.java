package me.marcronte.colisaocobblemon.storage;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EliteFourDataManager extends SavedData {

    private static final String DATA_NAME = ColisaoCobblemon.MOD_ID + "_elite_four";

    private final Map<UUID, EliteData> players = new HashMap<>();

    public static class EliteData {
        public int stage;
        public BlockPos anchor;

        public EliteData(int stage, BlockPos anchor) {
            this.stage = stage;
            this.anchor = anchor;
        }
    }

    public static EliteFourDataManager getServerState(MinecraftServer server) {
        var dataStorage = server.overworld().getDataStorage();

        return dataStorage.computeIfAbsent(
                new SavedData.Factory<>(
                        EliteFourDataManager::new,
                        EliteFourDataManager::load,
                        null
                ),
                DATA_NAME
        );
    }

    public EliteData getPlayerProgress(UUID uuid) {
        return players.get(uuid);
    }

    public void setPlayerProgress(UUID uuid, int stage, BlockPos anchor) {
        players.put(uuid, new EliteData(stage, anchor));
        this.setDirty();
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
        this.setDirty();
    }

    public Map<UUID, EliteData> getAllPlayers() {
        return players;
    }

    public static EliteFourDataManager load(CompoundTag nbt, HolderLookup.Provider provider) {
        EliteFourDataManager data = new EliteFourDataManager();

        ListTag list = nbt.getList("players", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag entry = (CompoundTag) t;
            UUID uuid = entry.getUUID("uuid");
            int stage = entry.getInt("stage");
            int x = entry.getInt("x");
            int y = entry.getInt("y");
            int z = entry.getInt("z");

            data.players.put(uuid, new EliteData(stage, new BlockPos(x, y, z)));
        }

        return data;
    }


    @Override
    public @NotNull CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        ListTag list = new ListTag();

        players.forEach((uuid, eliteData) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            entry.putInt("stage", eliteData.stage);
            entry.putInt("x", eliteData.anchor.getX());
            entry.putInt("y", eliteData.anchor.getY());
            entry.putInt("z", eliteData.anchor.getZ());
            list.add(entry);
        });

        nbt.put("players", list);
        return nbt;
    }
}