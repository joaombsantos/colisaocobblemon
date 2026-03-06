package me.marcronte.colisaocobblemon.features.blocks;

import me.marcronte.colisaocobblemon.config.ProfessionsPerksConfig;
import me.marcronte.colisaocobblemon.data.ProfessionPlayerData;
import me.marcronte.colisaocobblemon.features.items.PokemonPickaxeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MiningBlock extends Block {

    public MiningBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;

        ServerPlayer serverPlayer = (ServerPlayer) player;

        if (!(stack.getItem() instanceof PokemonPickaxeItem)) {
            serverPlayer.sendSystemMessage(Component.translatable("message.colisao-cobblemon.must_have_pokemon_pickaxe"));
            return ItemInteractionResult.FAIL;
        }

        ProfessionPlayerData data = ProfessionPlayerData.get(serverPlayer.serverLevel());
        ProfessionPlayerData.PlayerProf prof = data.getPlayer(serverPlayer.getUUID());

        if (!"engenheiro".equalsIgnoreCase(prof.profession)) {
            return ItemInteractionResult.FAIL;
        }

        String blockKey = "mining_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();

        long currentTime = System.currentTimeMillis();
        long lastUsed = prof.cooldowns.getOrDefault(blockKey, 0L);
        long timePassed = currentTime - lastUsed;
        long cooldownTarget = 30 * 60 * 1000L;

        if (timePassed < cooldownTarget) {
            long timeLeftMinutes = (cooldownTarget - timePassed) / 60000L;
            serverPlayer.sendSystemMessage(Component.translatable("message.colisao-cobblemon.mining_block_cooldown", timeLeftMinutes));
            return ItemInteractionResult.FAIL;
        }

        Map<String, Map<String, Object>> allPerks = ProfessionsPerksConfig.INSTANCE.perks;
        if (allPerks.containsKey("engenheiro")) {
            Map<String, Object> engRanks = allPerks.get("engenheiro");

            if (engRanks.containsKey(prof.rank)) {
                Object rankLootObj = engRanks.get(prof.rank);

                if (rankLootObj instanceof Map<?, ?> rankLootMap) {
                    boolean gotAnything = false;

                    for (Map.Entry<?, ?> entry : rankLootMap.entrySet()) {
                        String itemId = entry.getKey().toString();
                        double chance = Double.parseDouble(entry.getValue().toString());
                        double roll = level.random.nextDouble() * 100.0;

                        if (roll <= chance) {
                            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
                            if (item != net.minecraft.world.item.Items.AIR) {
                                serverPlayer.getInventory().placeItemBackInInventory(new ItemStack(item, 1));
                                gotAnything = true;
                            }
                        }
                    }

                    /*if (gotAnything) {
                        serverPlayer.sendSystemMessage(Component.literal("§aVocê extraiu materiais preciosos com sucesso!"));
                    } else {
                        serverPlayer.sendSystemMessage(Component.literal("§7Você escavou, mas só encontrou pedras inúteis desta vez."));
                    }*/
                }
            } else {
                serverPlayer.sendSystemMessage(Component.literal("§cComo chegou aqui?"));
            }
        }

        prof.cooldowns.put(blockKey, currentTime);
        data.setDirty();

        return ItemInteractionResult.SUCCESS;
    }
}