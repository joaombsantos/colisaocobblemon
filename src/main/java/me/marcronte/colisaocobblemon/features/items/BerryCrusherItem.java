package me.marcronte.colisaocobblemon.features.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BerryCrusherItem extends Item {

    public BerryCrusherItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        ItemStack remainder = stack.copy();

        remainder.setDamageValue(remainder.getDamageValue() + 1);

        if (remainder.getDamageValue() >= remainder.getMaxDamage()) {
            return ItemStack.EMPTY;
        }

        return remainder;
    }
}