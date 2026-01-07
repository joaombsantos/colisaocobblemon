package me.marcronte.colisaocobblemon.client;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import me.marcronte.colisaocobblemon.ModItems;
import me.marcronte.colisaocobblemon.ModScreenHandlers;
import me.marcronte.colisaocobblemon.features.eventblock.EventBlockRegistry;
import me.marcronte.colisaocobblemon.features.eventblock.PokemonBlockade;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeBlock;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeNetwork;
import me.marcronte.colisaocobblemon.features.hms.HmManager;
import me.marcronte.colisaocobblemon.features.pokeloot.PokeLootRegistry;
import me.marcronte.colisaocobblemon.client.gui.BadgeCaseScreen;
import me.marcronte.colisaocobblemon.client.gui.PokeLootScreen;
import me.marcronte.colisaocobblemon.client.gui.PokemonBlockadeScreen;
import me.marcronte.colisaocobblemon.client.gui.FadeBlockScreen;
import me.marcronte.colisaocobblemon.client.renderer.*;
import me.marcronte.colisaocobblemon.client.model.RunningShoesModel;
import me.marcronte.colisaocobblemon.features.switchstate.SwitchNetwork;
import me.marcronte.colisaocobblemon.features.switchstate.SwitchStateRegistry;
import me.marcronte.colisaocobblemon.network.BoostNetwork;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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

    private static RunningShoesModel runningShoesModel;

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
        MenuScreens.register(ModScreenHandlers.POKEMON_BLOCKADE_MENU, PokemonBlockadeScreen::new);


        // --- 4. CLIENT REGISTERS ---

        BoostNetwork.registerClient();
        FadeNetwork.registerClient();



        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) return;

            for (BlockPos pos : clientUnlockedBlocks) {
                if (client.level.isLoaded(pos)) {
                    BlockState currentState = client.level.getBlockState(pos);

                    if (currentState.getBlock() instanceof FadeBlock || currentState.getBlock() instanceof PokemonBlockade) {
                        client.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
                    }
                }
            }
        });

        BlockEntityRenderers.register(EventBlockRegistry.POKEMON_BLOCKADE_ENTITY, PokemonBlockadeRenderer::new);



        EntityModelLayerRegistry.registerModelLayer(RunningShoesModel.LAYER_LOCATION, RunningShoesModel::createBodyLayer);

        ArmorRenderer.register((poseStack, multiBufferSource, itemStack, livingEntity, equipmentSlot, light, humanoidModel) -> {

            if (runningShoesModel == null) {
                runningShoesModel = new RunningShoesModel(Minecraft.getInstance().getEntityModels().bakeLayer(RunningShoesModel.LAYER_LOCATION));
            }

            humanoidModel.copyPropertiesTo(runningShoesModel);

            runningShoesModel.right_leg.copyFrom(humanoidModel.rightLeg);
            runningShoesModel.left_leg.copyFrom(humanoidModel.leftLeg);

            runningShoesModel.setupAnim(livingEntity, 0, 0, 0, 0, 0);

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "textures/models/armor/running_shoes.png");

            runningShoesModel.renderToBuffer(
                    poseStack,
                    multiBufferSource.getBuffer(RenderType.armorCutoutNoCull(texture)),
                    light,
                    OverlayTexture.NO_OVERLAY,
                    0xFFFFFFFF
            );

        }, ModItems.RUNNING_SHOES);


        ModelLoadingPlugin.register(context -> context.addModels(
                ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "block/switch_statue"),
                ResourceLocation.fromNamespaceAndPath("colisao-cobblemon", "block/switch_statue_active")
        ));

        BlockEntityRendererRegistry.register(
                SwitchStateRegistry.STATE_BLOCK_BE,
                StateBlockRenderer::new
        );

        BlockEntityRendererRegistry.register(
                SwitchStateRegistry.SWITCH_STATUE_BE,
                SwitchStatueRenderer::new
        );

        SwitchNetwork.registerClient();

    }
}