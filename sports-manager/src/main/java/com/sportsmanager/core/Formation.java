package com.sportsmanager.core;

import java.util.List;


public interface Formation {


    String getFormationName();

    List<Position> getPositionSlots();


    int getDefenderCount();


    int getMidfielderCount();


    int getAttackerCount();
}
