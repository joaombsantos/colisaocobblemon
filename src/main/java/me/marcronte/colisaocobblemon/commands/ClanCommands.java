package me.marcronte.colisaocobblemon.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.marcronte.colisaocobblemon.features.clans.*;
import me.marcronte.colisaocobblemon.network.payloads.ClanPayloads;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClanCommands {

    private static final Map<UUID, String> PENDING_INVITES = new HashMap<>();
    private static final Map<UUID, Long> INVITE_TIMESTAMPS = new HashMap<>();

    private static final Map<String, Long> BONUS_COOLDOWNS = new HashMap<>();

    private static final Map<UUID, Long> PENDING_DELETIONS = new HashMap<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("clan")
                // /clan
                .executes(context -> openClanMenu(context.getSource().getPlayerOrException()))

                // /clan ajuda
                .then(Commands.literal("ajuda")
                        .executes(context -> showHelp(context.getSource().getPlayerOrException())))

                // /clan criar
                .then(Commands.literal("criar")
                        .executes(context -> openClanCreationUI(context.getSource().getPlayerOrException())))

                // /clan convidar <jogador>
                .then(Commands.literal("convidar")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(context -> invitePlayer(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "jogador")))))

                // /clan aceitar
                .then(Commands.literal("aceitar")
                        .executes(context -> acceptInvite(context.getSource().getPlayerOrException())))

                // /clan recusar
                .then(Commands.literal("recusar")
                        .executes(context -> declineInvite(context.getSource().getPlayerOrException())))

                // /clan sair
                .then(Commands.literal("sair")
                        .executes(context -> leaveClan(context.getSource().getPlayerOrException())))

                // /clan deletar
                .then(Commands.literal("deletar")
                        .executes(context -> deleteClan(context.getSource().getPlayerOrException())))

                // /clan sethome
                .then(Commands.literal("sethome")
                        .executes(context -> setClanHome(context.getSource().getPlayerOrException())))

                // /clan home
                .then(Commands.literal("home")
                        .executes(context -> teleportClanHome(context.getSource().getPlayerOrException())))

                // /clan chest
                .then(Commands.literal("chest")
                        .executes(context -> openClanChest(context.getSource().getPlayerOrException())))

                // /clan bonus
                .then(Commands.literal("bonus")
                        .executes(context -> activateClanBonus(context.getSource().getPlayerOrException())))

                // /clan expulsar <nick>
                .then(Commands.literal("expulsar")
                        .then(Commands.argument("nick", StringArgumentType.string())
                                .executes(context -> kickPlayer(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "nick")))))

                // /clan promover <nick>
                .then(Commands.literal("promover")
                        .then(Commands.argument("nick", StringArgumentType.string())
                                .executes(context -> promotePlayer(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "nick")))))

                // /clan demover <nick>
                .then(Commands.literal("demover")
                        .then(Commands.argument("nick", StringArgumentType.string())
                                .executes(context -> demotePlayer(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "nick")))))

                // --- ADMIN COMMANDS (PERMISSION LEVEL 2) ---
                .then(Commands.literal("admin")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("deletar")
                                .then(Commands.argument("nome", StringArgumentType.string())
                                        .executes(context -> adminDeleteClan(context.getSource(), StringArgumentType.getString(context, "nome")))))
                        .then(Commands.literal("darxp")
                                .then(Commands.argument("nome", StringArgumentType.string())
                                        .then(Commands.argument("qtd", IntegerArgumentType.integer(1))
                                                .executes(context -> adminGiveXp(context.getSource(), StringArgumentType.getString(context, "nome"), IntegerArgumentType.getInteger(context, "qtd"))))))
                )
        );

        // --- CHAT DO CLAN (/c <message>) ---
        dispatcher.register(Commands.literal("c")
                .then(Commands.argument("mensagem", StringArgumentType.greedyString())
                        .executes(context -> sendClanChat(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "mensagem")))
                )
        );
    }


    private static int openClanMenu(ServerPlayer player) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.clan_not_found_menu"));
            return 0;
        }

        java.util.List<String> members = new java.util.ArrayList<>();
        boolean isManager = false;

        for (ClanMember m : clan.getMembers().values()) {
            String rankTranslated = Component.translatable("message.colisao-cobblemon." + m.getRank().name().toLowerCase()).getString();
            members.add(rankTranslated + " - " + m.getName());
            if (m.getUuid().equals(player.getUUID()) && m.getRank().isAtLeast(ClanRank.MANAGER)) {
                isManager = true;
            }
        }

        java.util.List<String> perks = new java.util.ArrayList<>();
        perks.add(Component.translatable("message.colisao-cobblemon.perk_chat").getString());
        if (clan.getLevel() >= 2) perks.add(Component.translatable("message.colisao-cobblemon.perk_home").getString());
        if (clan.getLevel() >= 3) perks.add(Component.translatable("message.colisao-cobblemon.perk_chest").getString());
        if (clan.getLevel() >= 3) perks.add(Component.translatable("message.colisao-cobblemon.perk_affinity_1").getString());
        if (clan.getLevel() >= 4) perks.add(Component.translatable("message.colisao-cobblemon.perk_bonus").getString());
        if (clan.getLevel() >= 5) perks.add(Component.translatable("message.colisao-cobblemon.perk_affinity_2").getString());

        List<String> missions = getStrings(clan);

        String timeRemaining = ClanScheduler.getTimeRemaining(data.getNextResetTimestamp());

        long nextReset = data.getNextResetTimestamp();

        ServerPlayNetworking.send(player, new ClanPayloads.OpenClanMenuPayload(
                clan.getName(),
                clan.getLevel(),
                clan.getXp(),
                clan.getXpNeededForNextLevel(),
                members,
                perks,
                missions,
                timeRemaining,
                nextReset,
                isManager
        ));

        return 1;
    }

    private static @NotNull List<String> getStrings(Clan clan) {
        List<String> missions = new ArrayList<>();
        missions.add(Component.translatable("message.colisao-cobblemon.mission_defeat", clan.getDefeatedCount(), clan.getTargetDefeat()).getString());
        missions.add(Component.translatable("message.colisao-cobblemon.mission_defeat_type", clan.getDefeatedTypeCount(), clan.getTargetDefeatType()).getString());
        missions.add(Component.translatable("message.colisao-cobblemon.mission_catch", clan.getCaughtCount(), clan.getTargetCatch()).getString());
        missions.add(Component.translatable("message.colisao-cobblemon.mission_catch_type", clan.getCaughtTypeCount(), clan.getTargetCatchType()).getString());
        missions.add(Component.translatable("message.colisao-cobblemon.mission_hatch", clan.getHatchedCount(), clan.getTargetHatch()).getString());
        return missions;
    }

    private static int openClanCreationUI(ServerPlayer player) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        if (data.getClanByPlayer(player.getUUID()) != null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.already_in_clan"));
            return 0;
        }

        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.opening_clan_creation"));
        ServerPlayNetworking.send(player, new ClanPayloads.OpenClanCreationPayload());
        return 1;
    }

    private static int invitePlayer(ServerPlayer player, ServerPlayer target) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.you_not_in_clan"));
            return 0;
        }

        ClanMember sender = clan.getMembers().get(player.getUUID());
        if (sender == null || !sender.getRank().isAtLeast(ClanRank.MANAGER)) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.no_invite_permission"));
            return 0;
        }

        if (data.getClanByPlayer(target.getUUID()) != null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.target_already_in_clan"));
            return 0;
        }

        PENDING_INVITES.put(target.getUUID(), clan.getName());
        INVITE_TIMESTAMPS.put(target.getUUID(), System.currentTimeMillis());

        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.invite_sent", target.getScoreboardName()));
        target.sendSystemMessage(Component.translatable("message.colisao-cobblemon.invited_to_clan", clan.getName()));
        return 1;
    }

    private static int acceptInvite(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!PENDING_INVITES.containsKey(uuid) || (System.currentTimeMillis() - INVITE_TIMESTAMPS.getOrDefault(uuid, 0L) > 60000)) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.no_pending_invite"));
            PENDING_INVITES.remove(uuid);
            INVITE_TIMESTAMPS.remove(uuid);
            return 0;
        }

        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByName(PENDING_INVITES.get(uuid));

        if (clan != null) {
            clan.getMembers().put(uuid, new ClanMember(uuid, player.getScoreboardName(), ClanRank.MEMBER));
            data.updatePlayerCache(uuid, clan.getName());

            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.joined_clan", clan.getName()));
            for (ClanMember m : clan.getMembers().values()) {
                ServerPlayer p = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayer(m.getUuid());
                if (p != null && p != player) {
                    p.sendSystemMessage(Component.translatable("message.colisao-cobblemon.member_joined", player.getScoreboardName()));
                }
            }
        }

        PENDING_INVITES.remove(uuid);
        INVITE_TIMESTAMPS.remove(uuid);
        ClanSavedData.refreshTabList(player);
        return 1;
    }

    private static int declineInvite(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (PENDING_INVITES.remove(uuid) != null) {
            INVITE_TIMESTAMPS.remove(uuid);
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.invite_declined"));
            return 1;
        }
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.no_pending_invite_generic"));
        return 0;
    }

    private static int deleteClan(ServerPlayer player) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.you_not_in_clan"));
            return 0;
        }

        ClanMember member = clan.getMembers().get(player.getUUID());
        if (member == null || member.getRank() != ClanRank.OWNER) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.only_owner_delete"));
            return 0;
        }

        UUID uuid = player.getUUID();
        long now = System.currentTimeMillis();

        if (PENDING_DELETIONS.containsKey(uuid) && (now - PENDING_DELETIONS.get(uuid) < 30000)) {
            String clanName = clan.getName();

            java.util.List<ServerPlayer> onlineMembers = new java.util.ArrayList<>();
            for (ClanMember m : clan.getMembers().values()) {
                ServerPlayer onlinePlayer = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayer(m.getUuid());
                if (onlinePlayer != null) {
                    onlineMembers.add(onlinePlayer);
                    if (onlinePlayer != player) {
                        onlinePlayer.sendSystemMessage(Component.translatable("message.colisao-cobblemon.clan_disbanded"));
                    }
                }
            }

            data.removeClan(clanName);
            PENDING_DELETIONS.remove(uuid);

            for (ServerPlayer exMember : onlineMembers) {
                ClanSavedData.refreshTabList(exMember);
            }

            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.clan_deleted", clanName));
            return 1;
        } else {
            PENDING_DELETIONS.put(uuid, now);
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.clan_delete_confirm"));
            return 1;
        }
    }

    private static int leaveClan(ServerPlayer player) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.you_not_in_clan"));
            return 0;
        }

        ClanMember member = clan.getMembers().get(player.getUUID());
        if (member.getRank() == ClanRank.OWNER) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.owner_cannot_leave"));
            return 0;
        }

        clan.getMembers().remove(player.getUUID());
        data.updatePlayerCache(player.getUUID(), null);
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.left_clan"));
        ClanSavedData.refreshTabList(player);
        return 1;
    }

    private static int setClanHome(ServerPlayer player) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.you_not_in_clan"));
            return 0;
        }

        if (clan.getMembers().get(player.getUUID()).getRank() != ClanRank.OWNER) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.only_owner_sethome"));
            return 0;
        }

        if (clan.getLevel() < 2) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.home_level_required"));
            return 0;
        }

        clan.setHome(player.blockPosition(), player.level().dimension().location().toString());
        data.setDirty();
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.home_set"));
        return 1;
    }

    private static int teleportClanHome(ServerPlayer player) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null || clan.getHomePos() == null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.home_not_set"));
            return 0;
        }

        if (clan.getLevel() < 2) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.home_use_level_required"));
            return 0;
        }

        ResourceKey<Level> dimKey = net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.ResourceLocation.parse(clan.getHomeDimension()));
        ServerLevel destLevel = Objects.requireNonNull(player.getServer()).getLevel(dimKey);

        if (destLevel != null) {
            player.teleportTo(destLevel, clan.getHomePos().getX() + 0.5, clan.getHomePos().getY() + 1, clan.getHomePos().getZ() + 0.5, java.util.Set.of(), player.getYRot(), player.getXRot());
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.teleported_home"));
            return 1;
        }
        return 0;
    }

    private static int activateClanBonus(ServerPlayer player) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null) return 0;

        ClanMember member = clan.getMembers().get(player.getUUID());
        if (member == null || !member.getRank().isAtLeast(ClanRank.MANAGER)) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.no_bonus_permission"));
            return 0;
        }

        if (clan.getLevel() < 4) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.bonus_level_required"));
            return 0;
        }

        long now = System.currentTimeMillis();
        long nextAvailable = BONUS_COOLDOWNS.getOrDefault(clan.getName().toLowerCase(), 0L);
        if (now < nextAvailable) {
            long remainingMs = nextAvailable - now;
            long hours = remainingMs / 3600000;
            long minutes = (remainingMs % 3600000) / 60000;
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.bonus_cooldown", hours, minutes));
            return 0;
        }

        BONUS_COOLDOWNS.put(clan.getName().toLowerCase(), now + 86400000);

        // --- BONUS: 30 minutes (30 * 60 * 1000 = 1.800.000 ms) ---
        clan.setBonusEndTime(now + 1800000);
        data.setDirty();

        for (ClanMember m : clan.getMembers().values()) {
            ServerPlayer onlinePlayer = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayer(m.getUuid());
            if (onlinePlayer != null) {
                onlinePlayer.sendSystemMessage(Component.translatable("message.colisao-cobblemon.bonus_activated", player.getScoreboardName()));
            }
        }
        return 1;
    }

    private static int sendClanChat(ServerPlayer player, String message) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.you_not_in_clan"));
            return 0;
        }

        Component formattedMsg = Component.literal("§8[" + clan.getTagColor() + clan.getTag() + "§8] §7" + player.getScoreboardName() + " §f» " + message);

        for (ClanMember m : clan.getMembers().values()) {
            ServerPlayer onlinePlayer = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayer(m.getUuid());
            if (onlinePlayer != null) {
                onlinePlayer.sendSystemMessage(formattedMsg);
            }
        }
        return 1;
    }

    private static int kickPlayer(ServerPlayer player, String targetName) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null) return 0;
        ClanMember executer = clan.getMembers().get(player.getUUID());
        if (!executer.getRank().isAtLeast(ClanRank.MANAGER)) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.no_kick_permission"));
            return 0;
        }

        ClanMember target = null;
        UUID targetUuid = null;
        for (Map.Entry<UUID, ClanMember> entry : clan.getMembers().entrySet()) {
            if (entry.getValue().getName().equalsIgnoreCase(targetName)) {
                target = entry.getValue();
                targetUuid = entry.getKey();
                break;
            }
        }

        if (target == null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.player_not_found"));
            return 0;
        }

        if (executer.getRank() == ClanRank.MANAGER && target.getRank().isAtLeast(ClanRank.MANAGER)) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.kick_hierarchy_low"));
            return 0;
        }

        clan.getMembers().remove(targetUuid);
        data.updatePlayerCache(targetUuid, null);

        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.player_kicked", targetName));
        ServerPlayer targetOnline = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayer(targetUuid);
        if (targetOnline != null) {
            targetOnline.sendSystemMessage(Component.translatable("message.colisao-cobblemon.you_were_kicked"));
        }

        ClanSavedData.refreshTabList(player);
        data.setDirty();
        return 1;
    }

    private static int promotePlayer(ServerPlayer player, String targetName) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null || clan.getMembers().get(player.getUUID()).getRank() != ClanRank.OWNER) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.only_owner_promote"));
            return 0;
        }

        for (ClanMember m : clan.getMembers().values()) {
            if (m.getName().equalsIgnoreCase(targetName)) {
                if (m.getRank() == ClanRank.MEMBER) {
                    m.setRank(ClanRank.MANAGER);
                    player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.promoted", m.getName()));
                    data.setDirty();
                    return 1;
                }
            }
        }
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.not_eligible_promotion"));
        return 0;
    }

    private static int demotePlayer(ServerPlayer player, String targetName) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null || clan.getMembers().get(player.getUUID()).getRank() != ClanRank.OWNER) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.only_owner_demote"));
            return 0;
        }

        for (ClanMember m : clan.getMembers().values()) {
            if (m.getName().equalsIgnoreCase(targetName)) {
                if (m.getRank() == ClanRank.MANAGER) {
                    m.setRank(ClanRank.MEMBER);
                    player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.demoted", m.getName()));
                    data.setDirty();
                    return 1;
                }
            }
        }
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.not_eligible_demotion"));
        return 0;
    }

    private static int openClanChest(ServerPlayer player) {
        ClanSavedData data = ClanSavedData.get(player.serverLevel());
        Clan clan = data.getClanByPlayer(player.getUUID());

        if (clan == null) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.you_not_in_clan"));
            return 0;
        }

        if (clan.getLevel() < 3) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.chest_level_required"));
            return 0;
        }

        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (syncId, playerInventory, playerEntity) ->
                        net.minecraft.world.inventory.ChestMenu.sixRows(syncId, playerInventory, clan.getChest()),
                Component.translatable("message.colisao-cobblemon.chest_title", clan.getName())
        ));

        return 1;
    }

    private static int showHelp(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.header"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.clan"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.criar"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.convidar"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.aceitar_recusar"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.sair"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.sethome"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.home"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.chest"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.bonus"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.expulsar"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.promover"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.demover"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.deletar"));
        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.chat"));

        if (player.hasPermissions(2)) {
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.admin_header"));
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.admin_deletar"));
            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.admin_darxp"));
        }

        player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.help.footer"));
        return 1;
    }


    private static int adminDeleteClan(CommandSourceStack source, String name) {
        ClanSavedData data = ClanSavedData.get(source.getLevel());
        Clan clan = data.getClanByName(name);

        if (clan == null) {
            source.sendFailure(Component.translatable("message.colisao-cobblemon.admin_clan_not_found"));
            return 0;
        }

        data.removeClan(name);
        source.sendSuccess(() -> Component.translatable("message.colisao-cobblemon.admin_deleted", name), true);
        return 1;
    }

    private static int adminGiveXp(CommandSourceStack source, String name, int amount) {
        ClanSavedData data = ClanSavedData.get(source.getLevel());
        Clan clan = data.getClanByName(name);

        if (clan == null) {
            source.sendFailure(Component.translatable("message.colisao-cobblemon.admin_clan_not_found"));
            return 0;
        }

        clan.addXp(amount);
        data.setDirty();
        source.sendSuccess(() -> Component.translatable("message.colisao-cobblemon.admin_xp_given", amount, clan.getName(), clan.getLevel()), true);
        return 1;
    }
}