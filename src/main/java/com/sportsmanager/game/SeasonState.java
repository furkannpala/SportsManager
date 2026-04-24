package com.sportsmanager.game;

import com.sportsmanager.core.ILeague;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.Standings;
import com.sportsmanager.training.PlayerTrainingPlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SeasonState {


    private int currentWeek;


    private ILeague league;


    private Fixture currentFixture;


    private Standings currentStandings;


    private Team userTeam;


    private int seasonNumber;


    private List<Team> allTeams;


    private Sport currentSport;

    /** Active training assignments for the user's squad (player → plan). */
    private Map<Player, PlayerTrainingPlan> trainingPlans = new HashMap<>();

    public int getCurrentWeek()              { return currentWeek; }
    public void setCurrentWeek(int w)        { this.currentWeek = w; }

    public ILeague getLeague()               { return league; }
    public void setLeague(ILeague league)    { this.league = league; }

    public Fixture getCurrentFixture()                { return currentFixture; }
    public void setCurrentFixture(Fixture f)          { this.currentFixture = f; }

    public Standings getCurrentStandings()            { return currentStandings; }
    public void setCurrentStandings(Standings s)      { this.currentStandings = s; }

    public Team getUserTeam()                { return userTeam; }
    public void setUserTeam(Team t)          { this.userTeam = t; }

    public int getSeasonNumber()             { return seasonNumber; }
    public void setSeasonNumber(int n)       { this.seasonNumber = n; }

    public List<Team> getAllTeams()          { return allTeams; }
    public void setAllTeams(List<Team> t)   { this.allTeams = t; }

    public Sport getCurrentSport()           { return currentSport; }
    public void setCurrentSport(Sport s)     { this.currentSport = s; }

    public Map<Player, PlayerTrainingPlan> getTrainingPlans() { return trainingPlans; }
    public void setTrainingPlans(Map<Player, PlayerTrainingPlan> m) { this.trainingPlans = m; }

    public PlayerTrainingPlan getTrainingPlan(Player p)  { return trainingPlans.get(p); }

    public void assignTraining(Player p, PlayerTrainingPlan plan) {
        trainingPlans.put(p, plan);
    }

    public void cancelTraining(Player p) { trainingPlans.remove(p); }
}
