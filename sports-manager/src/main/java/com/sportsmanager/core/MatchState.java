package com.sportsmanager.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The MatchEngine uses this object to pass state between periods so the UI can
 * pause at period breaks, let the user make substitutions / tactic changes,
 * and then resume simulation.
 */
public class MatchState {

    private int homeScore = 0;
    private int awayScore = 0;

    private final int totalPeriods;
    private int currentPeriod = 1;
    private int currentMinute = 0;

    private boolean matchOver  = false;
    private boolean periodOver = false;   // true between periods, false during play

    private final List<MatchEvent> events = new ArrayList<>();

    // Substitution counters
    private int homeSubsUsed = 0;
    private int awaySubsUsed = 0;

    // Active player counts (decremented on red cards)
    private int homeActivePlayers = 11;
    private int awayActivePlayers = 11;

    private final List<Player> homeFieldPlayers = new ArrayList<>();
    private final List<Player> awayFieldPlayers = new ArrayList<>();

    private final String homeTeamId;
    private final String awayTeamId;

    public MatchState(int totalPeriods, String homeTeamId, String awayTeamId) {
        this.totalPeriods = totalPeriods;
        this.homeTeamId   = homeTeamId;
        this.awayTeamId   = awayTeamId;
    }

    // ── Substitution ──────────────────────────────────────────────────────────

    public boolean makeSubstitution(String teamId, Player out, Player in, int maxSubs) {
        if (teamId.equals(homeTeamId)) {
            return applySubstitution(homeFieldPlayers, out, in, homeSubsUsed, maxSubs,
                    count -> homeSubsUsed = count);
        } else if (teamId.equals(awayTeamId)) {
            return applySubstitution(awayFieldPlayers, out, in, awaySubsUsed, maxSubs,
                    count -> awaySubsUsed = count);
        }
        return false;
    }

    private boolean applySubstitution(List<Player> fieldPlayers,
                                       Player out, Player in,
                                       int subsUsed, int maxSubs,
                                       java.util.function.IntConsumer updateCount) {
        if (subsUsed >= maxSubs)          return false;
        if (!fieldPlayers.contains(out))  return false;
        if (!in.isAvailable())            return false;
        if (fieldPlayers.contains(in))    return false;

        fieldPlayers.remove(out);
        fieldPlayers.add(in);
        updateCount.accept(subsUsed + 1);
        return true;
    }

    public int getHomeScore()            { return homeScore; }
    public void setHomeScore(int v)      { homeScore = v; }
    public void incrementHomeScore()     { homeScore++; }

    public int getAwayScore()            { return awayScore; }
    public void setAwayScore(int v)      { awayScore = v; }
    public void incrementAwayScore()     { awayScore++; }

    public int getTotalPeriods()         { return totalPeriods; }

    public int getCurrentPeriod()        { return currentPeriod; }
    public void setCurrentPeriod(int v)  { currentPeriod = v; }

    public int getCurrentMinute()        { return currentMinute; }
    public void setCurrentMinute(int v)  { currentMinute = v; }

    public boolean isMatchOver()         { return matchOver; }
    public void setMatchOver(boolean v)  { matchOver = v; }

    public boolean isPeriodOver()        { return periodOver; }
    public void setPeriodOver(boolean v) { periodOver = v; }

    public List<MatchEvent> getEvents()  { return Collections.unmodifiableList(events); }
    public void addEvent(MatchEvent e)   { events.add(e); }

    public int getHomeSubsUsed()         { return homeSubsUsed; }
    public int getAwaySubsUsed()         { return awaySubsUsed; }

    public int getHomeActivePlayers()            { return homeActivePlayers; }
    public void setHomeActivePlayers(int v)      { homeActivePlayers = v; }
    public void decrementHomeActivePlayers()     { homeActivePlayers = Math.max(1, homeActivePlayers - 1); }

    public int getAwayActivePlayers()            { return awayActivePlayers; }
    public void setAwayActivePlayers(int v)      { awayActivePlayers = v; }
    public void decrementAwayActivePlayers()     { awayActivePlayers = Math.max(1, awayActivePlayers - 1); }

    public List<Player> getHomeFieldPlayers()    { return homeFieldPlayers; }
    public List<Player> getAwayFieldPlayers()    { return awayFieldPlayers; }

    public String getHomeTeamId()        { return homeTeamId; }
    public String getAwayTeamId()        { return awayTeamId; }
}
