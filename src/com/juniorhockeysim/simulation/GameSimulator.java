package com.juniorhockeysim.simulation;

import com.juniorhockeysim.domain.Player;
import com.juniorhockeysim.domain.Position;
import com.juniorhockeysim.domain.Team;

import java.util.*;

/**
 * Core game simulation engine.
 *
 * FIXED BUGS vs original:
 * - Team B's offense/defense now correctly uses Team B's lines (was using Team A's!)
 * - Assister collision check now properly tries a different player
 * - Fatigue modifiers properly scoped to each team
 *
 * NEW FEATURES:
 * - Overtime (5 min 3v3)
 * - Shootout
 * - In-game injuries
 * - Shots-on-goal tracking
 * - Stars-of-the-game selection
 * - Plus/minus tracking
 */
public class GameSimulator {

    private static final Random random = new Random();

    // Injury types and their duration ranges (in days)
    private static final String[][] INJURY_TYPES = {
        {"Upper body", "3", "10"},
        {"Lower body", "5", "14"},
        {"Shoulder", "7", "21"},
        {"Knee", "10", "30"},
        {"Concussion", "5", "20"},
        {"Wrist", "3", "12"},
        {"Ankle", "3", "10"},
        {"Back spasm", "2", "7"},
    };

    public static GameResult simulateGame(Team home, Team away) {

        home.resetFatigue();
        away.resetFatigue();
        home.setupLines();
        away.setupLines();

        int homeScore = 0;
        int awayScore = 0;

        int homeShotsOnGoal = 0;
        int awayShotsOnGoal = 0;

        List<String> events = new ArrayList<>();

        Player goalieHome = home.getStartingGoalie();
        Player goalieAway = away.getStartingGoalie();

        // Null-guard (should never happen in normal play)
        if (goalieHome == null || goalieAway == null) {
            return new GameResult(home.getName(), away.getName(),
                    0, 0, 0, 0, List.of("No goalie available"), GameResult.GameEndType.REGULATION,
                    null, null, null);
        }

        // Per-player scoring events for stars selection
        Map<Player, Integer> playerPoints = new HashMap<>();

        int powerPlayTimeRemaining = 0;
        Team powerPlayTeam = null;

        // ---- REGULATION (60 minutes) ----
        for (int minute = 1; minute <= 60; minute++) {

            // Penalty check
            if (powerPlayTimeRemaining <= 0 && random.nextDouble() < 0.035) {
                if (random.nextBoolean()) {
                    powerPlayTeam = home;
                    events.add(String.format("  %d' — PENALTY on %s", minute, away.getName()));
                } else {
                    powerPlayTeam = away;
                    events.add(String.format("  %d' — PENALTY on %s", minute, home.getName()));
                }
                powerPlayTimeRemaining = 2;
            }

            List<Player> fwdHome  = home.rollForwardLine();
            List<Player> defHome  = home.rollDefensePair();
            List<Player> fwdAway  = away.rollForwardLine();
            List<Player> defAway  = away.rollDefensePair();

            // Fatigue
            home.increaseLineFatigue(fwdHome, 3);
            home.increasePairFatigue(defHome, 2);
            away.increaseLineFatigue(fwdAway, 3);
            away.increasePairFatigue(defAway, 2);

            home.recoverLineFatigue(fwdHome, 1.5);
            home.recoverPairFatigue(defHome, 1.0);
            away.recoverLineFatigue(fwdAway, 1.5);
            away.recoverPairFatigue(defAway, 1.0);

            // ---- Effective attack/defend strengths ----
            double homeFatigueMod = Math.max(0.70, 1.0 - home.getLineFatigue(fwdHome) / 150.0);
            double awayFatigueMod = Math.max(0.70, 1.0 - away.getLineFatigue(fwdAway) / 150.0);
            double homeDefMod     = Math.max(0.70, 1.0 - home.getPairFatigue(defHome) / 150.0);
            double awayDefMod     = Math.max(0.70, 1.0 - away.getPairFatigue(defAway) / 150.0);

            // HOME offensive rating (average effective rating of active forwards)
            double homeOffense = fwdHome.stream().mapToInt(Player::effectiveRating).average().orElse(60) * homeFatigueMod;
            // HOME defensive rating
            double homeDefense = defHome.stream().mapToInt(Player::effectiveRating).average().orElse(60) * homeDefMod;
            // AWAY offensive rating  ← THIS WAS THE BUG (was using home's line!)
            double awayOffense = fwdAway.stream().mapToInt(Player::effectiveRating).average().orElse(60) * awayFatigueMod;
            // AWAY defensive rating  ← THIS TOO
            double awayDefense = defAway.stream().mapToInt(Player::effectiveRating).average().orElse(60) * awayDefMod;

            // Home ice advantage
            homeOffense *= 1.03;
            homeDefense *= 1.02;

            // Power play modifier
            if (powerPlayTimeRemaining > 0) {
                if (powerPlayTeam == home) {
                    homeOffense *= 1.25;
                    awayDefense *= 0.80;
                } else {
                    awayOffense *= 1.25;
                    homeDefense *= 0.80;
                }
                powerPlayTimeRemaining--;
            }

            // ---- HOME ATTACKING ----
            double shotChanceHome = 0.52 + (homeOffense - awayDefense) / 220.0;
            shotChanceHome = Math.max(0.25, Math.min(0.78, shotChanceHome));

            if (random.nextDouble() < shotChanceHome) {
                homeShotsOnGoal++;
                double scoringChance = 0.12 + (homeOffense - goalieAway.effectiveSaveRating()) / 300.0;
                scoringChance = Math.max(0.06, Math.min(0.28, scoringChance));

                boolean goal = random.nextDouble() < scoringChance;
                goalieAway.recordShotAgainst(goal);

                if (goal) {
                    homeScore++;
                    String desc = resolveGoal(home, fwdHome, defHome, minute, events, playerPoints, true);
                    // plus/minus: scoring line +1, defending line -1
                    fwdHome.forEach(p -> p.addPlusMinus(1));
                    defHome.forEach(p -> p.addPlusMinus(1));
                    fwdAway.forEach(p -> p.addPlusMinus(-1));
                    defAway.forEach(p -> p.addPlusMinus(-1));
                }
            }

            // ---- AWAY ATTACKING ----
            double shotChanceAway = 0.50 + (awayOffense - homeDefense) / 220.0;
            shotChanceAway = Math.max(0.22, Math.min(0.75, shotChanceAway));

            if (random.nextDouble() < shotChanceAway) {
                awayShotsOnGoal++;
                double scoringChance = 0.12 + (awayOffense - goalieHome.effectiveSaveRating()) / 300.0;
                scoringChance = Math.max(0.06, Math.min(0.28, scoringChance));

                boolean goal = random.nextDouble() < scoringChance;
                goalieHome.recordShotAgainst(goal);

                if (goal) {
                    awayScore++;
                    resolveGoal(away, fwdAway, defAway, minute, events, playerPoints, false);
                    fwdAway.forEach(p -> p.addPlusMinus(1));
                    defAway.forEach(p -> p.addPlusMinus(1));
                    fwdHome.forEach(p -> p.addPlusMinus(-1));
                    defHome.forEach(p -> p.addPlusMinus(-1));
                }
            }

            // ---- INJURY CHECK (once per period) ----
            if (minute % 20 == 0) {
                checkForInjury(home, events, minute);
                checkForInjury(away, events, minute);
            }
        }

        // ---- OVERTIME (5 min, 3-on-3) ----
        GameResult.GameEndType endType = GameResult.GameEndType.REGULATION;

        if (homeScore == awayScore) {
            endType = GameResult.GameEndType.OVERTIME;
            events.add("  --- OVERTIME ---");

            for (int minute = 61; minute <= 65; minute++) {
                // 3-on-3 means much more open — higher shot/score chance
                List<Player> fwdHome = home.rollForwardLine();
                List<Player> defHome = home.rollDefensePair();
                List<Player> fwdAway = away.rollForwardLine();
                List<Player> defAway = away.rollDefensePair();

                double otShotChance = 0.60; // wide open 3v3

                // HOME
                if (random.nextDouble() < otShotChance) {
                    homeShotsOnGoal++;
                    double sc = 0.18;
                    boolean goal = random.nextDouble() < sc;
                    goalieAway.recordShotAgainst(goal);
                    if (goal) {
                        homeScore++;
                        resolveGoal(home, fwdHome, defHome, minute, events, playerPoints, true);
                        break;
                    }
                }
                // AWAY
                if (random.nextDouble() < otShotChance) {
                    awayShotsOnGoal++;
                    double sc = 0.18;
                    boolean goal = random.nextDouble() < sc;
                    goalieHome.recordShotAgainst(goal);
                    if (goal) {
                        awayScore++;
                        resolveGoal(away, fwdAway, defAway, minute, events, playerPoints, false);
                        break;
                    }
                }
            }
        }

        // ---- SHOOTOUT ----
        if (homeScore == awayScore) {
            endType = GameResult.GameEndType.SHOOTOUT;
            events.add("  --- SHOOTOUT ---");

            int homeSOGoals = 0, awaySOGoals = 0;
            List<Player> homeShooters = new ArrayList<>(home.getForwards());
            List<Player> awayShooters = new ArrayList<>(away.getForwards());
            Collections.sort(homeShooters, Comparator.comparingInt(Player::overallRating).reversed());
            Collections.sort(awayShooters, Comparator.comparingInt(Player::overallRating).reversed());

            for (int i = 0; i < Math.min(3, Math.min(homeShooters.size(), awayShooters.size())); i++) {
                Player homeShooter = homeShooters.get(i);
                Player awayShooter = awayShooters.get(i);

                double homeConvert = 0.30 + (homeShooter.getShooting() - goalieAway.getSaveRating()) / 200.0;
                double awayConvert = 0.30 + (awayShooter.getShooting() - goalieHome.getSaveRating()) / 200.0;
                homeConvert = Math.max(0.15, Math.min(0.60, homeConvert));
                awayConvert = Math.max(0.15, Math.min(0.60, awayConvert));

                boolean homeGoal = random.nextDouble() < homeConvert;
                boolean awayGoal = random.nextDouble() < awayConvert;

                if (homeGoal) homeSOGoals++;
                if (awayGoal) awaySOGoals++;

                events.add(String.format("  SO: %s — %s | %s — %s",
                        homeShooter.getName(), homeGoal ? "GOAL" : "No goal",
                        awayShooter.getName(), awayGoal ? "GOAL" : "No goal"));
            }

            if (homeSOGoals > awaySOGoals)      homeScore++;
            else if (awaySOGoals > homeSOGoals) awayScore++;
            else {
                // Sudden death shootout rounds — safety cap at 20 rounds
                int sdRound = 0;
                while (homeScore == awayScore && sdRound < 20) {
                    sdRound++;
                    Player hShooter = homeShooters.get(sdRound % homeShooters.size());
                    Player aShooter = awayShooters.get(sdRound % awayShooters.size());
                    double hConv = Math.max(0.15, Math.min(0.55,
                            0.30 + (hShooter.getShooting() - goalieAway.getSaveRating()) / 200.0));
                    double aConv = Math.max(0.15, Math.min(0.55,
                            0.30 + (aShooter.getShooting() - goalieHome.getSaveRating()) / 200.0));
                    boolean hg = random.nextDouble() < hConv;
                    boolean ag = random.nextDouble() < aConv;
                    events.add(String.format("  SD%d: %s — %s | %s — %s",
                            sdRound, hShooter.getName(), hg ? "GOAL" : "No goal",
                            aShooter.getName(), ag ? "GOAL" : "No goal"));
                    if (hg && !ag) homeScore++;
                    else if (ag && !hg) awayScore++;
                }
                if (homeScore == awayScore) {
                    if (random.nextBoolean()) homeScore++; else awayScore++;
                }
            }

            if (homeScore > awayScore)
                events.add("  " + home.getName() + " wins the shootout!");
            else
                events.add("  " + away.getName() + " wins the shootout!");
        }

        // Record goalie decisions
        if (homeScore > awayScore) goalieHome.addGoalieWin();
        else                       goalieAway.addGoalieWin();

        if (homeShotsOnGoal > 0 && awayShotsOnGoal > 0) {
            int homeGA = awayScore - (endType != GameResult.GameEndType.REGULATION ? 1 : 0);
            int awayGA = homeScore - (endType != GameResult.GameEndType.REGULATION ? 1 : 0);
            if (homeGA == 0) goalieHome.addShutout();
            if (awayGA == 0) goalieAway.addShutout();
        }

        // Record goalie games played (only if they actually faced shots)
        if (homeShotsOnGoal > 0) goalieAway.addGoalieGamePlayed();
        if (awayShotsOnGoal > 0) goalieHome.addGoalieGamePlayed();

        // GP for all active players
        home.getPlayers().forEach(Player::addGamePlayed);
        away.getPlayers().forEach(Player::addGamePlayed);

        // Stars of the game
        Player[] stars = selectStars(playerPoints, goalieHome, goalieAway, homeScore, awayScore,
                goalieHome.getSaves(), goalieAway.getSaves());

        return new GameResult(
                home.getName(), away.getName(),
                homeScore, awayScore,
                homeShotsOnGoal, awayShotsOnGoal,
                events, endType,
                stars[0], stars[1], stars[2]
        );
    }

