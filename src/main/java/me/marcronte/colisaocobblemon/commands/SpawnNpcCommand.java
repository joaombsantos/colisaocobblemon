package me.marcronte.colisaocobblemon.commands;

import com.cobblemon.mod.common.api.npc.NPCClass;
import com.cobblemon.mod.common.api.npc.NPCClasses;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.marcronte.colisaocobblemon.config.NpcConfig;
import me.marcronte.colisaocobblemon.features.npcs.NpcData;
import me.marcronte.colisaocobblemon.util.SkinUrlHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SpawnNpcCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("colisao")
                .then(Commands.literal("spawnnpc")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("npc_id", StringArgumentType.string())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(NpcConfig.NPCS.keySet(), builder))
                                .executes(context -> {
                                    String npcId = StringArgumentType.getString(context, "npc_id");
                                    return spawnNPC(context.getSource(), npcId);
                                }))));
    }

    private static int spawnNPC(CommandSourceStack source, String npcId) {
        System.out.println("[DEBUG COLISAO] Comando spawnnpc executado para ID: " + npcId);
        NpcData data = NpcConfig.get(npcId);

        if (data == null) {
            source.sendFailure(Component.literal("NPC ID não encontrado no npcs.json: " + npcId));
            return 0;
        }

        try {
            ServerLevel level = source.getLevel();
            NPCEntity npc = new NPCEntity(level);

            NPCClass casualClass = NPCClasses.INSTANCE.getByName("casual");
            if (casualClass == null) casualClass = NPCClasses.INSTANCE.getByName("standard");
            if (casualClass == null) casualClass = NPCClasses.INSTANCE.random();
            npc.setNpc(casualClass);

            npc.setPos(source.getPosition());
            npc.setCustomName(Component.literal(data.npc_name));
            npc.setCustomNameVisible(true);
            npc.addTag("colisao_npc:" + npcId);

            npc.setInvulnerable(true);
            npc.setNoGravity(true);
            npc.setPersistenceRequired();

            if (npc.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                npc.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
            }

            npc.setMovable(false);

            boolean added = level.addFreshEntity(npc);

            if (!added) {
                return 0;
            }

            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                if (source.getServer() != null) {
                    source.getServer().execute(() -> {
                        if (npc.isRemoved()) return;

                        if (data.skin != null && !data.skin.isEmpty()) {
                            if (data.skin.startsWith("http")) {
                                SkinUrlHelper.applySkinFromUrlAsync(npc, data.skin);
                            } else {
                                SkinUrlHelper.applySkinFromNick(npc, data.skin);
                            }
                        } else {
                            SkinUrlHelper.applySkinFromNick(npc, "Steve");
                        }
                    });
                }
            }, 1, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }
}