package com.sportsmanager.league;

import com.sportsmanager.core.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Accumulates all season statistics for a single team.
 * Updated by Standings after every match result.
 */
public class TeamRecord {

    // ── Season totals ────────────────────────────────────────────────────────
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int goalsFor;
    private int goalsAgainst;
    private int points;

    // ── Head-to-head: opponent → list of (gf, ga) from THIS team's perspective
    private final Map<Team, List<H2HRecord>> h2hRecords;

    public TeamRecord() {
        this.h2hRecords = new HashMap<>();
    }

    // ── Mutation helpers called by Standings ─────────────────────────────────

    public void incrementPlayed()  { played++; }
    public void incrementWins()    { won++; }
    public void incrementDraws()   { drawn++; }
    public void incrementLosses()  { lost++; }
    public void addGoals(int gf, int ga) { goalsFor += gf; goalsAgainst += ga; }
    public void addPoints(int pts) { points += pts; }


    public void addH2H(Team opponent, int gf, int ga) {
        h2hRecords.computeIfAbsent(opponent, t -> new ArrayList<>())
                  .add(new H2HRecord(gf, ga));
    }


    public void reset() {
        played = won = drawn = lost = goalsFor = goalsAgainst = points = 0;
        h2hRecords.clear();
    }

    // ── Derived stats ─────────────────────────────────────────────────────────

    public int getGoalDifference() { return goalsFor - goalsAgainst; }


    public int getH2HPoints(Team opponent) {
        return h2hRecords.getOrDefault(opponent, List.of())
                         .stream()
                         .mapToInt(H2HRecord::getPoints)
                         .sum();
    }


    public int getH2HGoalDifference(Team opponent) {
        return h2hRecords.getOrDefault(opponent, List.of())
                         .stream()
                         .mapToInt(H2HRecord::getGoalDifference)
                         .sum();
    }



    public int getPlayed()       { return played; }
    public int getWon()          { return won; }
    public int getDrawn()        { return drawn; }
    public int getLost()         { return lost; }
    public int getGoalsFor()     { return goalsFor; }
    public int getGoalsAgainst() { return goalsAgainst; }
    public int getPoints()       { return points; }

    @Override
    public String toString() {
        return played + "p  " + won + "w " + drawn + "d " + lost + "l  "
                + goalsFor + ":" + goalsAgainst + "  GD " + getGoalDifference()
                + "  Pts " + points;
    }




    public static class H2HRecord {
        private final int goalsFor;
        private final int goalsAgainst;

        public H2HRecord(int goalsFor, int goalsAgainst) {
            this.goalsFor = goalsFor;
            this.goalsAgainst = goalsAgainst;
        }

        public int getPoints() {
            if (goalsFor > goalsAgainst) return 3;
            if (goalsFor == goalsAgainst) return 1;
            return 0;
        }

        public int getGoalDifference() { return goalsFor - goalsAgainst; }
    }
}
