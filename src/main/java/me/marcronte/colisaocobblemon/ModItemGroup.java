package me.marcronte.colisaocobblemon;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroup {

    public static final ResourceKey<CreativeModeTab> COLISAO_GROUP_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "general")
    );

    public static final CreativeModeTab COLISAO_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.KANTO_BADGE_CASE)) // Ícone da aba
            .title(Component.translatable("itemGroup.colisao-cobblemon.general"))
            .displayItems((context, entries) -> {

                // --- ADMIN ITEMS ---
                entries.accept(ModItems.ROUTE_TOOL);

                // --- BACKPACKs ---
                entries.accept(ModItems.PIKACHU_BACKPACK);
                entries.accept(ModItems.GRENINJA_BACKPACK);
                entries.accept(ModItems.GARCHOMP_BACKPACK);
                entries.accept(ModItems.DARKRAI_BACKPACK);
                entries.accept(ModItems.RAYQUAZA_BACKPACK);

                // --- KEY ITEMS ---
                entries.accept(ModItems.RUNNING_SHOES);
                entries.accept(ModItems.KANTO_BADGE_CASE);
                entries.accept(ModItems.CARD_KEY);
                entries.accept(ModItems.COIN_CASE);
                entries.accept(ModItems.GOLDEN_TEETH);
                entries.accept(ModItems.LIFT_KEY);
                entries.accept(ModItems.OAK_PARCEL);
                entries.accept(ModItems.POKE_FLUTE);
                entries.accept(ModItems.SECRET_KEY);
                entries.accept(ModItems.SILPH_SCOPE);
                entries.accept(ModItems.SS_TICKET);
                entries.accept(ModItems.TEA);

                // --- ITEMS ---
                entries.accept(ModItems.QUEST_BOOK);
                entries.accept(ModItems.POKEMON_PICKAXE);
                entries.accept(ModItems.ETERNATITE_SCRAP);
                entries.accept(ModItems.ETERNATITE_INGOT);
                entries.accept(ModItems.TERASTALITE_CRYSTAL);

                // --- BLOCKS ---
                entries.accept(ModItems.MINING_BLOCK_ITEM);
                entries.accept(ModItems.ETERNATITE_ORE_ITEM);
                entries.accept(ModItems.TERASTALITE_ORE_ITEM);
                entries.accept(ModBlocks.BREEDING_HABITAT);

                // --- ARMORs ---
                entries.accept(ModItems.ETERNATITE_HELMET);
                entries.accept(ModItems.ETERNATITE_CHESTPLATE);
                entries.accept(ModItems.ETERNATITE_LEGGINGS);
                entries.accept(ModItems.ETERNATITE_BOOTS);

                // --- TOOLs ---
                entries.accept(ModItems.ETERNATITE_SWORD);
                entries.accept(ModItems.ETERNATITE_PICKAXE);
                entries.accept(ModItems.ETERNATITE_AXE);
                entries.accept(ModItems.ETERNATITE_SHOVEL);
                entries.accept(ModItems.ETERNATITE_HOE);

                // --- HMs ---
                entries.accept(ModItems.SURF);
                entries.accept(ModItems.FLASH_HM);

                // --- BADGES ---
                entries.accept(ModItems.KANTO_BOULDER_BADGE);
                entries.accept(ModItems.KANTO_CASCADE_BADGE);
                entries.accept(ModItems.KANTO_THUNDER_BADGE);
                entries.accept(ModItems.KANTO_RAINBOW_BADGE);
                entries.accept(ModItems.KANTO_SOUL_BADGE);
                entries.accept(ModItems.KANTO_MARSH_BADGE);
                entries.accept(ModItems.KANTO_VOLCANO_BADGE);
                entries.accept(ModItems.KANTO_EARTH_BADGE);
                entries.accept(ModItems.KANTO_CHAMPION_BADGE);

                // --- INCENSES (BREEDING) ---
                entries.accept(ModItems.SEA_INCENSE);
                entries.accept(ModItems.LAX_INCENSE);
                entries.accept(ModItems.ROSE_INCENSE);
                entries.accept(ModItems.PURE_INCENSE);
                entries.accept(ModItems.ROCK_INCENSE);
                entries.accept(ModItems.ODD_INCENSE);
                entries.accept(ModItems.LUCK_INCENSE);
                entries.accept(ModItems.FULL_INCENSE);
                entries.accept(ModItems.WAVE_INCENSE);

                // --- POKEMON DROPS ---
                entries.accept(ModItems.FIBER);
                entries.accept(ModItems.NORMALIA);
                entries.accept(ModItems.MAGMA_CHARCOAL);
                entries.accept(ModItems.SUPERNOVA_CORE);
                entries.accept(ModItems.PURIFIED_WATER);
                entries.accept(ModItems.SCALE);
                entries.accept(ModItems.SCREW);
                entries.accept(ModItems.FLASHING_LIGHTNING);
                entries.accept(ModItems.LEAF);
                entries.accept(ModItems.NATURE_ESSENCE);
                entries.accept(ModItems.THIN_ICE);
                entries.accept(ModItems.ETERNAL_SNOW_FLAKE);
                entries.accept(ModItems.SCROLL);
                entries.accept(ModItems.COMBAT_SPIRIT);
                entries.accept(ModItems.VENOM);
                entries.accept(ModItems.VENOM_BARB);
                entries.accept(ModItems.MUD);
                entries.accept(ModItems.DENSE_DIRT);
                entries.accept(ModItems.LIGHT_FEATHER);
                entries.accept(ModItems.SKY_CORE);
                entries.accept(ModItems.SPOON);
                entries.accept(ModItems.MYSTIC_ARTIFACT);
                entries.accept(ModItems.SEED);
                entries.accept(ModItems.HORN);
                entries.accept(ModItems.ROCK);
                entries.accept(ModItems.SANDBAG);
                entries.accept(ModItems.DARK_ESSENCE);
                entries.accept(ModItems.SOUL);
                entries.accept(ModItems.DRAGON_WING);
                entries.accept(ModItems.DRAGON_FANG);
                entries.accept(ModItems.DARK_ORB);
                entries.accept(ModItems.DARK_POWDER);
                entries.accept(ModItems.COINS);
                entries.accept(ModItems.CONTROLLER);
                entries.accept(ModItems.ENHANCED_POWDER);
                entries.accept(ModItems.FAIRY_SOUL);

                // --- POKEDOLLs ---
                entries.accept(ModBlocks.PIKACHU_POKEDOLL);
                entries.accept(ModBlocks.SQUIRTLE_POKEDOLL);
                entries.accept(ModBlocks.BULBASAUR_POKEDOLL);
                entries.accept(ModBlocks.CHARMANDER_POKEDOLL);

                // --- FURNITUREs ---
                entries.accept(ModBlocks.FREEZER);
                entries.accept(ModBlocks.CHAIR);
                entries.accept(ModBlocks.TABLE);
                entries.accept(ModBlocks.MAILBOX);

                // --- KEYs ---
                entries.accept(ModItems.LEGENDARY_KEY);
                entries.accept(ModItems.MEGA_KEY);
                entries.accept(ModItems.MEGA_EQUIPMENT_KEY);
                entries.accept(ModItems.SHINY_KEY);
                entries.accept(ModItems.SKIN_KEY);

            })
            .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, COLISAO_GROUP_KEY, COLISAO_GROUP);
    }
}