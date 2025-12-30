package me.marcronte.colisaocobblemon.features.fadeblock;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FadeBlockData extends SavedData {

    // Map: Block position -> UUIDs' list from unlocked players
    private final Map<BlockPos, Set<UUID>> unlockedBlocks = new HashMap<>();

    public static FadeBlockData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(FadeBlockData::new, FadeBlockData::load, DataFixTypes.LEVEL),
                ColisaoCobblemon.MOD_ID + "_fade_blocks"
        );
    }

    // Load from NBT
    public static FadeBlockData load(CompoundTag tag, HolderLookup.Provider registries) {
        FadeBlockData data = new FadeBlockData();
        ListTag list = tag.getList("UnlockedBlocks", Tag.TAG_COMPOUND);

        for (Tag t : list) {
            CompoundTag entry = (CompoundTag) t;
            BlockPos pos = NbtUtils.readBlockPos(entry, "Pos").orElse(BlockPos.ZERO);

            ListTag players = entry.getList("Players", Tag.TAG_INT_ARRAY);
            Set<UUID> uuids = new HashSet<>();
            for (Tag p : players) {
                uuids.add(NbtUtils.loadUUID(p));
            }
            data.unlockedBlocks.put(pos, uuids);
        }
        return data;
    }

    // Save on NBT
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();

        for (Map.Entry<BlockPos, Set<UUID>> entry : unlockedBlocks.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("Pos", NbtUtils.writeBlockPos(entry.getKey()));

            ListTag players = new ListTag();
            for (UUID uuid : entry.getValue()) {
                players.add(NbtUtils.createUUID(uuid));
            }
            entryTag.put("Players", players);
            list.add(entryTag);
        }

        tag.put("UnlockedBlocks", list);
        return tag;
    }

    // Logic Methods
    public void unlock(BlockPos pos, UUID playerUUID) {
        unlockedBlocks.computeIfAbsent(pos, k -> new HashSet<>()).add(playerUUID);
        setDirty();
    }

    public boolean isUnlocked(BlockPos pos, UUID playerUUID) {
        return unlockedBlocks.containsKey(pos) && unlockedBlocks.get(pos).contains(playerUUID);
    }

    public List<BlockPos> getUnlockedPos(UUID playerUUID) {
        List<BlockPos> unlocked = new ArrayList<>();
        for (Map.Entry<BlockPos, Set<UUID>> entry : unlockedBlocks.entrySet()) {
            if (entry.getValue().contains(playerUUID)) {
                unlocked.add(entry.getKey());
            }
        }
        return unlocked;
    }
}