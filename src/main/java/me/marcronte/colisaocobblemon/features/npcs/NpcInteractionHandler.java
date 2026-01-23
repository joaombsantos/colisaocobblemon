package me.marcronte.colisaocobblemon.features.npcs;

import com.cobblemon.mod.common.api.dialogue.Dialogue;
import com.cobblemon.mod.common.api.dialogue.DialogueAction;
import com.cobblemon.mod.common.api.dialogue.DialogueManager;
import com.cobblemon.mod.common.api.dialogue.DialoguePage;
import com.cobblemon.mod.common.api.dialogue.WrappedDialogueText;
import com.cobblemon.mod.common.api.dialogue.input.DialogueOption;
import com.cobblemon.mod.common.api.dialogue.input.DialogueOptionSetInput;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import me.marcronte.colisaocobblemon.config.NpcConfig;
import me.marcronte.colisaocobblemon.data.QuestProgressData;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NpcInteractionHandler {

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
            if (world.isClientSide) return InteractionResult.PASS;

            String npcId = getNpcId(entity);
            if (npcId == null) return InteractionResult.PASS;

            NpcData data = NpcConfig.get(npcId);
            if (data == null) return InteractionResult.FAIL;

            NPCEntity npcEntity = (entity instanceof NPCEntity) ? (NPCEntity) entity : null;

            handleInteraction((ServerPlayer) player, data, (ServerLevel) world, npcEntity);

            return InteractionResult.SUCCESS;
        });
    }

    private static String getNpcId(Entity entity) {
        for (String tag : entity.getTags()) {
            if (tag.startsWith("colisao_npc:")) {
                return tag.replace("colisao_npc:", "");
            }
        }
        return null;
    }

    private static void handleInteraction(ServerPlayer player, NpcData data, ServerLevel level, NPCEntity npcEntity) {
        if ("quest".equalsIgnoreCase(data.type)) {
            handleQuestUI(player, data, level, npcEntity);
        } else {
            handleDialogUI(player, data, level, npcEntity);
        }
    }

    private static void handleDialogUI(ServerPlayer player, NpcData data, ServerLevel level, NPCEntity npcEntity) {
        String buttonLabel = (data.dialog_option != null && !data.dialog_option.isEmpty()) ? data.dialog_option : "Fechar";

        boolean isRepeatable = data.repeatable == null || data.repeatable;

        QuestProgressData progress = QuestProgressData.get(level);

        boolean alreadyInteracted = !progress.canDoQuest(player.getUUID(), data.npc_id, 0);

        DialogueAction action;

        if (!isRepeatable && alreadyInteracted) {
            action = (dialogue, value) -> {
                dialogue.close();
            };
        } else {
            action = (dialogue, value) -> {
                runCommands(player, data);

                if (!isRepeatable) {
                    progress.completeQuest(player.getUUID(), data.npc_id);
                }

                dialogue.close();
            };
        }

        openCobblemonUI(player, npcEntity, data.npc_name, data.dialog, buttonLabel, action);
    }

    private static void handleQuestUI(ServerPlayer player, NpcData data, ServerLevel level, NPCEntity npcEntity) {
        QuestProgressData progress = QuestProgressData.get(level);

        if (!progress.canDoQuest(player.getUUID(), data.npc_id, data.quest_cooldown_hours)) {
            String msg = (data.quest_cooldown_hours > 0)
                    ? (data.cooldown_message != null ? data.cooldown_message : "Volte mais tarde.")
                    : data.quest_complete;

            openCobblemonUI(player, npcEntity, data.npc_name, msg, "Fechar", (d, v) -> d.close());
            return;
        }

        Item requiredItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(data.quest_item));
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == requiredItem) count += stack.getCount();
        }

        if (count >= data.quest_amount) {
            String buttonLabel = (data.deliver_quest != null && !data.deliver_quest.isEmpty()) ? data.deliver_quest : "Entregar";

            DialogueAction completeAction = (dialogue, value) -> {
                int currentCount = 0;
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.getItem() == requiredItem) currentCount += stack.getCount();
                }

                if (currentCount >= data.quest_amount) {
                    int remaining = data.quest_amount;
                    for (int i = 0; i < player.getInventory().items.size(); i++) {
                        ItemStack stack = player.getInventory().items.get(i);
                        if (stack.getItem() == requiredItem) {
                            int take = Math.min(stack.getCount(), remaining);
                            stack.shrink(take);
                            remaining -= take;
                            if (remaining <= 0) break;
                        }
                    }
                    progress.completeQuest(player.getUUID(), data.npc_id);
                    runCommands(player, data);
                }
                dialogue.close();
            };

            openCobblemonUI(player, npcEntity, data.npc_name, data.quest_delivery, buttonLabel, completeAction);

        } else {

            boolean isInProgress = progress.hasStartedQuest(player.getUUID(), data.npc_id);

            if (isInProgress) {
                String buttonLabel = "Vou buscar";
                String text = data.quest_in_progress != null ? data.quest_in_progress : "Ainda não trouxe o que pedi?";
                text += "\n\n(Falta: " + data.quest_amount + "x " + requiredItem.getDescription().getString() + ")";

                openCobblemonUI(player, npcEntity, data.npc_name, text, buttonLabel, (d, v) -> d.close());

            } else {
                String buttonLabel = (data.quest_accept != null && !data.quest_accept.isEmpty()) ? data.quest_accept : "Aceitar";

                DialogueAction acceptAction = (dialogue, value) -> {
                    progress.startQuest(player.getUUID(), data.npc_id);
                    dialogue.close();
                };

                String text = data.give_quest;
                text += "\n\n(Requer: " + data.quest_amount + "x " + requiredItem.getDescription().getString() + ")";

                openCobblemonUI(player, npcEntity, data.npc_name, text, buttonLabel, acceptAction);
            }
        }
    }

    private static void openCobblemonUI(ServerPlayer player, NPCEntity npc, String title, String text, String btnLabel, DialogueAction btnAction) {
        if (text == null) text = "...";

        DialogueOption option = new DialogueOption(
                new WrappedDialogueText(Component.literal(btnLabel)),
                "primary_option",
                btnAction,
                dialogue -> true,
                dialogue -> true
        );

        DialogueOptionSetInput input = new DialogueOptionSetInput(
                new ArrayList<>(List.of(option)),
                null,
                false
        );

        List<MutableComponent> lines = new ArrayList<>();
        for (String line : text.split("\n")) {
            lines.add(Component.literal(line));
        }

        DialoguePage page = DialoguePage.Companion.of(
                "main_page",
                title,
                lines,
                null,
                input
        );

        Dialogue dialogue = new Dialogue(
                List.of(page),
                Dialogue.Companion.getDEFAULT_BACKGROUND(),
                (active, value) -> active.close(),
                Collections.emptyMap(),
                (active, value) -> { }
        );

        DialogueManager.INSTANCE.startDialogue(player, npc, dialogue);
    }

    private static void runCommands(ServerPlayer player, NpcData data) {
        if (data.commands == null) return;
        for (String cmd : data.commands) {
            String parsedCmd = cmd.replace("{player}", player.getGameProfile().getName());
            Objects.requireNonNull(player.getServer()).getCommands().performPrefixedCommand(
                    player.getServer().createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                    parsedCmd
            );
        }
    }
}