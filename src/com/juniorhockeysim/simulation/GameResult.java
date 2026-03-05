package com.juniorhockeysim.simulation;

import com.juniorhockeysim.domain.Player;
import java.io.Serializable;
import java.util.List;

public class GameResult implements Serializable {

    public enum GameEndType { REGULATION, OVERTIME, SHOOTOUT }

    private final String homeTeamName;
    private final String awayTeamName;
    private final int homeScore;
    private final int awayScore;
    private final int homeShotsOnGoal;
    private final int awayShotsOnGoal;
    private final List<String> events;
    private final GameEndType endType;

    // Stars of the game
    private final Player firstStar;
    private final Player secondStar;
    private final Player thirdStar;

    public GameResult(String homeTeamName, String awayTeamName,
                      int homeScore, int awayScore,
                      int homeShotsOnGoal, int awayShotsOnGoal,
                      List<String> events, GameEndType endType,
                      Player firstStar, Player secondStar, Player thirdStar) {
        this.homeTeamName  = homeTeamName;
        this.awayTeamName  = awayTeamName;
        this.homeScore     = homeScore;
        this.awayScore     = awayScore;
        this.homeShotsOnGoal  = homeShotsOnGoal;
        this.awayShotsOnGoal  = awayShotsOnGoal;
        this.events        = events;
        this.endType       = endType;
        this.firstStar     = firstStar;
        this.secondStar    = secondStar;
        this.thirdStar     = thirdStar;
    }

    public void printSummary() {
        System.out.println();
        System.out.println("┌─────────────────────────────────────┐");
        String endLabel = endType == GameEndType.REGULATION ? ""
                : endType == GameEndType.OVERTIME ? " (OT)" : " (SO)";
        System.out.printf("│  FINAL%s%-29s│%n", endLabel, "");
        System.out.printf("│  %-18s %2d  SOG: %2d       │%n", awayTeamName, awayScore, awayShotsOnGoal);
        System.out.printf("│  %-18s %2d  SOG: %2d       │%n", homeTeamName, homeScore, homeShotsOnGoal);
        System.out.println("└─────────────────────────────────────┘");
        System.out.println();

        if (!events.isEmpty()) {
            System.out.println("--- SCORING SUMMARY ---");
            events.forEach(System.out::println);
        }

        System.out.println();
        System.out.println("--- ⭐ STARS OF THE GAME ---");
        if (firstStar  != null) System.out.println("  1st Star: " + firstStar.getName());
        if (secondStar != null) System.out.println("  2nd Star: " + secondStar.getName());
        if (thirdStar  != null) System.out.println("  3rd Star: " + thirdStar.getName());
        System.out.println();
    }

    // Convenience: home team is A (index 0 in score)
    public int getHomeScore() { return homeScore; }
    public int getAwayScore()  { return awayScore; }

    // Legacy compatibility
    public int getScoreA() { return homeScore; }
    public int getScoreB() { return awayScore; }

    public boolean isOvertimeResult() {
        return endType == GameEndType.OVERTIME || endType == GameEndType.SHOOTOUT;
    }

    public boolean isShootoutResult() { return endType == GameEndType.SHOOTOUT; }

    public String getHomeTeamName() { return homeTeamName; }
    public String getAwayTeamName() { return awayTeamName; }
    public GameEndType getEndType()  { return endType; }

    public List<String> getEvents()  { return events; }
    public Player getFirstStar()     { return firstStar;  }
    public Player getSecondStar()    { return secondStar; }
    public Player getThirdStar()     { return thirdStar;  }
}
