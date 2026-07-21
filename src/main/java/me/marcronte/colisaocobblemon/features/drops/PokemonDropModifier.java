package me.marcronte.colisaocobblemon.features.drops;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.api.drop.DropEntry;
import com.cobblemon.mod.common.api.drop.ItemDropEntry;
import me.marcronte.colisaocobblemon.features.items.PokemonDropItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PokemonDropModifier {

    private static final List<PokemonDropItem> CUSTOM_DROPS = new ArrayList<>();

    public static void registerCustomDrop(PokemonDropItem item) {
        CUSTOM_DROPS.add(item);
    }

    public static void register() {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> {
            if (success) applyDrops();
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> applyDrops());
    }

    private static void applyDrops() {
        for (Species species : PokemonSpecies.INSTANCE.getSpecies()) {
            for (PokemonDropItem dropItem : CUSTOM_DROPS) {

                if (hasType(species, dropItem.getTargetType())) {

                    ResourceLocation itemLocation = BuiltInRegistries.ITEM.getKey(dropItem);

                    boolean alreadyHas = false;
                    if (species.getDrops() != null && species.getDrops().getEntries() != null) {
                        for (DropEntry entry : species.getDrops().getEntries()) {

                            if (entry instanceof ItemDropEntry itemEntry) {
                                if (itemEntry.getItem().equals(itemLocation)) {
                                    alreadyHas = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!alreadyHas && species.getDrops() != null) {
                        ItemDropEntry newEntry = new ItemDropEntry();
                        newEntry.setItem(itemLocation);
                        newEntry.setQuantity(1);
                        newEntry.setPercentage(dropItem.getDropChance());

                        species.getDrops().getEntries().add(newEntry);
                    }
                }
            }
        }
    }

    private static boolean hasType(Species species, String typeName) {
        String primary = species.getPrimaryType().getName().toLowerCase();
        String secondary = species.getSecondaryType() != null ? species.getSecondaryType().getName().toLowerCase() : "";
        return primary.equals(typeName) || secondary.equals(typeName);
    }
}