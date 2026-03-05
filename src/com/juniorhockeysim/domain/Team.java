package com.juniorhockeysim.domain;

import java.io.Serializable;
import java.util.*;

public class Team implements Serializable {

    private String name;
    private String city;
    private List<Player> players;

    // Record
    private int wins   = 0;
    private int losses = 0;
    private int otLosses = 0;
    private int points = 0;

    // Lines
    private List<Player> line1 = new ArrayList<>();
    private List<Player> line2 = new ArrayList<>();
    private List<Player> line3 = new ArrayList<>();
    private List<Player> line4 = new ArrayList<>();

    private List<Player> pair1 = new ArrayList<>();
    private List<Player> pair2 = new ArrayList<>();
    private List<Player> pair3 = new ArrayList<>();

    private Player startingGoalie;
    private Player backupGoalie;

    // Fatigue (per line/pair)
    private double line1Fatigue = 0, line2Fatigue = 0, line3Fatigue = 0, line4Fatigue = 0;
    private double pair1Fatigue = 0, pair2Fatigue = 0, pair3Fatigue = 0;

    // Morale
    private int winStreak  = 0;
    private int loseStreak = 0;

    // Waiver/scratch list
    private List<Player> scratched = new ArrayList<>();

    private static final Random random = new Random();

    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
        generateRoster();
    }

    // ---- Roster Generation ----

    private void generateRoster() {
        for (int i = 0; i < 14; i++)
            players.add(new Player(NameGenerator.generate(), randomAge(), Position.FORWARD));
        for (int i = 0; i < 7; i++)
            players.add(new Player(NameGenerator.generate(), randomAge(), Position.DEFENSE));
        for (int i = 0; i < 2; i++)
            players.add(new Player(NameGenerator.generate(), randomAge(), Position.GOALIE));

        setupLines();
    }

    public void setupLines() {
        List<Player> activePlayers = players.stream()
                .filter(p -> !scratched.contains(p) && !p.isInjured())
                .toList();

        List<Player> forwards = activePlayers.stream()
                .filter(p -> p.getPosition() == Position.FORWARD)
                .sorted(Comparator.comparingInt(Player::overallRating).reversed())
                .toList();

        List<Player> defense = activePlayers.stream()
                .filter(p -> p.getPosition() == Position.DEFENSE)
                .sorted(Comparator.comparingInt(Player::overallRating).reversed())
                .toList();

        List<Player> goalies = activePlayers.stream()
                .filter(p -> p.getPosition() == Position.GOALIE)
                .sorted(Comparator.comparingInt(Player::overallRating).reversed())
                .toList();

        // Build lines from what's available
        int fCount = forwards.size();
        line1 = fCount >= 3  ? new ArrayList<>(forwards.subList(0, 3))            : new ArrayList<>(forwards.subList(0, Math.min(3, fCount)));
        line2 = fCount >= 6  ? new ArrayList<>(forwards.subList(3, 6))            : new ArrayList<>();
        line3 = fCount >= 9  ? new ArrayList<>(forwards.subList(6, 9))            : new ArrayList<>();
        line4 = fCount >= 12 ? new ArrayList<>(forwards.subList(9, 12))           : new ArrayList<>();

        int dCount = defense.size();
        pair1 = dCount >= 2 ? new ArrayList<>(defense.subList(0, 2))             : new ArrayList<>(defense);
        pair2 = dCount >= 4 ? new ArrayList<>(defense.subList(2, 4))             : new ArrayList<>();
        pair3 = dCount >= 6 ? new ArrayList<>(defense.subList(4, 6))             : new ArrayList<>();

        startingGoalie = goalies.isEmpty()  ? null : goalies.get(0);
        backupGoalie   = goalies.size() > 1 ? goalies.get(1) : startingGoalie;
    }

    private int randomAge() {
        return random.nextInt(5) + 16; // 16–20
    }

    // ---- Lineup Rolling (with fatigue influence) ----

    public List<Player> rollForwardLine() {
        if (line2.isEmpty()) return line1;
        if (line3.isEmpty()) {
            return random.nextBoolean() ? line1 : line2;
        }
        double roll = random.nextDouble();
        if (roll < 0.35) return line1;
        else if (roll < 0.65) return line2;
        else if (roll < 0.85) return line3;
        else return line4.isEmpty() ? line3 : line4;
    }

    public List<Player> rollDefensePair() {
        if (pair2.isEmpty()) return pair1;
        if (pair3.isEmpty()) return random.nextBoolean() ? pair1 : pair2;
        double roll = random.nextDouble();
        if (roll < 0.40) return pair1;
        else if (roll < 0.75) return pair2;
        else return pair3;
    }

    // ---- Fatigue ----

    public double getLineFatigue(List<Player> line) {
        if (line == line1) return line1Fatigue;
        if (line == line2) return line2Fatigue;
        if (line == line3) return line3Fatigue;
        return line4Fatigue;
    }

    public void increaseLineFatigue(List<Player> line, double amount) {
        if (line == line1)      line1Fatigue += amount;
        else if (line == line2) line2Fatigue += amount;
        else if (line == line3) line3Fatigue += amount;
        else                    line4Fatigue += amount;
    }

    public void recoverLineFatigue(List<Player> activeLine, double amount) {
        if (activeLine != line1) line1Fatigue = Math.max(0, line1Fatigue - amount);
        if (activeLine != line2) line2Fatigue = Math.max(0, line2Fatigue - amount);
        if (activeLine != line3) line3Fatigue = Math.max(0, line3Fatigue - amount);
        if (activeLine != line4) line4Fatigue = Math.max(0, line4Fatigue - amount);
    }

    public double getPairFatigue(List<Player> pair) {
        if (pair == pair1) return pair1Fatigue;
        if (pair == pair2) return pair2Fatigue;
        return pair3Fatigue;
    }

    public void increasePairFatigue(List<Player> pair, double amount) {
        if (pair == pair1)      pair1Fatigue += amount;
        else if (pair == pair2) pair2Fatigue += amount;
        else                    pair3Fatigue += amount;
    }

    public void recoverPairFatigue(List<Player> activePair, double amount) {
        if (activePair != pair1) pair1Fatigue = Math.max(0, pair1Fatigue - amount);
        if (activePair != pair2) pair2Fatigue = Math.max(0, pair2Fatigue - amount);
        if (activePair != pair3) pair3Fatigue = Math.max(0, pair3Fatigue - amount);
    }

    public void resetFatigue() {
        line1Fatigue = line2Fatigue = line3Fatigue = line4Fatigue = 0;
        pair1Fatigue = pair2Fatigue = pair3Fatigue = 0;
    }

    // ---- Record ----

    public void addWin() {
        wins++;
        points += 2;
        winStreak++;
        loseStreak = 0;
        // Boost morale on wins
        players.forEach(p -> p.adjustMorale(winStreak >= 3 ? 4 : 2));
    }

    public void addLoss() {
        losses++;
        winStreak = 0;
        loseStreak++;
        players.forEach(p -> p.adjustMorale(loseStreak >= 3 ? -4 : -2));
    }

    public void addOTLoss() {
        otLosses++;
        points += 1;
        winStreak = 0;
        loseStreak++;
        players.forEach(p -> p.adjustMorale(-1));
    }

    // ---- Strength Metrics ----

    public int getTopLineStrength() {
        if (line1.isEmpty()) return 50;
        return (int) line1.stream().mapToInt(Player::overallRating).average().orElse(50);
    }

    public int getTopPairStrength() {
        if (pair1.isEmpty()) return 50;
        return (int) pair1.stream().mapToInt(Player::overallRating).average().orElse(50);
    }

    public int teamOffense() {
        List<Player> fwds = players.stream().filter(p -> p.getPosition() == Position.FORWARD).toList();
        if (fwds.isEmpty()) return 50;
        return (int) fwds.stream().mapToInt(Player::getOffensiveRating).average().orElse(50);
    }

    public int teamDefense() {
        List<Player> defs = players.stream().filter(p -> p.getPosition() == Position.DEFENSE).toList();
        if (defs.isEmpty()) return 50;
        return (int) defs.stream().mapToInt(Player::getDefensiveRating).average().orElse(50);
    }

    public int teamOverall() {
        return (teamOffense() + teamDefense() + (startingGoalie != null ? startingGoalie.getSaveRating() : 50)) / 3;
    }

    // ---- Injury ----

    /**
     * Heal all players by one day. Returns list of players who just recovered.
     */
    public List<Player> healInjuries() {
        List<Player> recovered = new ArrayList<>();
        for (Player p : players) {
            if (p.isInjured()) {
                p.healOneDay();
                if (!p.isInjured()) recovered.add(p);
            }
        }
        if (!recovered.isEmpty()) setupLines();
        return recovered;
    }

    public List<Player> getInjuredPlayers() {
        return players.stream().filter(Player::isInjured).toList();
    }

    // ---- Roster management ----

    public void addPlayer(Player p) {
        players.add(p);
        setupLines();
    }

    public void removePlayer(Player p) {
        players.remove(p);
        scratched.remove(p);
        setupLines();
    }

    public void scratchPlayer(Player p) {
        if (!scratched.contains(p)) scratched.add(p);
        setupLines();
    }

    public void unScratchPlayer(Player p) {
        scratched.remove(p);
        setupLines();
    }

    // ---- End of Season ----

    public void endSeason() {
        players.forEach(Player::birthday);
        players.forEach(Player::decrementContract);
        // Remove players who are too old (22+) or whose contracts expired (free agents)
        players.removeIf(p -> p.getAge() >= 22);
    }

    public void resetSeasonStats() {
        players.forEach(Player::resetSeasonStats);
        wins = losses = otLosses = points = 0;
        winStreak = loseStreak = 0;
    }

    // ---- Getters ----

    public String getName()     { return name; }
    public int getPoints()      { return points; }
    public int getWins()        { return wins; }
    public int getLosses()      { return losses; }
    public int getOTLosses()    { return otLosses; }
    public int getGamesPlayed() { return wins + losses + otLosses; }
    public int getWinStreak()   { return winStreak; }
    public int getLoseStreak()  { return loseStreak; }

    public String getRecord() {
        return wins + "-" + losses + (otLosses > 0 ? "-" + otLosses : "");
    }

    public Player getStartingGoalie() { return startingGoalie; }
    public Player getBackupGoalie()   { return backupGoalie;   }

    public List<Player> getPlayers()   { return Collections.unmodifiableList(players); }
    public List<Player> getSkaters()   {
        return players.stream().filter(p -> p.getPosition() != Position.GOALIE).toList();
    }
    public List<Player> getForwards()  {
        return players.stream().filter(p -> p.getPosition() == Position.FORWARD).toList();
    }
    public List<Player> getDefenders() {
        return players.stream().filter(p -> p.getPosition() == Position.DEFENSE).toList();
    }
    public List<Player> getGoalies()   {
        return players.stream().filter(p -> p.getPosition() == Position.GOALIE).toList();
    }
    public List<Player> getScratched() { return scratched; }

    public List<Player> getLine1() { return line1; }
    public List<Player> getLine2() { return line2; }
    public List<Player> getLine3() { return line3; }
    public List<Player> getLine4() { return line4; }
    public List<Player> getPair1() { return pair1; }
    public List<Player> getPair2() { return pair2; }
    public List<Player> getPair3() { return pair3; }


    /**
     * Adds a bench player to the specified line (by appending).
     * lineIndex: 0=line1,1=line2,2=line3,3=line4,4=pair1,5=pair2,6=pair3
     */
    public void addPlayerToLine(Player p, int lineIndex) {
        List<List<Player>> allLines = Arrays.asList(line1, line2, line3, line4, pair1, pair2, pair3);
        if (lineIndex < 0 || lineIndex >= allLines.size()) return;
        List<Player> target = allLines.get(lineIndex);
        if (!target.contains(p)) target.add(p);
    }

    /**
     * Swaps two players between any of the forward lines / defense pairs / goalie slots.
     * Finds which line each player belongs to, then swaps their slots.
     * Does NOT call setupLines() so manual arrangement is preserved.
     */
    public void swapPlayers(Player a, Player b) {
        // Collect all mutable line lists
        List<List<Player>> allLines = Arrays.asList(line1, line2, line3, line4, pair1, pair2, pair3);

        List<Player> lineA = null, lineB = null;
        int idxA = -1, idxB = -1;

        for (List<Player> line : allLines) {
            for (int i = 0; i < line.size(); i++) {
                if (line.get(i) == a) { lineA = line; idxA = i; }
                if (line.get(i) == b) { lineB = line; idxB = i; }
            }
        }
        // Handle goalie swaps
        if (a == startingGoalie && b == backupGoalie) { startingGoalie = b; backupGoalie = a; return; }
        if (a == backupGoalie  && b == startingGoalie) { startingGoalie = a; backupGoalie = b; return; }

        if (lineA != null && lineB != null) {
            lineA.set(idxA, b);
            lineB.set(idxB, a);
        } else if (lineA != null && idxB == -1) {
            // b is on the bench — put it on lineA, move a to bench
            lineA.set(idxA, b);
        } else if (lineB != null && idxA == -1) {
            lineB.set(idxB, a);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Team)) return false;
        return this.name.equals(((Team) obj).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }

    @Override
    public String toString() { return name; }
}
