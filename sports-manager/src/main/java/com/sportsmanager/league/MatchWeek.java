package com.sportsmanager.league;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatchWeek {

    private final int weekNumber;
    private final List<Match> matches;
    private boolean completed;

    public MatchWeek(int weekNumber, List<Match> matches) {
        this.weekNumber = weekNumber;
        this.matches = new ArrayList<>(matches);
        this.completed = false;
    }

    public int getWeekNumber()    { return weekNumber; }
    public List<Match> getMatches() { return Collections.unmodifiableList(matches); }
    public boolean isCompleted()  { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public String toString() {
        return "Week " + weekNumber + " (" + matches.size() + " matches)";
    }
}
