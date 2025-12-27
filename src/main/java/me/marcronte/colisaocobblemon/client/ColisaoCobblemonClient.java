package me.marcronte.colisaocobblemon.client;

import me.marcronte.colisaocobblemon.ModScreenHandlers;
import me.marcronte.colisaocobblemon.features.hms.HmManager;
import me.marcronte.colisaocobblemon.client.gui.BadgeCaseScreen;
import me.marcronte.colisaocobblemon.client.gui.PokeLootScreen;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;

public class ColisaoCobblemonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // --- 1. Rendering ---

        // Cut
        BlockRenderLayerMap.INSTANCE.putBlock(HmManager.CUT_OBSTACLE, RenderType.cutoutMipped());

        // Rock Smash
        BlockRenderLayerMap.INSTANCE.putBlock(HmManager.ROCK_SMASH, RenderType.cutout());

        // --- 2. Colors (Cut only) ---
        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
            if (tintIndex == 1) {
                if (view == null || pos == null) return -1;
                return BiomeColors.getAverageFoliageColor(view, pos);
            }
            return -1;
        }, HmManager.CUT_OBSTACLE);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> -1, HmManager.CUT_OBSTACLE);

        // --- 3. GUI SCREENS ---

        MenuScreens.register(ModScreenHandlers.KANTO_BADGE_CASE_MENU, BadgeCaseScreen::new);
        MenuScreens.register(ModScreenHandlers.POKE_LOOT_MENU, PokeLootScreen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(PokeLootRegistry.POKE_LOOT_BLOCK, RenderType.cutout());
    }
}