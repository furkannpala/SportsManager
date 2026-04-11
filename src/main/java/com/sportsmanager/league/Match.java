package com.sportsmanager.league;

import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.Team;

public class Match {

    private final Team homeTeam;
    private final Team awayTeam;
    private MatchResult result;
    private MatchStatus status;

    public Match(Team homeTeam, Team awayTeam) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.result = null;
        this.status = MatchStatus.UNPLAYED;
    }

    public Team getHomeTeam()  { return homeTeam; }
    public Team getAwayTeam()  { return awayTeam; }
    public MatchResult getResult() { return result; }
    public MatchStatus getStatus() { return status; }

    public void setResult(MatchResult result) {
        this.result = result;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return homeTeam.getTeamName() + " vs " + awayTeam.getTeamName()
                + " [" + status + "]"
                + (result != null ? " " + result : "");
    }
}
