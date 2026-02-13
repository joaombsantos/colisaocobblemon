package me.marcronte.colisaocobblemon.features.hms;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FlashItem extends Item {

    public FlashItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        boolean currentState = isActive(stack);
        setActive(stack, !currentState);

        float pitch = currentState ? 0.6f : 1.0f;
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, pitch);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player) || level.isClientSide) return;

        if (isActive(stack)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, false));
        }
    }

    public static boolean isActive(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        CompoundTag tag = customData.copyTag();
        return tag.getBoolean("FlashActive");
    }

    private void setActive(ItemStack stack, boolean active) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        tag.putBoolean("FlashActive", active);

        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if (isActive(stack)) {
            tooltipComponents.add(Component.translatable("message.colisao-cobblemon.state").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("message.colisao-cobblemon.on").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        } else {
            tooltipComponents.add(Component.translatable("message.colisao-cobblemon.state").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable("message.colisao-cobblemon.off").withStyle(ChatFormatting.RED)));
            tooltipComponents.add(Component.translatable("Clique direito para usar").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}