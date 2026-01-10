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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Blocks;

public class TeleportRegistry {

    public static final Block TELEPORT_BLOCK = new TeleportBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noLootTable());

    public static BlockEntityType<TeleportBlockEntity> TELEPORT_BLOCK_BE;

    public static void register() {
        Registry.register(
                BuiltInRegistries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "teleport_block"),
                TELEPORT_BLOCK
        );

        Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "teleport_block"),
                new BlockItem(TELEPORT_BLOCK, new Item.Properties())
        );

        TELEPORT_BLOCK_BE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "teleport_block"),
                BlockEntityType.Builder.of(TeleportBlockEntity::new, TELEPORT_BLOCK).build(null)
        );

        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.COLISAO_GROUP_KEY).register(entries -> {
            entries.accept(TELEPORT_BLOCK);
        });
    }
}