package com.juniorhockeysim.simulation;

import com.juniorhockeysim.domain.Team;
import java.util.*;

/**
 * Best-of-7 bracket playoff simulator.
 *
 * Seeding: 1 vs 8, 2 vs 7, 3 vs 6, 4 vs 5.
 * Re-seeding after each round (1-seed winner always plays the lowest remaining seed).
 * Higher seed has home ice (games 1, 2, 5, 7 at home; games 3, 4, 6 away).
 */
public class PlayoffSimulator {

    // Each entry is a seed (1-indexed). index 0 = seed 1 (best team).
    private final List<Team> seededTeams;
    private Team champion;

    public PlayoffSimulator(List<Team> standings) {
        // Accept up to 8 teams
        this.seededTeams = new ArrayList<>(standings.subList(0, Math.min(8, standings.size())));
    }

    public Team simulate(boolean verbose) {
        // Represent the bracket as a list of (seed, team) — we re-seed after each round.
        // Initial matchups: 1v8, 2v7, 3v6, 4v5
        List<BracketEntry> remaining = new ArrayList<>();
        for (int i = 0; i < seededTeams.size(); i++) {
            remaining.add(new BracketEntry(i + 1, seededTeams.get(i)));
        }

        String[] roundNames = {"Quarter-Finals", "Semi-Finals", "Championship Final"};
        int roundIdx = 0;

        while (remaining.size() > 1) {
            String roundName = roundIdx < roundNames.length ? roundNames[roundIdx] : "Round " + (roundIdx + 1);
            if (verbose) {
                System.out.println();
                System.out.println("=".repeat(52));
                System.out.println("  " + roundName.toUpperCase());
                System.out.println("=".repeat(52));
            }

            // Sort by seed so best seed is first
            remaining.sort(Comparator.comparingInt(e -> e.seed));

            List<BracketEntry> winners = new ArrayList<>();

            // Pair 1st vs last, 2nd vs 2nd-last, etc.
            int pairs = remaining.size() / 2;
            for (int i = 0; i < pairs; i++) {
                BracketEntry higher = remaining.get(i);
                BracketEntry lower  = remaining.get(remaining.size() - 1 - i);

                if (verbose) {
                    System.out.printf("%n  [%d] %s  vs  [%d] %s%n",
                            higher.seed, higher.team.getName(),
                            lower.seed,  lower.team.getName());
                }

                Team winner = simulateSeries(higher.team, lower.team, verbose);
                // Winner keeps the higher (lower-numbered) seed in next round
                int winnerSeed = winner.equals(higher.team) ? higher.seed : lower.seed;
                winners.add(new BracketEntry(winnerSeed, winner));
            }

            remaining = winners;
            roundIdx++;
        }

        this.champion = remaining.get(0).team;

        if (verbose) {
            System.out.println();
            System.out.println("=".repeat(52));
            System.out.println("  CHAMPION: " + champion.getName() + "  !!!");
            System.out.println("=".repeat(52));
        } else {
            System.out.println("\n  CHAMPION: " + champion.getName());
        }

        return champion;
    }

    /**
     * Simulates a best-of-7 series between two teams.
     * @param higher The higher-seeded team (gets home ice).
     * @param lower  The lower-seeded team.
     */
    private Team simulateSeries(Team higher, Team lower, boolean verbose) {
        int higherWins = 0;
        int lowerWins  = 0;
        int gameNum    = 0;

        while (higherWins < 4 && lowerWins < 4) {
            gameNum++;
            // Standard home ice: 1-2 at home (higher), 3-4 away, 5 home, 6 away, 7 home
            boolean higherIsHome = (gameNum == 1 || gameNum == 2
                    || gameNum == 5 || gameNum == 7);

            Team gameHome = higherIsHome ? higher : lower;
            Team gameAway = higherIsHome ? lower  : higher;

            GameResult result = GameSimulator.simulateGame(gameHome, gameAway);

            boolean homeWon    = result.getHomeScore() > result.getAwayScore();
            boolean higherWon  = (higherIsHome == homeWon); // XOR logic

            if (higherWon) higherWins++;
            else           lowerWins++;

            if (verbose) {
                String leader = higherWins > lowerWins ? higher.getName() : lower.getName();
                int leadW = Math.max(higherWins, lowerWins);
                int trailW = Math.min(higherWins, lowerWins);
                String otTag = result.isShootoutResult() ? " SO"
                             : result.isOvertimeResult()  ? " OT" : "";
                System.out.printf("    Gm %d: %s %d - %d %s%s  (%s leads %d-%d)%n",
                        gameNum,
                        gameAway.getName(), result.getAwayScore(),
                        result.getHomeScore(), gameHome.getName(),
                        otTag,
                        leader, leadW, trailW);
            }
        }

        Team winner = higherWins == 4 ? higher : lower;
        int totalGames = higherWins + lowerWins;

        if (verbose) {
            System.out.printf("  --> %s wins in %d games (%d-%d)%n%n",
                    winner.getName(), totalGames,
                    Math.max(higherWins, lowerWins),
                    Math.min(higherWins, lowerWins));
        }

        return winner;
    }

    public Team getChampion() { return champion; }

    // ── Inner class ────────────────────────────────────────────────────
    private static class BracketEntry {
        final int seed;
        final Team team;
        BracketEntry(int seed, Team team) {
            this.seed = seed;
            this.team = team;
        }
    }
}
