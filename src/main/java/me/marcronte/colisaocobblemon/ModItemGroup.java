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

                // --- BLOCKS ---

            })
            .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, COLISAO_GROUP_KEY, COLISAO_GROUP);
    }
}