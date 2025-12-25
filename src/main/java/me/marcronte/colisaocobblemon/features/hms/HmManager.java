package me.marcronte.colisaocobblemon.features.hms;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.ModItemGroup;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class HmManager {

    public static final Block CUT_OBSTACLE = new CutObstacleBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noCollission()
    );

    public static final Block ROCK_SMASH = new RockSmashBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noCollission()
    );

    public static final Item SURF = new Item(new Item.Properties().stacksTo(1));

    public static void register() {

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "cut_obstacle");
        Registry.register(BuiltInRegistries.BLOCK, id, CUT_OBSTACLE);
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(CUT_OBSTACLE, new Item.Properties()));

        ResourceLocation rockId = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "rock_smash");
        Registry.register(BuiltInRegistries.BLOCK, rockId, ROCK_SMASH);
        Registry.register(BuiltInRegistries.ITEM, rockId, new BlockItem(ROCK_SMASH, new Item.Properties()));

        ResourceLocation surfId = ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "surf");
        Registry.register(BuiltInRegistries.ITEM, surfId, SURF);

        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.COLISAO_GROUP_KEY).register(entries -> {
            entries.accept(CUT_OBSTACLE);
            entries.accept(ROCK_SMASH);
            entries.accept(SURF);
        });

        SurfHandler.register();

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            InteractionResult cutResult = CutObstacleBlock.handleInteract(player, world, hand, hitResult);
            if (cutResult == InteractionResult.SUCCESS) return InteractionResult.SUCCESS;

            return RockSmashBlock.handleInteract(player, world, hand, hitResult);
        });
    }
}