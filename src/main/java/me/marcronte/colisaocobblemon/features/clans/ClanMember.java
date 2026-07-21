package me.marcronte.colisaocobblemon.features.clans;

import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

public class ClanMember {
    private final UUID uuid;
    private final String name;
    private ClanRank rank;
    private int weeklyContribution;

    public ClanMember(UUID uuid, String name, ClanRank rank) {
        this.uuid = uuid;
        this.name = name;
        this.rank = rank;
        this.weeklyContribution = 0;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public ClanRank getRank() { return rank; }
    public void setRank(ClanRank rank) { this.rank = rank; }
    public int getWeeklyContribution() { return weeklyContribution; }
    public void addContribution(int amount) { this.weeklyContribution += amount; }
    public void resetWeeklyContribution() { this.weeklyContribution = 0; }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("UUID", uuid);
        tag.putString("Name", name);
        tag.putString("Rank", rank.name());
        tag.putInt("Contribution", weeklyContribution);
        return tag;
    }

    public static ClanMember fromNbt(CompoundTag tag) {
        UUID uuid = tag.getUUID("UUID");
        String name = tag.getString("Name");
        ClanRank rank = ClanRank.valueOf(tag.getString("Rank"));
        ClanMember member = new ClanMember(uuid, name, rank);
        member.weeklyContribution = tag.getInt("Contribution");
        return member;
    }
}