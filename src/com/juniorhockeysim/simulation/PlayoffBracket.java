package com.juniorhockeysim.simulation;

import com.juniorhockeysim.core.GameDate;
import com.juniorhockeysim.domain.Team;

import java.io.Serializable;
import java.util.*;

/**
 * Manages the full playoff bracket with day-by-day series progression.
 * Supports re-seeding after each round.
 */
public class PlayoffBracket implements Serializable {

    private static final long serialVersionUID = 2L;

    private final List<Team> originalSeeds;
    private List<PlayoffSeries> activeSeries = new ArrayList<>();
    private List<PlayoffSeries> completedSeries = new ArrayList<>();
    private int currentRound = 0;
    private Team champion = null;

    private Map<Integer, Team> seedMap = new LinkedHashMap<>();

    private static final String[] ROUND_NAMES = {
        "Quarter-Finals", "Semi-Finals", "Championship Final"
    };

    /** Use the two-arg constructor so date is always correct. */
    public PlayoffBracket(List<Team> seededTeams) {
        this(seededTeams, new GameDate(15, 1, 2027));
    }

    public PlayoffBracket(List<Team> seededTeams, GameDate startDate) {
        this.originalSeeds = new ArrayList<>(seededTeams);
        for (int i = 0; i < seededTeams.size(); i++) {
            seedMap.put(i + 1, seededTeams.get(i));
        }
        buildRound(startDate);
    }

    private void buildRound(GameDate startDate) {
        activeSeries.clear();

        List<Integer> seedNums = new ArrayList<>(seedMap.keySet());
        Collections.sort(seedNums);

        int n = seedNums.size();
        int seriesOffset = 0;
        for (int i = 0; i < n / 2; i++) {
            int highSeedNum = seedNums.get(i);
            int lowSeedNum  = seedNums.get(n - 1 - i);
            Team higher = seedMap.get(highSeedNum);
            Team lower  = seedMap.get(lowSeedNum);

            GameDate seriesStart = startDate.copy();
            for (int d = 0; d < seriesOffset; d++) seriesStart.nextDay();
            seriesOffset++;

            activeSeries.add(new PlayoffSeries(higher, highSeedNum, lower, lowSeedNum, seriesStart));
        }
    }

    /**
     * Called when all active series are complete. Advances to next round.
     * Safe to call multiple times — guards against empty or incomplete series.
     */
    public void advanceRound() {
        if (activeSeries.isEmpty()) return; // nothing to advance
        if (!activeSeries.stream().allMatch(PlayoffSeries::isComplete)) return;

        // Capture the latest next-game-date BEFORE clearing activeSeries
        GameDate nextStart = activeSeries.stream()
                .map(PlayoffSeries::getNextGameDate)
                .max(Comparator.naturalOrder())
                .orElse(null);

        completedSeries.addAll(activeSeries);
        currentRound++;

        // Build next seed map from winners
        Map<Integer, Team> newSeedMap = new LinkedHashMap<>();
        for (PlayoffSeries s : activeSeries) {
            if (s.getWinner() != null) {
                newSeedMap.put(s.getWinnerSeedNum(), s.getWinner());
            }
        }
        seedMap = newSeedMap;
        activeSeries.clear(); // clear AFTER extracting dates and winners

        if (seedMap.size() <= 1) {
            // Champion decided
            if (!seedMap.isEmpty()) champion = seedMap.values().iterator().next();
            return;
        }

        if (nextStart == null) nextStart = new GameDate(1, 3, 2027); // safe fallback
        buildRound(nextStart);
    }

    public boolean isComplete() {
        return champion != null || seedMap.size() <= 1;
    }

    public Team getChampion() {
        if (champion != null) return champion;
        if (seedMap.size() == 1) return seedMap.values().iterator().next();
        return null;
    }

    public List<PlayoffSeries> getActiveSeries()    { return activeSeries;    }
    public List<PlayoffSeries> getCompletedSeries() { return completedSeries; }
    public int getCurrentRound()                    { return currentRound;    }

    public String getCurrentRoundName() {
        if (currentRound < ROUND_NAMES.length) return ROUND_NAMES[currentRound];
        return "Round " + (currentRound + 1);
    }

    public List<Team> getOriginalSeeds() { return originalSeeds; }
}
