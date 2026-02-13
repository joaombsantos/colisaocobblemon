package me.marcronte.colisaocobblemon.features.fadeblock;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.ModItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public class FadeBlockRegistry {

    // Block definition
    public static final FadeBlock FADE_BLOCK = new FadeBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .isValidSpawn((state, world, pos, entity) -> false)
                    .isSuffocating((state, world, pos) -> false)
                    .isViewBlocking((state, world, pos) -> false)
    );

    public static BlockEntityType<FadeBlockEntity> FADE_BLOCK_ENTITY;

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "fade_block"), FADE_BLOCK);

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "fade_block"), new BlockItem(FADE_BLOCK, new Item.Properties()));


        FADE_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "fade_block_entity"),
                BlockEntityType.Builder.of(FadeBlockEntity::new, FADE_BLOCK).build(null)
        );

        FadeBlock.ENTITY_TYPE = FADE_BLOCK_ENTITY;

        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.COLISAO_GROUP_KEY).register(entries -> {
            entries.accept(FADE_BLOCK);
        });

        FadeNetwork.registerCommon();

        registerEvents();
    }

    private static void registerEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            ServerLevel level = (ServerLevel) player.level();
            FadeBlockData data = FadeBlockData.get(level);

            List<BlockPos> unlocked = data.getUnlockedPos(player.getUUID());

            if (!unlocked.isEmpty()) {
                FadeNetwork.sendSync(player, unlocked);
            }
        });
    }
}