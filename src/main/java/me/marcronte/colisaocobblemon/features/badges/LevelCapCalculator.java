package me.marcronte.colisaocobblemon.features.badges;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import me.marcronte.colisaocobblemon.storage.BadgeDataManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class LevelCapCalculator {

    public static int getPlayerLevelCap(ServerPlayer player) {
        int maxLevel = LevelCapConfig.get().startLevel;

        MinecraftServer server = ColisaoCobblemon.getServer();
        if (server == null) return maxLevel;

        // 1. CHECK DATA MANAGER
        BadgeDataManager dataManager = BadgeDataManager.getServerState(server);

        // --- 1. SYNC (Inventory → Data) ---
        Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

            if (LevelCapConfig.get().badges.containsKey(itemId)) {
                if (!dataManager.hasBadge(player.getUUID(), itemId)) {
                    dataManager.addBadge(player.getUUID(), itemId);
                }
            }
        }

        // --- 2. CALCULATE ---
        Set<String> unlockedBadges = dataManager.getBadges(player.getUUID());

        for (String badgeId : unlockedBadges) {
            int capFromBadge = LevelCapConfig.get().getCapForBadge(badgeId);
            if (capFromBadge > maxLevel) {
                maxLevel = capFromBadge;
            }
        }

        return maxLevel;
    }
}