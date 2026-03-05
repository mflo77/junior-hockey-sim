package com.juniorhockeysim.simulation;

import com.juniorhockeysim.core.GameDate;
import com.juniorhockeysim.domain.Team;

import java.io.Serializable;

/**
 * Represents a single best-of-7 playoff series.
 * Tracks game wins and schedules game dates (every 2 days).
 */
public class PlayoffSeries implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Team higherSeed;  // gets home ice
    private final Team lowerSeed;
    private final int higherSeedNum;
    private final int lowerSeedNum;

    private int higherWins = 0;
    private int lowerWins  = 0;
    private int gameNum    = 0;

    private GameDate nextGameDate;

    public PlayoffSeries(Team higherSeed, int higherSeedNum, Team lowerSeed, int lowerSeedNum, GameDate startDate) {
        this.higherSeed    = higherSeed;
        this.higherSeedNum = higherSeedNum;
        this.lowerSeed     = lowerSeed;
        this.lowerSeedNum  = lowerSeedNum;
        this.nextGameDate  = startDate.copy();
    }

    public boolean shouldPlayToday(GameDate date) {
        return !isComplete() && date.isSameDate(nextGameDate);
    }

    public void recordGame(GameResult result) {
        gameNum++;
        boolean higherIsHome = (gameNum == 1 || gameNum == 2 || gameNum == 5 || gameNum == 7);
        Team home = higherIsHome ? higherSeed : lowerSeed;
        boolean homeWon = result.getHomeScore() > result.getAwayScore();
        boolean higherWon = (higherIsHome == homeWon);

        if (higherWon) higherWins++;
        else           lowerWins++;

        // Next game in 2 days
        nextGameDate = nextGameDate.copy();
        nextGameDate.nextDay();
        nextGameDate.nextDay();
    }

    public Team getHomeTeam() {
        int g = gameNum + 1; // game we're about to play
        boolean higherHome = (g == 1 || g == 2 || g == 5 || g == 7);
        return higherHome ? higherSeed : lowerSeed;
    }

    public Team getAwayTeam() {
        return getHomeTeam().equals(higherSeed) ? lowerSeed : higherSeed;
    }

    public boolean isComplete() {
        return higherWins >= 4 || lowerWins >= 4;
    }

    public Team getWinner() {
        if (!isComplete()) return null;
        return higherWins >= 4 ? higherSeed : lowerSeed;
    }

    public int getWinnerSeedNum() {
        if (getWinner() == null) return -1;
        return getWinner().equals(higherSeed) ? higherSeedNum : lowerSeedNum;
    }

    public Team getHigherSeed()    { return higherSeed;    }
    public Team getLowerSeed()     { return lowerSeed;     }
    public int  getHigherSeedNum() { return higherSeedNum; }
    public int  getLowerSeedNum()  { return lowerSeedNum;  }
    public int  getHigherWins()    { return higherWins;    }
    public int  getLowerWins()     { return lowerWins;     }
    public int  getGameNum()       { return gameNum;       }
    public GameDate getNextGameDate() { return nextGameDate; }

    public String getSummary() {
        return higherSeed.getName() + " " + higherWins + " - " + lowerWins + " " + lowerSeed.getName();
    }

    public String getStatusLine() {
        if (isComplete()) return getWinner().getName() + " win " + Math.max(higherWins,lowerWins) + "-" + Math.min(higherWins,lowerWins);
        return higherSeed.getName() + " " + higherWins + "-" + lowerWins + " " + lowerSeed.getName() + " (Gm " + (gameNum+1) + ")";
    }
}
