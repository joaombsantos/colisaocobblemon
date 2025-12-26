package me.marcronte.colisaocobblemon;

import me.marcronte.colisaocobblemon.features.badgecase.BadgeCaseMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {

    public static final MenuType<BadgeCaseMenu> KANTO_BADGE_CASE_MENU = new ExtendedScreenHandlerType<>(
            BadgeCaseMenu::new, BadgeCaseMenu.Payload.CODEC
    );

    public static void register() {
        Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "kanto_badge_case"), KANTO_BADGE_CASE_MENU);
    }
}