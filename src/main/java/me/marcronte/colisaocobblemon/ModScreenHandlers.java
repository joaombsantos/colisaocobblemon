package me.marcronte.colisaocobblemon;

import me.marcronte.colisaocobblemon.features.badgecase.BadgeCaseMenu;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootMenu;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeBlockMenu;
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

    public static void register() {
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "kanto_badge_case"), KANTO_BADGE_CASE_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "poke_loot_menu"), POKE_LOOT_MENU);
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "fade_block_menu"), FADE_BLOCK_MENU);
    }
}