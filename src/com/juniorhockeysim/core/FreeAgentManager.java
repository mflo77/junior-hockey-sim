package com.juniorhockeysim.core;

import com.juniorhockeysim.domain.NameGenerator;
import com.juniorhockeysim.domain.Player;
import com.juniorhockeysim.domain.Position;
import com.juniorhockeysim.domain.Team;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class FreeAgentManager implements Serializable {

    private static final long serialVersionUID = 2L;

    private final List<Player> freeAgents = new ArrayList<>();
    private static final Random random = new Random();

    public static class TeamOffer implements Serializable {
        public final Team team;
        public final int years;
        public final int offerScore;
        public final boolean isUser;
        public TeamOffer(Team team, int years, int offerScore, boolean isUser) {
            this.team = team; this.years = years;
            this.offerScore = offerScore; this.isUser = isUser;
        }
        public String describe() {
            String quality = offerScore >= 80 ? "Max" : offerScore >= 60 ? "Strong" : offerScore >= 40 ? "Moderate" : "Low";
            return team.getName() + " — " + years + "yr / " + quality + " offer";
        }
    }

    public static class SigningResult implements Serializable {
        public final boolean userWon;
        public final Team signedWith;
        public final String message;
        public final List<String> competingTeams;
        public SigningResult(boolean userWon, Team signedWith, String message, List<String> competing) {
            this.userWon = userWon; this.signedWith = signedWith;
            this.message = message; this.competingTeams = competing;
        }
    }

    public FreeAgentManager() {}

    public void generateFreeAgentPool(int count) {
        freeAgents.clear();
        for (int i = 0; i < count; i++) {
            Position pos;
            double roll = random.nextDouble();
            if (roll < 0.50) pos = Position.FORWARD;
            else if (roll < 0.78) pos = Position.DEFENSE;
            else pos = Position.GOALIE;
            int age = 17 + random.nextInt(4);
            freeAgents.add(new Player(NameGenerator.generate(), age, pos));
        }
        freeAgents.sort(Comparator.comparingInt(Player::tradeValue).reversed());
    }

    public int getExpectedInterest(Player p) {
        int ovr = p.overallRating();
        if (ovr >= 80) return 3 + random.nextInt(2);
        if (ovr >= 70) return 2 + random.nextInt(2);
        if (ovr >= 60) return 1 + random.nextInt(2);
        return random.nextInt(2);
    }

    public List<TeamOffer> getAIOffers(Player player, Team userTeam, List<Team> allTeams) {
        int numAI = getExpectedInterest(player);
        List<TeamOffer> aiOffers = new ArrayList<>();
        List<Team> bidders = allTeams.stream()
                .filter(t -> !t.equals(userTeam) && t.getPlayers().size() < 24)
                .collect(Collectors.toList());
        Collections.shuffle(bidders);
        for (int i = 0; i < Math.min(numAI, bidders.size()); i++) {
            Team ai = bidders.get(i);
            int score = 35 + random.nextInt(55);
            if (ai.getWins() > ai.getLosses()) score = Math.min(100, score + 10);
            aiOffers.add(new TeamOffer(ai, 1 + random.nextInt(3), score, false));
        }
        return aiOffers;
    }

    public SigningResult resolveCompetition(Player player, Team userTeam, int years, int offerScore, List<Team> allTeams) {
        List<TeamOffer> aiOffers = getAIOffers(player, userTeam, allTeams);
        TeamOffer userOffer = new TeamOffer(userTeam, years, offerScore, true);

        List<TeamOffer> allOffers = new ArrayList<>();
        allOffers.add(userOffer);
        allOffers.addAll(aiOffers);

        TeamOffer winner = null;
        double bestScore = -1;
        for (TeamOffer offer : allOffers) {
            double score = offer.offerScore + offer.years * 3.0 + (random.nextDouble() * 20 - 10);
            if (score > bestScore) { bestScore = score; winner = offer; }
        }

        List<String> competing = aiOffers.stream().map(TeamOffer::describe).collect(Collectors.toList());
        boolean userWon = winner != null && winner.isUser;

        if (userWon && freeAgents.contains(player)) {
            player.signContract(years);
            freeAgents.remove(player);
            userTeam.addPlayer(player);
            String msg = aiOffers.isEmpty()
                    ? player.getName() + " signed with you (no competition)."
                    : player.getName() + " chose your offer over " + aiOffers.size() + " other team(s).";
            return new SigningResult(true, userTeam, msg, competing);
        } else {
            if (winner != null && freeAgents.contains(player)) {
                player.signContract(winner.years);
                freeAgents.remove(player);
                winner.team.addPlayer(player);
            }
            String reason = offerScore < 50
                    ? player.getName() + " wanted a better offer. Try increasing your bid."
                    : player.getName() + " chose " + (winner != null ? winner.team.getName() : "another team") + " over you.";
            return new SigningResult(false, winner != null ? winner.team : null, reason, competing);
        }
    }

    public boolean signPlayer(Player player, Team team, int contractYears) {
        if (!freeAgents.contains(player)) return false;
        if (team.getPlayers().size() >= 25) return false;
        player.signContract(contractYears);
        freeAgents.remove(player);
        team.addPlayer(player);
        return true;
    }

    public void addFreeAgent(Player p) {
        if (!freeAgents.contains(p)) {
            freeAgents.add(p);
            freeAgents.sort(Comparator.comparingInt(Player::tradeValue).reversed());
        }
    }

    public void releasePlayer(Player player, Team team) {
        team.removePlayer(player);
        player.signContract(0);
        addFreeAgent(player);
    }

    public List<String> processAISignings(List<Team> aiTeams) {
        List<String> news = new ArrayList<>();
        if (freeAgents.isEmpty()) return news;
        for (Team team : aiTeams) {
            int fwd = team.getForwards().size(), def = team.getDefenders().size(), gol = team.getGoalies().size();
            if (fwd < 9 || def < 4 || gol < 1) {
                Position needed = gol < 1 ? Position.GOALIE : def < 4 ? Position.DEFENSE : Position.FORWARD;
                freeAgents.stream().filter(p -> p.getPosition() == needed).findFirst().ifPresent(p -> {
                    if (signPlayer(p, team, 1 + random.nextInt(2)))
                        news.add("📝 " + team.getName() + " signed " + p.getName() + " (" + p.getPosition() + ", OVR:" + p.overallRating() + ")");
                });
            }
        }
        return news;
    }

    public void collectExpiredContracts(List<Team> allTeams) {
        for (Team team : allTeams) {
            new ArrayList<>(team.getPlayers()).stream().filter(Player::isFreeAgent).forEach(p -> {
                team.removePlayer(p); addFreeAgent(p);
            });
        }
    }

    // Also add a method to FranchiseMode to pass allTeams
    public List<Player> getFreeAgents() { return Collections.unmodifiableList(freeAgents); }
    public List<Player> getFreeAgentsByPosition(Position pos) {
        return freeAgents.stream().filter(p -> p.getPosition() == pos).collect(Collectors.toList());
    }
    public int size() { return freeAgents.size(); }
}
