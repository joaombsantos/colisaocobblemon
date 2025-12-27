package me.marcronte.colisaocobblemon.features.pokeloot;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.ModItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PokeLootRegistry {

    public static final Block POKE_LOOT_BLOCK = new PokeLootBlock(BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.METAL).noOcclusion());
    public static final Item POKE_LOOT_ITEM = new BlockItem(POKE_LOOT_BLOCK, new Item.Properties());

    // Block Entity Type
    public static BlockEntityType<PokeLootBlockEntity> POKE_LOOT_BE;

    public static void register() {
        // Block and Item
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "poke_loot"), POKE_LOOT_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "poke_loot"), POKE_LOOT_ITEM);
        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.COLISAO_GROUP_KEY).register(entries -> entries.accept(POKE_LOOT_ITEM));

        // Block Entity
        POKE_LOOT_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "poke_loot_be"),
                BlockEntityType.Builder.of(PokeLootBlockEntity::new, POKE_LOOT_BLOCK).build(null));
    }
}