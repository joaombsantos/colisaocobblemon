package me.marcronte.colisaocobblemon.features.eventblock;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.ModItemGroup;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeBlockData;
import me.marcronte.colisaocobblemon.features.fadeblock.FadeNetwork;
import me.marcronte.colisaocobblemon.network.EventNetwork;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public class EventBlockRegistry {

    public static final PokemonBlockade EVENT_BLOCK = new PokemonBlockade(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .isValidSpawn((state, world, pos, type) -> false)
                    .isSuffocating((state, world, pos) -> false)
                    .isViewBlocking((state, world, pos) -> false)
    );

    public static final Item POKE_FLUTE = new Item(new Item.Properties().stacksTo(1));
    public static final Item SILPH_SCOPE = new Item(new Item.Properties().stacksTo(1));

    public static BlockEntityType<PokemonBlockadeEntity> POKEMON_BLOCKADE_ENTITY;

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "event_block"), EVENT_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "event_block"), new BlockItem(EVENT_BLOCK, new Item.Properties()));

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "poke_flute"), POKE_FLUTE);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "silph_scope"), SILPH_SCOPE);

        POKEMON_BLOCKADE_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "pokemon_blockade_entity"),
                BlockEntityType.Builder.of(PokemonBlockadeEntity::new, EVENT_BLOCK).build(null)
        );
        PokemonBlockade.ENTITY_TYPE = POKEMON_BLOCKADE_ENTITY;

        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.COLISAO_GROUP_KEY).register(entries -> {
            entries.accept(EVENT_BLOCK);
            entries.accept(POKE_FLUTE);
            entries.accept(SILPH_SCOPE);
        });

        EventBattleHandler.register();
        EventNetwork.registerCommon();

        BlockadeInteractionHandler.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            ServerLevel level = (ServerLevel) player.level();

            FadeBlockData data = FadeBlockData.get(level);
            List<BlockPos> unlocked = data.getUnlockedPos(player.getUUID());

            if (!unlocked.isEmpty()) {
                FadeNetwork.sendSync(player, unlocked);

                for (BlockPos pos : unlocked) {
                    if (level.getBlockState(pos).is(EVENT_BLOCK) ||
                            level.getBlockState(pos).getBlock().getDescriptionId().contains("fade")) {

                        player.connection.send(new ClientboundBlockUpdatePacket(pos, Blocks.AIR.defaultBlockState()));
                    }
                }
            }
        });
    }
}