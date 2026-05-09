package com.sportsmanager.league;

import com.sportsmanager.core.ILeague;
import com.sportsmanager.core.MatchEngine;
import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.handball.HandballRuleSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a 16-team, 30-week handball league (double round-robin).
 * Fixture generation uses the Circle Method: one pivot is fixed,
 * the remaining 15 teams rotate for 15 weeks; weeks 16-30 mirror with home/away swapped.
 */
public class HandballLeague implements ILeague {

    private static final int TEAM_COUNT  = 16;
    private static final int TOTAL_WEEKS = 30;
    private static final int HALF_WEEKS  = 15;

    private final List<Team> teams;
    private final Sport sport;
    private final HandballRuleSet ruleSet;
    private final Standings standings;

    private Fixture fixture;
    private int currentWeek = 1;

    public HandballLeague(List<Team> teams, Sport sport) {
        if (teams.size() != TEAM_COUNT) {
            throw new IllegalArgumentException(
                    "Handball league requires exactly " + TEAM_COUNT + " teams, got " + teams.size());
        }
        this.teams    = new ArrayList<>(teams);
        this.sport    = sport;
        this.ruleSet  = new HandballRuleSet();
        this.standings = new Standings(ruleSet, this.teams);
        this.ruleSet.setStandings(this.standings);
    }

    @Override
    public void generateFixture() {
        fixture = new Fixture();

        Team pivot = teams.get(0);
        List<Team> rest = new ArrayList<>(teams.subList(1, TEAM_COUNT)); // 15 teams

        for (int week = 1; week <= HALF_WEEKS; week++) {
            List<Match> matches = new ArrayList<>();

            // Pivot vs last team in the rotating ring
            Team lastInRing = rest.get(HALF_WEEKS - 1);
            if (week % 2 == 1) {
                matches.add(new Match(pivot, lastInRing));
            } else {
                matches.add(new Match(lastInRing, pivot));
            }

            // 7 pairs from the remaining 14 teams (indices 0–13)
            for (int i = 0; i <= 6; i++) {
                matches.add(new Match(rest.get(i), rest.get(13 - i)));
            }

            fixture.addWeek(new MatchWeek(week, matches));

            // Rotate: move last element to front
            Team last = rest.remove(HALF_WEEKS - 1);
            rest.add(0, last);
        }

        // Second half: mirror home/away
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
        if (fixture == null) throw new IllegalStateException("Call generateFixture() first.");
        MatchWeek weekData = fixture.getWeek(currentWeek);
        MatchEngine engine = sport.createMatchEngine();

        for (Match match : weekData.getMatches()) {
            if (match.getStatus() == MatchStatus.UNPLAYED) {
                MatchResult result = resolveMatch(engine, match);
                match.setResult(result);
                match.setStatus(MatchStatus.FINISHED);
                standings.update(match);
            }
        }
        weekData.setCompleted(true);
    }

    private MatchResult resolveMatch(MatchEngine engine, Match match) {
        int homeAvailable = match.getHomeTeam().getAvailablePlayers().size();
        int awayAvailable = match.getAwayTeam().getAvailablePlayers().size();
        int required = sport.getStartingLineupSize();

        if (homeAvailable < required && awayAvailable < required) return new MatchResult(0, 0, List.of());
        if (homeAvailable < required) return new MatchResult(0, 2, List.of());
        if (awayAvailable < required) return new MatchResult(2, 0, List.of());
        return engine.simulateMatch(match.getHomeTeam(), match.getAwayTeam());
    }

    @Override public List<Team> getStandings()     { return standings.getSortedTeams(); }
    @Override public void advanceWeek()             { currentWeek++; }
    @Override public void resetWeek()               { currentWeek = 1; }
    @Override public boolean isSeasonOver()         { return currentWeek > TOTAL_WEEKS; }
    @Override public Fixture getFixture()           { return fixture; }
    @Override public void setFixture(Fixture f)     { this.fixture = f; }
    @Override public void setCurrentWeek(int week)  { this.currentWeek = week; }
    @Override public Standings getStandingsObject() { return standings; }

    @Override
    public void recordMatchResult(Match match, MatchResult result) {
        match.setResult(result);
        match.setStatus(MatchStatus.FINISHED);
        standings.update(match);
    }

    public int getCurrentWeek() { return currentWeek; }
    public List<Team> getTeams() { return new ArrayList<>(teams); }
}
