package me.marcronte.colisaocobblemon.network;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.marcronte.colisaocobblemon.ModItems;
import me.marcronte.colisaocobblemon.features.breeding.BreedingCalculator;
import me.marcronte.colisaocobblemon.features.breeding.BreedingData;
import me.marcronte.colisaocobblemon.network.payloads.BreedingButtonPayload;
import me.marcronte.colisaocobblemon.network.payloads.BreedingSelectPayload;
import me.marcronte.colisaocobblemon.network.payloads.BreedingSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.UUID;

public class BreedingNetwork {
    public static final ResourceLocation SYNC_SCREEN = ResourceLocation.parse("colisao-cobblemon:breeding_sync");
    public static final ResourceLocation CLICK_BUTTON = ResourceLocation.parse("colisao-cobblemon:breeding_click");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(BreedingButtonPayload.ID, (payload, context) ->
                context.server().execute(() -> handleButtonClick(context.player(), payload.buttonId())));

        ServerPlayNetworking.registerGlobalReceiver(BreedingSelectPayload.ID, (payload, context) ->
                context.server().execute(() -> handleSelection(context.player(), payload.slot(), payload.pokemonUuid())));
    }

    public static void openBreedingScreen(ServerPlayer player) {
        BreedingData data = BreedingData.get(player.serverLevel());
        BreedingData.PlayerBreedingInfo info = data.getPlayerInfo(player.getUUID());

        String mSpecies = null;
        String fSpecies = null;

        if (info.isActive) {
            if (info.motherData != null) mSpecies = info.motherData.getString("Species");
            if (info.fatherData != null) fSpecies = info.fatherData.getString("Species");
        } else {
            Pokemon m = data.getPokemon(player, info.motherId);
            if (m != null) mSpecies = m.getSpecies().resourceIdentifier.toString();

            Pokemon f = data.getPokemon(player, info.fatherId);
            if (f != null) fSpecies = f.getSpecies().resourceIdentifier.toString();
        }

        ServerPlayNetworking.send(player, new BreedingSyncPayload(
                info.motherId,
                info.fatherId,
                mSpecies,
                fSpecies,
                info.startTime,
                BreedingData.PlayerBreedingInfo.BREEDING_DURATION,
                info.isActive,
                info.isReady()
        ));
    }


    private static void handleButtonClick(ServerPlayer player, int buttonId) {
        if (player == null) return;

        ServerLevel level = player.serverLevel();
        BreedingData data = BreedingData.get(level);
        BreedingData.PlayerBreedingInfo info = data.getPlayerInfo(player.getUUID());

        if (buttonId == 2) {
            if (info.isActive) return;

            Pokemon mother = data.getPokemon(player, info.motherId);
            Pokemon father = data.getPokemon(player, info.fatherId);

            if (mother == null || father == null) {
                player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.breeding_select_pokemon").withStyle(ChatFormatting.RED));
                return;
            }

            if (BreedingCalculator.createOffspring(mother, father) == null) {
                player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.breeding_incompatible").withStyle(ChatFormatting.RED));
                return;
            }

            info.motherData = savePokemonSecurely(mother, player);
            info.fatherData = savePokemonSecurely(father, player);

            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            party.remove(mother);
            party.remove(father);

            info.isActive = true;
            info.startTime = System.currentTimeMillis();
            data.setDirty();

            player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.breeding_started").withStyle(ChatFormatting.GREEN));
            openBreedingScreen(player);
        }
        else if (buttonId == 3) {
            if (!info.isReady()) return;

            Pokemon mother = reconstructPokemon(info.motherData, player.registryAccess());
            Pokemon father = reconstructPokemon(info.fatherData, player.registryAccess());

            if (mother != null && father != null) {
                Pokemon child = BreedingCalculator.createOffspring(mother, father);

                if (child != null) {
                    ItemStack eggStack = new ItemStack(ModItems.POKEMON_EGG);
                    CompoundTag tag = new CompoundTag();
                    CompoundTag pokemonData = new CompoundTag();
                    child.saveToNBT(player.registryAccess(), pokemonData);
                    tag.put("PokemonData", pokemonData);
                    tag.putString("SpeciesIdentifier", child.getSpecies().resourceIdentifier.toString());
                    tag.putString("NatureInternal", child.getNature().getName().getPath());
                    tag.putString("AbilityInternal", child.getAbility().getTemplate().getName());
                    tag.putBoolean("IsShiny", child.getShiny());
                    tag.putInt("IV_HP", child.getIvs().getOrDefault(Stats.HP));
                    tag.putInt("IV_ATK", child.getIvs().getOrDefault(Stats.ATTACK));
                    tag.putInt("IV_DEF", child.getIvs().getOrDefault(Stats.DEFENCE));
                    tag.putInt("IV_SPA", child.getIvs().getOrDefault(Stats.SPECIAL_ATTACK));
                    tag.putInt("IV_SPD", child.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE));
                    tag.putInt("IV_SPE", child.getIvs().getOrDefault(Stats.SPEED));

                    int cycles = child.getSpecies().getEggCycles();
                    if (cycles <= 0) cycles = 20;
                    int eggCycles = 100;
                    tag.putInt("RemainingSteps", cycles * eggCycles);
                    tag.putFloat("LastKnownWalkDist", player.walkDist);

                    CustomData.set(DataComponents.CUSTOM_DATA, eggStack, tag);

                    givePokemonOrToPC(player, mother);
                    givePokemonOrToPC(player, father);

                    if (!player.getInventory().add(eggStack)) {
                        player.drop(eggStack, false);
                    }

                    player.sendSystemMessage(Component.translatable("message.colisao-cobblemon.breeding_concluded").withStyle(ChatFormatting.GOLD));

                    info.isActive = false;
                    info.startTime = 0;
                    info.motherId = null;
                    info.fatherId = null;
                    info.motherData = null;
                    info.fatherData = null;
                    data.setDirty();

                    ServerPlayNetworking.send(player, new BreedingSyncPayload(
                            null, null, null, null, 0, BreedingData.PlayerBreedingInfo.BREEDING_DURATION, false, false
                    ));
                }
            }
        }
    }

    public static void givePokemonOrToPC(ServerPlayer player, Pokemon pokemon) {
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        if (party.add(pokemon)) {
            player.sendSystemMessage(Component.translatable(pokemon.getSpecies().getName(), "message.colisao-cobblemon.pokemon_came_back").withStyle(ChatFormatting.GREEN));
        } else {
            Cobblemon.INSTANCE.getStorage().getPC(player).add(pokemon);
            player.sendSystemMessage(Component.translatable(pokemon.getSpecies().getName(), "message.colisao-cobblemon.pokemon_sent_pc").withStyle(ChatFormatting.YELLOW));
        }
    }

    private static void handleSelection(ServerPlayer player, int slot, UUID pokemonUuid) {
        if (player == null) return;
        BreedingData data = BreedingData.get(player.serverLevel());
        BreedingData.PlayerBreedingInfo info = data.getPlayerInfo(player.getUUID());
        if (info.isActive) return;
        if (slot == 0) info.motherId = pokemonUuid;
        else if (slot == 1) info.fatherId = pokemonUuid;
        data.setDirty();
        openBreedingScreen(player);
    }

    public static CompoundTag savePokemonSecurely(Pokemon pokemon, ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        pokemon.saveToNBT(player.registryAccess(), tag);

        tag.putString("Species", pokemon.getSpecies().resourceIdentifier.toString());
        tag.putInt("Level", pokemon.getLevel());
        tag.putInt("Experience", pokemon.getExperience());
        tag.putString("Gender", pokemon.getGender().toString());
        tag.putString("Form", pokemon.getForm().getName());
        tag.putBoolean("Shiny", pokemon.getShiny());
        tag.putInt("Friendship", pokemon.getFriendship());
        tag.putString("Nature", pokemon.getNature().getName().toString());
        tag.putString("Ability", pokemon.getAbility().getTemplate().getName());

        if (pokemon.getTeraType() != null) {
            tag.putString("TeraType", pokemon.getTeraType().getName());
        }

        if (!pokemon.heldItem().isEmpty()) {
            tag.put("HeldItemBackup", pokemon.heldItem().save(player.registryAccess()));
        }

        // IVS
        CompoundTag ivsTag = new CompoundTag();
        ivsTag.putInt("HP", pokemon.getIvs().getOrDefault(Stats.HP));
        ivsTag.putInt("ATK", pokemon.getIvs().getOrDefault(Stats.ATTACK));
        ivsTag.putInt("DEF", pokemon.getIvs().getOrDefault(Stats.DEFENCE));
        ivsTag.putInt("SPA", pokemon.getIvs().getOrDefault(Stats.SPECIAL_ATTACK));
        ivsTag.putInt("SPD", pokemon.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE));
        ivsTag.putInt("SPE", pokemon.getIvs().getOrDefault(Stats.SPEED));
        tag.put("IVsBackup", ivsTag);

        // EVS
        CompoundTag evsTag = new CompoundTag();
        evsTag.putInt("HP", pokemon.getEvs().getOrDefault(Stats.HP));
        evsTag.putInt("ATK", pokemon.getEvs().getOrDefault(Stats.ATTACK));
        evsTag.putInt("DEF", pokemon.getEvs().getOrDefault(Stats.DEFENCE));
        evsTag.putInt("SPA", pokemon.getEvs().getOrDefault(Stats.SPECIAL_ATTACK));
        evsTag.putInt("SPD", pokemon.getEvs().getOrDefault(Stats.SPECIAL_DEFENCE));
        evsTag.putInt("SPE", pokemon.getEvs().getOrDefault(Stats.SPEED));
        tag.put("EVsBackup", evsTag);

        ListTag movesList = new ListTag();
        List<Move> currentMoves = pokemon.getMoveSet().getMoves();
        for (int i = 0; i < 4; i++) {
            if (i < currentMoves.size()) {
                Move move = currentMoves.get(i);
                if (move != null) {
                    movesList.add(StringTag.valueOf(move.getTemplate().getName()));
                } else {
                    movesList.add(StringTag.valueOf("EMPTY"));
                }
            } else {
                movesList.add(StringTag.valueOf("EMPTY"));
            }
        }
        tag.put("MovesBackup", movesList);

        ListTag benchedList = new ListTag();
        for (BenchedMove bm : pokemon.getBenchedMoves()) {
            benchedList.add(StringTag.valueOf(bm.getMoveTemplate().getName()));
        }
        tag.put("BenchedMovesBackup", benchedList);

        return tag;
    }

    public static Pokemon reconstructPokemon(CompoundTag tag, net.minecraft.core.RegistryAccess registryAccess) {
        if (tag == null || tag.isEmpty()) return null;

        String speciesStr = tag.contains("Species") ? tag.getString("Species") : "";
        if (speciesStr.isEmpty() && tag.contains("species")) speciesStr = tag.getString("species");

        int level = tag.contains("Level") ? tag.getInt("Level") : 1;

        Pokemon p;
        try {
            if (!speciesStr.isEmpty()) {
                p = PokemonProperties.Companion.parse("species=" + speciesStr + " level=" + level).create();
            } else {
                p = new Pokemon();
            }
        } catch (Exception e) {
            p = new Pokemon();
        }

        p.loadFromNBT(registryAccess, tag);


        if (tag.contains("Experience")) {
            PokemonProperties.Companion.parse("xp=" + tag.getInt("Experience")).apply(p);
        }

        if (tag.contains("Friendship")) {
            PokemonProperties.Companion.parse("friendship=" + tag.getInt("Friendship")).apply(p);
        }

        if (tag.contains("Shiny")) {
            p.setShiny(tag.getBoolean("Shiny"));
        }

        if (tag.contains("Gender")) {
            String g = tag.getString("Gender");
            if (g.equalsIgnoreCase("MALE")) p.setGender(com.cobblemon.mod.common.pokemon.Gender.MALE);
            else if (g.equalsIgnoreCase("FEMALE")) p.setGender(com.cobblemon.mod.common.pokemon.Gender.FEMALE);
            else p.setGender(com.cobblemon.mod.common.pokemon.Gender.GENDERLESS);
        }

        if (tag.contains("Form")) {
            PokemonProperties.Companion.parse("form=" + tag.getString("Form")).apply(p);
        }

        if (tag.contains("Nature")) {
            PokemonProperties.Companion.parse("nature=" + tag.getString("Nature")).apply(p);
        }

        if (tag.contains("Ability")) {
            PokemonProperties.Companion.parse("ability=" + tag.getString("Ability")).apply(p);
        }

        if (tag.contains("TeraType")) {
            PokemonProperties.Companion.parse("tera_type=" + tag.getString("TeraType")).apply(p);
        }

        if (tag.contains("HeldItemBackup")) {
            ItemStack held = ItemStack.parseOptional(registryAccess, tag.getCompound("HeldItemBackup"));
            if (!held.isEmpty()) {
                p.swapHeldItem(held, false, false);
            }
        }

        if (tag.contains("IVsBackup")) {
            CompoundTag t = tag.getCompound("IVsBackup");
            p.getIvs().set(Stats.HP, t.getInt("HP"));
            p.getIvs().set(Stats.ATTACK, t.getInt("ATK"));
            p.getIvs().set(Stats.DEFENCE, t.getInt("DEF"));
            p.getIvs().set(Stats.SPECIAL_ATTACK, t.getInt("SPA"));
            p.getIvs().set(Stats.SPECIAL_DEFENCE, t.getInt("SPD"));
            p.getIvs().set(Stats.SPEED, t.getInt("SPE"));
        }

        if (tag.contains("EVsBackup")) {
            CompoundTag t = tag.getCompound("EVsBackup");
            p.getEvs().set(Stats.HP, t.getInt("HP"));
            p.getEvs().set(Stats.ATTACK, t.getInt("ATK"));
            p.getEvs().set(Stats.DEFENCE, t.getInt("DEF"));
            p.getEvs().set(Stats.SPECIAL_ATTACK, t.getInt("SPA"));
            p.getEvs().set(Stats.SPECIAL_DEFENCE, t.getInt("SPD"));
            p.getEvs().set(Stats.SPEED, t.getInt("SPE"));
        }

        p.getMoveSet().getMoves().clear();
        p.getBenchedMoves().clear();

        if (tag.contains("BenchedMovesBackup")) {
            ListTag benchedList = (ListTag) tag.get("BenchedMovesBackup");
            if (benchedList != null) {
                for (int i = 0; i < benchedList.size(); i++) {
                    String moveName = benchedList.getString(i);
                    MoveTemplate template = Moves.getByName(moveName);
                    if (template != null) {
                        BenchedMove bm = new BenchedMove(template, 0);
                        p.getBenchedMoves().add(bm);
                    }
                }
            }
        }

        if (tag.contains("MovesBackup")) {
            ListTag movesList = (ListTag) tag.get("MovesBackup");
            if (movesList != null && !movesList.isEmpty()) {
                for (int i = 0; i < 4; i++) {
                    if (i >= movesList.size()) break;

                    String moveId = movesList.getString(i);

                    if (moveId.equals("EMPTY")) {
                        p.getMoveSet().getMoves().set(i, null);
                    } else {
                        MoveTemplate template = Moves.getByName(moveId);
                        if (template != null) {
                            Move move = new Move(template, template.getMaxPp(), 0);

                            boolean knowsMove = false;
                            for (BenchedMove bm : p.getBenchedMoves()) {
                                if (bm.getMoveTemplate().getName().equals(template.getName())) {
                                    knowsMove = true;
                                    break;
                                }
                            }

                            if (!knowsMove) {
                                p.getBenchedMoves().add(new BenchedMove(template, 0));
                            }

                            p.getMoveSet().getMoves().set(i, move);
                        }
                    }
                }
            }
        }

        p.heal();
        return p;
    }
}