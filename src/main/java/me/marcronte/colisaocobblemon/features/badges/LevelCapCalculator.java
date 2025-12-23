package me.marcronte.colisaocobblemon.features.badges;

import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

public class LevelCapCalculator {

    public static int getPlayerLevelCap(ServerPlayerEntity player) {
        // 1. Começa com o nível inicial definido no JSON (ex: 10)
        int maxLevel = LevelCapConfig.get().startLevel;

        PlayerInventory inv = player.getInventory();

        // 2. Vasculha o inventário principal do jogador
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;

            // Pega o ID do item (ex: "colisao-cobblemon:kanto_boulder_badge")
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();

            // Pergunta para a Config: "Esse item dá level cap?"
            int capFromItem = LevelCapConfig.get().getCapForBadge(itemId);

            // Se der, e for maior que o atual, atualizamos o maxLevel
            if (capFromItem > maxLevel) {
                maxLevel = capFromItem;
            }
        }

        // TODO: Futuramente, aqui vamos checar dentro da Badge Case (NBT)

        return maxLevel;
    }
}