package com.juniorhockeysim.simulation;

import com.juniorhockeysim.core.GameDate;
import com.juniorhockeysim.domain.Team;
import java.io.Serializable;

public class ScheduledGame implements Serializable {

    private final GameDate date;
    private final Team homeTeam;
    private final Team awayTeam;
    private boolean played = false;
    private GameResult result = null;

    public ScheduledGame(GameDate date, Team homeTeam, Team awayTeam) {
        this.date     = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public GameDate getDate()    { return date;     }
    public Team getHomeTeam()    { return homeTeam; }
    public Team getAwayTeam()    { return awayTeam; }
    public boolean isPlayed()    { return played;   }
    public GameResult getResult() { return result;  }

    public void setPlayed(boolean played) { this.played = played; }
    public void setResult(GameResult result) {
        this.result = result;
        this.played = true;
    }

    public boolean involves(Team team) {
        return homeTeam.equals(team) || awayTeam.equals(team);
    }
}
