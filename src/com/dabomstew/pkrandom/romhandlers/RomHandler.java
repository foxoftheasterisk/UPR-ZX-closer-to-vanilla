package com.dabomstew.pkrandom.romhandlers;

/*----------------------------------------------------------------------------*/
/*--  RomHandler.java - defines the functionality that each randomization   --*/
/*--                    handler must implement.                             --*/
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

import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.dabomstew.pkrandom.MiscTweak;
import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.graphics.packs.GraphicsPack;
import com.dabomstew.pkrandom.graphics.palettes.PaletteHandler;
import com.dabomstew.pkrandom.pokemon.*;
import com.dabomstew.pkrandom.services.RestrictedPokemonService;
import com.dabomstew.pkrandom.services.TypeService;

public interface RomHandler {

    abstract class Factory {
        public RomHandler create(Random random) {
            return create(random, null);
        }

        public abstract RomHandler create(Random random, PrintStream log);

        public abstract boolean isLoadable(String filename);
    }

    // =======================
    // Basic load/save methods
    // =======================

    boolean loadRom(String filename);
    
    boolean saveRom(String filename, long seed, boolean saveAsDirectory);

    String loadedFilename();

    // =============================================================
    // Methods relating to game updates for the 3DS and Switch games
    // =============================================================

    boolean hasGameUpdateLoaded();

    boolean loadGameUpdate(String filename);

    void removeGameUpdate();

    String getGameUpdateVersion();

    // ===========
    // Log methods
    // ===========

    void setLog(PrintStream logStream);

    void printRomDiagnostics(PrintStream logStream);

    boolean isRomValid();

    // ======================================================
    // Methods for retrieving a list of Pokemon objects.
    // Note that for many of these lists, index 0 is null.
    // Instead, you use index on the species' National Dex ID
    // ======================================================

    List<Pokemon> getPokemon();

    List<Pokemon> getPokemonInclFormes();

    PokemonSet<Pokemon> getAltFormes();
    
    PokemonSet<Pokemon> getPokemonSet();
    
    PokemonSet<Pokemon> getPokemonSetInclFormes();

    List<MegaEvolution> getMegaEvolutions();

    Pokemon getAltFormeOfPokemon(Pokemon pk, int forme);

    PokemonSet<Pokemon> getIrregularFormes();

    RestrictedPokemonService getRestrictedPokemonService();

    // ==================================
    // Methods to set up Gen Restrictions
    // ==================================

    void removeEvosForPokemonPool();

    // ===============
    // Starter Pokemon
    // ===============

    List<Pokemon> getStarters();

    boolean setStarters(List<Pokemon> newStarters);

    boolean hasStarterAltFormes();

    int starterCount();

    boolean hasStarterTypeTriangleSupport();

    boolean supportsStarterHeldItems();

    List<Integer> getStarterHeldItems();

    void setStarterHeldItems(List<Integer> items);

    // =======================
    // Pokemon Base Statistics
    // =======================

    // Update base stats to specified generation
    void updatePokemonStats(Settings settings);

    Map<Integer,StatChange> getUpdatedPokemonStats(int generation);

    // =================
    // Pokemon Abilities
    // =================

    int abilitiesPerPokemon();

    int highestAbilityIndex();

    String abilityName(int number);

    Map<Integer,List<Integer>> getAbilityVariations();

    List<Integer> getUselessAbilities();

    int getAbilityForTrainerPokemon(TrainerPokemon tp);

    boolean hasMegaEvolutions();

    // ============
    // Wild Pokemon
    // ============

    List<EncounterArea> getEncounters(boolean useTimeOfDay);

    /**
     * Returns a list identical to {@link #getEncounters(boolean)}, except it is sorted according to when in the game
     * the player is expected to go to the location of each {@link EncounterArea}.<br>
     * E.g. {@link EncounterArea}s at early routes come early, and victory road and post-game locations ones are at
     * the end.<br>
     * (if the order has been implemented; the default implementation does not sort)
     */
    List<EncounterArea> getSortedEncounters(boolean useTimeOfDay);

    PokemonSet<Pokemon> getMainGameWildPokemon(boolean useTimeOfDay);

