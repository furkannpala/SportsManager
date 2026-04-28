package com.sportsmanager.handball;

import com.sportsmanager.core.MatchEvent;
import com.sportsmanager.core.Player;

public class HandballMatchEvent implements MatchEvent {

    private final HandballEventType eventType;
    private final int minute;
    private final Player involvedPlayer;
    private final Player secondaryPlayer;
    private final String teamId;

    public HandballMatchEvent(HandballEventType eventType, int minute,
                               Player involvedPlayer, Player secondaryPlayer, String teamId) {
        this.eventType       = eventType;
        this.minute          = minute;
        this.involvedPlayer  = involvedPlayer;
        this.secondaryPlayer = secondaryPlayer;
        this.teamId          = teamId;
    }

    public HandballMatchEvent(HandballEventType eventType, int minute,
                               Player involvedPlayer, String teamId) {
        this(eventType, minute, involvedPlayer, null, teamId);
    }

    @Override public int getMinute()             { return minute; }
    @Override public Player getInvolvedPlayer()  { return involvedPlayer; }
    @Override public Player getSecondaryPlayer() { return secondaryPlayer; }
    @Override public String getTeamId()          { return teamId; }

    public HandballEventType getEventType() { return eventType; }

    @Override
    public String getDescription() {
        String p = involvedPlayer != null ? involvedPlayer.getName() : "Unknown";
        return switch (eventType) {
            case GOAL -> secondaryPlayer != null
                    ? minute + "' GOAL — " + p + " (Assist: " + secondaryPlayer.getName() + ")"
                    : minute + "' GOAL — " + p;
            case YELLOW_CARD        -> minute + "' YELLOW CARD — " + p;
            case TWO_MIN_SUSPENSION -> minute + "' 2-MIN SUSPENSION — " + p;
            case RED_CARD           -> minute + "' RED CARD — " + p;
            case PENALTY_AWARDED    -> minute + "' PENALTY — " + p;
            case FOUL               -> minute + "' FOUL — " + p;
        };
    }
}
