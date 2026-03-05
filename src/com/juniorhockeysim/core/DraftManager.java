package com.juniorhockeysim.core;

import com.juniorhockeysim.domain.NameGenerator;
import com.juniorhockeysim.domain.Player;
import com.juniorhockeysim.domain.Position;
import com.juniorhockeysim.domain.Team;

import java.util.*;

/**
 * Manages the annual draft of 16-year-old prospects.
 * Worst teams pick first (reverse standings order).
 */
public class DraftManager {

    public static final int TOTAL_ROUNDS = 5;

    private final List<Player> draftClass;
    private final List<Team> draftOrder; // reverse standings: worst team first
    private int currentRound = 0;

    public DraftManager(List<Team> standingsOrder) {
        this.draftOrder = new ArrayList<>(standingsOrder);
        Collections.reverse(this.draftOrder); // worst to best
        this.draftClass = generateDraftClass(standingsOrder.size() * 6); // enough for 5 rounds
    }

    private List<Player> generateDraftClass(int size) {
        List<Player> prospects = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            Position pos;
            double roll = random.nextDouble();
            if (roll < 0.5) pos = Position.FORWARD;
            else if (roll < 0.78) pos = Position.DEFENSE;
            else pos = Position.GOALIE;
            prospects.add(new Player(NameGenerator.generate(), 16, pos));
        }

        // ~35% chance of ONE generational gem per draft class
        // Goalies are less likely to be gems (more balanced position)
        if (random.nextDouble() < 0.35) {
            Position gemPos;
            double pr = random.nextDouble();
            if (pr < 0.55) gemPos = Position.FORWARD;
            else if (pr < 0.80) gemPos = Position.DEFENSE;
            else gemPos = Position.GOALIE;
            Player gem = new Player(NameGenerator.generate(), 16, gemPos);
            gem.makeGem();
            prospects.add(gem);
        }

        // Sort by overall desc — gem will likely NOT be #1 overall (raw ratings are modest)
        // but their potential shines in the scouting view
        prospects.sort(Comparator.comparingInt(Player::overallRating).reversed());
        return prospects;
    }

    public List<Player> getDraftClass() { return Collections.unmodifiableList(draftClass); }
    public List<Team> getDraftOrder()   { return Collections.unmodifiableList(draftOrder); }

    /**
     * Execute one full round of the draft.
     * Returns pick results as formatted strings.
     */
    public int getCurrentRound() { return currentRound; }

    public List<String> executeRound(Team userTeam, Scanner scanner) {
        List<String> results = new ArrayList<>();
        currentRound++;

        for (int pick = 0; pick < draftOrder.size(); pick++) {
            Team team = draftOrder.get(pick);
            if (draftClass.isEmpty()) break;

            Player selected;
            if (team.equals(userTeam)) {
                // User picks
                System.out.println("\n--- YOUR PICK (#" + (pick + 1) + ") ---");
                System.out.println("Available top prospects:");
                int show = Math.min(5, draftClass.size());
                for (int i = 0; i < show; i++) {
                    Player p = draftClass.get(i);
                    System.out.printf("  %d. %-20s %s  OVR: %d  POT: %d%n",
                            i + 1, p.getName(), p.getPosition(), p.overallRating(), p.getPotential());
                }
                System.out.print("Select prospect (1-" + show + "): ");

                int choice = -1;
                try {
                    String line = scanner.nextLine().trim();
                    choice = Integer.parseInt(line) - 1;
                } catch (Exception e) { /* default to 0 */ }

                if (choice < 0 || choice >= show) choice = 0;
                selected = draftClass.remove(choice);
            } else {
                // AI picks best available for their needs
                selected = aiBestPick(team);
                draftClass.remove(selected);
            }

            selected.signContract(6); // full career contract
            team.addPlayer(selected);
            results.add(String.format("  Pick %2d: %-22s selects %-20s (%s OVR:%d POT:%d)",
                    pick + 1, team.getName(), selected.getName(),
                    selected.getPosition(), selected.overallRating(), selected.getPotential()));
        }

        return results;
    }

    private Player aiBestPick(Team team) {
        // Check if a gem is available — AI always takes the gem if present
        Player gem = draftClass.stream().filter(Player::isGem).findFirst().orElse(null);
        if (gem != null) return gem;

        // Simple need-based: pick best available, weighting by position needs
        long forwards  = team.getForwards().size();
        long defense   = team.getDefenders().size();
        long goalies   = team.getGoalies().size();

        return draftClass.stream()
                .max(Comparator.comparingInt(p -> {
                    int base = p.tradeValue();
                    if (p.getPosition() == Position.FORWARD && forwards < 10) base += 5;
                    if (p.getPosition() == Position.DEFENSE  && defense < 6)  base += 5;
                    if (p.getPosition() == Position.GOALIE   && goalies < 2)  base += 8;
                    return base;
                }))
                .orElse(draftClass.get(0));
    }
}
