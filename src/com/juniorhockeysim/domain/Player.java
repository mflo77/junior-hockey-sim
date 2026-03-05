package com.juniorhockeysim.domain;

import java.io.Serializable;
import java.util.Random;

public class Player implements Serializable {

    private String name;
    private int age;
    private Position position;

    // Core ratings (1–100)
    private int shooting;
    private int passing;
    private int skating;
    private int defense;
    private int physical;

    // Development traits
    private int potential;      // 60–99: ceiling overall
    private int workEthic;      // 50–100: affects how fast they develop
    private int consistency;    // 50–100: affects per-game variance
    private boolean gem = false; // generational talent — rare, very high potential

    // Season stats — skaters
    private int goals    = 0;
    private int assists  = 0;
    private int plusMinus = 0;
    private int gamesPlayed = 0;

    // Season stats — goalies
    private int shotsAgainst = 0;
    private int saves        = 0;
    private int goalsAgainst = 0;
    private int wins         = 0;
    private int shutouts     = 0;
    private double gaa       = 0.0; // goals against average (computed)
    private int goalieGamesPlayed = 0; // only counts games goalie actually played

    private int saveRating; // core goalie rating

    // Career totals
    private int careerGoals   = 0;
    private int careerAssists = 0;
    private int careerGP      = 0;

    // Injury
    private int injuryDaysRemaining = 0;
    private String injuryDescription = null;

    // Contract
    private int contractYearsRemaining = 1;

    // Morale (0–100); affects consistency
    private int morale = 70;

    private static final Random random = new Random();

    public Player(String name, int age, Position position) {
        this.name     = name;
        this.age      = age;
        this.position = position;

        this.shooting = randomBetween(45, 75);
        this.passing  = randomBetween(45, 75);
        this.skating  = randomBetween(45, 75);
        this.defense  = randomBetween(45, 75);
        this.physical = randomBetween(40, 75);

        this.workEthic   = randomBetween(45, 100);
        this.consistency = randomBetween(45, 100);

        if (position == Position.GOALIE) {
            this.saveRating = randomBetween(45, 80);
        }

        // Potential is always above the player's starting OVR so they always have room to grow.
        // Add 5–20 points on top of starting OVR — many players will have ceilings in the 80s.
        int startingOvr = overallRating();
        int ceiling = startingOvr + randomBetween(5, 20);
        this.potential = Math.min(ceiling, 89); // normal players hard cap at 89

        this.contractYearsRemaining = randomBetween(1, 3);
    }

