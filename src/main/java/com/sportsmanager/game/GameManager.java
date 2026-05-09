package com.sportsmanager.game;

import com.sportsmanager.core.ILeague;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.handball.HandballSport;
import com.sportsmanager.league.FootballLeague;
import com.sportsmanager.league.HandballLeague;
import com.sportsmanager.training.TrainingEngine;

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
        ILeague league = createLeague(sport, allTeams);
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

    public void loadGame(SeasonState loadedState, ILeague loadedLeague) {
        this.state = loadedState;
    }

    public void advanceGameCycle() {
        if (state == null) return;
        state.getLeague().advanceWeek();
        state.setCurrentWeek(state.getCurrentWeek() + 1);
        for (Team team : state.getAllTeams()) {
            for (Player player : team.getSquad()) {
                player.decrementInjury();
                player.decrementSuspension();
            }
        }
        TrainingEngine.executeWeeklyTraining(state);
    }

    public void advanceSeason() {
        if (state == null) return;
        ILeague league = state.getLeague();
        state.getCurrentStandings().resetAll();
        league.resetWeek();
        league.generateFixture();
        state.setCurrentFixture(league.getFixture());
        state.setCurrentWeek(1);
        state.setSeasonNumber(state.getSeasonNumber() + 1);
    }

    public boolean isSeasonOver() {
        return state != null && state.getLeague().isSeasonOver();
    }

    public SeasonState getState() { return state; }

    private static ILeague createLeague(Sport sport, List<Team> teams) {
        if (sport instanceof HandballSport) return new HandballLeague(teams, sport);
        return new FootballLeague(teams, sport);
    }

    static void resetForTesting() { instance = null; }
}
