package com.sportsmanager.football;

import com.sportsmanager.core.MatchEvent;
import com.sportsmanager.core.Player;

public class FootballMatchEvent implements MatchEvent {

    private final FootballEventType eventType;
    private final int minute;
    private final Player involvedPlayer;
    private final Player secondaryPlayer;   // nullable — e.g. assister on a GOAL
    private final String teamId;
    private final boolean secondYellow;     // true when red card is caused by two yellows

    public FootballMatchEvent(FootballEventType eventType,
                              int minute,
                              Player involvedPlayer,
                              Player secondaryPlayer,
                              String teamId) {
        this.eventType       = eventType;
        this.minute          = minute;
        this.involvedPlayer  = involvedPlayer;
        this.secondaryPlayer = secondaryPlayer;
        this.teamId          = teamId;
        this.secondYellow    = false;
    }
    public FootballMatchEvent(FootballEventType eventType,
                              int minute,
                              Player involvedPlayer,
                              String teamId) {
        this(eventType, minute, involvedPlayer, null, teamId);
    }
    public FootballMatchEvent(FootballEventType eventType,
                              int minute,
                              Player involvedPlayer,
                              String teamId,
                              boolean secondYellow) {
        this.eventType       = eventType;
        this.minute          = minute;
        this.involvedPlayer  = involvedPlayer;
        this.secondaryPlayer = null;
        this.teamId          = teamId;
        this.secondYellow    = secondYellow;
    }
    @Override
    public int getMinute() {
        return minute;
    }

    @Override
    public Player getInvolvedPlayer() {
        return involvedPlayer;
    }

    @Override
    public Player getSecondaryPlayer() {
        return secondaryPlayer;
    }

    @Override
    public String getTeamId() {
        return teamId;
    }

    @Override
    public String getDescription() {
        String playerName = involvedPlayer != null ? involvedPlayer.getName() : "Unknown";
        return switch (eventType) {
            case GOAL -> secondaryPlayer != null
                    ? minute + "' GOAL — " + playerName + " (Assist: " + secondaryPlayer.getName() + ")"
                    : minute + "' GOAL — " + playerName;
            case ASSIST      -> minute + "' ASSIST — " + playerName;
            case YELLOW_CARD -> minute + "' YELLOW CARD — " + playerName;
            case RED_CARD    -> secondYellow
                    ? minute + "' RED CARD (2nd Yellow) — " + playerName
                    : minute + "' RED CARD — " + playerName;
            case SUBSTITUTION -> secondaryPlayer != null
                    ? minute + "' ↕ SUB — ↓ " + playerName + "  ↑ " + secondaryPlayer.getName()
                    : minute + "' ↕ SUB — ↓ " + playerName + " off";
            case INJURY      -> minute + "' INJURY — " + playerName;
            case OFFSIDE     -> minute + "' OFFSIDE — " + playerName;
            case FOUL        -> minute + "' FOUL — " + playerName;
        };
    }

    // Extra getter for match engine / UI
    public FootballEventType getEventType() {
        return eventType;
    }
}
