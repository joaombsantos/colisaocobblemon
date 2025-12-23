package me.marcronte.colisaocobblemon;

import me.marcronte.colisaocobblemon.features.badges.BadgeItems;
import me.marcronte.colisaocobblemon.features.hms.HmManager;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {

    // Define a chave (ID) do grupo
    public static final RegistryKey<ItemGroup> COLISAO_GROUP_KEY = RegistryKey.of(
            Registries.ITEM_GROUP.getKey(),
            Identifier.of(ColisaoCobblemon.MOD_ID, "general")
    );

    // Cria o grupo visualmente
    public static final ItemGroup COLISAO_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(BadgeItems.KANTO_BADGE_CASE)) // Ícone da aba (Estojo)
            .displayName(Text.translatable("itemGroup.colisao-cobblemon.general")) // Nome traduzível
            .build();

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, COLISAO_GROUP_KEY, COLISAO_GROUP);
    }
}