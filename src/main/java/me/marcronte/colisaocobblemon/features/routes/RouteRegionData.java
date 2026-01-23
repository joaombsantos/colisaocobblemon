package me.marcronte.colisaocobblemon.features.routes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RouteRegionData extends SavedData {

    private static final String ID = "colisao_routes_regions";

    private final List<RouteBox> regions = new ArrayList<>();

    public static class RouteBox {
        public String routeName;
        public AABB box;

        public RouteBox(String name, BlockPos p1, BlockPos p2) {
            this.routeName = name;

            this.box = new AABB(
                    Math.min(p1.getX(), p2.getX()),
                    Math.min(p1.getY(), p2.getY()),
                    Math.min(p1.getZ(), p2.getZ()),
                    Math.max(p1.getX(), p2.getX()) + 1.0,
                    Math.max(p1.getY(), p2.getY()) + 1.0,
                    Math.max(p1.getZ(), p2.getZ()) + 1.0
            );
        }

        public RouteBox(String name, AABB box) {
            this.routeName = name;
            this.box = box;
        }
    }

    public void addRegion(String name, BlockPos p1, BlockPos p2) {
        regions.add(new RouteBox(name, p1, p2));
        setDirty();
    }

    public String removeRegionAt(BlockPos pos) {
        Iterator<RouteBox> iterator = regions.iterator();
        while (iterator.hasNext()) {
            RouteBox region = iterator.next();
            if (region.box.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                String removedName = region.routeName;
                iterator.remove();
                setDirty();
                return removedName;
            }
        }
        return null;
    }

    public String getRouteAt(BlockPos pos) {
        for (RouteBox region : regions) {
            if (region.box.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                return region.routeName;
            }
        }
        return null;
    }

    public static RouteRegionData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new Factory<>(RouteRegionData::new, RouteRegionData::load, null), ID
        );
    }

    public static RouteRegionData load(CompoundTag tag, HolderLookup.Provider provider) {
        RouteRegionData data = new RouteRegionData();
        ListTag list = tag.getList("Regions", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag entry = (CompoundTag) t;
            String name = entry.getString("Name");
            ListTag coords = entry.getList("Box", Tag.TAG_DOUBLE);
            AABB box = new AABB(
                    coords.getDouble(0), coords.getDouble(1), coords.getDouble(2),
                    coords.getDouble(3), coords.getDouble(4), coords.getDouble(5)
            );
            data.regions.add(new RouteBox(name, box));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (RouteBox region : regions) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Name", region.routeName);
            ListTag coords = new ListTag();
            coords.add(net.minecraft.nbt.DoubleTag.valueOf(region.box.minX));
            coords.add(net.minecraft.nbt.DoubleTag.valueOf(region.box.minY));
            coords.add(net.minecraft.nbt.DoubleTag.valueOf(region.box.minZ));
            coords.add(net.minecraft.nbt.DoubleTag.valueOf(region.box.maxX));
            coords.add(net.minecraft.nbt.DoubleTag.valueOf(region.box.maxY));
            coords.add(net.minecraft.nbt.DoubleTag.valueOf(region.box.maxZ));
            entry.put("Box", coords);
            list.add(entry);
        }
        tag.put("Regions", list);
        return tag;
    }

    public List<RouteBox> getRegions() {
        return this.regions;
    }
}