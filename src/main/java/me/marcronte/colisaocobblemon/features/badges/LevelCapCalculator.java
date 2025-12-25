package me.marcronte.colisaocobblemon.features.badges;

import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class LevelCapCalculator {

    public static int getPlayerLevelCap(ServerPlayer player) {
        int maxLevel = LevelCapConfig.get().startLevel;

        Inventory inv = player.getInventory();

        // Search on player inventory
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

            int capFromItem = LevelCapConfig.get().getCapForBadge(itemId);

            if (capFromItem > maxLevel) {
                maxLevel = capFromItem;
            }
        }

        return maxLevel;
    }
}