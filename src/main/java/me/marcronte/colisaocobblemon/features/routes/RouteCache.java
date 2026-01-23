package me.marcronte.colisaocobblemon.features.routes;

import me.marcronte.colisaocobblemon.ColisaoCobblemon;
import me.marcronte.colisaocobblemon.config.RouteConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.*;

public class RouteCache {

    private static final Map<String, List<BlockPos>> CACHED_POSITIONS = new HashMap<>();

    public static void clear() {
        CACHED_POSITIONS.clear();
    }

    public static void buildCache(ServerLevel level) {
        clear();
        long start = System.currentTimeMillis();

        RouteRegionData regionData = RouteRegionData.get(level);
        RouteConfig config = RouteConfig.get();

        for (RouteRegionData.RouteBox region : regionData.getRegions()) {
            refreshSingleRoute(level, region, config);
        }

        long time = System.currentTimeMillis() - start;
        int totalSpots = CACHED_POSITIONS.values().stream().mapToInt(List::size).sum();
        ColisaoCobblemon.LOGGER.info("Initail cache built in {}ms. Total spawns: {}", time, totalSpots);
    }

    private static void refreshSingleRoute(ServerLevel level, RouteRegionData.RouteBox region, RouteConfig config) {
        if (!config.routes.containsKey(region.routeName)) {
            return;
        }

        RouteConfig.RouteData routeConfig = config.routes.get(region.routeName);
        Set<String> validBlocks = new HashSet<>();
        for (RouteConfig.SpawnEntry entry : routeConfig.species) {
            validBlocks.addAll(entry.spawnsOn);
        }

        List<BlockPos> validSpots = scanRegion(level, region.box, validBlocks);

        if (!validSpots.isEmpty()) {
            CACHED_POSITIONS.computeIfAbsent(region.routeName, k -> new ArrayList<>()).addAll(validSpots);
        }
    }

    private static List<BlockPos> scanRegion(ServerLevel level, AABB box, Set<String> validBlocks) {
        List<BlockPos> spots = new ArrayList<>();

        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);
        int maxX = (int) Math.ceil(box.maxX);
        int maxY = (int) Math.ceil(box.maxY);
        int maxZ = (int) Math.ceil(box.maxZ);

        if (maxX <= minX) maxX = minX + 1;
        if (maxY <= minY) maxY = minY + 1;
        if (maxZ <= minZ) maxZ = minZ + 1;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    pos.set(x, y, z);

                    if (!level.isLoaded(pos)) continue;

                    BlockState state = level.getBlockState(pos);

                    if (isValidTarget(level, pos, state, validBlocks)) {
                        spots.add(pos.immutable());
                        continue;
                    }

                    BlockPos abovePos = pos.above();
                    BlockState aboveState = level.getBlockState(abovePos);
                    if (isValidTarget(level, abovePos, aboveState, validBlocks)) {
                        if (state.isCollisionShapeFullBlock(level, pos)) {
                            spots.add(abovePos);
                            continue;
                        }
                    }

                    BlockPos belowPos = pos.below();
                    BlockState belowState = level.getBlockState(belowPos);
                    String belowId = BuiltInRegistries.BLOCK.getKey(belowState.getBlock()).toString();
                    if (validBlocks.contains(belowId)) {
                        if (state.getCollisionShape(level, pos, CollisionContext.empty()).isEmpty()) {
                            spots.add(pos.immutable());
                        }
                    }
                }
            }
        }
        return spots;
    }

    private static boolean isValidTarget(ServerLevel level, BlockPos pos, BlockState state, Set<String> validBlocks) {
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        if (validBlocks.contains(id)) {
            return state.getCollisionShape(level, pos, CollisionContext.empty()).isEmpty()
                    || state.getFluidState().isSource();
        }
        return false;
    }

    public static BlockPos getRandomPos(ServerLevel level, String routeName, RandomSource random) {
        List<BlockPos> spots = CACHED_POSITIONS.get(routeName);

        if (spots == null || spots.isEmpty()) {

            RouteRegionData regionData = RouteRegionData.get(level);
            RouteConfig config = RouteConfig.get();

            for (RouteRegionData.RouteBox region : regionData.getRegions()) {
                if (region.routeName.equals(routeName)) {
                    refreshSingleRoute(level, region, config);
                    break;
                }
            }

            spots = CACHED_POSITIONS.get(routeName);
        }

        if (spots == null || spots.isEmpty()) return null;
        return spots.get(random.nextInt(spots.size()));
    }
}