package me.marcronte.colisaocobblemon.features.switchstate;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SwitchStateManager extends SavedData {

    private static final String DATA_NAME = "colisao_switch_states";
    private final Map<UUID, String> playerStates = new HashMap<>();

    public static SwitchStateManager get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new Factory<>(SwitchStateManager::new, SwitchStateManager::load, null),
                DATA_NAME
        );
    }

    public static SwitchStateManager load(CompoundTag tag, HolderLookup.Provider registries) {
        SwitchStateManager data = new SwitchStateManager();
        ListTag list = tag.getList("States", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag entry = (CompoundTag) t;
            data.playerStates.put(entry.getUUID("UUID"), entry.getString("State"));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        playerStates.forEach((uuid, state) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("UUID", uuid);
            entry.putString("State", state);
            list.add(entry);
        });
        tag.put("States", list);
        return tag;
    }


    public static String getState(Player player) {
        if (player.level().isClientSide) {
            return SwitchNetwork.CLIENT_STATE;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            return get((ServerLevel) player.level()).playerStates.getOrDefault(player.getUUID(), "A");
        }
        return "A";
    }

    public static void setState(ServerPlayer player, String state) {
        SwitchStateManager data = get((ServerLevel) player.level());
        data.playerStates.put(player.getUUID(), state);
        data.setDirty();
        SwitchNetwork.sendSync(player, state);
    }

    public static void toggleState(ServerPlayer player) {
        String current = getState(player);
        String next = current.equals("A") ? "B" : "A";
        setState(player, next);
    }
}