    private int randomBetween(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Elevates this player to generational/gem status.
     * Called by DraftManager for one special prospect per draft class.
     */
    public void makeGem() {
        this.gem = true;
        // Gem players have elite potential - they will reach near their ceiling
        this.potential   = randomBetween(92, 99);
        this.workEthic   = randomBetween(92, 100);
        this.consistency = randomBetween(82, 100);
        // Starting ratings modest - their ceiling is the distinguishing factor
        this.shooting = randomBetween(50, 65);
        this.passing  = randomBetween(50, 65);
        this.skating  = randomBetween(50, 65);
        this.defense  = randomBetween(45, 60);
        if (position == Position.GOALIE) {
            this.saveRating = randomBetween(50, 65);
        }
    }

    // ---- Ratings ----

    public int overallRating() {
        if (position == Position.GOALIE) return saveRating;
        return (shooting + passing + skating + defense) / 4;
    }

    /**
     * Effective rating this game — adds noise based on consistency and morale.
     */
    public int effectiveRating() {
        if (injuryDaysRemaining > 0) return 0; // shouldn't be playing
        double noiseRange = (100 - consistency) / 10.0;    // low consistency = bigger swings
        double moraleBonus = (morale - 70) / 50.0 * 5.0;  // ±5 from morale
        int noise = (int)((random.nextDouble() - 0.5) * 2 * noiseRange + moraleBonus);
        return Math.max(30, Math.min(99, overallRating() + noise));
    }

    public int getOffensiveRating() { return (shooting + passing) / 2; }
    public int getDefensiveRating() { return (defense + skating) / 2;  }
    public int getSaveRating()      { return saveRating; }

    public int effectiveSaveRating() {
        double moraleBonus = (morale - 70) / 50.0 * 4.0;
        double noiseRange  = (100 - consistency) / 12.0;
        int noise = (int)((random.nextDouble() - 0.5) * 2 * noiseRange + moraleBonus);
        return Math.max(30, Math.min(99, saveRating + noise));
    }

    // ---- Development ----

    /**
     * Called at end of each season. Improves ratings toward potential.
     * Returns a description of how the player developed.
     */
    public String developEndOfSeason() {
        if (age > 21) {
            // Veteran — might decline slightly
            if (random.nextDouble() < 0.3) {
                int decline = randomBetween(1, 3);
                if (position == Position.GOALIE) {
                    saveRating = Math.max(40, saveRating - decline);
                } else {
                    // Decline is in OVR points, so subtract 4x from each stat to keep average correct
                    int statDrop = decline * 4;
                    shooting = Math.max(40, shooting - statDrop / 4);
                    passing  = Math.max(40, passing  - statDrop / 4);
                    skating  = Math.max(40, skating  - statDrop / 4);
                    defense  = Math.max(40, defense  - statDrop / 4);
                }
                return name + " showed signs of decline (-" + decline + " OVR)";
            }
            return null;
        }

        int currentOvr = overallRating();
        int gap = potential - currentOvr; // gap is in OVR points
        if (gap <= 0) return null;

        // Development chance
        double devChance = gem
                ? 0.97 + (workEthic / 100.0) * 0.03  // 97–100% for gems
                : 0.55 + (workEthic / 100.0) * 0.30; // 55–85% for normals
        if (random.nextDouble() > devChance) return null;

        // ovrGain is the number of OVERALL RATING points this player gains this season.
        // For skaters, overallRating() = (shooting+passing+skating+defense)/4,
        // so to raise OVR by N we must add N*4 total stat points.
        int ovrGain;
        if (gem) {
            // Gems gain 8–15 OVR per season when young, slowing as they near their ceiling.
            // Gap shrinks naturally each year, so growth automatically decelerates (exponential feel).
            double growthRate = 0.28 + (workEthic / 100.0) * 0.14; // 28–42% of remaining gap
            if (age <= 17) growthRate += 0.10; // extra burst at 16–17
            else if (age <= 18) growthRate += 0.05;
            ovrGain = (int)(gap * growthRate);
            ovrGain = Math.max(8, Math.min(ovrGain, 16)); // hard floor 8, ceiling 16 per season
        } else {
            // Normal players gain 1–5 OVR per season; high work ethic means closer to 5.
            double growthRate = 0.15 + (workEthic / 100.0) * 0.18; // 15–33% of remaining gap
            ovrGain = (int)(gap * growthRate);
            ovrGain = Math.max(1, Math.min(ovrGain, 5)); // 1–5 OVR per season
        }

        // Apply to stats — OVR = avg of 4 stats, so each stat point = 0.25 OVR.
        // We need to add ovrGain * 4 total stat points to move OVR by ovrGain.
        if (position == Position.GOALIE) {
            // Goalies: saveRating IS the OVR, add directly.
            saveRating = Math.min(potential, saveRating + ovrGain);
        } else {
            int totalStatPoints = ovrGain * 4;
            int each = totalStatPoints / 4;
            int rem  = totalStatPoints % 4;
            shooting = Math.min(99, shooting + each + (rem > 0 ? 1 : 0)); if (rem > 0) rem--;
            passing  = Math.min(99, passing  + each + (rem > 0 ? 1 : 0)); if (rem > 0) rem--;
            skating  = Math.min(99, skating  + each + (rem > 0 ? 1 : 0)); if (rem > 0) rem--;
            defense  = Math.min(99, defense  + each);
        }

        int newOvr = overallRating();
        int actualGain = newOvr - currentOvr; // may be slightly different due to integer math / cap
        if (actualGain <= 0) return null;

        String label;
        if (gem && actualGain >= 12)     label = "💎 GENERATIONAL LEAP!";
        else if (gem && actualGain >= 8) label = "💎 Explosive growth!";
        else if (actualGain >= 3)        label = "🌟 Breakout season!";
        else                             label = "improved";

        return String.format("%s %s (+%d OVR, now %d)", name, label, actualGain, newOvr);
    }

    public void birthday() {
        age++;
    }

    // ---- Injury ----

    public boolean isInjured() { return injuryDaysRemaining > 0; }

    public void injure(int days, String description) {
        this.injuryDaysRemaining = days;
        this.injuryDescription = description;
    }

    public void healOneDay() {
        if (injuryDaysRemaining > 0) injuryDaysRemaining--;
        if (injuryDaysRemaining == 0) injuryDescription = null;
    }

    public int getInjuryDays() { return injuryDaysRemaining; }
    public String getInjuryDescription() { return injuryDescription; }

    // ---- Stats ----

    public void addGoal()   { goals++;  careerGoals++;  }
    public void addAssist() { assists++; careerAssists++; }
    public void addGamePlayed() { gamesPlayed++; careerGP++; }
    public void addPlusMinus(int delta) { plusMinus += delta; }

    public void recordShotAgainst(boolean goal) {
        shotsAgainst++;
        if (!goal) saves++;
        else goalsAgainst++;
    }

    public void addGoalieWin() { wins++; }
    public void addShutout()   { shutouts++; }
    public void addGoalieGamePlayed() { goalieGamesPlayed++; }

    public double getSavePercentage() {
        if (shotsAgainst == 0) return 0.0;
        return (double) saves / shotsAgainst;
    }

    public double getGAA(int gamesPlayedGoalie) {
        if (gamesPlayedGoalie == 0) return 0.0;
        return (double) goalsAgainst / gamesPlayedGoalie;
    }

    public double getGAA() { return getGAA(goalieGamesPlayed); }
    public int getGoalieGamesPlayed() { return goalieGamesPlayed; }

    // ---- Morale ----

    public void adjustMorale(int delta) {
        morale = Math.max(0, Math.min(100, morale + delta));
    }

    public int getMorale() { return morale; }

    // ---- Last Year ----

    /**
     * Returns true if this is the player's last year — either their contract is expiring
     * (1 year left) OR they will age out next year (currently 21, turning 22 at end of season).
     */
    public boolean isLastYear() {
        return contractYearsRemaining <= 1 || age >= 21;
    }

    // ---- Contract ----

    public int getContractYearsRemaining() { return contractYearsRemaining; }
    public void decrementContract() { if (contractYearsRemaining > 0) contractYearsRemaining--; }
    public boolean isFreeAgent() { return contractYearsRemaining <= 0; }
    public void signContract(int years) { contractYearsRemaining = years; }

    // ---- Trade value ----

    public int tradeValue() {
        int base = overallRating();
        int ageFactor = Math.max(0, 21 - age) * 3; // younger = more valuable
        int potentialFactor = (potential - overallRating()) / 2;
        return base + ageFactor + potentialFactor;
    }

    // ---- Reset season stats ----

    public void resetSeasonStats() {
        goals = assists = plusMinus = gamesPlayed = 0;
        shotsAgainst = saves = goalsAgainst = wins = shutouts = 0;
        goalieGamesPlayed = 0;
    }

    // ---- Getters ----

    public String getName()     { return name; }
    public int getAge()         { return age;  }
    public Position getPosition() { return position; }
    public int getPotential()   { return potential; }
    public int getWorkEthic()   { return workEthic; }
    public int getConsistency() { return consistency; }
    public boolean isGem()      { return gem; }
    public int getShooting()    { return shooting; }
    public int getPassing()     { return passing;  }
    public int getSkating()     { return skating;  }
    public int getDefense()     { return defense;  }
    public int getPhysical()    { return physical; }

    public int getGoals()        { return goals;   }
    public int getAssists()      { return assists; }
    public int getPlusMinus()    { return plusMinus; }
    public int getGamesPlayed()  { return gamesPlayed; }
    public int getPoints()       { return goals + assists; }
    public int getShotsAgainst() { return shotsAgainst; }
    public int getSaves()        { return saves;    }
    public int getGoalsAgainst() { return goalsAgainst; }
    public int getGoalieWins()   { return wins; }
    public int getShutouts()     { return shutouts; }

    public int getCareerGoals()   { return careerGoals;   }
    public int getCareerAssists() { return careerAssists; }
    public int getCareerGP()      { return careerGP;      }

    public String getMoraleLabel() {
        if (morale >= 85) return "Excellent";
        if (morale >= 70) return "Good";
        if (morale >= 50) return "Fair";
        if (morale >= 30) return "Low";
        return "Miserable";
    }

    @Override
    public String toString() {
        if (position == Position.GOALIE) {
            return String.format("%-20s %s  OVR:%2d  Age:%2d", name, position, overallRating(), age);
        }
        return String.format("%-20s %s  OVR:%2d  Age:%2d", name, position, overallRating(), age);
    }
}