package me.marcronte.colisaocobblemon;

import me.marcronte.colisaocobblemon.features.badges.BadgeItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroup {

    // Define a chave (ID) do grupo
    // RegistryKey -> ResourceKey
    // ItemGroup -> CreativeModeTab
    public static final ResourceKey<CreativeModeTab> COLISAO_GROUP_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            ResourceLocation.fromNamespaceAndPath(ColisaoCobblemon.MOD_ID, "general")
    );

    // Cria o grupo visualmente
    public static final CreativeModeTab COLISAO_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(BadgeItems.KANTO_BADGE_CASE)) // Icon on creative mode
            .title(Component.translatable("itemGroup.colisao-cobblemon.general"))
            .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, COLISAO_GROUP_KEY, COLISAO_GROUP);
    }
}