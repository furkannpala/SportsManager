package com.sportsmanager.core;

import java.util.List;


public interface ILeague {


    void generateFixture();


    void playMatchWeek();


    List<Team> getStandings();


    void advanceWeek();

    boolean isSeasonOver();
}
