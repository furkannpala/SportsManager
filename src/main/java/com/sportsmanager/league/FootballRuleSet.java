package com.sportsmanager.league;

import com.sportsmanager.core.ISportRuleSet;
import com.sportsmanager.core.Team;


public class FootballRuleSet implements ISportRuleSet {

    // Standings reference is injected after construction to avoid circular init.
    private Standings standings;

    public void setStandings(Standings standings) {
        this.standings = standings;
    }

    @Override public int getWinPoints()  { return 3; }
    @Override public int getDrawPoints() { return 1; }
    @Override public int getLossPoints() { return 0; }


    @Override
    public int calculateTieBreaker(Team a, Team b) {
        if (standings == null) return 0;
        return standings.compareByTieBreaker(a, b);
    }
}
