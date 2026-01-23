package me.marcronte.colisaocobblemon.features.routes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RouteToolItem extends Item {

    public static final Map<UUID, Selection> SELECTIONS = new HashMap<>();

    public static class Selection {
        public BlockPos pos1;
        public BlockPos pos2;
    }

    public RouteToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.hasPermissions(2)) return InteractionResult.FAIL;

        BlockPos pos = context.getClickedPos();
        Selection sel = SELECTIONS.computeIfAbsent(player.getUUID(), k -> new Selection());
        sel.pos1 = pos;

        player.displayClientMessage(Component.literal("§aPos1: " + pos.toShortString()), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.hasPermissions(2)) return InteractionResultHolder.pass(player.getItemInHand(hand));

        if (player.isCrouching()) {
            if (!level.isClientSide) {
                RouteNetwork.openRouteScreen((ServerPlayer) player);
            }
        } else {
            // Direito normal: Define Posição 2 (No ar ou bloco)
            Selection sel = SELECTIONS.computeIfAbsent(player.getUUID(), k -> new Selection());
            sel.pos2 = player.blockPosition();
            player.displayClientMessage(Component.literal("§bPos2: " + sel.pos2.toShortString()), true);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}