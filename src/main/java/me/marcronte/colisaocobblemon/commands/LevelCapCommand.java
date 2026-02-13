package me.marcronte.colisaocobblemon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.marcronte.colisaocobblemon.config.LevelCapConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.*;

public class LevelCapCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("levelcap")
                .requires(source -> true)
                .executes(LevelCapCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            LevelCapConfig config = LevelCapConfig.get();

            Set<Integer> capsSet = new HashSet<>();
            capsSet.add(config.startLevel);
            capsSet.addAll(config.badges.values());

            List<Integer> sortedCaps = new ArrayList<>(capsSet);
            Collections.sort(sortedCaps);

            int playerCurrentCap = calculatePlayerCap(player, config);

            MutableComponent message = Component.empty();
            message.append(Component.translatable("message.colisao-cobblemon.your_cap").withStyle(ChatFormatting.GOLD));

            for (int i = 0; i < sortedCaps.size(); i++) {
                int cap = sortedCaps.get(i);
                boolean isCurrent = (cap == playerCurrentCap);

                if (isCurrent) {
                    message.append(Component.literal("(" + cap + ")")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                } else {
                    message.append(Component.literal(String.valueOf(cap))
                            .withStyle(ChatFormatting.GRAY));
                }

                if (i < sortedCaps.size() - 1) {
                    message.append(Component.literal(" -> ").withStyle(ChatFormatting.DARK_GRAY));
                }
            }

            player.sendSystemMessage(message);

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("message.colisao-cobblemon.error_level_cap"));
            e.printStackTrace();
        }
        return 1;
    }

    private static int calculatePlayerCap(ServerPlayer player, LevelCapConfig config) {
        int maxCap = config.startLevel;

        for (Map.Entry<String, Integer> entry : config.badges.entrySet()) {
            String itemId = entry.getKey();
            int capLevel = entry.getValue();

            if (hasBadge(player, itemId)) {
                if (capLevel > maxCap) {
                    maxCap = capLevel;
                }
            }
        }
        return maxCap;
    }

    private static boolean hasBadge(ServerPlayer player, String badgeId) {
        try {
            ResourceLocation id = ResourceLocation.parse(badgeId);
            Item badgeItem = BuiltInRegistries.ITEM.get(id);
            if (badgeItem.toString().equals("air")) return false;

            for (ItemStack stack : player.getInventory().items) {
                if (stack.isEmpty()) continue;
                if (stack.getItem() == badgeItem) return true;

                if (isBadgeCase(stack)) {
                    if (checkInsideCase(player, stack, badgeItem)) return true;
                }
            }

            for (ItemStack stack : player.getInventory().offhand) {
                if (stack.isEmpty()) continue;
                if (stack.getItem() == badgeItem) return true;
                if (isBadgeCase(stack)) {
                    if (checkInsideCase(player, stack, badgeItem)) return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isBadgeCase(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.getPath().contains("badge_case");
    }

    private static boolean checkInsideCase(ServerPlayer player, ItemStack caseStack, Item targetBadge) {
        CustomData customData = caseStack.get(DataComponents.CUSTOM_DATA);

        if (customData != null) {
            CompoundTag tag = customData.copyTag();

            if (tag.contains("Items", Tag.TAG_LIST)) {
                ListTag itemsList = tag.getList("Items", Tag.TAG_COMPOUND);

                for (int i = 0; i < itemsList.size(); i++) {
                    CompoundTag itemTag = itemsList.getCompound(i);

                    ItemStack stackInside = ItemStack.parseOptional(player.registryAccess(), itemTag);

                    if (!stackInside.isEmpty() && stackInside.getItem() == targetBadge) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}