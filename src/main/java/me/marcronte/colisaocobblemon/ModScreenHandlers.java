package me.marcronte.colisaocobblemon;

import me.marcronte.colisaocobblemon.client.gui.PokeLurerMenu;
import me.marcronte.colisaocobblemon.features.badgecase.BadgeCaseMenu;
import me.marcronte.colisaocobblemon.features.blocks.machines.harvester.HarvesterMenu;
import me.marcronte.colisaocobblemon.features.blocks.machines.pokefurnace.PokeFurnaceMenu;
import me.marcronte.colisaocobblemon.features.blocks.machines.pokeminer.PokeMinerMenu;
import me.marcronte.colisaocobblemon.features.breeding.habitat.HabitatMenu;
import me.marcronte.colisaocobblemon.features.eventblock.PokemonBlockadeMenu;
import me.marcronte.colisaocobblemon.features.items.backpack.BackpackMenu;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootMenu;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeBlockMenu;
import me.marcronte.colisaocobblemon.features.eventblock.PokemonBlockadeEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {

    public static final MenuType<BadgeCaseMenu> KANTO_BADGE_CASE_MENU = new ExtendedScreenHandlerType<>(
            BadgeCaseMenu::new, BadgeCaseMenu.Payload.CODEC
    );

    public static final MenuType<PokeLootMenu> POKE_LOOT_MENU = new ExtendedScreenHandlerType<>(
            PokeLootMenu::new, BlockPos.STREAM_CODEC
    );

    public static final MenuType<FadeBlockMenu> FADE_BLOCK_MENU = new ExtendedScreenHandlerType<>(
            FadeBlockMenu::new, BlockPos.STREAM_CODEC
    );

    public static final MenuType<PokemonBlockadeMenu> POKEMON_BLOCKADE_MENU = new ExtendedScreenHandlerType<>(
            PokemonBlockadeMenu::new, PokemonBlockadeEntity.OpeningData.CODEC
    );

    public static final MenuType<BackpackMenu> BACKPACK_MENU = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.parse("colisao-cobblemon:backpack"),
            new ExtendedScreenHandlerType<>(BackpackMenu::new, BackpackMenu.Payload.CODEC)
    );

    public static final MenuType<HabitatMenu> HABITAT_MENU = new ExtendedScreenHandlerType<>(
            HabitatMenu::new, BlockPos.STREAM_CODEC
    );

    public static final MenuType<PokeLurerMenu> POKE_LURER_MENU = new ExtendedScreenHandlerType<>(
            PokeLurerMenu::new, BlockPos.STREAM_CODEC
    );

    public static final MenuType<HarvesterMenu> HARVESTER_MENU = new ExtendedScreenHandlerType<>(
            HarvesterMenu::new, BlockPos.STREAM_CODEC
    );

    public static final MenuType<PokeMinerMenu> POKE_MINER_MENU = new ExtendedScreenHandlerType<>(
            PokeMinerMenu::new, BlockPos.STREAM_CODEC
    );

    public static final MenuType<PokeFurnaceMenu> POKE_FURNACE_MENU = new ExtendedScreenHandlerType<>(
            PokeFurnaceMenu::new, BlockPos.STREAM_CODEC
    );

    public static void register() {
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "kanto_badge_case"), KANTO_BADGE_CASE_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "poke_loot_menu"), POKE_LOOT_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "fade_block_menu"), FADE_BLOCK_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "pokemon_blockade_menu"), POKEMON_BLOCKADE_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "habitat_menu"), HABITAT_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "poke_lurer_menu"), POKE_LURER_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "harvester_menu"), HARVESTER_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "poke_miner_menu"), POKE_MINER_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "poke_furnace_menu"), POKE_FURNACE_MENU);
    }
}