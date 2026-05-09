package com.sportsmanager.core;

import com.sportsmanager.core.MatchResult;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.Match;
import com.sportsmanager.league.Standings;

import java.util.List;

public interface ILeague {

    void generateFixture();
    void playMatchWeek();
    List<Team> getStandings();
    void advanceWeek();
    boolean isSeasonOver();

    void resetWeek();
    Fixture getFixture();
    void setFixture(Fixture fixture);
    void setCurrentWeek(int week);
    Standings getStandingsObject();
    void recordMatchResult(Match match, MatchResult result);
}
