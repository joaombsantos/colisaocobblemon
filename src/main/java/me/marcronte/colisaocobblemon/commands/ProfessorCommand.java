package me.marcronte.colisaocobblemon.commands;

import com.mojang.brigadier.CommandDispatcher;
import me.marcronte.colisaocobblemon.data.ProfessionPlayerData;
import me.marcronte.colisaocobblemon.features.professions.PlantationManager; // IMPORTANTE: Importe o Manager!
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ProfessorCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("plantacao")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ProfessionPlayerData data = ProfessionPlayerData.get(player.serverLevel());
                    ProfessionPlayerData.PlayerProf prof = data.getPlayer(player.getUUID());

                    if (!"professor".equalsIgnoreCase(prof.profession)) {
                        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.must_be_professor"));
                        return 0;
                    }

                    player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.opening_plantation"));

                    PlantationManager.open(player, prof);

                    return 1;
                })
        );
    }
}