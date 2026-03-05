package com.juniorhockeysim.core;

import com.juniorhockeysim.domain.Player;
import com.juniorhockeysim.domain.Team;

import java.util.*;

/**
 * Handles trade logic between teams.
 * The AI evaluates trades based on trade value totals.
 */
public class TradeManager {

    private static final Random random = new Random();
    private static final double AI_ACCEPT_THRESHOLD = 0.85; // AI accepts if they get ≥85% of value

    /**
     * Attempt to trade players from userTeam to targetTeam.
     * Returns a message describing the result.
     */
    public static String proposeTrade(Team userTeam, List<Player> userOffers,
                                       Team targetTeam, List<Player> targetRequests) {
        if (userOffers.isEmpty() || targetRequests.isEmpty()) {
            return "Trade must include players on both sides.";
        }

        // Validate ownership
        for (Player p : userOffers) {
            if (!userTeam.getPlayers().contains(p)) {
                return "❌ " + p.getName() + " is not on your roster.";
            }
        }
        for (Player p : targetRequests) {
            if (!targetTeam.getPlayers().contains(p)) {
                return "❌ " + p.getName() + " is not on " + targetTeam.getName() + "'s roster.";
            }
        }

        int userValue   = userOffers.stream().mapToInt(Player::tradeValue).sum();
        int targetValue = targetRequests.stream().mapToInt(Player::tradeValue).sum();

        // AI decision: accepts if they're getting at least as good a deal
        double ratio = (double) userValue / Math.max(1, targetValue);
        boolean aiAccepts = ratio >= AI_ACCEPT_THRESHOLD || random.nextDouble() < 0.10; // 10% random accept

        if (!aiAccepts) {
            int shortfall = targetValue - userValue;
            return String.format("❌ %s rejected the trade. Your offer was undervalued by ~%d trade points. " +
                            "Consider adding a better prospect or pick.",
                    targetTeam.getName(), shortfall);
        }

        // Execute trade
        for (Player p : userOffers) {
            userTeam.removePlayer(p);
            targetTeam.addPlayer(p);
        }
        for (Player p : targetRequests) {
            targetTeam.removePlayer(p);
            userTeam.addPlayer(p);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("✅ TRADE COMPLETED!\n");
        sb.append(userTeam.getName()).append(" receives: ");
        targetRequests.forEach(p -> sb.append(p.getName()).append(", "));
        sb.append("\n");
        sb.append(targetTeam.getName()).append(" receives: ");
        userOffers.forEach(p -> sb.append(p.getName()).append(", "));

        return sb.toString().replaceAll(", $", "").replaceAll(",\n", "\n");
    }

    /**
     * Check if a player is tradeable (not injured long-term, etc.)
     */
    public static boolean isTradeable(Player p) {
        return p.getInjuryDays() <= 7;
    }

    /**
     * Generate AI-to-AI trades to keep the sim dynamic.
     * Returns description of what happened (or null if no trade).
     */
    public static String simulateAITrade(List<Team> aiTeams) {
        if (aiTeams.size() < 2 || random.nextDouble() > 0.15) return null;

        Team teamA = aiTeams.get(random.nextInt(aiTeams.size()));
        Team teamB;
        do { teamB = aiTeams.get(random.nextInt(aiTeams.size())); } while (teamB == teamA);

        List<Player> poolA = teamA.getPlayers().stream()
                .filter(p -> !p.isInjured() && p.getAge() >= 18)
                .toList();
        List<Player> poolB = teamB.getPlayers().stream()
                .filter(p -> !p.isInjured() && p.getAge() >= 18)
                .toList();

        if (poolA.isEmpty() || poolB.isEmpty()) return null;

        Player fromA = poolA.get(random.nextInt(poolA.size()));
        Player fromB = poolB.get(random.nextInt(poolB.size()));

        // Only if values are reasonably close
        if (Math.abs(fromA.tradeValue() - fromB.tradeValue()) <= 10) {
            teamA.removePlayer(fromA);
            teamB.addPlayer(fromA);
            teamB.removePlayer(fromB);
            teamA.addPlayer(fromB);

            return String.format("📰 TRADE: %s sends %s to %s in exchange for %s",
                    teamA.getName(), fromA.getName(), teamB.getName(), fromB.getName());
        }

        return null;
    }
}
