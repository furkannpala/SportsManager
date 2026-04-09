package com.sportsmanager.league;

import com.sportsmanager.core.ISportRuleSet;
import com.sportsmanager.core.Team;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maintains the live league table.
 * Owns the TeamRecord map and the full 4-level football tiebreaker logic.
 */
public class Standings {

    private final Map<Team, TeamRecord> records;
    private final ISportRuleSet ruleSet;

    public Standings(ISportRuleSet ruleSet, List<Team> teams) {
        this.ruleSet = ruleSet;
        this.records = new LinkedHashMap<>();
        for (Team t : teams) {
            records.put(t, new TeamRecord());
        }
    }

    // ── Update after a finished match ─────────────────────────────────────────

    public void update(Match match) {
        if (match.getResult() == null) return;

        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        int hg = match.getResult().getHomeScore();
        int ag = match.getResult().getAwayScore();

        TeamRecord hr = records.computeIfAbsent(home, t -> new TeamRecord());
        TeamRecord ar = records.computeIfAbsent(away, t -> new TeamRecord());

        hr.incrementPlayed();
        ar.incrementPlayed();
        hr.addGoals(hg, ag);
        ar.addGoals(ag, hg);
        hr.addH2H(away, hg, ag);
        ar.addH2H(home, ag, hg);

        if (hg > ag) {
            hr.incrementWins();   hr.addPoints(ruleSet.getWinPoints());
            ar.incrementLosses(); ar.addPoints(ruleSet.getLossPoints());
        } else if (hg == ag) {
            hr.incrementDraws();  hr.addPoints(ruleSet.getDrawPoints());
            ar.incrementDraws();  ar.addPoints(ruleSet.getDrawPoints());
        } else {
            hr.incrementLosses(); hr.addPoints(ruleSet.getLossPoints());
            ar.incrementWins();   ar.addPoints(ruleSet.getWinPoints());
        }
    }

    // ── Sorting ───────────────────────────────────────────────────────────────

    /** Returns teams sorted from 1st to last place. */
    public List<Team> getSortedTeams() {
        return records.entrySet().stream()
                .sorted((e1, e2) -> compareTeams(
                        e1.getKey(), e1.getValue(),
                        e2.getKey(), e2.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /** Returns 1-based league position of the given team. */
    public int getPosition(Team team) {
        return getSortedTeams().indexOf(team) + 1;
    }

    public TeamRecord getRecord(Team team) {
        return records.get(team);
    }

    /** Package-private: used by FootballRuleSet.calculateTieBreaker(). */
    int compareByTieBreaker(Team a, Team b) {
        TeamRecord ra = records.get(a);
        TeamRecord rb = records.get(b);
        return compareTeams(a, ra, b, rb);
    }

    // ── Tiebreaker hierarchy ──────────────────────────────────────────────────

    private int compareTeams(Team a, TeamRecord ra, Team b, TeamRecord rb) {
        if (ra == null || rb == null) return 0;

        // 1. Points
        int d = rb.getPoints() - ra.getPoints();
        if (d != 0) return d;

        // 2. Goal Difference
        d = rb.getGoalDifference() - ra.getGoalDifference();
        if (d != 0) return d;

        // 3. Goals For
        d = rb.getGoalsFor() - ra.getGoalsFor();
        if (d != 0) return d;

        // 4. Head-to-Head points between the tied teams
        d = rb.getH2HPoints(a) - ra.getH2HPoints(b);
        if (d != 0) return d;

        // 4b. Head-to-Head goal difference
        d = rb.getH2HGoalDifference(a) - ra.getH2HGoalDifference(b);
        if (d != 0) return d;

        // 5. Alphabetical by team name (deterministic fallback)
        return a.getTeamName().compareTo(b.getTeamName());
    }

    /** Resets all records (new season). */
    public void resetAll() {
        records.values().forEach(TeamRecord::reset);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Standings:\n");
        int pos = 1;
        for (Team t : getSortedTeams()) {
            sb.append(pos++).append(". ").append(t.getTeamName())
              .append("  ").append(records.get(t)).append("\n");
        }
        return sb.toString();
    }
}
