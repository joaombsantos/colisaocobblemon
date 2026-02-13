package me.marcronte.colisaocobblemon.features.badgecase;

import me.marcronte.colisaocobblemon.ModItems;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class BadgeCaseData {

    public static final Map<Integer, Item> KANTO_SLOTS = new HashMap<>();

    static {
        KANTO_SLOTS.put(0, ModItems.KANTO_BOULDER_BADGE);
        KANTO_SLOTS.put(1, ModItems.KANTO_CASCADE_BADGE);
        KANTO_SLOTS.put(2, ModItems.KANTO_THUNDER_BADGE);
        KANTO_SLOTS.put(3, ModItems.KANTO_RAINBOW_BADGE);
        KANTO_SLOTS.put(4, ModItems.KANTO_SOUL_BADGE);
        KANTO_SLOTS.put(5, ModItems.KANTO_MARSH_BADGE);
        KANTO_SLOTS.put(6, ModItems.KANTO_VOLCANO_BADGE);
        KANTO_SLOTS.put(7, ModItems.KANTO_EARTH_BADGE);
        KANTO_SLOTS.put(8, ModItems.KANTO_CHAMPION_BADGE);
    }

    public static Item getBadgeForSlot(int slot) {
        return KANTO_SLOTS.get(slot);
    }
}