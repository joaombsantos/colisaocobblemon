package me.marcronte.colisaocobblemon.features.teleportblock;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.ModItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TeleportRegistry {

    public static final Block TELEPORT_BLOCK = new TeleportBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noLootTable(), TeleportType.STATIC);
    public static final Block TELEPORT_HUB = new TeleportBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noLootTable(), TeleportType.HUB);
    public static final Block TELEPORT_SPOKE = new TeleportBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noLootTable(), TeleportType.SPOKE);

    public static BlockEntityType<TeleportBlockEntity> TELEPORT_BLOCK_BE;

    public static void register() {
        registerBlockAndItem("teleport_block", TELEPORT_BLOCK);
        registerBlockAndItem("teleport_hub", TELEPORT_HUB);
        registerBlockAndItem("teleport_spoke", TELEPORT_SPOKE);

        TELEPORT_BLOCK_BE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "teleport_block"),
                BlockEntityType.Builder.of(TeleportBlockEntity::new, TELEPORT_BLOCK, TELEPORT_HUB, TELEPORT_SPOKE).build(null)
        );

        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.COLISAO_GROUP_KEY).register(entries -> {
            entries.accept(TELEPORT_BLOCK);
            entries.accept(TELEPORT_HUB);
            entries.accept(TELEPORT_SPOKE);
        });
    }

    private static void registerBlockAndItem(String path, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, path), block);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, path), new BlockItem(block, new Item.Properties()));
    }
}