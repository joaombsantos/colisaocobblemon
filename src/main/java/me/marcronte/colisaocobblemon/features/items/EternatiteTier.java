package me.marcronte.colisaocobblemon.features.items;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import me.marcronte.colisaocobblemon.ModItems;
import org.jetbrains.annotations.NotNull;

public class EternatiteTier implements Tier {

    public static final EternatiteTier INSTANCE = new EternatiteTier();

    @Override
    public int getUses() {
        return 3000;
    }

    @Override
    public float getSpeed() {
        return 12.0f;
    }

    @Override
    public float getAttackDamageBonus() {
        return 5.0f;
    }

    @Override
    public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.ETERNATITE_INGOT);
    }
}