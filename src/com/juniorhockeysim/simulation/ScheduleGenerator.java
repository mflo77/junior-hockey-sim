package com.juniorhockeysim.simulation;

import com.juniorhockeysim.core.GameDate;
import com.juniorhockeysim.domain.Team;

import java.util.*;

/**
 * Generates a realistic junior hockey regular season schedule.
 *
 * Season: October 1 → mid-February. Each team plays exactly 44 games.
 * With 12 teams:
 *   - 4 complete round-robins (each team faces every other team 4 times)
 *   - 11 rounds/RR × 4 RRs = 44 game-rounds total
 *   - Each round: 6 games (all 12 teams play once)
 *   - Rounds 2 and 4 have home/away flipped for balance
 *
 * Pacing: 3 game days per week (Mon/Wed/Fri cadence).
 * 44 rounds ÷ 3/week ≈ 14.7 weeks → finishes around Feb 8-14.
 */
public class ScheduleGenerator {

    public static List<ScheduledGame> generateSchedule(List<Team> teams) {
        return generateSchedule(teams, 2026);
    }

    public static List<ScheduledGame> generateSchedule(List<Team> teams, int startYear) {
        List<ScheduledGame> schedule = new ArrayList<>();
        if (teams.size() < 2) return schedule;

        // Pad to even number
        List<Team> t = new ArrayList<>(teams);
        if (t.size() % 2 != 0) t.add(null); // bye slot
        int size = t.size();

        // Build all rounds using the circle algorithm.
        // Pin the last element; rotate the rest.
        // 4 complete round-robins, alternating home/away direction.
        List<List<int[]>> allRounds = new ArrayList<>();

        for (int rr = 0; rr < 4; rr++) {
            boolean flip = (rr % 2 == 1); // flip home/away on rounds 2 & 4
            for (int r = 0; r < size - 1; r++) {
                List<int[]> roundPairs = new ArrayList<>();
                for (int i = 0; i < size / 2; i++) {
                    int a, b;
                    if (i == 0) {
                        // Pinned team (size-1) vs slot r
                        a = size - 1;
                        b = r % (size - 1);
                    } else {
                        a = (r + i) % (size - 1);
                        b = (r + size - 1 - i) % (size - 1);
                    }
                    roundPairs.add(flip ? new int[]{b, a} : new int[]{a, b});
                }
                allRounds.add(roundPairs);
            }
        }

        // Assign dates: 3 game days per 7-day block (offsets 0, 2, 4).
        // Round 0 → day 0 (Oct 1), Round 1 → day 2, Round 2 → day 4,
        // Round 3 → day 7, Round 4 → day 9, etc.
        int[] withinBlockOffset = {0, 2, 4};

        for (int idx = 0; idx < allRounds.size(); idx++) {
            int block  = idx / 3;
            int slot   = idx % 3;
            int dayOffset = block * 7 + withinBlockOffset[slot];

            GameDate date = new GameDate(1, 10, startYear);
            for (int d = 0; d < dayOffset; d++) date.nextDay();

            for (int[] pair : allRounds.get(idx)) {
                Team home = t.get(pair[0]);
                Team away = t.get(pair[1]);
                if (home != null && away != null) {
                    schedule.add(new ScheduledGame(date.copy(), home, away));
                }
            }
        }

        schedule.sort(Comparator.comparing(ScheduledGame::getDate));
        return schedule;
    }

    /** Returns how many games each team is scheduled for. Useful for verification. */
    public static Map<String, Integer> countGamesPerTeam(List<ScheduledGame> schedule) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ScheduledGame g : schedule) {
            counts.merge(g.getHomeTeam().getName(), 1, Integer::sum);
            counts.merge(g.getAwayTeam().getName(), 1, Integer::sum);
        }
        return counts;
    }
}
