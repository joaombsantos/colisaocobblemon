package me.marcronte.colisaocobblemon.features.hms;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class HmManager {

    public static final Block CUT_OBSTACLE = new CutObstacleBlock(
            AbstractBlock.Settings.copy(Blocks.OAK_PLANKS).noCollision()
    );

    public static final Block ROCK_SMASH = new RockSmashBlock(
            AbstractBlock.Settings.copy(Blocks.STONE).noCollision()
    );

    public static final Item SURF = new Item(new Item.Settings().maxCount(1));

    public static void register() {
        Identifier id = Identifier.of(ColisaoCobblemon.MOD_ID, "cut_obstacle");

        Registry.register(Registries.BLOCK, id, CUT_OBSTACLE);
        Registry.register(Registries.ITEM, id, new BlockItem(CUT_OBSTACLE, new Item.Settings()));

        Identifier rockId = Identifier.of(ColisaoCobblemon.MOD_ID, "rock_smash");
        Registry.register(Registries.BLOCK, rockId, ROCK_SMASH);
        Registry.register(Registries.ITEM, rockId, new BlockItem(ROCK_SMASH, new Item.Settings()));

        Identifier surfId = Identifier.of(ColisaoCobblemon.MOD_ID, "surf");
        Registry.register(Registries.ITEM, surfId, SURF);

        // 1. Inicializa o EventHandler do Surf
        SurfHandler.register();

        // 2. Inicializa os eventos de clique (Cut e Rock Smash)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // Tenta o Cut primeiro
            ActionResult cutResult = CutObstacleBlock.handleInteract(player, world, hand, hitResult);
            if (cutResult == ActionResult.SUCCESS) return ActionResult.SUCCESS;

            // Se não foi Cut, tenta Rock Smash
            return RockSmashBlock.handleInteract(player, world, hand, hitResult);
        });
    }
}