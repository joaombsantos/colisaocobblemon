package me.marcronte.colisaocobblemon.features.routes;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.RouteConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RouteFishingHandler {

    public static PokemonEntity tryFish(FishingHook hook, ServerPlayer player) {
        try {
            ServerLevel level = player.serverLevel();
            BlockPos hookPos = hook.blockPosition();

            RouteRegionData regionData = RouteRegionData.get(level);
            String routeName = regionData.getRouteAt(hookPos);

            if (routeName == null) return null;

            RouteConfig config = RouteConfig.get();
            if (!config.routes.containsKey(routeName)) return null;

            RouteConfig.RouteData routeData = config.routes.get(routeName);
            List<RouteConfig.SpawnEntry> fishingList = routeData.fishing_species;

            if (fishingList == null || fishingList.isEmpty()) return null;

            for (RouteConfig.SpawnEntry entry : fishingList) {
                if (player.getRandom().nextFloat() * 100 > entry.chance) continue;
                if (!checkConditions(level, entry)) continue;

                return spawnFishingPokemon(level, hook, player, entry);
            }

        } catch (Exception e) {
            ColisaoCobblemon.LOGGER.error("ERROR ON ROUTE FISHING: ", e);
        }

        return null;
    }

    private static PokemonEntity spawnFishingPokemon(ServerLevel level, FishingHook hook, ServerPlayer player, RouteConfig.SpawnEntry entry) {
        try {
            boolean isShiny = player.getRandom().nextInt(8192) == 0;
            int levelNum = entry.min_lvl + player.getRandom().nextInt(entry.max_lvl - entry.min_lvl + 1);

            String properties = entry.species + " level=" + levelNum;
            if (isShiny) properties += " shiny";

            PokemonEntity entity = PokemonProperties.Companion.parse(properties, " ", "=").createEntity(level);

            if (entity != null) {
                entity.setPos(hook.getX(), hook.getY(), hook.getZ());

                int hpIv = entry.min_iv + player.getRandom().nextInt(entry.max_iv - entry.min_iv + 1);
                entity.getPokemon().getIvs().set(com.cobblemon.mod.common.api.pokemon.stats.Stats.HP, hpIv);

                double d0 = player.getX() - hook.getX();
                double d1 = player.getY() - hook.getY();
                double d2 = player.getZ() - hook.getZ();

                Vec3 vector3d = new Vec3(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                entity.setDeltaMovement(vector3d);

                if (entry.specific_pokeball != null && !entry.specific_pokeball.isEmpty()) {
                    entity.addTag("ball_restriction:" + entry.specific_pokeball);
                }

                entity.setUUID(java.util.UUID.randomUUID());

                entity.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, player.position());

                level.addFreshEntity(entity);
                return entity;
            }
        } catch (Exception e) {
            ColisaoCobblemon.LOGGER.error("Error when spawning pokemon: {}", entry.species, e);
        }
        return null;
    }

    private static boolean checkConditions(ServerLevel level, RouteConfig.SpawnEntry entry) {
        String time = entry.spawnTime != null ? entry.spawnTime : "all";
        String weather = entry.spawnWeather != null ? entry.spawnWeather : "all";

        if (time.equalsIgnoreCase("day") && !level.isDay()) return false;
        if (time.equalsIgnoreCase("night") && level.isDay()) return false;

        if (weather.equalsIgnoreCase("rain") && !level.isRaining()) return false;
        if (weather.equalsIgnoreCase("clear") && level.isRaining()) return false;
        if (weather.equalsIgnoreCase("thunder") && !level.isThundering()) return false;

        return true;
    }
}