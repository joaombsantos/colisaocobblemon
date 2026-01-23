package me.marcronte.colisaocobblemon.features.npcs;

import java.util.List;

public class NpcData {
    public String npc_id;
    public String npc_name;
    public String skin; // Nick or URL
    public String type; // "quest" or "dialog"
    public List<String> commands; // Reward Commands

    // DIALOG NPCS
    public String dialog; // Npc dialog
    public String dialog_option; // Player's response
    public Boolean repeatable; // Is the command repeatable?

    // QUEST NPCS
    public String quest_item;    // Ex.: "minecraft:diamond"
    public int quest_amount;     // Ex.: 5

    public String give_quest;      // NPC first interaction
    public String quest_delivery;  // NPC interation when fulfill requirements
    public String quest_complete;  // NPC interaction when quest is completed
    public String quest_in_progress; // NPC interaction while quest is in progress
    public String deliver_quest;    // Player response whe going to deliver the quest
    public String cooldown_message; // Cooldowns message
    public String quest_accept; // Player's response to accept quest

    public int quest_cooldown_hours; // 0 or null = once
}