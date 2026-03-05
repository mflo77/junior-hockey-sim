package com.juniorhockeysim.simulation;

import com.juniorhockeysim.domain.League;
import com.juniorhockeysim.domain.Team;
import java.util.Comparator;

public class SeasonSimulator {

    public static void simulateSeason(League league) {
        var teams = league.getTeams();

        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Team home = teams.get(i);
                Team away = teams.get(j);
                GameResult result = GameSimulator.simulateGame(home, away);

                if (result.getHomeScore() > result.getAwayScore()) {
                    home.addWin();
                    away.addLoss();
                } else if (result.isOvertimeResult()) {
                    // Loser still gets 1 point in OT/SO
                    if (result.getHomeScore() > result.getAwayScore()) {
                        home.addWin(); away.addOTLoss();
                    } else {
                        away.addWin(); home.addOTLoss();
                    }
                } else {
                    away.addWin();
                    home.addLoss();
                }
            }
        }

        teams.sort(Comparator.comparing(Team::getPoints).reversed());

        System.out.println("FINAL STANDINGS");
        System.out.printf("%-22s %4s %4s %4s %4s %4s%n",
                "Team", "GP", "W", "L", "OTL", "PTS");
        for (Team t : teams) {
            System.out.printf("%-22s %4d %4d %4d %4d %4d%n",
                    t.getName(), t.getGamesPlayed(),
                    t.getWins(), t.getLosses(), t.getOTLosses(), t.getPoints());
        }
    }
}
