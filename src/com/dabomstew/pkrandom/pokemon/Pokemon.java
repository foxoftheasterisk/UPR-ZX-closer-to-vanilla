package com.dabomstew.pkrandom.pokemon;

/*----------------------------------------------------------------------------*/
/*--  Pokemon.java - represents an individual Pokemon, and contains         --*/
/*--                 common Pokemon-related functions.                      --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.dabomstew.pkrandom.constants.Species;
import javafx.util.Pair;

import java.util.*;

public class Pokemon implements Comparable<Pokemon> {

    public String name;
    public int number;

    public String formeSuffix = "";
    public Pokemon baseForme = null;
    public int formeNumber = 0;
    public int cosmeticForms = 0;
    public int formeSpriteIndex = 0;
    public boolean actuallyCosmetic = false;
    public List<Integer> realCosmeticFormNumbers = new ArrayList<>();

    public Type primaryType, secondaryType;

    //best practices says these should be private with a public get,
    //but I can't be arsed.
    //(If it was C#, I would, but...)
    public Type originalPrimaryType, originalSecondaryType;
    private PokemonSet originalEvolvedForms, originalPreEvolvedForms;

    public int hp, attack, defense, spatk, spdef, speed, special;

    public int ability1, ability2, ability3;

    public int catchRate, expYield;

    public int guaranteedHeldItem, commonHeldItem, rareHeldItem, darkGrassHeldItem;

    public int genderRatio;

    public int frontSpritePointer, picDimensions;

    public int callRate;

    public ExpCurve growthCurve;

    public List<Evolution> evolutionsFrom = new ArrayList<>();
    public List<Evolution> evolutionsTo = new ArrayList<>();

    public List<MegaEvolution> megaEvolutionsFrom = new ArrayList<>();
    public List<MegaEvolution> megaEvolutionsTo = new ArrayList<>();

    protected List<Integer> shuffledStatsOrder;

    // A flag to use for things like recursive stats copying.
    // Must not rely on the state of this flag being preserved between calls.
    public boolean temporaryFlag;

    public Pokemon() {
        shuffledStatsOrder = Arrays.asList(0, 1, 2, 3, 4, 5);
    }

    public void shuffleStats(Random random) {
        Collections.shuffle(shuffledStatsOrder, random);
        applyShuffledOrderToStats();
    }
    
    public void copyShuffledStatsUpEvolution(Pokemon evolvesFrom) {
        // If stats were already shuffled once, un-shuffle them
        shuffledStatsOrder = Arrays.asList(
                shuffledStatsOrder.indexOf(0),
                shuffledStatsOrder.indexOf(1),
                shuffledStatsOrder.indexOf(2),
                shuffledStatsOrder.indexOf(3),
                shuffledStatsOrder.indexOf(4),
                shuffledStatsOrder.indexOf(5));
        applyShuffledOrderToStats();
        shuffledStatsOrder = evolvesFrom.shuffledStatsOrder;
        applyShuffledOrderToStats();
    }

    protected void applyShuffledOrderToStats() {
        List<Integer> stats = Arrays.asList(hp, attack, defense, spatk, spdef, speed);

        // Copy in new stats
        hp = stats.get(shuffledStatsOrder.get(0));
        attack = stats.get(shuffledStatsOrder.get(1));
        defense = stats.get(shuffledStatsOrder.get(2));
        spatk = stats.get(shuffledStatsOrder.get(3));
        spdef = stats.get(shuffledStatsOrder.get(4));
        speed = stats.get(shuffledStatsOrder.get(5));
    }

    public void randomizeStatsWithinBST(Random random) {
        if (number == Species.shedinja) {
            // Shedinja is horribly broken unless we restrict him to 1HP.
            int bst = bst() - 51;

            // Make weightings
            double atkW = random.nextDouble(), defW = random.nextDouble();
            double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

            double totW = atkW + defW + spaW + spdW + speW;

            hp = 1;
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;
        } else {
            // Minimum 20 HP, 10 everything else
            int bst = bst() - 70;

            // Make weightings
            double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
            double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

            double totW = hpW + atkW + defW + spaW + spdW + speW;

            hp = (int) Math.max(1, Math.round(hpW / totW * bst)) + 20;
            attack = (int) Math.max(1, Math.round(atkW / totW * bst)) + 10;
            defense = (int) Math.max(1, Math.round(defW / totW * bst)) + 10;
            spatk = (int) Math.max(1, Math.round(spaW / totW * bst)) + 10;
            spdef = (int) Math.max(1, Math.round(spdW / totW * bst)) + 10;
            speed = (int) Math.max(1, Math.round(speW / totW * bst)) + 10;
        }

        // Check for something we can't store
        if (hp > 255 || attack > 255 || defense > 255 || spatk > 255 || spdef > 255 || speed > 255) {
            // re roll
            randomizeStatsWithinBST(random);
        }

    }

    public void copyRandomizedStatsUpEvolution(Pokemon evolvesFrom) {
        double ourBST = bst();
        double theirBST = evolvesFrom.bst();

        double bstRatio = ourBST / theirBST;

        hp = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.hp * bstRatio)));
        attack = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.attack * bstRatio)));
        defense = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.defense * bstRatio)));
        speed = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.speed * bstRatio)));
        spatk = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spatk * bstRatio)));
        spdef = (int) Math.min(255, Math.max(1, Math.round(evolvesFrom.spdef * bstRatio)));
    }

    public void assignNewStatsForEvolution(Pokemon evolvesFrom, Random random) {

        double ourBST = bst();
        double theirBST = evolvesFrom.bst();

        double bstDiff = ourBST - theirBST;

        // Make weightings
        double hpW = random.nextDouble(), atkW = random.nextDouble(), defW = random.nextDouble();
        double spaW = random.nextDouble(), spdW = random.nextDouble(), speW = random.nextDouble();

        double totW = hpW + atkW + defW + spaW + spdW + speW;

        double hpDiff = Math.round((hpW / totW) * bstDiff);
        double atkDiff = Math.round((atkW / totW) * bstDiff);
        double defDiff = Math.round((defW / totW) * bstDiff);
        double spaDiff = Math.round((spaW / totW) * bstDiff);
        double spdDiff = Math.round((spdW / totW) * bstDiff);
        double speDiff = Math.round((speW / totW) * bstDiff);

        hp = (int) Math.min(255, Math.max(1, evolvesFrom.hp + hpDiff));
        attack = (int) Math.min(255, Math.max(1, evolvesFrom.attack + atkDiff));
        defense = (int) Math.min(255, Math.max(1, evolvesFrom.defense + defDiff));
        speed = (int) Math.min(255, Math.max(1, evolvesFrom.speed + speDiff));
        spatk = (int) Math.min(255, Math.max(1, evolvesFrom.spatk + spaDiff));
        spdef = (int) Math.min(255, Math.max(1, evolvesFrom.spdef + spdDiff));
    }

    protected int bst() {
        return hp + attack + defense + spatk + spdef + speed;
    }

    public int bstForPowerLevels() {
        // Take into account Shedinja's purposefully nerfed HP
        if (number == Species.shedinja) {
            return (attack + defense + spatk + spdef + speed) * 6 / 5;
        } else {
            return hp + attack + defense + spatk + spdef + speed;
        }
    }

    public double getAttackSpecialAttackRatio() {
        return (double)attack / ((double)attack + (double)spatk);
    }

    public int getBaseNumber() {
        Pokemon base = this;
        while (base.baseForme != null) {
            base = base.baseForme;
        }
        return base.number;
    }

    /**
     * Gets all Pokemon that this Pokemon can evolve directly into.
     * @return A PokemonSet containing all possible evolved forms of this Pokemon.
     */
    public PokemonSet getAllEvolvedPokemon() {
        PokemonSet evolvedPokemon = new PokemonSet();
        for(Evolution evo : evolutionsFrom) {
            evolvedPokemon.add(evo.to);
        }
        return evolvedPokemon;
    }

    /**
     * Gets all Pokemon that can evolve directly into this Pokemon.
     * @return A PokemonSet containing all pre-evolved forms of this Pokemon.
     */
    public PokemonSet getAllPreEvolvedPokemon() {
        PokemonSet evolvedPokemon = new PokemonSet();
        for(Evolution evo : evolutionsTo) {
            evolvedPokemon.add(evo.from);
        }
        return evolvedPokemon;
    }

    /**
     * Gets all Pokemon that this Pokemon could evolve directly into before randomization.
     * @return A PokemonSet containing all possible evolved forms of this Pokemon.
     */
    public PokemonSet getOriginalEvolvedForms() {
        return new PokemonSet(this.originalEvolvedForms);
    }

    /**
     * Gets all Pokemon that could evolve directly into this Pokemon before randomization.
     * @return A PokemonSet containing all pre-evolved forms of this Pokemon.
     */
    public PokemonSet getOriginalPreEvolvedForms() {
        return new PokemonSet(this.originalPreEvolvedForms);
    }

    /**
     * Gets all Pokemon that this Pokemon is related to by evolution.
     * @return a PokemonSet containing all Pokemon this Pokemon is related to (including itself)
     */
    public PokemonSet getAllRelatedPokemon() {
        PokemonSet family = new PokemonSet();
        family.addFamily(this);
        return family;
    }

    /**
     * Gets all Pokemon that this Pokemon was related to by evolution before randomization.
     * @return a PokemonSet containing all Pokemon this Pokemon was related to (including itself)
     */
    public PokemonSet getAllOriginallyRelatedPokemon() {
        PokemonSet family = new PokemonSet();
        family.addOriginalFamily(this);
        return family;
    }

    /**
     * Gets the relative position of the given Pokemon in the evolutionary family.
     * If the family is a cycle, will return the closest path. This is not necessarily
     * the lowest absolute value.
     * @return A number indicating the relative position of the given Pokemon.
     *         For example, if the given Pokemon evolves directly into this Pokemon,
     *         the number will be -1.
     * @throws IllegalArgumentException if the Pokemon are not related.
     */
    public int getRelation(Pokemon relative) {
        Queue<Pair<Pokemon, Integer>> toCheck = new ArrayDeque<>();
        PokemonSet checked = new PokemonSet();
        toCheck.add(new Pair<>(this, 0));

        while(!toCheck.isEmpty()) {
            Pair<Pokemon, Integer> current = toCheck.remove();
            Pokemon currentPokemon = current.getKey();
            int currentPosition = current.getValue();
            if(checked.contains(currentPokemon)) {
                continue;
            }
            checked.add(currentPokemon);

            if(currentPokemon == relative) {
                return currentPosition;
            }

            for(Pokemon evo : currentPokemon.getAllEvolvedPokemon()) {
                toCheck.add(new Pair<>(evo, currentPosition + 1));
            }
            for(Pokemon evo : currentPokemon.getAllPreEvolvedPokemon()) {
                toCheck.add(new Pair<>(evo, currentPosition - 1));
            }
        }

        throw new IllegalArgumentException("Cannot find relation of a non-related Pokemon!");
    }

    /**
     * Gets the relative position of the given Pokemon in the evolutionary family, as
     * it was before randomization. If the family is a cycle, will return the closest
     * path. (This is not necessarily the lowest absolute value.)
     * @return A number indicating the relative position of the given Pokemon.
     *         For example, if the given Pokemon evolved directly into this Pokemon,
     *         the number will be -1.
     * @throws IllegalArgumentException if the Pokemon are not related.
     */
    public int getOriginalRelation(Pokemon relative) {
        Queue<Pair<Pokemon, Integer>> toCheck = new ArrayDeque<>();
        PokemonSet checked = new PokemonSet();
        toCheck.add(new Pair<>(this, 0));

        while(!toCheck.isEmpty()) {
            Pair<Pokemon, Integer> current = toCheck.remove();
            Pokemon currentPokemon = current.getKey();
            int currentPosition = current.getValue();
            if(checked.contains(currentPokemon)) {
                continue;
            }
            checked.add(currentPokemon);

            if(currentPokemon == relative) {
                return currentPosition;
            }

            for(Pokemon evo : currentPokemon.originalEvolvedForms) {
                toCheck.add(new Pair<>(evo, currentPosition + 1));
            }
            for(Pokemon evo : currentPokemon.originalPreEvolvedForms) {
                toCheck.add(new Pair<>(evo, currentPosition - 1));
            }
        }

        throw new IllegalArgumentException("Cannot find relation of a non-related Pokemon!");
    }

    /**
     * Gets all Pokemon related to this one that are at the given relative position, evolution-wise,
     * from this Pokemon, including those on different branches.
     * For example, a value of +1 on a Cascoon would give both Dustox and Beautifly.
     * @param position The relative position to find.
     * @return A PokemonSet including all Pokemon at this relative position, or an empty set if
     *         there are none.
     */
    public PokemonSet getRelativesAtPosition(int position) {
        Queue<Pair<Pokemon, Integer>> toCheck = new ArrayDeque<>();
        PokemonSet checked = new PokemonSet();
        PokemonSet relatives = new PokemonSet();
        toCheck.add(new Pair<>(this, 0));

        while(!toCheck.isEmpty()) {
            Pair<Pokemon, Integer> current = toCheck.remove();
            Pokemon currentPokemon = current.getKey();
            int currentPosition = current.getValue();
            if(checked.contains(currentPokemon)) {
                continue;
            }
            checked.add(currentPokemon);

            if(currentPosition == position) {
                relatives.add(currentPokemon);
            }

            for(Pokemon evo : currentPokemon.getAllEvolvedPokemon()) {
                toCheck.add(new Pair<>(evo, currentPosition + 1));
            }
            for(Pokemon evo : currentPokemon.getAllPreEvolvedPokemon()) {
                toCheck.add(new Pair<>(evo, currentPosition - 1));
            }
        }

        return relatives;
    }

    /**
     * Gets all Pokemon related to this one before randomization that were at the given relative
     * position, evolution-wise, from this Pokemon, including those on different branches.
     * For example, a value of +1 on a Cascoon would give both Dustox and Beautifly.
     * @param position The relative position to find.
     * @return A PokemonSet including all Pokemon at this relative position, or an empty set if
     *         there are none.
     */
    public PokemonSet getOriginalRelativesAtPosition(int position) {
        Queue<Pair<Pokemon, Integer>> toCheck = new ArrayDeque<>();
        PokemonSet checked = new PokemonSet();
        PokemonSet relatives = new PokemonSet();
        toCheck.add(new Pair<>(this, 0));

        while(!toCheck.isEmpty()) {
            Pair<Pokemon, Integer> current = toCheck.remove();
            Pokemon currentPokemon = current.getKey();
            int currentPosition = current.getValue();
            if(checked.contains(currentPokemon)) {
                continue;
            }
            checked.add(currentPokemon);

            if(currentPosition == position) {
                relatives.add(currentPokemon);
            }

            for(Pokemon evo : currentPokemon.getOriginalEvolvedForms()) {
                toCheck.add(new Pair<>(evo, currentPosition + 1));
            }
            for(Pokemon evo : currentPokemon.getOriginalPreEvolvedForms()) {
                toCheck.add(new Pair<>(evo, currentPosition - 1));
            }
        }

        return relatives;
    }

    /**
     * Gets all Pokemon related to this one by the given number of evolutions (or pre-evolutions,
     * if position is negative).
     * Does not include those on different branches.
     * @param position The relative position to find.
     * @return A PokemonSet including all Pokemon at this relative position, or an empty set if
     *         there are none.
     */
    public PokemonSet getRelativesAtPositionSameBranch(int position) {
        PokemonSet currentStage = new PokemonSet();
        currentStage.add(this);

        if(position == 0) {
            return currentStage;
        }

        int step = position > 0 ? 1 : -1;

        for(int i = 0; i != position; i += step) {
            PokemonSet nextStage = new PokemonSet();
            for(Pokemon poke : currentStage) {
                nextStage.addAll(position > 0 ? poke.getAllEvolvedPokemon() : poke.getAllPreEvolvedPokemon());
            }

            currentStage = nextStage;
        }

        return currentStage;
    }

    /**
     * Gets all Pokemon related to this one by the given number of evolutions (or pre-evolutions,
     * if position is negative) before evolutions.
     * Does not include those on different branches.
     * @param position The relative position to find.
     * @return A PokemonSet including all Pokemon at this relative position, or an empty set if
     *         there are none.
     */
    public PokemonSet getOriginalRelativesAtPositionSameBranch(int position) {
        PokemonSet currentStage = new PokemonSet();
        currentStage.add(this);

        if(position == 0) {
            return currentStage;
        }

        int step = position > 0 ? 1 : -1;

        for(int i = 0; i != position; i += step) {
            PokemonSet nextStage = new PokemonSet();
            for(Pokemon poke : currentStage) {
                nextStage.addAll(position > 0 ? poke.getOriginalEvolvedForms() : poke.getOriginalPreEvolvedForms());
            }

            currentStage = nextStage;
        }

        return currentStage;
    }

    /**
     * Saves certain pieces of data that can be randomized, but that
     * we want to know the original version of for later randomization.
     * Currently: Types, evolutions.
     * Must be called before randomizing any of this data.
     */
    public void saveOriginalData() {
        originalPrimaryType = primaryType;
        originalSecondaryType = secondaryType;
        originalEvolvedForms = getAllEvolvedPokemon();
        originalPreEvolvedForms = getAllPreEvolvedPokemon();
    }

    public void copyBaseFormeBaseStats(Pokemon baseForme) {
        hp = baseForme.hp;
        attack = baseForme.attack;
        defense = baseForme.defense;
        speed = baseForme.speed;
        spatk = baseForme.spatk;
        spdef = baseForme.spdef;
    }

    public void copyBaseFormeAbilities(Pokemon baseForme) {
        ability1 = baseForme.ability1;
        ability2 = baseForme.ability2;
        ability3 = baseForme.ability3;
    }

    public void copyBaseFormeEvolutions(Pokemon baseForme) {
        evolutionsFrom = baseForme.evolutionsFrom;
    }

    public int getSpriteIndex() {
        return formeNumber == 0 ? number : formeSpriteIndex + formeNumber - 1;
    }

    public String fullName() {
        return name + formeSuffix;
    }

    @Override
    public String toString() {
        return "Pokemon [name=" + name + formeSuffix + ", number=" + number + ", primaryType=" + primaryType
                + ", secondaryType=" + secondaryType + ", hp=" + hp + ", attack=" + attack + ", defense=" + defense
                + ", spatk=" + spatk + ", spdef=" + spdef + ", speed=" + speed + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + number;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pokemon other = (Pokemon) obj;
        return number == other.number;
    }

    @Override
    public int compareTo(Pokemon o) {
        return number - o.number;
    }

    private static final List<Integer> legendaries = Arrays.asList(Species.articuno, Species.zapdos, Species.moltres,
            Species.mewtwo, Species.mew, Species.raikou, Species.entei, Species.suicune, Species.lugia, Species.hoOh,
            Species.celebi, Species.regirock, Species.regice, Species.registeel, Species.latias, Species.latios,
            Species.kyogre, Species.groudon, Species.rayquaza, Species.jirachi, Species.deoxys, Species.uxie,
            Species.mesprit, Species.azelf, Species.dialga, Species.palkia, Species.heatran, Species.regigigas,
            Species.giratina, Species.cresselia, Species.phione, Species.manaphy, Species.darkrai, Species.shaymin,
            Species.arceus, Species.victini, Species.cobalion, Species.terrakion, Species.virizion, Species.tornadus,
            Species.thundurus, Species.reshiram, Species.zekrom, Species.landorus, Species.kyurem, Species.keldeo,
            Species.meloetta, Species.genesect, Species.xerneas, Species.yveltal, Species.zygarde, Species.diancie,
            Species.hoopa, Species.volcanion, Species.typeNull, Species.silvally, Species.tapuKoko, Species.tapuLele,
            Species.tapuBulu, Species.tapuFini, Species.cosmog, Species.cosmoem, Species.solgaleo, Species.lunala,
            Species.necrozma, Species.magearna, Species.marshadow, Species.zeraora);

    private static final List<Integer> strongLegendaries = Arrays.asList(Species.mewtwo, Species.lugia, Species.hoOh,
            Species.kyogre, Species.groudon, Species.rayquaza, Species.dialga, Species.palkia, Species.regigigas,
            Species.giratina, Species.arceus, Species.reshiram, Species.zekrom, Species.kyurem, Species.xerneas,
            Species.yveltal, Species.cosmog, Species.cosmoem, Species.solgaleo, Species.lunala);

    private static final List<Integer> ultraBeasts = Arrays.asList(Species.nihilego, Species.buzzwole, Species.pheromosa,
            Species.xurkitree, Species.celesteela, Species.kartana, Species.guzzlord, Species.poipole, Species.naganadel,
            Species.stakataka, Species.blacephalon);

    public boolean isLegendary() {
        return formeNumber == 0 ? legendaries.contains(this.number) : legendaries.contains(this.baseForme.number);
    }

    public boolean isStrongLegendary() {
        return formeNumber == 0 ? strongLegendaries.contains(this.number) : strongLegendaries.contains(this.baseForme.number);
    }

    // This method can only be used in contexts where alt formes are NOT involved; otherwise, some alt formes
    // will be considered as Ultra Beasts in SM.
    // In contexts where formes are involved, use "if (ultraBeastList.contains(...))" instead,
    // assuming "checkPokemonRestrictions" has been used at some point beforehand.
    public boolean isUltraBeast() {
        return ultraBeasts.contains(this.number);
    }

    public int getCosmeticFormNumber(int num) {
        return realCosmeticFormNumbers.isEmpty() ? num : realCosmeticFormNumbers.get(num);
    }

}
