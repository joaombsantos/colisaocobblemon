package me.marcronte.colisaocobblemon.features.npcs;

import java.util.List;

public class NpcData {
    public String npc_id;
    public String npc_name;
    public String skin; // Nick or URL
    public String type; // "quest", "dialog" or "questline"
    public List<String> commands; // Reward Commands

    // DIALOG NPCS
    public String dialog; // Npc dialog
    public String dialog_option; // Player's response
    public Boolean repeatable; // Is the command repeatable?

    // OBJECTIVES
    public String objective_type;   // "item", "dex_specific", "dex_count", "dex_gen", "defeat_pokemon", "defeat_type"
    public String objective_target; // ex: "minecraft:diamond", "pikachu", "1-151", "fire"

    // QUEST NPCS
    public String quest_item;    // Ex.: "minecraft:diamond"
    public int quest_amount;     // Ex.: 5

    public String quest_name;
    public String give_quest;      // NPC first interaction
    public String quest_delivery;  // NPC interation when fulfill requirements
    public String quest_complete;  // NPC interaction when quest is completed
    public String quest_in_progress; // NPC interaction while quest is in progress
    public String deliver_quest;    // Player response whe going to deliver the quest
    public String cooldown_message; // Cooldowns message
    public String quest_accept; // Player's response to accept quest

    public int quest_cooldown_hours; // 0 or null = once


    public String questline_finished;
    public List<QuestNode> quest_line;

    public static class QuestNode {
        public String quest_name;
        public String objective_type;
        public String objective_target;
        public String quest_item;
        public int quest_amount;
        public String give_quest;
        public String quest_delivery;
        public String quest_complete;
        public String quest_in_progress;
        public String deliver_quest;
        public String quest_accept;
        public List<String> commands;
    }
}