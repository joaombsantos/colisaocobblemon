package me.marcronte.colisaocobblemon.mixin.client;

import com.cobblemon.mod.common.api.pokedex.entry.PokedexEntry;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.client.gui.pokedex.widgets.EntriesScrollingWidget;
import me.marcronte.colisaocobblemon.client.ClientGenLimit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(EntriesScrollingWidget.class)
public class EntriesScrollingWidgetMixin {

    @Unique
    private static int getGen(int dex) {
        if (dex <= 151) return 1;
        if (dex <= 251) return 2;
        if (dex <= 386) return 3;
        if (dex <= 493) return 4;
        if (dex <= 649) return 5;
        if (dex <= 721) return 6;
        if (dex <= 809) return 7;
        if (dex <= 905) return 8;
        return 9;
    }

    @Inject(method = "createEntries", at = @At("HEAD"), remap = false)
    private void filterEntriesInPlace(Collection rawEntries, CallbackInfo ci) {
        int limit = ClientGenLimit.getMaxGeneration();

        if (limit >= 9) return;

        @SuppressWarnings("unchecked")
        Collection<PokedexEntry> entries = (Collection<PokedexEntry>) rawEntries;

        entries.removeIf(entry -> {
            var species = PokemonSpecies.getByIdentifier(entry.getSpeciesId());
            if (species != null) {
                return getGen(species.getNationalPokedexNumber()) > limit;
            }
            return false;
        });
    }
}