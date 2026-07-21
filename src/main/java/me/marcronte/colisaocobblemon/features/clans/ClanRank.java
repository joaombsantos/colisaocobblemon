package me.marcronte.colisaocobblemon.features.clans;

public enum ClanRank {
    OWNER(3),
    MANAGER(2),
    MEMBER(1);

    private final int power;

    ClanRank(int power) {
        this.power = power;
    }

    public int getPower() {
        return this.power;
    }

    public boolean isAtLeast(ClanRank minimumRank) {
        return this.power >= minimumRank.getPower();
    }
}