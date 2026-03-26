package com.sportsmanager.game;

import com.sportsmanager.core.ILeague;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.Standings;

import java.util.List;


public class SeasonState {


    private int currentWeek;


    private ILeague league;


    private Fixture currentFixture;


    private Standings currentStandings;


    private Team userTeam;


    private int seasonNumber;


    private List<Team> allTeams;


    private Sport currentSport;


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
}
