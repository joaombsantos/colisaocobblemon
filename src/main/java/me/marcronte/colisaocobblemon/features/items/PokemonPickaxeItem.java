package me.marcronte.colisaocobblemon.features.items;

import me.marcronte.colisaocobblemon.data.ProfessionPlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

public class PokemonPickaxeItem extends PickaxeItem {

    public PokemonPickaxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {

            boolean isHolding = isSelected || player.getOffhandItem() == stack;

            if (isHolding) {
                ProfessionPlayerData data = ProfessionPlayerData.get(player.serverLevel());
                ProfessionPlayerData.PlayerProf prof = data.getPlayer(player.getUUID());

                if (!"engenheiro".equalsIgnoreCase(prof.profession)) {
                    player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.pokemon_pickaxe_must_be_engineer"));

                    player.drop(stack.copy(), false, true);
                    stack.shrink(stack.getCount());
                }
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
}