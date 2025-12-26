package me.marcronte.colisaocobblemon.features.badgecase;

import me.marcronte.colisaocobblemon.features.badges.BadgeItems;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class BadgeCaseData {

    public static final Map<Integer, Item> KANTO_SLOTS = new HashMap<>();

    static {
        KANTO_SLOTS.put(0, BadgeItems.KANTO_BOULDER_BADGE);
        KANTO_SLOTS.put(1, BadgeItems.KANTO_CASCADE_BADGE);
        KANTO_SLOTS.put(2, BadgeItems.KANTO_THUNDER_BADGE);
        KANTO_SLOTS.put(3, BadgeItems.KANTO_RAINBOW_BADGE);
        KANTO_SLOTS.put(4, BadgeItems.KANTO_SOUL_BADGE);
        KANTO_SLOTS.put(5, BadgeItems.KANTO_MARSH_BADGE);
        KANTO_SLOTS.put(6, BadgeItems.KANTO_VOLCANO_BADGE);
        KANTO_SLOTS.put(7, BadgeItems.KANTO_EARTH_BADGE);
        KANTO_SLOTS.put(8, BadgeItems.KANTO_CHAMPION_BADGE);
    }

    public static Item getBadgeForSlot(int slot) {
        return KANTO_SLOTS.get(slot);
    }
}