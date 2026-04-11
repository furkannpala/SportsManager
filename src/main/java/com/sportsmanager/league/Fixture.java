package com.sportsmanager.league;

import com.sportsmanager.core.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Fixture {

    private final List<MatchWeek> weeks;
    private int totalWeeks;

    public Fixture() {
        this.weeks = new ArrayList<>();
    }


    public void addWeek(MatchWeek week) {
        weeks.add(week);
    }


    public MatchWeek getWeek(int weekNumber) {
        if (weekNumber < 1 || weekNumber > weeks.size()) {
            throw new IndexOutOfBoundsException("Week " + weekNumber + " does not exist.");
        }
        return weeks.get(weekNumber - 1);
    }

    public List<Match> getMatchesForTeam(Team team) {
        List<Match> result = new ArrayList<>();
        for (MatchWeek week : weeks) {
            for (Match match : week.getMatches()) {
                if (match.getHomeTeam().equals(team) || match.getAwayTeam().equals(team)) {
                    result.add(match);
                }
            }
        }
        return result;
    }

    public List<MatchWeek> getWeeks()  { return Collections.unmodifiableList(weeks); }
    public int getTotalWeeks()         { return totalWeeks; }
    public void setTotalWeeks(int n)   { this.totalWeeks = n; }

    @Override
    public String toString() {
        return "Fixture [" + weeks.size() + " weeks]";
    }
}
