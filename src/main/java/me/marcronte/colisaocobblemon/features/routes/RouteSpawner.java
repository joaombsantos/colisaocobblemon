package me.marcronte.colisaocobblemon.features.routes;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.RouteConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

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

            // 1. Check Chance
            if (RANDOM.nextFloat() * 100 > entry.chance) {
                continue;
            }

            // 2. Check Conditions
            if (!checkConditions(level, entry)) {
                continue;
            }

            // 3. Get from Cache
            BlockPos spawnPos = RouteCache.getRandomPos(level, routeName, RANDOM);

            if (spawnPos == null) {
                ColisaoCobblemon.LOGGER.error("EMPTY CACHE or invalid Route on Cache for: {}. Verify if exist valid blocks (ex: minecraft:grass_block) inside the area.", routeName);
                return;
            }

            // Success
            spawnPokemon(level, spawnPos, entry);
            return;
        }
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

            String properties = entry.species + " level=" + levelNum;
            if (isShiny) properties += " shiny";

            PokemonEntity entity = PokemonProperties.Companion.parse(properties, " ", "=").createEntity(level);

            if (entity != null) {
                entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

                int hpIv = entry.min_iv + RANDOM.nextInt(entry.max_iv - entry.min_iv + 1);
                entity.getPokemon().getIvs().set(com.cobblemon.mod.common.api.pokemon.stats.Stats.HP, hpIv);

                if (entry.specific_pokeball != null && !entry.specific_pokeball.isEmpty()) {
                    entity.addTag("ball_restriction:" + entry.specific_pokeball);
                }

                level.addFreshEntity(entity);

                level.addFreshEntity(entity);
            }
        } catch (Exception e) {
        }
    }
}