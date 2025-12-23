package me.marcronte.colisaocobblemon.features.badges;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.ModItemGroup; // Importe o Grupo
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BadgeItems {

    // ... (Mantenha as declarações dos itens iguais: KANTO_BOULDER_BADGE, etc...)
    // Vou resumir para não ocupar espaço, mas mantenha todas as linhas "public static final Item..."

    public static final Item KANTO_BOULDER_BADGE = new Item(new Item.Settings().maxCount(1));
    public static final Item KANTO_CASCADE_BADGE = new Item(new Item.Settings().maxCount(1));
    public static final Item KANTO_THUNDER_BADGE = new Item(new Item.Settings().maxCount(1));
    public static final Item KANTO_RAINBOW_BADGE = new Item(new Item.Settings().maxCount(1));
    public static final Item KANTO_SOUL_BADGE = new Item(new Item.Settings().maxCount(1));
    public static final Item KANTO_MARSH_BADGE = new Item(new Item.Settings().maxCount(1));
    public static final Item KANTO_VOLCANO_BADGE = new Item(new Item.Settings().maxCount(1));
    public static final Item KANTO_EARTH_BADGE = new Item(new Item.Settings().maxCount(1));
    public static final Item KANTO_CHAMPION_BADGE = new Item(new Item.Settings().maxCount(1));
    public static final Item KANTO_BADGE_CASE = new Item(new Item.Settings().maxCount(1));

    public static void register() {
        registerItem("kanto_boulder_badge", KANTO_BOULDER_BADGE);
        registerItem("kanto_cascade_badge", KANTO_CASCADE_BADGE);
        registerItem("kanto_thunder_badge", KANTO_THUNDER_BADGE);
        registerItem("kanto_rainbow_badge", KANTO_RAINBOW_BADGE);
        registerItem("kanto_soul_badge", KANTO_SOUL_BADGE);
        registerItem("kanto_marsh_badge", KANTO_MARSH_BADGE);
        registerItem("kanto_volcano_badge", KANTO_VOLCANO_BADGE);
        registerItem("kanto_earth_badge", KANTO_EARTH_BADGE);
        registerItem("kanto_champion_badge", KANTO_CHAMPION_BADGE);

        registerItem("kanto_badge_case", KANTO_BADGE_CASE);
    }

    private static void registerItem(String path, Item item) {
        Registry.register(Registries.ITEM, Identifier.of(ColisaoCobblemon.MOD_ID, path), item);

        // MUDANÇA: Adiciona ao nosso grupo personalizado
        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.COLISAO_GROUP_KEY).register(entries -> {
            entries.add(item);
        });
    }
}