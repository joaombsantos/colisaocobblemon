package me.marcronte.colisaocobblemon.features;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokeball.PokeBallCaptureCalculatedEvent;
import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext;
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokeball.PokeBall;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CaptureRestrictionHandler {

    public static void register() {
        CobblemonEvents.POKE_BALL_CAPTURE_CALCULATED.subscribe(com.cobblemon.mod.common.api.Priority.HIGH, event -> {
            handleCapture(event);
            return Unit.INSTANCE;
        });
    }

    private static void handleCapture(PokeBallCaptureCalculatedEvent event) {

        PokemonEntity pokemon = event.getPokemonEntity();
        EmptyPokeBallEntity ballEntity = event.getPokeBallEntity();

        String restriction = null;
        for (String tag : pokemon.getTags()) {
            if (tag.startsWith("ball_restriction:")) {
                restriction = tag.replace("ball_restriction:", "").trim();
                break;
            }
        }

        if (restriction == null) return;

        PokeBall internalBall = ballEntity.getPokeBall();

        ResourceLocation ballId = internalBall.getName();

        String usedBall = ballId.getPath().toLowerCase();
        String cleanRestriction = restriction.replace("cobblemon:", "").toLowerCase();


        if (!usedBall.equals(cleanRestriction)) {

            CaptureContext failContext = new CaptureContext(0, false, false);
            event.setCaptureResult(failContext);

            if (event.getThrower() instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.restricted_capture", formatBallName(restriction)));
            }
        }
    }

    private static String formatBallName(String id) {
        String[] parts = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}