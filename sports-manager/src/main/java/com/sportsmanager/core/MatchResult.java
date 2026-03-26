package com.sportsmanager.core;

import java.util.Collections;
import java.util.List;


public class MatchResult {

    private final int homeScore;
    private final int awayScore;
    private final List<MatchEvent> events;

    public MatchResult(int homeScore, int awayScore, List<MatchEvent> events) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.events = Collections.unmodifiableList(events);
    }


    public int getHomeScore() {
        return homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }


    public List<MatchEvent> getEvents() {
        return events;
    }

    @Override
    public String toString() {
        return homeScore + " - " + awayScore;
    }
}
