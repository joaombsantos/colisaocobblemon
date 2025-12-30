package me.marcronte.colisaocobblemon.client;

import me.marcronte.colisaocobblemon.ModScreenHandlers;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeBlock;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeNetwork;
import me.marcronte.colisaocobblemon.features.hms.HmManager;
import me.marcronte.colisaocobblemon.client.gui.BadgeCaseScreen;
import me.marcronte.colisaocobblemon.client.gui.PokeLootScreen;
import me.marcronte.colisaocobblemon.client.gui.FadeBlockScreen;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootRegistry;
import me.marcronte.colisaocobblemon.network.BoostNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ColisaoCobblemonClient implements ClientModInitializer {

    public static boolean isPlayerBoosting = false;

    private static final Set<BlockPos> clientUnlockedBlocks = new HashSet<>();

    public static void updateUnlockedBlocks(List<BlockPos> positions) {
        clientUnlockedBlocks.addAll(positions);
    }

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
        MenuScreens.register(ModScreenHandlers.FADE_BLOCK_MENU, FadeBlockScreen::new);


        // --- 4. CLIENT REGISTERS ---

        BoostNetwork.registerClient();
        FadeNetwork.registerClient();



        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) return;

            for (BlockPos pos : clientUnlockedBlocks) {
                if (client.level.isLoaded(pos)) {
                    BlockState currentState = client.level.getBlockState(pos);

                    if (currentState.getBlock() instanceof FadeBlock) {
                        client.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
                    }
                }
            }
        });

    }
}