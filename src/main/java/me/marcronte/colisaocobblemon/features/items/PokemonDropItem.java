package me.marcronte.colisaocobblemon.features.items;

import me.marcronte.colisaocobblemon.features.drops.PokemonDropModifier;
import net.minecraft.world.item.Item;

public class PokemonDropItem extends Item {

    private final String targetType;
    private final float dropChance;

    public PokemonDropItem(String targetType, float dropChance, Properties properties) {
        super(properties);
        this.targetType = targetType.toLowerCase();
        this.dropChance = dropChance;

        PokemonDropModifier.registerCustomDrop(this);
    }

    public String getTargetType() { return targetType; }
    public float getDropChance() { return dropChance; }
}