package me.marcronte.colisaocobblemon.features.genlimit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.marcronte.colisaocobblemon.config.GenerationConfig;
import me.marcronte.colisaocobblemon.network.GenLimitNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class GenerationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("colisao")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("genlimit")
                        .then(Commands.argument("generation", IntegerArgumentType.integer(1, 9))
                                .executes(context -> {
                                    int newGen = IntegerArgumentType.getInteger(context, "generation");

                                    GenerationConfig config = GenerationConfig.get();
                                    config.max_generation = newGen;
                                    GenerationConfig.save();
                                    GenLimitNetwork.sendToAll(context.getSource().getServer());
                                    context.getSource().sendSuccess(() ->
                                            Component.translatable("message.colisao-cobblemon.generation_command", newGen), true);

                                    return 1;
                                })
                        )
                )
        );
    }
}