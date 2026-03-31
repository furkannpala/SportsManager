package com.sportsmanager.league;

import com.sportsmanager.core.ILeague;
import com.sportsmanager.core.MatchEngine;
import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a 20-team, 38-week football league.
 *
 * Fixture generation uses the Circle Method (Polygon Method):
 * one pivot is fixed, the remaining 19 teams rotate clockwise for 19 weeks.
 * Weeks 20–38 mirror weeks 1–19 with home/away swapped.
 */
public class FootballLeague implements ILeague {

    private static final int TEAM_COUNT  = 20;
    private static final int TOTAL_WEEKS = 38;
    private static final int HALF_WEEKS  = 19;

    private final List<Team> teams;
    private final Sport sport;
    private final FootballRuleSet ruleSet;
    private final Standings standings;

    private Fixture fixture;
    private int currentWeek = 1;

    public FootballLeague(List<Team> teams, Sport sport) {
        if (teams.size() != TEAM_COUNT) {
            throw new IllegalArgumentException(
                    "Football league requires exactly " + TEAM_COUNT + " teams, got " + teams.size());
        }
        this.teams    = new ArrayList<>(teams);
        this.sport    = sport;
        this.ruleSet  = new FootballRuleSet();
        this.standings = new Standings(ruleSet, this.teams);
        this.ruleSet.setStandings(this.standings);
    }




    @Override
    public void generateFixture() {
        fixture = new Fixture();

        Team pivot = teams.get(0);
        List<Team> rest = new ArrayList<>(teams.subList(1, TEAM_COUNT));


        for (int week = 1; week <= HALF_WEEKS; week++) {
            List<Match> matches = new ArrayList<>();

            // Pivot vs last team in the rotating ring
            Team lastInRing = rest.get(HALF_WEEKS - 1);
            if (week % 2 == 1) {
                matches.add(new Match(pivot, lastInRing));
            } else {
                matches.add(new Match(lastInRing, pivot));
            }


            for (int i = 0; i <= 8; i++) {
                matches.add(new Match(rest.get(i), rest.get(17 - i)));
            }

            fixture.addWeek(new MatchWeek(week, matches));


            Team last = rest.remove(HALF_WEEKS - 1);
            rest.add(0, last);
        }


        for (int week = HALF_WEEKS + 1; week <= TOTAL_WEEKS; week++) {
            MatchWeek firstHalf = fixture.getWeek(week - HALF_WEEKS);
            List<Match> mirrored = new ArrayList<>();
            for (Match m : firstHalf.getMatches()) {
                mirrored.add(new Match(m.getAwayTeam(), m.getHomeTeam()));
            }
            fixture.addWeek(new MatchWeek(week, mirrored));
        }

        fixture.setTotalWeeks(TOTAL_WEEKS);
    }


    @Override
    public void playMatchWeek() {
        if (fixture == null) {
            throw new IllegalStateException("Call generateFixture() before playMatchWeek().");
        }
        MatchWeek weekData = fixture.getWeek(currentWeek);
        MatchEngine engine = sport.createMatchEngine();

        for (Match match : weekData.getMatches()) {
            if (match.getStatus() == MatchStatus.UNPLAYED) {
                MatchResult result = engine.simulateMatch(
                        match.getHomeTeam(), match.getAwayTeam());
                match.setResult(result);
                match.setStatus(MatchStatus.FINISHED);
                standings.update(match);
            }
        }
        weekData.setCompleted(true);
    }


    @Override
    public List<Team> getStandings() {
        return standings.getSortedTeams();
    }

    @Override
    public void advanceWeek() {
        currentWeek++;
    }

    @Override
    public boolean isSeasonOver() {
        return currentWeek > TOTAL_WEEKS;
    }

    // ── Extra accessors ───────────────────────────────────────────────────────

    public int getCurrentWeek()      { return currentWeek; }
    public Fixture getFixture()      { return fixture; }
    public Standings getStandingsObject() { return standings; }
    public List<Team> getTeams()     { return new ArrayList<>(teams); }


    public void recordMatchResult(Match match, MatchResult result) {
        match.setResult(result);
        match.setStatus(MatchStatus.FINISHED);
        standings.update(match);
    }
}
