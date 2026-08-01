package me.marcronte.colisaocobblemon.client.gui;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.ModItems;
import me.marcronte.colisaocobblemon.network.payloads.LensRequestPayload;
import me.marcronte.colisaocobblemon.network.payloads.LensResponsePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PokeLensOverlay {
    private static int lastRequestedEntityId = -1;
    public static LensResponsePayload cachedData = null;

    public static void register() {

        ClientPlayNetworking.registerGlobalReceiver(LensResponsePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                cachedData = payload;
            });
        });

        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player == null || !player.isUsingItem()) {
                lastRequestedEntityId = -1;
                return;
            }

            if (!player.getUseItem().is(ModItems.POKE_LENS)) return;

            Entity target = mc.crosshairPickEntity;

            if (target instanceof PokemonEntity pokemonEntity) {
                int currentTargetId = target.getId();

                if (currentTargetId != lastRequestedEntityId) {
                    lastRequestedEntityId = currentTargetId;
                    cachedData = null;
                    ClientPlayNetworking.send(new LensRequestPayload(currentTargetId));
                }

                if (cachedData != null && cachedData.entityId() == currentTargetId) {

                    Pokemon pokemon = pokemonEntity.getPokemon();
                    Font font = mc.font;
                    int startX = (mc.getWindow().getGuiScaledWidth() / 2) + 20;
                    int startY = (mc.getWindow().getGuiScaledHeight() / 2) - 30;

                    String name = "§l" + pokemon.getSpecies().getName();
                    String genderRaw = pokemon.getGender().name();
                    String gender = "Gênero: " + (genderRaw.equals("MALE") ? "§bMacho" : genderRaw.equals("FEMALE") ? "§dFêmea" : "§7Sem Gênero");

                    String nature = "Natureza: " + cachedData.nature().substring(0, 1).toUpperCase() + cachedData.nature().substring(1);
                    String ability = "Habilidade: " + cachedData.ability().substring(0, 1).toUpperCase() + cachedData.ability().substring(1);

                    String ivs = String.format("IVs: %d/%d/%d/%d/%d/%d",
                            cachedData.hp(), cachedData.atk(), cachedData.def(),
                            cachedData.spa(), cachedData.spd(), cachedData.spe());

                    graphics.drawString(font, name, startX, startY, 0xFFD700, true);
                    graphics.drawString(font, gender, startX, startY + 12, 0xFFFFFF, true);
                    graphics.drawString(font, nature, startX, startY + 24, 0xFFFFFF, true);
                    graphics.drawString(font, ability, startX, startY + 36, 0xFFFFFF, true);
                    graphics.drawString(font, ivs, startX, startY + 48, 0x55FF55, true);
                }
            } else {
                lastRequestedEntityId = -1;
            }
        });
    }
}