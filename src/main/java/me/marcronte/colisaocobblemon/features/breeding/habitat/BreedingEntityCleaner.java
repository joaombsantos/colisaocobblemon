package me.marcronte.colisaocobblemon.features.breeding.habitat;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BreedingEntityCleaner {

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof PokemonEntity poke && poke.getTags().contains("habitat_display_entity")) {

                BlockPos ownerPos = getOwnerPosFromTags(poke);

                if (ownerPos == null) {
                    poke.discard();
                    return;
                }

                BlockEntity be = world.getBlockEntity(ownerPos);
                if (!(be instanceof BreedingHabitatBlockEntity habitatBE)) {
                    poke.discard();
                    return;
                }

                if (!entity.getUUID().equals(habitatBE.getSpawnedMotherId()) &&
                        !entity.getUUID().equals(habitatBE.getSpawnedFatherId())) {
                    poke.discard();
                }
            }
        });
    }

    private static BlockPos getOwnerPosFromTags(PokemonEntity poke) {
        for (String tag : poke.getTags()) {
            if (tag.startsWith("habitat_owner_")) {
                String[] parts = tag.split("_");
                if (parts.length == 5) {
                    try {
                        return new BlockPos(
                                Integer.parseInt(parts[2]),
                                Integer.parseInt(parts[3]),
                                Integer.parseInt(parts[4])
                        );
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return null;
    }
}