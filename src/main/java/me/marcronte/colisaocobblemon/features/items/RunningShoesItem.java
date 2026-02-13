package me.marcronte.colisaocobblemon.features.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RunningShoesItem extends ArmorItem{

    public RunningShoesItem(Properties properties) {
        super(ArmorMaterials.LEATHER, Type.BOOTS, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {

            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

            if (boots.getItem() == this) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 0, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 10, 0, false, false, false));
            }
        }
    }
}