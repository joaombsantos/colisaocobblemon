package me.marcronte.colisaocobblemon.features.badgecase;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class BadgeCaseItem extends Item {

    public BadgeCaseItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ExtendedScreenHandlerFactory<BadgeCaseMenu.Payload>() {
                @Override
                public BadgeCaseMenu.Payload getScreenOpeningData(ServerPlayer player) {
                    return new BadgeCaseMenu.Payload(hand == InteractionHand.MAIN_HAND);
                }

                @Override
                public @NotNull Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                    return new BadgeCaseMenu(syncId, inventory, new BadgeCaseMenu.Payload(hand == InteractionHand.MAIN_HAND));
                }
            });
        }

        return InteractionResultHolder.success(stack);
    }
}