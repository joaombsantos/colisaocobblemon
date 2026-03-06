package me.marcronte.colisaocobblemon.features.professions;

import me.marcronte.colisaocobblemon.config.ProfessionsPerksConfig;
import me.marcronte.colisaocobblemon.data.ProfessionPlayerData;
import me.marcronte.colisaocobblemon.network.payloads.PlantationPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlantationManager {

    private static final long GROW_TIME_MS = 30 * 60 * 1000L;

    public static void open(ServerPlayer player, ProfessionPlayerData.PlayerProf prof) {
        int unlockedSlots = getUnlockedSlots(prof.rank);
        List<PlantationPayloads.SyncPayload.SlotData> slots = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            String berry = prof.plantedBerries.getOrDefault(i, "empty");
            long time = prof.plantTimes.getOrDefault(i, 0L);
            slots.add(new PlantationPayloads.SyncPayload.SlotData(i, berry, time));
        }

        List<String> availableBerries = getAvailableBerries(prof.rank);

        ServerPlayNetworking.send(player, new PlantationPayloads.SyncPayload(unlockedSlots, slots, availableBerries));
    }

    public static void handleAction(ServerPlayer player, PlantationPayloads.ActionPayload payload) {
        ProfessionPlayerData data = ProfessionPlayerData.get(player.serverLevel());
        ProfessionPlayerData.PlayerProf prof = data.getPlayer(player.getUUID());

        int slot = payload.slotIndex();
        if (slot < 0 || slot >= getUnlockedSlots(prof.rank)) return;

        if ("plant".equals(payload.action())) {
            if (getAvailableBerries(prof.rank).contains(payload.berryId())) {
                prof.plantedBerries.put(slot, payload.berryId());
                prof.plantTimes.put(slot, System.currentTimeMillis());
                data.setDirty();
                open(player, prof);
            }
        }
        else if ("harvest".equals(payload.action())) {
            String berryId = prof.plantedBerries.get(slot);
            long plantTime = prof.plantTimes.getOrDefault(slot, 0L);

            if (berryId != null && !"empty".equals(berryId) && (System.currentTimeMillis() - plantTime >= GROW_TIME_MS)) {

                int min = 1, max = 3;
                Map<String, Object> professorPerks = ProfessionsPerksConfig.INSTANCE.perks.get("professor");
                if (professorPerks != null && professorPerks.containsKey(prof.rank)) {
                    Map<?, ?> rankData = (Map<?, ?>) professorPerks.get(prof.rank);
                    if (rankData.containsKey("min_amount")) min = ((Number) rankData.get("min_amount")).intValue();
                    if (rankData.containsKey("max_amount")) max = ((Number) rankData.get("max_amount")).intValue();
                }

                int amount = min + player.serverLevel().random.nextInt((max - min) + 1);

                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(berryId));
                player.getInventory().placeItemBackInInventory(new ItemStack(item, amount));
                player.sendSystemMessage(Component.literal("message.colisao-cobblemon.professor_harvested"));

                prof.plantedBerries.put(slot, "empty");
                prof.plantTimes.remove(slot);
                data.setDirty();

                open(player, prof);
            }
        }
    }

    private static int getUnlockedSlots(String rank) {
        return switch (rank.toLowerCase()) {
            case "rank_e" -> 1;
            case "rank_d" -> 2;
            case "rank_c" -> 3;
            case "rank_b" -> 4;
            case "rank_a" -> 5;
            default -> 0;
        };
    }

    private static List<String> getAvailableBerries(String rank) {
        List<String> berries = new ArrayList<>();
        String[] ranks = {"rank_e", "rank_d", "rank_c", "rank_b", "rank_a"};

        Map<String, Object> professorPerks = ProfessionsPerksConfig.INSTANCE.perks.get("professor");
        if (professorPerks == null) return berries;

        for (String r : ranks) {
            if (professorPerks.containsKey(r)) {
                Map<?, ?> rankData = (Map<?, ?>) professorPerks.get(r);
                if (rankData.containsKey("berry_list")) {
                    List<?> list = (List<?>) rankData.get("berry_list");
                    for (Object obj : list) berries.add(obj.toString());
                }
            }
            if (r.equalsIgnoreCase(rank)) break;
        }
        return berries;
    }
}