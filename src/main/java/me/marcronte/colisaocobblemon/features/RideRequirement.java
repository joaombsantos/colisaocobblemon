package me.marcronte.colisaocobblemon.features;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import kotlin.Unit; // Necessário para interagir com eventos Kotlin
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RideRequirement {

    public static void register() {
        CobblemonEvents.RIDE_EVENT_PRE.subscribe(Priority.NORMAL, event -> {

            Entity rider = event.getPlayer();

            Entity vehicle = event.getPokemon();

            if (rider instanceof ServerPlayer player && vehicle instanceof PokemonEntity) {

                if (!hasSaddle(player)) {

                    event.cancel();

                    player.displayClientMessage(
                            Component.translatable("message.colisao-cobblemon.need_saddle")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
            }

            return Unit.INSTANCE;
        });
    }

    private static boolean hasSaddle(ServerPlayer player) {
        if (player.getMainHandItem().is(Items.SADDLE) || player.getOffhandItem().is(Items.SADDLE)) return true;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.SADDLE)) return true;
        }
        return false;
    }
}