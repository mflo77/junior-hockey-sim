package com.juniorhockeysim.core;

import com.juniorhockeysim.domain.*;
import com.juniorhockeysim.simulation.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class FranchiseMode implements Serializable {

    private static final long serialVersionUID = 3L;

    // ── Core state ────────────────────────────────────────────────────
    private final List<Team> leagueTeams;
    private List<ScheduledGame> schedule;
    private final Team userTeam;
    private GameDate currentDate;

    // ── Season calendar constants ─────────────────────────────────────
    // Regular season: Oct 1 → Jan 9 (44 games per team, 3x/week)
    // Trade deadline: Dec 15
    // Playoffs: Jan 15 onward
    private int seasonYear = 2026;

    private GameDate seasonEndDate()    { return new GameDate(10, 1,  seasonYear + 1); }
    private GameDate tradeDeadline()    { return new GameDate(15, 12, seasonYear);     }
    private GameDate playoffStartDate() { return new GameDate(15, 1,  seasonYear + 1); }
    private GameDate seasonStartDate()  { return new GameDate(1,  10, seasonYear);     }

    // ── Season state ──────────────────────────────────────────────────
    public enum SeasonPhase { PRE_SEASON, REGULAR_SEASON, PLAYOFFS, OFF_SEASON }
    private SeasonPhase phase = SeasonPhase.REGULAR_SEASON;
    private boolean seasonOver      = false;
    private boolean playoffsRun     = false;
    private boolean developmentDone = false;
    private boolean draftDone       = false;   // prevents double-draft
    private DraftManager currentDraftManager = null; // persisted between UI calls

    // Playoff state (for day-by-day sim)
    private PlayoffBracket playoffBracket = null;
    private List<PlayoffSeries> activeSeries = new ArrayList<>();

    // ── Free agents ───────────────────────────────────────────────────
    private FreeAgentManager freeAgentManager;

    // ── Signing budget (resets each offseason, shared across all FA signings) ──
    private int signingBudget    = 120; // points available this offseason
    private int signingBudgetMax = 120; // max budget (could expand with future upgrades)

    // ── Season history ────────────────────────────────────────────────
    public static class SeasonRecord implements java.io.Serializable {
        public final int startYear;
        public final String champion;
        public final String regularSeasonLeader;
        public final int leaderPoints;
        public SeasonRecord(int yr, String champ, String rsLeader, int pts) {
            this.startYear = yr; this.champion = champ;
            this.regularSeasonLeader = rsLeader; this.leaderPoints = pts;
        }
        public String seasonLabel() { return startYear + "-" + (startYear + 1); }
    }
    private final List<SeasonRecord> seasonHistory = new ArrayList<>();

    // ── News ──────────────────────────────────────────────────────────
    private final List<String> recentNews = new ArrayList<>();

    private static final Random random = new Random();

    // ─────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────

    public FranchiseMode(List<Team> leagueTeams, Team userTeam) {
        this.leagueTeams     = leagueTeams;
        this.userTeam        = userTeam;
        this.currentDate     = seasonStartDate();
        this.schedule        = ScheduleGenerator.generateSchedule(leagueTeams, seasonYear);
        this.freeAgentManager = new FreeAgentManager();
        this.freeAgentManager.generateFreeAgentPool(30);
    }

    // ─────────────────────────────────────────────────────────────────
    // Primary advance methods
    // ─────────────────────────────────────────────────────────────────

    /** Advance one full day (verbose output). */
    public List<String> advanceDay() {
        List<String> dayEvents = new ArrayList<>();

        if (phase == SeasonPhase.OFF_SEASON) {
            dayEvents.add("It's the off-season. Use the menus to manage your roster.");
            return dayEvents;
        }

        dayEvents.add("=== " + currentDate + " ===");

        // Injury recoveries
        for (Team team : leagueTeams) {
            for (Player p : team.healInjuries()) {
                String msg = p.getName() + " (" + team.getName() + ") has returned from injury.";
                dayEvents.add(msg);
                addNews("RECOVERY: " + msg);
            }
        }

        if (phase == SeasonPhase.REGULAR_SEASON) {
            // Simulate today's games
            for (ScheduledGame game : schedule) {
                if (!game.isPlayed() && game.getDate().isSameDate(currentDate)) {
                    GameResult result = simulateAndRecordGame(game);
                    String line = formatGameLine(game, result);
                    dayEvents.add(line);
                }
            }

            // AI trades
            if (!currentDate.isAfter(tradeDeadline())) {
                double tradeChance = currentDate.compareTo(new GameDate(1, 12, seasonYear)) > 0 ? 0.22 : 0.07;
                if (random.nextDouble() < tradeChance) {
                    List<Team> aiTeams = leagueTeams.stream().filter(t -> !t.equals(userTeam)).collect(Collectors.toList());
                    String news = TradeManager.simulateAITrade(aiTeams);
                    if (news != null) { addNews(news); dayEvents.add(news); }
                }
            }

            currentDate.nextDay();

            // Check if regular season ended
            if (!currentDate.isBefore(seasonEndDate())) {
                seasonOver = true;
                phase = SeasonPhase.PLAYOFFS;
                currentDate = playoffStartDate();
                String msg = "REGULAR SEASON COMPLETE! Playoffs begin " + playoffStartDate();
                dayEvents.add(msg);
                addNews(msg);
                initPlayoffBracket();
            }

        } else if (phase == SeasonPhase.PLAYOFFS) {
            // Advance playoff games for today
            List<String> playoffEvents = advancePlayoffDay();
            dayEvents.addAll(playoffEvents);
            currentDate.nextDay();

            // Check if playoffs are done
            if (playoffBracket != null && playoffBracket.isComplete()) {
                playoffsRun = true;
                phase = SeasonPhase.OFF_SEASON;
                Team champ = playoffBracket.getChampion();
                String msg = "PLAYOFFS COMPLETE! " + champ.getName() + " are the " + seasonYear + "-" + (seasonYear+1) + " Champions!";
                dayEvents.add(msg);
                addNews(msg);
            }
        }

        return dayEvents;
    }

    /** Silent advance (used by bulk sim). Returns true if something notable happened. */
    public void advanceDaySilent() {
        if (phase == SeasonPhase.OFF_SEASON) return;

        for (Team team : leagueTeams) {
            for (Player p : team.healInjuries()) {
                addNews("RECOVERY: " + p.getName() + " (" + team.getName() + ") returned from injury.");
            }
        }

        if (phase == SeasonPhase.REGULAR_SEASON) {
            for (ScheduledGame game : schedule) {
                if (!game.isPlayed() && game.getDate().isSameDate(currentDate)) {
                    simulateAndRecordGame(game);
                }
            }
            if (!currentDate.isAfter(tradeDeadline())) {
                if (random.nextDouble() < 0.08) {
                    List<Team> aiTeams = leagueTeams.stream().filter(t -> !t.equals(userTeam)).collect(Collectors.toList());
                    String news = TradeManager.simulateAITrade(aiTeams);
                    if (news != null) addNews(news);
                }
            }
            currentDate.nextDay();
            if (!currentDate.isBefore(seasonEndDate())) {
                seasonOver = true;
                phase = SeasonPhase.PLAYOFFS;
                currentDate = playoffStartDate();
                addNews("REGULAR SEASON COMPLETE! Playoffs begin.");
                initPlayoffBracket();
            }

        } else if (phase == SeasonPhase.PLAYOFFS) {
            advancePlayoffDay();
            currentDate.nextDay();
            if (playoffBracket != null && playoffBracket.isComplete()) {
                playoffsRun = true;
                phase = SeasonPhase.OFF_SEASON;
                addNews("PLAYOFFS COMPLETE! " + playoffBracket.getChampion().getName() + " win the championship!");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Bulk simulation methods
    // ─────────────────────────────────────────────────────────────────

    /** Simulates up to and including the user's next game. Returns the played game, or null. */
    public ScheduledGame simulateNextUserGame() {
        ScheduledGame nextGame = null;
        for (ScheduledGame g : schedule) {
            if (!g.isPlayed() && g.involves(userTeam)) { nextGame = g; break; }
        }
        if (nextGame == null) return null;

        // Silently advance days up to but not including game day
        while (currentDate.isBefore(nextGame.getDate()) && phase == SeasonPhase.REGULAR_SEASON) {
            advanceDaySilent();
        }

        // Now simulate the game itself (if still unplayed — regular season)
        if (phase == SeasonPhase.REGULAR_SEASON && !nextGame.isPlayed()
                && nextGame.getDate().isSameDate(currentDate)) {
            // Heal injuries for today first
            for (Team team : leagueTeams) team.healInjuries();
            simulateAndRecordGame(nextGame);
            // Simulate other games on same day
            for (ScheduledGame g : schedule) {
                if (!g.isPlayed() && g.getDate().isSameDate(currentDate) && !g.involves(userTeam)) {
                    simulateAndRecordGame(g);
                }
            }
            currentDate.nextDay();
            if (!currentDate.isBefore(seasonEndDate())) {
                seasonOver = true;
                phase = SeasonPhase.PLAYOFFS;
                currentDate = playoffStartDate();
                addNews("REGULAR SEASON COMPLETE! Playoffs begin " + playoffStartDate());
                initPlayoffBracket();
            }
            return nextGame;
        }
        return nextGame;
    }

    public void simulateToTradeDeadline() {
        GameDate dl = tradeDeadline();
        while (!currentDate.isAfter(dl) && phase == SeasonPhase.REGULAR_SEASON) advanceDaySilent();
    }

    public void simulateToEndOfSeason() {
        while (phase == SeasonPhase.REGULAR_SEASON) advanceDaySilent();
    }

    public void simulateToDate(GameDate target) {
        while (currentDate.isBefore(target) && phase != SeasonPhase.OFF_SEASON) advanceDaySilent();
    }

    public void simulateEntirePlayoffs() {
        int safety = 0;
        while (phase == SeasonPhase.PLAYOFFS && (playoffBracket == null || !playoffBracket.isComplete()) && safety < 500) {
            advanceDaySilent();
            safety++;
        }
        // Force completion if bracket is stuck
        if (phase == SeasonPhase.PLAYOFFS && playoffBracket != null && !playoffBracket.isComplete()) {
            // Force-simulate any incomplete series directly
            for (PlayoffSeries series : new ArrayList<>(activeSeries)) {
                while (!series.isComplete()) {
                    GameResult result = GameSimulator.simulateGame(series.getHomeTeam(), series.getAwayTeam());
                    series.recordGame(result);
                }
            }
            playoffBracket.advanceRound();
            activeSeries = playoffBracket.getActiveSeries();
            // Keep advancing until done
            int innerSafety = 0;
            while (!playoffBracket.isComplete() && innerSafety < 100) {
                for (PlayoffSeries series : new ArrayList<>(activeSeries)) {
                    while (!series.isComplete()) {
                        GameResult result = GameSimulator.simulateGame(series.getHomeTeam(), series.getAwayTeam());
                        series.recordGame(result);
                    }
                }
                playoffBracket.advanceRound();
                activeSeries = playoffBracket.getActiveSeries();
                innerSafety++;
            }
            playoffsRun = true;
            phase = SeasonPhase.OFF_SEASON;
            if (playoffBracket.getChampion() != null)
                addNews("PLAYOFFS COMPLETE! " + playoffBracket.getChampion().getName() + " win the championship!");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Game simulation
    // ─────────────────────────────────────────────────────────────────

    private GameResult simulateAndRecordGame(ScheduledGame sg) {
        Team home = sg.getHomeTeam();
        Team away = sg.getAwayTeam();

        GameResult result = GameSimulator.simulateGame(home, away);
        sg.setResult(result);

        boolean homeWon = result.getHomeScore() > result.getAwayScore();
        boolean otGame  = result.isOvertimeResult();

        if (homeWon) {
            home.addWin();
            if (otGame) away.addOTLoss(); else away.addLoss();
        } else {
            away.addWin();
            if (otGame) home.addOTLoss(); else home.addLoss();
        }

        if (sg.involves(userTeam)) {
            boolean userIsHome = home.equals(userTeam);
            boolean userWon    = userIsHome ? homeWon : !homeWon;
            int userScore  = userIsHome ? result.getHomeScore() : result.getAwayScore();
            int otherScore = userIsHome ? result.getAwayScore() : result.getHomeScore();
            String opponent = userIsHome ? away.getName() : home.getName();
            String suffix   = result.isShootoutResult() ? " (SO)" : otGame ? " (OT)" : "";
            addNews((userWon ? "WIN" : "LOSS") + ": " + userTeam.getName() + " " +
                    (userWon ? "defeated" : "lost to") + " " + opponent +
                    " " + userScore + "-" + otherScore + suffix);
        }

        return result;
    }

    private String formatGameLine(ScheduledGame game, GameResult result) {
        String suffix = result.isShootoutResult() ? " SO" : result.isOvertimeResult() ? " OT" : "";
        return game.getAwayTeam().getName() + " " + result.getAwayScore() +
               " @ " + game.getHomeTeam().getName() + " " + result.getHomeScore() + suffix;
    }

    // ─────────────────────────────────────────────────────────────────
    // Playoff bracket management
    // ─────────────────────────────────────────────────────────────────

    private void initPlayoffBracket() {
        List<Team> seeds = getSortedStandings().subList(0, Math.min(8, leagueTeams.size()));
        // Use the correct playoff start date for this season year
        playoffBracket = new PlayoffBracket(new ArrayList<>(seeds), playoffStartDate());
        activeSeries = playoffBracket.getActiveSeries();
        addNews("Playoff bracket set: " + seeds.get(0).getName() + " is the #1 seed.");
    }

    private List<String> advancePlayoffDay() {
        List<String> events = new ArrayList<>();
        if (playoffBracket == null || playoffBracket.isComplete()) return events;

        // Each active series plays one game every 2 days
        List<PlayoffSeries> current = new ArrayList<>(activeSeries);
        for (PlayoffSeries series : current) {
            if (series.isComplete()) continue;
            if (series.shouldPlayToday(currentDate)) {
                GameResult result = GameSimulator.simulateGame(series.getHomeTeam(), series.getAwayTeam());
                series.recordGame(result);
                String line = formatSeriesGame(series, result);
                events.add(line);
                addNews(line);

                if (series.isComplete()) {
                    Team winner = series.getWinner();
                    String win = winner.getName() + " win series " + series.getHigherWins() + "-" + series.getLowerWins();
                    events.add(win);
                    addNews(win);
                }
            }
        }

        // Only advance bracket if we have active series AND they're all complete
        if (!activeSeries.isEmpty() && activeSeries.stream().allMatch(PlayoffSeries::isComplete)) {
            playoffBracket.advanceRound();
            activeSeries = playoffBracket.getActiveSeries();
            if (!activeSeries.isEmpty() && !playoffBracket.isComplete()) {
                String roundMsg = "--- " + playoffBracket.getCurrentRoundName() + " ---";
                events.add(roundMsg);
                addNews(roundMsg);
            }
        }

        return events;
    }

    private String formatSeriesGame(PlayoffSeries series, GameResult result) {
        String suffix = result.isShootoutResult() ? " SO" : result.isOvertimeResult() ? " OT" : "";
        return "[" + playoffBracket.getCurrentRoundName() + "] " +
               series.getAwayTeam().getName() + " " + result.getAwayScore() +
               " @ " + series.getHomeTeam().getName() + " " + result.getHomeScore() + suffix +
               " (" + series.getHigherSeed().getName() + " leads " + series.getHigherWins() +
               "-" + series.getLowerWins() + ")";
    }

    // ─────────────────────────────────────────────────────────────────
    // Trades & free agents
    // ─────────────────────────────────────────────────────────────────

    public String proposeTrade(List<Player> userOffers, Team targetTeam, List<Player> targetRequests) {
        if (currentDate.isAfter(tradeDeadline()) && phase == SeasonPhase.REGULAR_SEASON) {
            return "Trade deadline has passed.";
        }
        String result = TradeManager.proposeTrade(userTeam, userOffers, targetTeam, targetRequests);
        addNews("TRADE: " + result.split("\n")[0]);
        return result;
    }

    public boolean signFreeAgent(Player player, int contractYears) {
        boolean signed = freeAgentManager.signPlayer(player, userTeam, contractYears);
        if (signed) addNews("SIGNING: " + userTeam.getName() + " signed " + player.getName() +
                " to a " + contractYears + "-year deal.");
        return signed;
    }

    /** Competitive signing — AI teams also bid. Returns full result with message.
     *  offerScore costs budget points (score/10 rounded up, min 5). */
    public FreeAgentManager.SigningResult signFreeAgentCompetitive(Player player, int years, int offerScore) {
        // Budget cost: lower cost = 3–8 points depending on offer quality (was 10–60)
        int cost = Math.max(3, (int) Math.ceil(offerScore * 0.25));
        if (cost > signingBudget) {
            List<String> noFunds = new ArrayList<>();
            noFunds.add("Not enough signing budget! Need " + cost + " pts, have " + signingBudget + " pts.");
            return new FreeAgentManager.SigningResult(false, null,
                    "Not enough signing budget! Need " + cost + " pts, have " + signingBudget + " pts. Try a lower offer or wait for next offseason.",
                    noFunds);
        }
        FreeAgentManager.SigningResult result = freeAgentManager.resolveCompetition(player, userTeam, years, offerScore, leagueTeams);
        if (result.userWon) {
            signingBudget -= cost; // deduct only if won
            addNews("SIGNING: " + userTeam.getName() + " signed " + player.getName() + " to a " + years + "-year deal.");
        } else if (result.signedWith != null) {
            addNews("MISSED: " + player.getName() + " signed with " + result.signedWith.getName() + ".");
        }
        return result;
    }

    public int getSigningBudget()    { return signingBudget;    }
    public int getSigningBudgetMax() { return signingBudgetMax; }

    /** Returns the active draft manager for this offseason (creates one on first call). */
    public DraftManager getDraftManager() {
        if (currentDraftManager == null) {
            currentDraftManager = new DraftManager(getSortedStandings());
        }
        return currentDraftManager;
    }

    public boolean isDraftDone() { return draftDone; }
    public void    markDraftDone() { draftDone = true; }

    /** Get expected AI interest level for a free agent. */
    public int getFAInterest(Player p) { return freeAgentManager.getExpectedInterest(p); }

    public boolean releasePlayer(Player player) {
        if (!userTeam.getPlayers().contains(player)) return false;
        freeAgentManager.releasePlayer(player, userTeam);
        addNews("RELEASE: " + userTeam.getName() + " released " + player.getName() + ".");
        return true;
    }

    // ─────────────────────────────────────────────────────────────────
    // End of season
    // ─────────────────────────────────────────────────────────────────

    public List<String> endSeasonDevelopment() {
        if (developmentDone) {
            List<String> msg = new ArrayList<>();
            msg.add("Player development has already been processed for this off-season.");
            return msg;
        }
        developmentDone = true;
        List<String> log = new ArrayList<>();
        for (Team team : leagueTeams) {
            for (Player p : new ArrayList<>(team.getPlayers())) {
                String msg = p.developEndOfSeason();
                if (msg != null) log.add(team.getName() + ": " + msg);
            }
            int before = team.getPlayers().size();
            team.endSeason();
            int removed = before - team.getPlayers().size();
            if (removed > 0) log.add(team.getName() + ": " + removed + " player(s) aged out.");
        }
        // Collect expired contracts
        freeAgentManager.collectExpiredContracts(leagueTeams);
        // AI teams sign FAs
        List<Team> aiTeams = leagueTeams.stream().filter(t -> !t.equals(userTeam)).collect(Collectors.toList());
        log.addAll(freeAgentManager.processAISignings(aiTeams));
        return log;
    }

    public void newSeason() {
        // Record this completed season in history before resetting
        String champ = (playoffBracket != null && playoffBracket.getChampion() != null)
                ? playoffBracket.getChampion().getName() : "N/A";
        List<Team> standings = getSortedStandings();
        String rsLeader = standings.isEmpty() ? "N/A" : standings.get(0).getName();
        int leaderPts   = standings.isEmpty() ? 0    : standings.get(0).getPoints();
        seasonHistory.add(new SeasonRecord(seasonYear, champ, rsLeader, leaderPts));

        seasonYear++;
        currentDate     = seasonStartDate();
        schedule        = ScheduleGenerator.generateSchedule(leagueTeams, seasonYear);
        seasonOver      = false;
        playoffsRun     = false;
        developmentDone = false;
        draftDone       = false;
        currentDraftManager = null;  // fresh draft each season
        signingBudget   = signingBudgetMax; // reset budget for new offseason
        phase           = SeasonPhase.REGULAR_SEASON;
        playoffBracket  = null;
        activeSeries.clear();
        leagueTeams.forEach(Team::resetSeasonStats);
        freeAgentManager.generateFreeAgentPool(25);
        recentNews.clear();
        addNews("Welcome to the " + seasonYear + "-" + (seasonYear+1) + " season!");
    }

    // ─────────────────────────────────────────────────────────────────
    // Standings (THE FIX — proper multi-key sort)
    // ─────────────────────────────────────────────────────────────────

    public List<Team> getSortedStandings() {
        // IMPORTANT: use lambda negation to avoid .reversed() chaining bug.
        // Java's chained .reversed() inverts the ENTIRE preceding comparator,
        // not just the last thenComparing — using negative int avoids this.
        return leagueTeams.stream()
                .sorted(Comparator.<Team, Integer>comparing(t -> -t.getPoints())      // 1. Most points first
                    .thenComparing(t -> -t.getWins())                                  // 2. Most wins (tie-break)
                    .thenComparingInt(Team::getGamesPlayed))                           // 3. Fewer GP played
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────
    // News
    // ─────────────────────────────────────────────────────────────────

    public void addNews(String msg) {
        recentNews.add(0, "[" + currentDate.toShortString() + "] " + msg);
        if (recentNews.size() > 50) recentNews.remove(recentNews.size() - 1);
    }

    public List<String> getRecentNews() { return Collections.unmodifiableList(recentNews); }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    private List<Player> getAllPlayers() {
        return leagueTeams.stream().flatMap(t -> t.getPlayers().stream()).collect(Collectors.toList());
    }

    public String getTeamOf(Player p) {
        for (Team t : leagueTeams) if (t.getPlayers().contains(p)) return t.getName();
        return "Free Agent";
    }

    public List<Player> getLeagueGoalLeaders(int n) {
        return getAllPlayers().stream()
                .filter(p -> p.getPosition() != Position.GOALIE && p.getGoals() > 0)
                .sorted(Comparator.<Player, Integer>comparing(p -> -p.getGoals())
                        .thenComparing(p -> -p.getPoints()))
                .limit(n).collect(Collectors.toList());
    }

    public List<Player> getLeaguePointLeaders(int n) {
        return getAllPlayers().stream()
                .filter(p -> p.getPosition() != Position.GOALIE && p.getPoints() > 0)
                .sorted(Comparator.<Player, Integer>comparing(p -> -p.getPoints())
                        .thenComparing(p -> -p.getGoals()))
                .limit(n).collect(Collectors.toList());
    }

    public List<Player> getTopGoalies(int n) {
        return getAllPlayers().stream()
                .filter(p -> p.getPosition() == Position.GOALIE && p.getGoalieGamesPlayed() >= 3)
                .sorted(Comparator.<Player, Double>comparing(p -> -p.getSavePercentage()))
                .limit(n).collect(Collectors.toList());
    }

    public ScheduledGame getNextUserGame() {
        return schedule.stream()
                .filter(g -> !g.isPlayed() && g.involves(userTeam))
                .findFirst().orElse(null);
    }

    public List<ScheduledGame> getUpcomingUserGames(int n) {
        return schedule.stream()
                .filter(g -> !g.isPlayed() && g.involves(userTeam))
                .limit(n).collect(Collectors.toList());
    }

    public List<ScheduledGame> getRecentUserGames(int n) {
        List<ScheduledGame> played = schedule.stream()
                .filter(g -> g.isPlayed() && g.involves(userTeam))
                .collect(Collectors.toList());
        int start = Math.max(0, played.size() - n);
        return played.subList(start, played.size());
    }

    public int countRemainingGames() {
        return (int) schedule.stream().filter(g -> !g.isPlayed() && g.involves(userTeam)).count();
    }

    // ─────────────────────────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────────────────────────

    public GameDate getCurrentDate()              { return currentDate;       }
    public Team getUserTeam()                     { return userTeam;          }
    public List<Team> getLeagueTeams()            { return leagueTeams;       }
    public List<ScheduledGame> getSchedule()      { return schedule;          }
    public boolean isSeasonOver()                 { return seasonOver;        }
    public boolean isPlayoffsRun()                { return playoffsRun;       }
    public int getSeasonYear()                    { return seasonYear;        }
    public SeasonPhase getPhase()                 { return phase;             }
    public FreeAgentManager getFreeAgentManager() { return freeAgentManager;  }
    public PlayoffBracket getPlayoffBracket()     { return playoffBracket;    }
    public List<PlayoffSeries> getActiveSeries()  { return activeSeries;      }
    public GameDate getSeasonEndDate()            { return seasonEndDate();   }
    public GameDate getTradeDeadline()            { return tradeDeadline();   }
    public GameDate getPlayoffStartDate()         { return playoffStartDate();}
    public GameDate getSeasonStartDate()          { return seasonStartDate(); }
    public List<SeasonRecord> getSeasonHistory()  { return Collections.unmodifiableList(seasonHistory); }
    public boolean isDevelopmentDone()            { return developmentDone;   }
}