    /**
     * Resolves a goal event: picks scorer and assister, logs the event,
     * updates player stats.
     */
    private static String resolveGoal(Team team,
                                       List<Player> fwds, List<Player> defs,
                                       int minute, List<String> events,
                                       Map<Player, Integer> playerPoints,
                                       boolean isHomeTeam) {
        // Pick scorer: 80% forward, 20% defense
        List<Player> scorerPool = random.nextDouble() < 0.80 ? new ArrayList<>(fwds) : new ArrayList<>(defs);
        if (scorerPool.isEmpty()) scorerPool = new ArrayList<>(fwds);
        if (scorerPool.isEmpty()) return "";

        // Gems get heavily weighted scoring chances — they dominate statistically
        Player scorer;
        List<Player> gemPlayers = scorerPool.stream().filter(Player::isGem).collect(java.util.stream.Collectors.toList());
        if (!gemPlayers.isEmpty() && random.nextDouble() < 0.55) {
            // 55% chance gem scores when on ice (they are generational)
            scorer = gemPlayers.get(random.nextInt(gemPlayers.size()));
        } else {
            // Weight by shooting rating for non-gems
            scorer = scorerPool.get(random.nextInt(scorerPool.size()));
        }
        scorer.addGoal();
        playerPoints.merge(scorer, 2, Integer::sum); // goals worth 2 for stars

        // Pick assister from all skaters on that line (excluding scorer)
        List<Player> assisterPool = new ArrayList<>(fwds);
        assisterPool.addAll(defs);
        assisterPool.remove(scorer);

        // Gems also pick up assists more often
        Player assister = null;
        if (!assisterPool.isEmpty() && random.nextDouble() < 0.85) {
            List<Player> gemAssisters = assisterPool.stream().filter(Player::isGem).collect(java.util.stream.Collectors.toList());
            if (!gemAssisters.isEmpty() && random.nextDouble() < 0.45) {
                assister = gemAssisters.get(random.nextInt(gemAssisters.size()));
            } else {
                assister = assisterPool.get(random.nextInt(assisterPool.size()));
            }
            assister.addAssist();
            playerPoints.merge(assister, 1, Integer::sum);
        }

        String assistStr = assister != null ? " (Ast: " + assister.getName() + ")" : " (unassisted)";
        String marker = isHomeTeam ? "🏒" : "🏒";
        events.add(String.format("  %2d' — %s %s   GOAL: %s%s",
                minute, marker, team.getName(), scorer.getName(), assistStr));

        return scorer.getName();
    }

