package com.juniorhockeysim.domain;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class League implements Serializable {

    private List<Team> teams;

    public League() {
        teams = new ArrayList<>();
        createTeams();
    }

    private void createTeams() {
        teams.add(new Team("North Hawks"));
        teams.add(new Team("River Kings"));
        teams.add(new Team("Iron Wolves"));
        teams.add(new Team("Coastal Bears"));
        teams.add(new Team("Prairie Falcons"));
        teams.add(new Team("Metro Knights"));
        teams.add(new Team("Valley Storm"));
        teams.add(new Team("Silver Foxes"));
        teams.add(new Team("Timber Raiders"));
        teams.add(new Team("Ice Vipers"));
        teams.add(new Team("Harbor Titans"));
        teams.add(new Team("Granite Blades"));
    }

    public List<Team> getTeams() {
        return teams;
    }

    /**
     * Returns teams sorted by points (then wins as tiebreaker).
     */
    public List<Team> getStandings() {
        return teams.stream()
                .sorted(Comparator.comparingInt(Team::getPoints).reversed()
                        .thenComparingInt(Team::getWins).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns the top N teams for playoffs.
     */
    public List<Team> getPlayoffTeams(int n) {
        return getStandings().subList(0, Math.min(n, teams.size()));
    }
}
