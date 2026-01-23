package me.marcronte.colisaocobblemon;

import me.marcronte.colisaocobblemon.features.items.RunningShoesItem;
import me.marcronte.colisaocobblemon.features.routes.RouteToolItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.Unbreakable;

public class ModItems {

    public static final Item RUNNING_SHOES = register("running_shoes",
            new RunningShoesItem(new Item.Properties()
                    .stacksTo(1)
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                    .component(DataComponents.DYED_COLOR, new DyedItemColor(0xE8A858, true))
            )
    );

    public static final Item ROUTE_TOOL = register("route_tool",
            new RouteToolItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));


    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, name), item);
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.COLISAO_GROUP_KEY).register(entries -> {
            entries.accept(RUNNING_SHOES);
            entries.accept(ROUTE_TOOL);
        });
    }
}