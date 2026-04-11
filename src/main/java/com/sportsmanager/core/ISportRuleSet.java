package com.sportsmanager.core;


public interface ISportRuleSet {


    int getWinPoints();


    int getDrawPoints();

    int getLossPoints();


    int calculateTieBreaker(Team a, Team b);
}