    void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounters);

    boolean hasEncounterLocations();

    boolean hasTimeBasedEncounters();

    boolean hasWildAltFormes();

    PokemonSet<Pokemon> getBannedForWildEncounters();

    void enableGuaranteedPokemonCatching();

    // ===============
    // Trainer Pokemon
    // ===============

    List<Trainer> getTrainers();

    List<Integer> getMainPlaythroughTrainers();

    /**
     * Returns a list of the indices (in the main trainer list via {@link #getTrainers()}) of the trainers
     * consisting of the non-rematch Elite 4 challenge, including the Champion (or Ghetsis in BW1). <br>
     * If isChallengeMode is true, it returns the indexes for the Challenge Mode e4+champion (only in BW2).
     */
    List<Integer> getEliteFourTrainers(boolean isChallengeMode);

    void setTrainers(List<Trainer> trainerData);

    boolean canAddPokemonToBossTrainers();

    boolean canAddPokemonToImportantTrainers();

    boolean canAddPokemonToRegularTrainers();

    boolean canAddHeldItemsToBossTrainers();

    boolean canAddHeldItemsToImportantTrainers();

    boolean canAddHeldItemsToRegularTrainers();

    List<Integer> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves);

    List<Integer> getAllConsumableHeldItems();

    List<Integer> getAllHeldItems();

    boolean hasRivalFinalBattle();

    // =========
    // Move Data
    // =========

    boolean hasPhysicalSpecialSplit();

    void updateMoves(Settings settings);

    // stuff for printing move changes
    void initMoveUpdates();

    Map<Integer, boolean[]> getMoveUpdates();

    // return all the moves valid in this game.
    List<Move> getMoves();

    int getPerfectAccuracy();

    // ================
    // Pokemon Movesets
    // ================

    Map<Integer, List<MoveLearnt>> getMovesLearnt();

    void setMovesLearnt(Map<Integer, List<MoveLearnt>> movesets);

    List<Integer> getMovesBannedFromLevelup();

    Map<Integer, List<Integer>> getEggMoves();

    void setEggMoves(Map<Integer, List<Integer>> eggMoves);

    boolean supportsFourStartingMoves();

    // ==============
    // Static Pokemon
    // ==============

    List<StaticEncounter> getStaticPokemon();

    boolean setStaticPokemon(List<StaticEncounter> staticPokemon);

    boolean canChangeStaticPokemon();

    boolean hasStaticAltFormes();

    PokemonSet<Pokemon> getBannedForStaticPokemon();

    boolean forceSwapStaticMegaEvos();

    boolean hasMainGameLegendaries();

    List<Integer> getMainGameLegendaries();

    List<Integer> getSpecialMusicStatics();

    void applyCorrectStaticMusic(Map<Integer,Integer> specialMusicStaticChanges);

    boolean hasStaticMusicFix();

    // =============
    // Totem Pokemon
    // =============

    List<TotemPokemon> getTotemPokemon();

    void setTotemPokemon(List<TotemPokemon> totemPokemon);

    // =========
    // TMs & HMs
    // =========

    List<Integer> getTMMoves();

    List<Integer> getHMMoves();

    void setTMMoves(List<Integer> moveIndexes);

    int getTMCount();

    int getHMCount();

    /**
     * Get TM/HM compatibility data from this rom. The result should contain a
     * boolean array for each Pokemon indexed as such:
     * <br>
     * 0: blank (false) / 1 - (getTMCount()) : TM compatibility /
     * (getTMCount()+1) - (getTMCount()+getHMCount()) - HM compatibility
     * 
     * @return Map of TM/HM compatibility
     */

    Map<Pokemon, boolean[]> getTMHMCompatibility();

    void setTMHMCompatibility(Map<Pokemon, boolean[]> compatData);

    // ===========
    // Move Tutors
    // ===========

    boolean hasMoveTutors();

    List<Integer> getMoveTutorMoves();

    void setMoveTutorMoves(List<Integer> moves);

    Map<Pokemon, boolean[]> getMoveTutorCompatibility();

    void setMoveTutorCompatibility(Map<Pokemon, boolean[]> compatData);

    // =============
    // Trainer Names
    // =============

    boolean canChangeTrainerText();

    List<String> getTrainerNames();

    void setTrainerNames(List<String> trainerNames);

    enum TrainerNameMode {
        SAME_LENGTH, MAX_LENGTH, MAX_LENGTH_WITH_CLASS
    }

    TrainerNameMode trainerNameMode();

    // Returns this with or without the class
    int maxTrainerNameLength();

    // Only relevant for gen2, which has fluid trainer name length but
    // only a certain amount of space in the ROM bank.
    int maxSumOfTrainerNameLengths();

    // Only needed if above mode is "MAX LENGTH WITH CLASS"
    List<Integer> getTCNameLengthsByTrainer();

    // ===============
    // Trainer Classes
    // ===============

    List<String> getTrainerClassNames();

    void setTrainerClassNames(List<String> trainerClassNames);

    boolean fixedTrainerClassNamesLength();

    int maxTrainerClassNameLength();

    List<Integer> getDoublesTrainerClasses();

    // =====
    // Items
    // =====

    ItemList getAllowedItems();

    ItemList getNonBadItems();

    List<Integer> getEvolutionItems();

    List<Integer> getXItems();

    List<Integer> getUniqueNoSellItems();

    List<Integer> getRegularShopItems();

    List<Integer> getOPShopItems();

    String[] getItemNames();

    // ===========
    // Field Items
    // ===========

    // TMs on the field

    List<Integer> getRequiredFieldTMs();

    List<Integer> getCurrentFieldTMs();

    void setFieldTMs(List<Integer> fieldTMs);

    // Everything else

    List<Integer> getRegularFieldItems();

    void setRegularFieldItems(List<Integer> items);

    // ============
    // Special Shops
    // =============

    boolean hasShopRandomization();

    Map<Integer, Shop> getShopItems();

    void setShopItems(Map<Integer, Shop> shopItems);

    void setBalancedShopPrices();

    // ============
    // Pickup Items
    // ============

    List<PickupItem> getPickupItems();

    void setPickupItems(List<PickupItem> pickupItems);

    // ==============
    // In-Game Trades
    // ==============

    List<IngameTrade> getIngameTrades();

    void setIngameTrades(List<IngameTrade> trades);

    boolean hasDVs();

    int maxTradeNicknameLength();

    int maxTradeOTNameLength();

    // ==================
    // Pokemon Evolutions
    // ==================

    void removeImpossibleEvolutions(Settings settings);

    void condenseLevelEvolutions(int maxLevel, int maxIntermediateLevel);

    void makeEvolutionsEasier(Settings settings);

    void removeTimeBasedEvolutions();

    Set<EvolutionUpdate> getImpossibleEvoUpdates();

    Set<EvolutionUpdate> getEasierEvoUpdates();

    Set<EvolutionUpdate> getTimeBasedEvoUpdates();

    // In the earlier games, alt formes use the same evolutions as the base forme.
    // In later games, this was changed so that alt formes can have unique evolutions
    // compared to the base forme.
    boolean altFormesCanHaveDifferentEvolutions();

    // ==================================
    // (Mostly) unchanging lists of moves
    // ==================================

    List<Integer> getGameBreakingMoves();

    List<Integer> getIllegalMoves();

    // includes game or gen-specific moves like Secret Power
    // but NOT healing moves (Softboiled, Milk Drink)
    List<Integer> getFieldMoves();

    // any HMs required to obtain 4 badges
    // (excluding Gameshark codes or early drink in RBY)
    List<Integer> getEarlyRequiredHMMoves();

    // ====
    // Misc
    // ====

    boolean isYellow();

    boolean isORAS();

    boolean hasMultiplePlayerCharacters();

    String getROMName();

    String getROMCode();

    int getROMType();

    String getSupportLevel();

    String getDefaultExtension();

    int internalStringLength(String string);

    /**
     * Sets the Pokemon shown in the intro. Returns false if pk is not a valid intro Pokemon.
     */
    boolean setIntroPokemon(Pokemon pk);

    BufferedImage getMascotImage();

    int generationOfPokemon();

    void writeCheckValueToROM(int value);

    // ===========
    // code tweaks
    // ===========

    int miscTweaksAvailable();

    void applyMiscTweaks(Settings settings);

    void applyMiscTweak(MiscTweak tweak);

    // ==========================
    // Misc forme-related methods
    // ==========================

    boolean hasFunctionalFormes();

    PokemonSet<Pokemon> getBannedFormesForTrainerPokemon();
    
    // ========
    // Graphics
    // ========

    boolean hasCustomPlayerGraphicsSupport();

    void setCustomPlayerGraphics(GraphicsPack playerGraphics, Settings.PlayerCharacterMod toReplace);
    
    void randomizePokemonPalettes(Settings settings);

    PaletteHandler getPaletteHandler();

    void dumpAllPokemonImages();

    List<BufferedImage> getAllPokemonImages();

    // ======
    // Types
    // ======

    TypeService getTypeService();

    TypeTable getTypeTable();

    void setTypeTable(TypeTable typeTable);

    void updateTypeEffectiveness();

    boolean hasTypeEffectivenessSupport();

}