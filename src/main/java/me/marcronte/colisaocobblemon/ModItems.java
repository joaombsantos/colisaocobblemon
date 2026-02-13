package me.marcronte.colisaocobblemon;

import me.marcronte.colisaocobblemon.features.badgecase.BadgeCaseItem;
import me.marcronte.colisaocobblemon.features.hms.FlashItem;
import me.marcronte.colisaocobblemon.features.items.CobblemonEggItem;
import me.marcronte.colisaocobblemon.features.items.RunningShoesItem;
import me.marcronte.colisaocobblemon.features.routes.RouteToolItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {

    // --- ADMIN ITEMS ---
    public static final Item ROUTE_TOOL = registerItem("route_tool", new RouteToolItem(new Item.Properties().stacksTo(1)));

    // --- KEY ITEMS ---
    public static final Item KANTO_BADGE_CASE = registerItem("kanto_badge_case", new BadgeCaseItem(new Item.Properties().stacksTo(1)));
    public static final Item RUNNING_SHOES = registerItem("running_shoes", new RunningShoesItem(new Item.Properties().stacksTo(1)));
    public static final Item CARD_KEY = registerItem("card_key", new Item(new Item.Properties().stacksTo(1)));
    public static final Item COIN_CASE = registerItem("coin_case", new Item(new Item.Properties().stacksTo(1)));
    public static final Item GOLDEN_TEETH = registerItem("golden_teeth", new Item(new Item.Properties().stacksTo(1)));
    public static final Item LIFT_KEY = registerItem("lift_key", new Item(new Item.Properties().stacksTo(1)));
    public static final Item OAK_PARCEL = registerItem("oak_parcel", new Item(new Item.Properties().stacksTo(1)));
    public static final Item POKE_FLUTE = registerItem("poke_flute", new Item(new Item.Properties().stacksTo(1)));
    public static final Item SECRET_KEY = registerItem("secret_key", new Item(new Item.Properties().stacksTo(1)));
    public static final Item SILPH_SCOPE = registerItem("silph_scope", new Item(new Item.Properties().stacksTo(1)));
    public static final Item SS_TICKET = registerItem("ss_ticket", new Item(new Item.Properties().stacksTo(1)));
    public static final Item TEA = registerItem("tea", new Item(new Item.Properties().stacksTo(1)));

    // HMs
    public static final Item SURF = registerItem("surf", new Item(new Item.Properties().stacksTo(1)));
    public static final Item FLASH_HM = registerItem("flash", new FlashItem(new Item.Properties().stacksTo(1)));

    // --- BADGES (Kanto) ---
    public static final Item KANTO_BOULDER_BADGE = registerItem("kanto_boulder_badge", new Item(new Item.Properties().stacksTo(1)));
    public static final Item KANTO_CASCADE_BADGE = registerItem("kanto_cascade_badge", new Item(new Item.Properties().stacksTo(1)));
    public static final Item KANTO_THUNDER_BADGE = registerItem("kanto_thunder_badge", new Item(new Item.Properties().stacksTo(1)));
    public static final Item KANTO_RAINBOW_BADGE = registerItem("kanto_rainbow_badge", new Item(new Item.Properties().stacksTo(1)));
    public static final Item KANTO_SOUL_BADGE = registerItem("kanto_soul_badge", new Item(new Item.Properties().stacksTo(1)));
    public static final Item KANTO_MARSH_BADGE = registerItem("kanto_marsh_badge", new Item(new Item.Properties().stacksTo(1)));
    public static final Item KANTO_VOLCANO_BADGE = registerItem("kanto_volcano_badge", new Item(new Item.Properties().stacksTo(1)));
    public static final Item KANTO_EARTH_BADGE = registerItem("kanto_earth_badge", new Item(new Item.Properties().stacksTo(1)));
    public static final Item KANTO_CHAMPION_BADGE = registerItem("kanto_champion_badge", new Item(new Item.Properties().stacksTo(1)));

    // --- INCENSES (Breeding) ---
    public static final Item FULL_INCENSE = registerItem("full_incense", new Item(new Item.Properties()));
    public static final Item LAX_INCENSE = registerItem("lax_incense", new Item(new Item.Properties()));
    public static final Item LUCK_INCENSE = registerItem("luck_incense", new Item(new Item.Properties()));
    public static final Item ODD_INCENSE = registerItem("odd_incense", new Item(new Item.Properties()));
    public static final Item PURE_INCENSE = registerItem("pure_incense", new Item(new Item.Properties()));
    public static final Item ROCK_INCENSE = registerItem("rock_incense", new Item(new Item.Properties()));
    public static final Item ROSE_INCENSE = registerItem("rose_incense", new Item(new Item.Properties()));
    public static final Item SEA_INCENSE = registerItem("sea_incense", new Item(new Item.Properties()));
    public static final Item WAVE_INCENSE = registerItem("wave_incense", new Item(new Item.Properties()));

    // Pokemon Egg (Breeding)
    public static final Item POKEMON_EGG = registerItem("pokemon_egg", new CobblemonEggItem(new Item.Properties()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", name), item);
    }

    public static void registerModItems() {
        ColisaoCobblemon.LOGGER.info("Registrando itens do Colisão Cobblemon");
    }
}