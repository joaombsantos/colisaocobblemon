package me.marcronte.colisaocobblemon.features.clans;

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

public class ClanSavedData extends SavedData {
    private static final String DATA_ID = "colisao_clans_data";

    private final Map<String, Clan> clansByName = new HashMap<>();
    private final Map<UUID, String> playerClanMap = new HashMap<>();

    public void registerClan(Clan clan) {
        clansByName.put(clan.getName().toLowerCase(), clan);
        for (UUID uuid : clan.getMembers().keySet()) {
            playerClanMap.put(uuid, clan.getName().toLowerCase());
        }
        setDirty();
    }

    public void removeClan(String clanName) {
        Clan clan = clansByName.remove(clanName.toLowerCase());
        if (clan != null) {
            for (UUID uuid : clan.getMembers().keySet()) {
                playerClanMap.remove(uuid);
            }
            setDirty();
        }
    }

    public Clan getClanByName(String name) {
        return clansByName.get(name.toLowerCase());
    }

    public Clan getClanByPlayer(UUID uuid) {
        String clanName = playerClanMap.get(uuid);
        return clanName != null ? clansByName.get(clanName) : null;
    }

    public void updatePlayerCache(UUID uuid, String clanName) {
        if (clanName == null) {
            playerClanMap.remove(uuid);
        } else {
            playerClanMap.put(uuid, clanName.toLowerCase());
        }
        setDirty();
    }

    public Map<String, Clan> getAllClans() {
        return clansByName;
    }

    private long nextResetTimestamp = 0;

    public long getNextResetTimestamp() {
        if (this.nextResetTimestamp == 0) {
            // Se for um mundo novo, calcula a próxima terça-feira
            this.nextResetTimestamp = ClanScheduler.calculateNextReset();
            this.setDirty();
        }
        return this.nextResetTimestamp;
    }

    public void setNextResetTimestamp(long timestamp) {
        this.nextResetTimestamp = timestamp;
        this.setDirty();
    }

    public static ClanSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        ClanSavedData data = new ClanSavedData();
        data.nextResetTimestamp = tag.getLong("NextResetTimestamp");
        ListTag clanList = tag.getList("Clans", Tag.TAG_COMPOUND);

        for (int i = 0; i < clanList.size(); i++) {
            Clan clan = Clan.load(clanList.getCompound(i), provider);
            data.registerClan(clan);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLong("NextResetTimestamp", this.nextResetTimestamp);
        ListTag clanList = new ListTag();
        for (Clan clan : clansByName.values()) {
            clanList.add(clan.save(new CompoundTag(), provider));
        }
        tag.put("Clans", clanList);
        return tag;
    }

    public static ClanSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new Factory<>(ClanSavedData::new, ClanSavedData::load, null), DATA_ID
        );
    }

    public static void refreshTabList(net.minecraft.server.level.ServerPlayer player) {
        if (player.getServer() != null) {
            player.getServer().getPlayerList().broadcastAll(new net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket(
                    net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));
        }
    }
}