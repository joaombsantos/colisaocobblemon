package me.marcronte.colisaocobblemon.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class EndBattleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("endbattle")
                .requires(source -> source.hasPermission(0))
                .executes(context -> {
                    if (!context.getSource().isPlayer()) {
                        context.getSource().sendFailure(Component.literal("§cApenas jogadores podem usar este comando."));
                        return 0;
                    }

                    ServerPlayer player = context.getSource().getPlayerOrException();
                    String playerName = player.getGameProfile().getName();

                    CommandSourceStack opSource = context.getSource().withPermission(4).withSuppressedOutput();

                    context.getSource().getServer().getCommands().performPrefixedCommand(
                            opSource,
                            "stopbattle " + playerName
                    );

                    player.sendSystemMessage(Component.literal("§aSaindo de batalha!"));

                    return 1;
                })
        );
    }
}