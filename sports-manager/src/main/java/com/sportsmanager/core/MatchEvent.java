package com.sportsmanager.core;


public interface MatchEvent {


    int getMinute();

    Player getInvolvedPlayer();


    Player getSecondaryPlayer();


    String getTeamId();


    String getDescription();
}
