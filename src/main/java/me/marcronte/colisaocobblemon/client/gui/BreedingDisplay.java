package me.marcronte.colisaocobblemon.client.gui;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.network.BreedingNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class BreedingDisplay {

    private static boolean renderFailureLogged = false;

    private BreedingDisplay() {}

    @Nullable
    public static Pokemon fromData(@Nullable CompoundTag data, @Nullable String speciesFallback) {
        if (data != null && !data.isEmpty()) {
            try {
                Level level = Minecraft.getInstance().level;
                if (level != null) {
                    Pokemon pokemon;
                    if (!data.contains("UUID") && data.contains("AspectsBackup")) {
                        pokemon = BreedingNetwork.reconstructPokemon(data, level.registryAccess());
                    } else {
                        pokemon = Pokemon.Companion.loadFromNBT(level.registryAccess(), data);
                    }
                    if (pokemon != null) return applyDisplayForm(pokemon, data);
                }
            } catch (Exception e) {
                ColisaoCobblemon.LOGGER.warn("[Breeding] Não foi possível reconstruir o Pokémon no cliente, usando fallback pela espécie.", e);
            }
        }

        String species = speciesFallback;
        if ((species == null || species.isBlank()) && data != null) {
            species = data.getString("UI_Species");
        }
        return applyDisplayForm(dummyFromSpecies(species), data);
    }

    private static final String[] REGIONAL_ASPECTS = {"alolan", "galarian", "hisuian", "paldean"};

    @Nullable
    public static Pokemon applyDisplayForm(@Nullable Pokemon pokemon, @Nullable CompoundTag data) {
        if (pokemon == null) return null;

        try {
            String formName = pokemon.getForm().getName();
            String aspect = matchRegionalAspect(formName);
            if (aspect == null && data != null) aspect = findRegionalAspect(data);

            if (aspect == null) return pokemon;

            if (!pokemon.getAspects().contains(aspect)) {
                try {
                    pokemon.getAspects().add(aspect);
                } catch (UnsupportedOperationException ex) {
                    ColisaoCobblemon.LOGGER.warn("[Display] getAspects() é imutável; aplicando '{}' via PokemonProperties.", aspect);
                    PokemonProperties.Companion.parse(aspect).apply(pokemon);
                }
            }
            return pokemon;
        } catch (Exception e) {
            ColisaoCobblemon.LOGGER.warn("[Breeding] Não foi possível aplicar a forma ao Pokémon exibido, mantendo a forma atual.", e);
            return pokemon;
        }
    }

    @Nullable
    private static String matchRegionalAspect(@Nullable String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase();
        return switch (v) {
            case "alola", "alolan" -> "alolan";
            case "galar", "galarian" -> "galarian";
            case "hisui", "hisuian" -> "hisuian";
            case "paldea", "paldean" -> "paldean";
            default -> null;
        };
    }

    @Nullable
    private static String findRegionalAspect(@Nullable Tag tag) {
        if (tag == null) return null;

        if (tag instanceof StringTag stringTag) {
            return matchRegionalAspect(stringTag.getAsString());
        }
        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                String found = findRegionalAspect(compound.get(key));
                if (found != null) return found;
            }
        } else if (tag instanceof ListTag list) {
            for (Tag element : list) {
                String found = findRegionalAspect(element);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Nullable
    public static Pokemon dummyFromSpecies(@Nullable String speciesStr) {
        if (speciesStr == null || speciesStr.isBlank()) return null;

        try {
            String cleanName = speciesStr.toLowerCase().replace("cobblemon:", "").trim();
            Species species = PokemonSpecies.getByName(cleanName);
            if (species == null) return null;

            Pokemon dummy = new Pokemon();
            dummy.setSpecies(species);
            return dummy;
        } catch (Exception e) {
            ColisaoCobblemon.LOGGER.warn("[Breeding] Falha ao criar Pokémon de exibição para '{}'.", speciesStr, e);
            return null;
        }
    }

    public static ItemStack spriteStack(@Nullable Pokemon pokemon) {
        if (pokemon == null) return ItemStack.EMPTY;
        return PokemonItem.from(Objects.requireNonNull(applyDisplayForm(pokemon, null)), 1);
    }

    private static final int CASTRATED_COLOR = 0xFF5555;

    private static Set<UUID> castratedParty = new HashSet<>();

    public static void setCastratedParty(Collection<UUID> uuids) {
        castratedParty = new HashSet<>(uuids);
    }

    public static boolean isCastrated(@Nullable Pokemon pokemon) {
        return pokemon != null && castratedParty.contains(pokemon.getUuid());
    }

    public static void drawCastratedTag(GuiGraphics graphics, Font font, int centerX, int y) {
        graphics.drawCenteredString(font, Component.translatable("gui.colisao-cobblemon.castrated"), centerX, y, CASTRATED_COLOR);
    }

    public static void drawCastratedTagSmall(GuiGraphics graphics, Font font, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(0.7f, 0.7f, 1f);
        graphics.drawString(font, Component.translatable("gui.colisao-cobblemon.castrated"), 0, 0, CASTRATED_COLOR);
        graphics.pose().popPose();
    }

    public static int partyEntryColor(boolean castrated, boolean hovered) {
        if (castrated) return hovered ? 0xC0CC4444 : 0xC0661111;
        return hovered ? 0xC0FFFFFF : 0xC0000000;
    }

    public static void logRenderFailure(Exception e) {
        if (renderFailureLogged) return;
        renderFailureLogged = true;
        ColisaoCobblemon.LOGGER.warn("[Breeding] Falha ao desenhar o sprite do Pokémon na tela de breeding.", e);
    }
}