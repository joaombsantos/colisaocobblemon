package me.marcronte.colisaocobblemon.features.clans;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public class ClanScheduler {

    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= 100) {
                tickCounter = 0;

                ServerLevel overworld = server.overworld();
                ClanSavedData data = ClanSavedData.get(overworld);
                long now = System.currentTimeMillis();

                if (now >= data.getNextResetTimestamp()) {

                    for (Clan clan : data.getAllClans().values()) {
                        clan.applyDecay();
                    }

                    long nextReset = calculateNextReset();
                    data.setNextResetTimestamp(nextReset);
                    data.setDirty();

                    Component alert = Component.literal("§6§l[CLAN] §eAs missões semanais foram reiniciadas e o decaimento de XP foi aplicado!");
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        player.sendSystemMessage(alert);
                    }
                }
            }
        });
    }

    public static long calculateNextReset() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime nextTuesday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
                .withHour(12).withMinute(0).withSecond(0).withNano(0);

        if (now.isAfter(nextTuesday)) {
            nextTuesday = nextTuesday.plusWeeks(1);
        }

        return nextTuesday.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static String getTimeRemaining(long nextResetMs) {
        long remainingMs = nextResetMs - System.currentTimeMillis();
        if (remainingMs <= 0) return "0 dias 00:00:00";

        long days = remainingMs / (1000 * 60 * 60 * 24);
        long hours = (remainingMs / (1000 * 60 * 60)) % 24;
        long minutes = (remainingMs / (1000 * 60)) % 60;
        long seconds = (remainingMs / (1000 * 10)) % 60;

        return String.format("%d dias %02d:%02d:%02d", days, hours, minutes, seconds);
    }
}