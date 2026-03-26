package com.sportsmanager.football;

import com.sportsmanager.core.MatchEvent;
import com.sportsmanager.core.Player;

public class FootballMatchEvent implements MatchEvent {

    private final FootballEventType eventType;
    private final int minute;
    private final Player involvedPlayer;
    private final Player secondaryPlayer;   // nullable — e.g. assister on a GOAL
    private final String teamId;

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
    }
    public FootballMatchEvent(FootballEventType eventType,
                              int minute,
                              Player involvedPlayer,
                              String teamId) {
        this(eventType, minute, involvedPlayer, null, teamId);
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
            case RED_CARD    -> minute + "' RED CARD — " + playerName;
            case SUBSTITUTION -> secondaryPlayer != null
                    ? minute + "' SUB — " + playerName + " off, " + secondaryPlayer.getName() + " on"
                    : minute + "' SUB — " + playerName + " off";
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