    /**
     * Chance of an in-game injury occurring.
     */
    private static void checkForInjury(Team team, List<String> events, int minute) {
        if (random.nextDouble() < 0.015) { // ~1.5% chance per period per team
            List<Player> skaters = team.getSkaters().stream()
                    .filter(p -> !p.isInjured())
                    .toList();
            if (skaters.isEmpty()) return;

            Player injured = skaters.get(random.nextInt(skaters.size()));
            String[] injType = INJURY_TYPES[random.nextInt(INJURY_TYPES.length)];
            int minDays = Integer.parseInt(injType[1]);
            int maxDays = Integer.parseInt(injType[2]);
            int days = random.nextInt(maxDays - minDays + 1) + minDays;

            injured.injure(days, injType[0]);
            team.setupLines();

            events.add(String.format("  %2d' — ⚠️  INJURY: %s (%s) — %s — expected %d days",
                    minute, injured.getName(), team.getName(), injType[0], days));
        }
    }

    /**
     * Select the 3 stars from a game.
     */
    private static Player[] selectStars(Map<Player, Integer> playerPoints,
                                         Player goalieHome, Player goalieAway,
                                         int homeScore, int awayScore,
                                         int homeSaves, int awaySaves) {
        // Add goalie bonuses
        if (homeScore < awayScore && awaySaves >= 20)
            playerPoints.merge(goalieAway, 3, Integer::sum);
        else if (awayScore < homeScore && homeSaves >= 20)
            playerPoints.merge(goalieHome, 3, Integer::sum);
        else {
            // Decent performances still add to the pool
            if (homeSaves >= 15) playerPoints.merge(goalieHome, 1, Integer::sum);
            if (awaySaves >= 15) playerPoints.merge(goalieAway, 1, Integer::sum);
        }

        List<Map.Entry<Player, Integer>> sorted = new ArrayList<>(playerPoints.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        Player first  = sorted.size() > 0 ? sorted.get(0).getKey() : null;
        Player second = sorted.size() > 1 ? sorted.get(1).getKey() : null;
        Player third  = sorted.size() > 2 ? sorted.get(2).getKey() : null;

        return new Player[]{first, second, third};
    }
}
