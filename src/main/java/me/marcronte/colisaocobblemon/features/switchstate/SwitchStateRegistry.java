package me.marcronte.colisaocobblemon.features.switchstate;

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

public class SwitchStateRegistry {

    public static final Block STATE_BLOCK = new StateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion());
    public static final Block SWITCH_STATUE = new SwitchStatueBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion());

    public static final BlockItem STATE_BLOCK_ITEM = new BlockItem(STATE_BLOCK, new Item.Properties());
    public static final BlockItem SWITCH_STATUE_ITEM = new BlockItem(SWITCH_STATUE, new Item.Properties());

    public static BlockEntityType<StateBlockEntity> STATE_BLOCK_BE;
    public static BlockEntityType<SwitchStatueEntity> SWITCH_STATUE_BE;

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "state_block"), STATE_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "switch_statue"), SWITCH_STATUE);

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "state_block"), STATE_BLOCK_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "switch_statue"), SWITCH_STATUE_ITEM);

        STATE_BLOCK_BE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "state_block"),
                BlockEntityType.Builder.of(StateBlockEntity::new, STATE_BLOCK).build(null)
        );

        SWITCH_STATUE_BE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "switch_statue"),
                BlockEntityType.Builder.of(SwitchStatueEntity::new, SWITCH_STATUE).build(null)
        );

        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.COLISAO_GROUP_KEY).register(entries -> {
            entries.accept(STATE_BLOCK_ITEM);
            entries.accept(SWITCH_STATUE_ITEM);
        });
    }
}