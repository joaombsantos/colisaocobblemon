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

    // KEYS
    public static final Item CARD_KEY = new Item(new Item.Properties().stacksTo(1));
    public static final Item COIN_CASE = new Item(new Item.Properties().stacksTo(1));
    public static final Item GOLDEN_TEETH = new Item(new Item.Properties().stacksTo(1));
    public static final Item LIFT_KEY = new Item(new Item.Properties().stacksTo(1));
    public static final Item OAK_PARCEL = new Item(new Item.Properties().stacksTo(1));
    public static final Item SECRET_KEY = new Item(new Item.Properties().stacksTo(1));
    public static final Item SS_TICKET = new Item(new Item.Properties().stacksTo(1));
    public static final Item TEA = new Item(new Item.Properties().stacksTo(1));


    public static BlockEntityType<FadeBlockEntity> FADE_BLOCK_ENTITY;

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "fade_block"), FADE_BLOCK);

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "fade_block"), new BlockItem(FADE_BLOCK, new Item.Properties()));

        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "card_key"), CARD_KEY);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "coin_case"), COIN_CASE);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "golden_teeth"), GOLDEN_TEETH);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "lift_key"), LIFT_KEY);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "oak_parcel"), OAK_PARCEL);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "secret_key"), SECRET_KEY);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "ss_ticket"), SS_TICKET);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "tea"), TEA);

        FADE_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "fade_block_entity"),
                BlockEntityType.Builder.of(FadeBlockEntity::new, FADE_BLOCK).build(null)
        );

        FadeBlock.ENTITY_TYPE = FADE_BLOCK_ENTITY;

        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.COLISAO_GROUP_KEY).register(entries -> {
            entries.accept(FADE_BLOCK);
            entries.accept(CARD_KEY);
            entries.accept(COIN_CASE);
            entries.accept(GOLDEN_TEETH);
            entries.accept(LIFT_KEY);
            entries.accept(OAK_PARCEL);
            entries.accept(SECRET_KEY);
            entries.accept(SS_TICKET);
            entries.accept(TEA);
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