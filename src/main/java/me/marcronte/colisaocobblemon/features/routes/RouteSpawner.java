package me.marcronte.colisaocobblemon.features.routes;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.RouteConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

public class RouteSpawner {

    private static final RandomSource RANDOM = RandomSource.create();
    private static final int SPAWN_INTERVAL = 40; // 2 seconds

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world instanceof ServerLevel serverLevel && serverLevel.getGameTime() % SPAWN_INTERVAL == 0) {
                tickRoutes(serverLevel);
            }
        });
    }

    private static void tickRoutes(ServerLevel level) {
        RouteRegionData regionData = RouteRegionData.get(level);
        RouteConfig config = RouteConfig.get();

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;

            String routeName = regionData.getRouteAt(player.blockPosition());

            if (routeName != null && config.routes.containsKey(routeName)) {
                RouteConfig.RouteData routeData = config.routes.get(routeName);
                attemptSpawn(level, player, routeName, routeData);
            }
        }
    }

    private static void attemptSpawn(ServerLevel level, ServerPlayer player, String routeName, RouteConfig.RouteData routeData) {

        int currentCount = level.getEntitiesOfClass(
                PokemonEntity.class,
                player.getBoundingBox().inflate(48)
        ).size();

        if (currentCount >= routeData.limit) {
            return;
        }

        for (RouteConfig.SpawnEntry entry : routeData.species) {

            if (RANDOM.nextFloat() * 100 > entry.chance) {
                continue;
            }

            if (!checkConditions(level, entry)) {
                continue;
            }

            BlockPos spawnPos = findSpawnPosNearPlayer(level, player, routeName, entry);

            if (spawnPos == null) {
                continue;
            }

            spawnPokemon(level, spawnPos, entry);
            return;
        }
    }

    private static BlockPos findSpawnPosNearPlayer(ServerLevel level, ServerPlayer player, String routeName, RouteConfig.SpawnEntry entry) {
        BlockPos playerPos = player.blockPosition();

        for (int i = 0; i < 10; i++) {
            int dx = RANDOM.nextInt(13) - 6; // -6 a +6
            int dy = RANDOM.nextInt(7) - 3;  // -3 a +3
            int dz = RANDOM.nextInt(13) - 6; // -6 a +6

            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue;

            BlockPos targetPos = playerPos.offset(dx, dy, dz);

            if (!routeName.equals(RouteRegionData.get(level).getRouteAt(targetPos))) {
                continue;
            }

            if (isValidSpawnSpot(level, targetPos, entry.spawnsOn)) {
                return targetPos;
            }
        }
        return null;
    }

    private static boolean isValidSpawnSpot(ServerLevel level, BlockPos pos, List<String> validBlocks) {
        BlockState state = level.getBlockState(pos);
        BlockState belowState = level.getBlockState(pos.below());

        String stateId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        String belowId = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).toString();

        if (validBlocks.contains(stateId)) {
            if (state.getCollisionShape(level, pos, CollisionContext.empty()).isEmpty() || state.getFluidState().isSource()) {
                return true;
            }
        }

        if (validBlocks.contains(belowId)) {
            if (state.getCollisionShape(level, pos, CollisionContext.empty()).isEmpty()) {
                BlockState aboveState = level.getBlockState(pos.above());
                if (aboveState.getCollisionShape(level, pos.above(), CollisionContext.empty()).isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean checkConditions(ServerLevel level, RouteConfig.SpawnEntry entry) {
        if (entry.spawnTime.equalsIgnoreCase("day") && !level.isDay()) return false;
        if (entry.spawnTime.equalsIgnoreCase("night") && level.isDay()) return false;

        if (entry.spawnWeather.equalsIgnoreCase("rain") && !level.isRaining()) return false;
        if (entry.spawnWeather.equalsIgnoreCase("clear") && level.isRaining()) return false;
        return !entry.spawnWeather.equalsIgnoreCase("thunder") || level.isThundering();
    }

    private static void spawnPokemon(ServerLevel level, BlockPos pos, RouteConfig.SpawnEntry entry) {
        try {
            boolean isShiny = RANDOM.nextInt(8192) == 0;
            int levelNum = entry.min_lvl + RANDOM.nextInt(entry.max_lvl - entry.min_lvl + 1);

            String speciesStr = entry.species;
            String aspectProperty = "";

            if (speciesStr.contains("-")) {
                String[] parts = speciesStr.split("-", 2);
                speciesStr = parts[0];
                String regionName = parts[1].toLowerCase();

                switch (regionName) {
                    case "alola": aspectProperty = " alolan"; break;
                    case "galar": aspectProperty = " galarian"; break;
                    case "hisui": aspectProperty = " hisuian"; break;
                    case "paldea": aspectProperty = " paldean"; break;
                    default: aspectProperty = " " + regionName; break;
                }
            }

            String properties = speciesStr + aspectProperty + " level=" + levelNum;
            if (isShiny) properties += " shiny";

            PokemonEntity entity = PokemonProperties.Companion.parse(properties, " ", "=").createEntity(level);

            if (entity != null) {
                entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

                int hpIv = entry.min_iv + RANDOM.nextInt(entry.max_iv - entry.min_iv + 1);
                entity.getPokemon().getIvs().set(com.cobblemon.mod.common.api.pokemon.stats.Stats.HP, hpIv);

                if (entry.specific_pokeball != null && !entry.specific_pokeball.isEmpty()) {
                    entity.addTag("ball_restriction:" + entry.specific_pokeball);
                }

                entity.setUUID(java.util.UUID.randomUUID());

                level.addFreshEntity(entity);
            }
        } catch (Exception e) {
            ColisaoCobblemon.LOGGER.error("Error when spawning Route Pokemon: " + entry.species, e);
        }
    }
}