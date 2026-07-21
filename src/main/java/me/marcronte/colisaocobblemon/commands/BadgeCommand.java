package me.marcronte.colisaocobblemon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.marcronte.colisaocobblemon.features.badgecase.BadgeCaseMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class BadgeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("badges")
                .requires(CommandSourceStack::isPlayer)
                .executes(BadgeCommand::execute));
    }

    public static int execute(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            player.openMenu(new ExtendedScreenHandlerFactory<BadgeCaseMenu.Payload>() {
                @Override
                public BadgeCaseMenu.Payload getScreenOpeningData(ServerPlayer p) {
                    return new BadgeCaseMenu.Payload(true, true); // (MainHand, isVirtual)
                }

                @Override
                public @NotNull Component getDisplayName() {
                    return Component.literal("Estojo de Insígnias");
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player p) {
                    return new BadgeCaseMenu(syncId, inventory, new BadgeCaseMenu.Payload(true, true));
                }
            });

        } catch (Exception e) {
            context.getSource().sendFailure(Component.translatable("message.colisao-cobblemon.error_badge_command"));
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
}
