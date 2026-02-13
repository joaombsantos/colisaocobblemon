package me.marcronte.colisaocobblemon.features.breeding;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityPool;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import me.marcronte.colisaocobblemon.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class BreedingCalculator {

    private static final Map<Item, String> INCENSE_BABIES = Map.of(
            ModItems.SEA_INCENSE, "azurill",
            ModItems.LAX_INCENSE, "wynaut",
            ModItems.ROSE_INCENSE, "budew",
            ModItems.PURE_INCENSE, "chingling",
            ModItems.ROCK_INCENSE, "bonsly",
            ModItems.ODD_INCENSE, "mimejr",
            ModItems.LUCK_INCENSE, "happiny",
            ModItems.FULL_INCENSE, "munchlax",
            ModItems.WAVE_INCENSE, "mantyke"
    );

    private static final Map<String, Stats> POWER_ITEMS_MAP = Map.of(
            "cobblemon:power_weight", Stats.HP,
            "cobblemon:power_bracer", Stats.ATTACK,
            "cobblemon:power_belt", Stats.DEFENCE,
            "cobblemon:power_lens", Stats.SPECIAL_ATTACK,
            "cobblemon:power_band", Stats.SPECIAL_DEFENCE,
            "cobblemon:power_anklet", Stats.SPEED
    );

    public static Pokemon createOffspring(Pokemon parentA, Pokemon parentB) {
        if (!canBreed(parentA, parentB)) return null;

        Pokemon mother = determineMother(parentA, parentB);
        Pokemon father = (mother == parentA) ? parentB : parentA;

        Species childSpecies = determineChildSpecies(mother, father);
        if (childSpecies == null) return null;

        Pokemon offspring = childSpecies.create(1);

        inheritNature(offspring, mother, father);
        inheritAbility(offspring, mother, father);
        inheritIVs(offspring, mother, father);

        rollShiny(offspring, mother, father);

        offspring.setCaughtBall(mother.getCaughtBall());
        offspring.heal();

        return offspring;
    }

    private static boolean canBreed(Pokemon a, Pokemon b) {
        if (a.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED) ||
                b.getForm().getEggGroups().contains(EggGroup.UNDISCOVERED)) {
            return false;
        }

        boolean aIsDitto = a.getSpecies().getName().equalsIgnoreCase("ditto");
        boolean bIsDitto = b.getSpecies().getName().equalsIgnoreCase("ditto");

        if (aIsDitto && bIsDitto) return false;

        if (aIsDitto || bIsDitto) return true;

        if (a.getGender() == Gender.GENDERLESS || b.getGender() == Gender.GENDERLESS) return false;
        if (a.getGender() == b.getGender()) return false;

        Set<EggGroup> groupsA = a.getForm().getEggGroups();
        Set<EggGroup> groupsB = b.getForm().getEggGroups();

        return !Collections.disjoint(groupsA, groupsB);
    }

    private static Pokemon determineMother(Pokemon a, Pokemon b) {
        boolean aIsDitto = a.getSpecies().getName().equalsIgnoreCase("ditto");
        boolean bIsDitto = b.getSpecies().getName().equalsIgnoreCase("ditto");

        if (aIsDitto) return b;
        if (bIsDitto) return a;

        return (a.getGender() == Gender.FEMALE) ? a : b;
    }

    private static Species determineChildSpecies(Pokemon mother, Pokemon father) {
        Species current = mother.getSpecies();
        while (current.getPreEvolution() != null) {
            current = current.getPreEvolution().getSpecies();
        }
        Species baseSpecies = current;

        for (Map.Entry<Item, String> entry : INCENSE_BABIES.entrySet()) {
            String babyName = entry.getValue();
            Item incense = entry.getKey();

            if (baseSpecies.getName().equalsIgnoreCase(babyName)) {
                boolean motherHas = isHolding(mother, incense);
                boolean fatherHas = isHolding(father, incense);

                if (!motherHas && !fatherHas) {
                    Collection<Evolution> evos = baseSpecies.getStandardForm().getEvolutions();
                    if (!evos.isEmpty()) {
                        Evolution firstEvo = evos.iterator().next();

                        String speciesStr = Objects.requireNonNull(firstEvo.getResult().getSpecies());
                        Species evoSpecies = PokemonSpecies.getByName(speciesStr);

                        if (evoSpecies != null) return evoSpecies;
                    }
                }
            }
        }
        return baseSpecies;
    }

    private static boolean isHolding(Pokemon p, Item item) {
        return p.heldItem().getItem() == item;
    }

    private static boolean isHoldingItem(Pokemon p, String itemId) {
        return BuiltInRegistries.ITEM.getKey(p.heldItem().getItem()).toString().equals(itemId);
    }

    private static void inheritNature(Pokemon child, Pokemon mother, Pokemon father) {
        boolean motherStone = isHoldingItem(mother, "cobblemon:everstone");
        boolean fatherStone = isHoldingItem(father, "cobblemon:everstone");

        if (motherStone && fatherStone) {
            child.setNature(new Random().nextBoolean() ? mother.getNature() : father.getNature());
        } else if (motherStone) {
            child.setNature(mother.getNature());
        } else if (fatherStone) {
            child.setNature(father.getNature());
        }
    }

    private static void inheritAbility(Pokemon child, Pokemon mother, Pokemon father) {
        AbilityTemplate motherTemplate = mother.getAbility().getTemplate();
        AbilityPool pool = child.getForm().getAbilities();

        List<PotentialAbility> allPotentials = new ArrayList<>();
        pool.getMapping().values().forEach(allPotentials::addAll);

        PotentialAbility hiddenInChild = null;
        List<AbilityTemplate> normalAbilities = new ArrayList<>();

        for (PotentialAbility pot : allPotentials) {
            if (pot.getPriority() == Priority.LOW) {
                hiddenInChild = pot;
            } else {
                normalAbilities.add(pot.getTemplate());
            }
        }

        Random rand = new Random();

        boolean motherHasHidden = hiddenInChild != null && hiddenInChild.getTemplate().equals(motherTemplate);
        boolean motherHasNormal = normalAbilities.contains(motherTemplate);

        boolean passMotherAbility = rand.nextInt(100) < 60;

        if (passMotherAbility) {
            if (motherHasHidden) {
                child.updateAbility(new Ability(motherTemplate, false, Priority.LOW));
                return;
            }
            else if (motherHasNormal) {
                child.updateAbility(new Ability(motherTemplate, false, Priority.NORMAL));
                return;
            }
        }


        if (!normalAbilities.isEmpty()) {
            AbilityTemplate selected = normalAbilities.get(rand.nextInt(normalAbilities.size()));
            child.updateAbility(new Ability(selected, false, Priority.NORMAL));
        }
    }

    private static void inheritIVs(Pokemon child, Pokemon mother, Pokemon father) {
        IVs childIVs = child.getIvs();
        List<Stats> availableStats = new ArrayList<>(List.of(
                Stats.HP, Stats.ATTACK, Stats.DEFENCE,
                Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
        ));

        Pokemon destinyKnotHolder = null;
        if (isHoldingItem(mother, "cobblemon:destiny_knot")) {
            destinyKnotHolder = mother;
        }
        if (destinyKnotHolder == null && isHoldingItem(father, "cobblemon:destiny_knot")) {
            destinyKnotHolder = father;
        }
        if (isHoldingItem(mother, "cobblemon:destiny_knot") && isHoldingItem(father, "cobblemon:destiny_knot")) {
            destinyKnotHolder = new Random().nextBoolean() ? mother : father;
        }

        Stats motherPowerStat = getPowerItemStat(mother.heldItem());
        Stats fatherPowerStat = getPowerItemStat(father.heldItem());

        Stats powerItemStat = null;
        Pokemon powerItemProvider = null;

        if (destinyKnotHolder != null) {
            if (destinyKnotHolder == mother && fatherPowerStat != null) {
                powerItemStat = fatherPowerStat;
                powerItemProvider = father;
            } else if (destinyKnotHolder == father && motherPowerStat != null) {
                powerItemStat = motherPowerStat;
                powerItemProvider = mother;
            } else {
                Stats holderPower = (destinyKnotHolder == mother) ? motherPowerStat : fatherPowerStat;
                if (holderPower != null) {
                    powerItemStat = holderPower;
                    powerItemProvider = destinyKnotHolder;
                }
            }
        } else {
            if (motherPowerStat != null) {
                powerItemStat = motherPowerStat;
                powerItemProvider = mother;
            } else if (fatherPowerStat != null) {
                powerItemStat = fatherPowerStat;
                powerItemProvider = father;
            }
        }


        int inheritedCount = 0;
        int maxInheritance = (destinyKnotHolder != null) ? 5 : 3;

        if (powerItemStat != null && powerItemProvider != null) {
            int val = powerItemProvider.getIvs().getOrDefault(powerItemStat);
            childIVs.set(powerItemStat, val);

            availableStats.remove(powerItemStat);
            inheritedCount++;
        }

        Collections.shuffle(availableStats);
        Random rand = new Random();

        while (inheritedCount < maxInheritance && !availableStats.isEmpty()) {
            Stats stat = availableStats.remove(0);

            if (destinyKnotHolder != null) {
                int val = destinyKnotHolder.getIvs().getOrDefault(stat);
                childIVs.set(stat, val);
            } else {
                Pokemon donor = rand.nextBoolean() ? mother : father;
                childIVs.set(stat, donor.getIvs().getOrDefault(stat));
            }
            inheritedCount++;
        }
    }

    private static Stats getPowerItemStat(ItemStack stack) {
        if (stack.isEmpty()) return null;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return POWER_ITEMS_MAP.get(id);
    }

    private static void rollShiny(Pokemon child, Pokemon mother, Pokemon father) {
        Random rand = new Random();

        int chance = 4096;

        child.setShiny(rand.nextInt(chance) == 0);
    }
}