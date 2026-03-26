package com.sportsmanager.game;

import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.league.FootballLeague;

import java.util.List;


public class GameManager {

    private static GameManager instance;

    private SeasonState state;

    private GameManager() {}

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }




    public void initNewGame(Sport sport, List<Team> allTeams, Team userTeam) {
        FootballLeague league = new FootballLeague(allTeams, sport);
        league.generateFixture();

        state = new SeasonState();
        state.setCurrentSport(sport);
        state.setLeague(league);
        state.setCurrentFixture(league.getFixture());
        state.setCurrentStandings(league.getStandingsObject());
        state.setAllTeams(allTeams);
        state.setUserTeam(userTeam);
        state.setCurrentWeek(1);
        state.setSeasonNumber(1);
    }


    public void advanceGameCycle() {
        if (state == null) return;
        state.getLeague().advanceWeek();
        state.setCurrentWeek(state.getCurrentWeek() + 1);
    }


    public void advanceSeason() {
        if (state == null) return;
        state.getCurrentStandings().resetAll();
        state.getLeague().generateFixture();
        state.setCurrentFixture(
                ((FootballLeague) state.getLeague()).getFixture());
        state.setCurrentWeek(1);
        state.setSeasonNumber(state.getSeasonNumber() + 1);
    }


    public boolean isSeasonOver() {
        return state != null && state.getLeague().isSeasonOver();
    }

    public SeasonState getState() { return state; }


    static void resetForTesting() { instance = null; }
}
