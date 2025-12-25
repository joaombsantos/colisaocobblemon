package me.marcronte.colisaocobblemon.features.badges;

import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import net.minecraft.core.registries.BuiltInRegistries; // Mudou de Registries
import net.minecraft.server.level.ServerPlayer;         // Mudou de ServerPlayerEntity
import net.minecraft.world.entity.player.Inventory;     // Mudou de PlayerInventory
import net.minecraft.world.item.ItemStack;              // Pacote diferente

public class LevelCapCalculator {

    public static int getPlayerLevelCap(ServerPlayer player) {
        int maxLevel = LevelCapConfig.get().startLevel;

        Inventory inv = player.getInventory();

        // 2. Vasculha o inventário principal do jogador
        // inv.getContainerSize() substitui inv.size()
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i); // getStack virou getItem
            if (stack.isEmpty()) continue;

            // Pega o ID do item. getKey() substitui getId()
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

            // Pergunta para a Config: "Esse item dá level cap?"
            int capFromItem = LevelCapConfig.get().getCapForBadge(itemId);

            // Se der, e for maior que o atual, atualizamos o maxLevel
            if (capFromItem > maxLevel) {
                maxLevel = capFromItem;
            }
        }

        return maxLevel;
    }
}