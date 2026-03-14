package me.marcronte.colisaocobblemon.features.professions;

import me.marcronte.colisaocobblemon.config.ProfessionsCraftsConfig;
import me.marcronte.colisaocobblemon.data.ProfessionPlayerData;
import me.marcronte.colisaocobblemon.network.payloads.ProfessionCraftPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CraftingManager {

    public static void handleCraft(ServerPlayer player, ProfessionCraftPayloads.PerformCraftPayload payload) {
        ProfessionPlayerData data = ProfessionPlayerData.get(player.serverLevel());
        ProfessionPlayerData.PlayerProf prof = data.getPlayer(player.getUUID());

        ProfessionsCraftsConfig.RankData rankData = ProfessionsCraftsConfig.INSTANCE.crafts.get(prof.profession).get(payload.rank());
        if (rankData == null || payload.recipeIndex() >= rankData.craft_list.size()) return;

        ProfessionsCraftsConfig.CraftData recipe = rankData.craft_list.get(payload.recipeIndex());

        int playerRankVal = getRankValue(prof.rank);
        int recipeRankVal = getRankValue(payload.rank());

        if (playerRankVal < recipeRankVal) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.player_need_profession_xp"));
            return;
        }

        if (playerRankVal == recipeRankVal && prof.progress < recipe.exp_needed) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.player_need_profession_xp"));
            return;
        }

        for (String ing : recipe.ingredients) {
            String[] parts = ing.split("_", 2);
            int amountNeeded = Integer.parseInt(parts[0]);
            Item itemNeeded = BuiltInRegistries.ITEM.get(ResourceLocation.parse(parts[1]));

            if (countItems(player, itemNeeded) < amountNeeded) {
                player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.player_need_materials", amountNeeded, itemNeeded.getDescription().getString()));
                return;
            }
        }

        for (String ing : recipe.ingredients) {
            String[] parts = ing.split("_", 2);
            int amountToRemove = Integer.parseInt(parts[0]);
            Item itemToRemove = BuiltInRegistries.ITEM.get(ResourceLocation.parse(parts[1]));
            removeItems(player, itemToRemove, amountToRemove);
        }

        Item resultItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(recipe.result_item));
        player.getInventory().placeItemBackInInventory(new ItemStack(resultItem, 1));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

        if (prof.rank.equals(payload.rank())) {

            int limit = recipe.limit_exp > 0 ? recipe.limit_exp : 100;

            if (prof.progress < limit && prof.progress < 100) {
                prof.progress += recipe.exp_reward;

                if (prof.progress >= 100) {

                    if (prof.rank.equals("rank_a")) {
                        prof.progress = 100;
                        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.max_rank"));
                        ServerPlayNetworking.send(player, new ProfessionCraftPayloads.SyncExpPayload(prof.progress));
                    } else {
                        prof.rank = getNextRank(prof.rank);
                        prof.progress = 0;

                        player.serverLevel().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);

                        String rankUp = prof.rank;
                        String[] parts = rankUp.split("_");
                        String formated = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1) + " " + parts[1].toUpperCase();

                        player.sendSystemMessage(Component.translatable(("message.colisao-cobblemon.profession_rank_up"), formated));
                        player.closeContainer();
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.profession_earned_xp", recipe.exp_reward));
                    ServerPlayNetworking.send(player, new ProfessionCraftPayloads.SyncExpPayload(prof.progress));
                }
            }
        }
        data.setDirty();
    }

    private static int countItems(ServerPlayer player, Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static void removeItems(ServerPlayer player, Item item, int amount) {
        int leftToRemove = amount;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.is(item)) {
                if (stack.getCount() >= leftToRemove) {
                    stack.shrink(leftToRemove);
                    return;
                } else {
                    leftToRemove -= stack.getCount();
                    stack.setCount(0);
                }
            }
        }
    }

    private static String getNextRank(String current) {
        return switch (current) {
            case "rank_e" -> "rank_d";
            case "rank_d" -> "rank_c";
            case "rank_c" -> "rank_b";
            case "rank_b" -> "rank_a";
            default -> current;
        };
    }

    public static int getRankValue(String rank) {
        return switch (rank) {
            case "rank_e" -> 1;
            case "rank_d" -> 2;
            case "rank_c" -> 3;
            case "rank_b" -> 4;
            case "rank_a" -> 5;
            default -> 0;
        };
    }
}