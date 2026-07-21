package me.marcronte.colisaocobblemon.features.breeding.habitat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class HabitatDictionary {

    public static final Map<String, String> TYPE_BERRIES = new HashMap<>();
    public static final Map<String, Map<String, Integer>> HABITAT_BLOCKS = new HashMap<>();

    static {
        registerType("normal", "chilan_berry");
        registerType("fire", "occa_berry");
        registerType("water", "passho_berry");
        registerType("electric", "wacan_berry");
        registerType("grass", "rindo_berry");
        registerType("ice", "yache_berry");
        registerType("fighting", "chople_berry");
        registerType("poison", "kebia_berry");
        registerType("ground", "shuca_berry");
        registerType("flying", "coba_berry");
        registerType("psychic", "payapa_berry");
        registerType("bug", "tanga_berry");
        registerType("rock", "charti_berry");
        registerType("ghost", "kasib_berry");
        registerType("dragon", "haban_berry");
        registerType("dark", "colbur_berry");
        registerType("steel", "babiri_berry");
        registerType("fairy", "roseli_berry");

        addBlocks("normal", "minecraft:grass_block", "minecraft:white_wool", "minecraft:quartz_block");
        addBlocks("fire", "minecraft:coal_block", "minecraft:netherrack", "minecraft:magma_block");
        addBlocks("water", "minecraft:sand", "minecraft:prismarine", "minecraft:sea_lantern");
        addBlocks("electric", "minecraft:redstone_lamp", "minecraft:redstone_block", "minecraft:waxed_copper_block");
        addBlocks("grass", "minecraft:grass_block", "minecraft:hay_block", "minecraft:moss_block");
        addBlocks("ice", "minecraft:ice", "minecraft:packed_ice", "minecraft:blue_ice");
        addBlocks("fighting", "minecraft:stone_bricks", "minecraft:bricks", "minecraft:deepslate_bricks");
        addBlocks("poison", "minecraft:grass_block", "minecraft:mushroom_stem", "minecraft:mycelium");
        addBlocks("ground", "minecraft:dirt", "minecraft:coarse_dirt", "minecraft:packed_mud");
        addBlocks("flying", "minecraft:grass_block", "minecraft:dispenser", "minecraft:tinted_glass");
        addBlocks("psychic", "minecraft:bookshelf", "minecraft:quartz_block", "minecraft:amethyst_block");
        addBlocks("bug", "minecraft:hay_block", "minecraft:carved_pumpkin", "minecraft:honeycomb_block");
        addBlocks("rock", "minecraft:cobblestone", "minecraft:gravel", "minecraft:deepslate");
        addBlocks("ghost", "minecraft:obsidian", "minecraft:blackstone", "minecraft:soul_soil");
        addBlocks("dragon", "minecraft:stone", "minecraft:glowstone", "minecraft:end_stone");
        addBlocks("dark", "minecraft:deepslate", "minecraft:coal_block", "minecraft:obsidian");
        addBlocks("steel", "minecraft:stone", "minecraft:iron_ore", "minecraft:iron_block");
        addBlocks("fairy", "minecraft:white_wool", "minecraft:glowstone", "minecraft:amethyst_block");
    }

    private static void registerType(String type, String berryId) {
        TYPE_BERRIES.put(type.toLowerCase(), "cobblemon:" + berryId);
        HABITAT_BLOCKS.put(type.toLowerCase(), new HashMap<>());
    }

    private static void addBlocks(String type, String b1, String b2, String b3) {
        Map<String, Integer> blocks = HABITAT_BLOCKS.get(type.toLowerCase());
        if (blocks != null) {
            blocks.put(b1, 1);
            blocks.put(b2, 2);
            blocks.put(b3, 3);
        }
    }

    public static String getRequiredBerryId(String primaryType) {
        if (primaryType == null) return null;
        return TYPE_BERRIES.get(primaryType.toLowerCase());
    }

    public static boolean isBerryMatch(ItemStack stack, String requiredBerryId) {
        if (requiredBerryId == null || stack.isEmpty()) return false;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return itemId.equals(requiredBerryId);
    }

    public static int getBlockValueForTypes(Block block, String type1, String type2) {
        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
        int val1 = getBlockValue(blockId, type1);
        int val2 = getBlockValue(blockId, type2);
        return Math.max(val1, val2);
    }

    private static int getBlockValue(String blockId, String type) {
        if (type == null) return 0;
        Map<String, Integer> map = HABITAT_BLOCKS.get(type.toLowerCase());
        return map != null ? map.getOrDefault(blockId, 0) : 0;
    }
}