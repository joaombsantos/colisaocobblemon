package me.marcronte.colisaocobblemon.client;

import me.marcronte.colisaocobblemon.features.hms.HmManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.RenderLayer;

public class ColisaoCobblemonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // --- 1. Camadas de Renderização (Transparência/Recorte) ---

        // Cut (Folhas precisam de transparência)
        BlockRenderLayerMap.INSTANCE.putBlock(HmManager.CUT_OBSTACLE, RenderLayer.getCutoutMipped());

        // Rock Smash (Geometria complexa, Cutout ajuda a renderizar bordas corretamente)
        BlockRenderLayerMap.INSTANCE.putBlock(HmManager.ROCK_SMASH, RenderLayer.getCutout());

        // --- 2. Cores (Apenas para o Cut) ---
        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
            if (tintIndex == 1) {
                if (view == null || pos == null) return -1;
                return BiomeColors.getFoliageColor(view, pos);
            }
            return -1;
        }, HmManager.CUT_OBSTACLE);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            return -1;
        }, HmManager.CUT_OBSTACLE);
    }
}