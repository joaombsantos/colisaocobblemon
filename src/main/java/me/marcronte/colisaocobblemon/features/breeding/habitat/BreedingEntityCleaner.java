package me.marcronte.colisaocobblemon.features.breeding.habitat;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class BreedingEntityCleaner {

    private static final List<PendingCheck> pendingChecks = new ArrayList<>();

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof PokemonEntity poke && poke.getTags().contains("habitat_display_entity")) {

                BlockPos ownerPos = getOwnerPosFromTags(poke);

                if (ownerPos == null) {
                    poke.discard();
                    return;
                }

                pendingChecks.add(new PendingCheck(poke.getUUID(), ownerPos, (ServerLevel) world, 5));
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (pendingChecks.isEmpty()) return;

            Iterator<PendingCheck> iterator = pendingChecks.iterator();
            while (iterator.hasNext()) {
                PendingCheck check = iterator.next();
                check.ticksLeft--;

                if (check.ticksLeft <= 0) {
                    iterator.remove();
                    verifyAndDeleteGhost(check);
                }
            }
        });
    }

    private static void verifyAndDeleteGhost(PendingCheck check) {
        net.minecraft.world.entity.Entity entity = check.level.getEntity(check.entityId);
        if (!(entity instanceof PokemonEntity poke) || !poke.isAlive()) return;

        if (!check.level.hasChunkAt(check.pos)) return;

        BlockEntity be = check.level.getBlockEntity(check.pos);

        if (!(be instanceof BreedingHabitatBlockEntity habitatBE)) {
            poke.discard();
            return;
        }

        if (!poke.getUUID().equals(habitatBE.getSpawnedMotherId()) &&
                !poke.getUUID().equals(habitatBE.getSpawnedFatherId())) {
            poke.discard();
        }
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

    private static class PendingCheck {
        UUID entityId;
        BlockPos pos;
        ServerLevel level;
        int ticksLeft;

        PendingCheck(UUID entityId, BlockPos pos, ServerLevel level, int ticksLeft) {
            this.entityId = entityId;
            this.pos = pos;
            this.level = level;
            this.ticksLeft = ticksLeft;
        }
    }
}