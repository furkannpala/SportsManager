package com.sportsmanager.handball;

import com.sportsmanager.core.ISportRuleSet;
import com.sportsmanager.core.Team;
import com.sportsmanager.league.Standings;

public class HandballRuleSet implements ISportRuleSet {

    private Standings standings;

    public void setStandings(Standings standings) {
        this.standings = standings;
    }

    @Override public int getWinPoints()  { return 2; }
    @Override public int getDrawPoints() { return 1; }
    @Override public int getLossPoints() { return 0; }

    @Override
    public int calculateTieBreaker(Team a, Team b) {
        if (standings == null) return 0;
        return standings.compareByTieBreaker(a, b);
    }
}
