package me.marcronte.colisaocobblemon.features.items.backpack;

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

public class BackpackItem extends Item {

    private final int slots;

    public BackpackItem(int slots, Properties properties) {
        super(properties.stacksTo(1));
        this.slots = slots;
    }

    public int getSlots() {
        return slots;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ExtendedScreenHandlerFactory<BackpackMenu.Payload>() {
                @Override
                public BackpackMenu.Payload getScreenOpeningData(ServerPlayer player) {
                    return new BackpackMenu.Payload(hand == InteractionHand.MAIN_HAND, slots);
                }

                @Override
                public @NotNull Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                    return new BackpackMenu(syncId, inventory, new BackpackMenu.Payload(hand == InteractionHand.MAIN_HAND, slots));
                }
            });
        }

        return InteractionResultHolder.success(stack);
    }
}