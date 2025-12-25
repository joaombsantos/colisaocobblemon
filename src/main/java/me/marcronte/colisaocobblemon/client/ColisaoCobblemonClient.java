package me.marcronte.colisaocobblemon.client;

import me.marcronte.colisaocobblemon.features.hms.HmManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.BiomeColors; // Mudou o pacote
import net.minecraft.client.renderer.RenderType;  // Mudou de RenderLayer para RenderType

public class ColisaoCobblemonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // --- 1. Camadas de Renderização (Transparência/Recorte) ---

        // Cut (Folhas precisam de transparência)
        // RenderLayer.getCutoutMipped() -> RenderType.cutoutMipped()
        BlockRenderLayerMap.INSTANCE.putBlock(HmManager.CUT_OBSTACLE, RenderType.cutoutMipped());

        // Rock Smash (Geometria complexa, Cutout ajuda a renderizar bordas corretamente)
        // RenderLayer.getCutout() -> RenderType.cutout()
        BlockRenderLayerMap.INSTANCE.putBlock(HmManager.ROCK_SMASH, RenderType.cutout());

        // --- 2. Cores (Apenas para o Cut) ---
        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
            if (tintIndex == 1) {
                if (view == null || pos == null) return -1;
                // BiomeColors.getFoliageColor -> BiomeColors.getAverageFoliageColor
                return BiomeColors.getAverageFoliageColor(view, pos);
            }
            return -1;
        }, HmManager.CUT_OBSTACLE);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> -1, HmManager.CUT_OBSTACLE);
    }
}