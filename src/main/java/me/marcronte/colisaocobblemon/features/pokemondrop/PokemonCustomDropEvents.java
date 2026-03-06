package me.marcronte.colisaocobblemon.features.pokemondrop;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import me.marcronte.colisaocobblemon.config.PokemonDropConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;
import java.util.Random;

public class PokemonCustomDropEvents {

    private static final Random RANDOM = new Random();

    public static void register() {
        LootTableEvents.REPLACE.register((key, original, source, wrapperLookup) -> {
            if (key.location().getNamespace().equals("cobblemon") && key.location().getPath().startsWith("entities/")) {
                return LootTable.EMPTY;
            }
            return null;
        });

        CobblemonEvents.POKEMON_FAINTED.subscribe(Priority.NORMAL, event -> {

            Pokemon pokemon = event.getPokemon();

            PokemonEntity entity = pokemon.getEntity();

            if (entity == null || entity.level().isClientSide()) return Unit.INSTANCE;

            if (!pokemon.isWild()) return Unit.INSTANCE;

            String pokeName = pokemon.getSpecies().getName().toLowerCase();
            String primaryType = pokemon.getPrimaryType().getName().toLowerCase();
            String secondaryType = pokemon.getSecondaryType() != null ? pokemon.getSecondaryType().getName().toLowerCase() : null;

            processDrops(pokeName, entity);

            processDrops(primaryType, entity);

            if (secondaryType != null) {
                processDrops(secondaryType, entity);
            }

            return Unit.INSTANCE;
        });
    }

    private static void processDrops(String jsonKey, PokemonEntity entity) {
        if (PokemonDropConfig.INSTANCE == null || PokemonDropConfig.INSTANCE.drops == null) return;

        List<PokemonDropConfig.DropEntry> dropList = PokemonDropConfig.INSTANCE.drops.get(jsonKey);

        if (dropList == null) return;

        Level level = entity.level();

        for (PokemonDropConfig.DropEntry entry : dropList) {
            double roll = RANDOM.nextDouble() * 100.0;

            if (roll <= entry.chance) {
                int amount = entry.min_quantity;
                if (entry.max_quantity > entry.min_quantity) {
                    amount += RANDOM.nextInt((entry.max_quantity - entry.min_quantity) + 1);
                }

                if (amount > 0) {
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.item.trim()));
                    ItemStack dropStack = new ItemStack(item, amount);

                    ItemEntity dropEntity = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), dropStack);
                    level.addFreshEntity(dropEntity);
                }
            }
        }
    }
}