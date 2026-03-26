package com.sportsmanager.core;

import java.util.List;


public interface Sport {


    String getSportName();


    List<Position> getPositions();


    List<Formation> getFormations();


    List<Tactic> getTactics();


    List<String> getPlayerAttributeNames();


    List<TrainingSession> getTrainingOptions();


    MatchEngine createMatchEngine();


    int getMatchPeriodCount();


    int getMatchPeriodDurationMinutes();

    int getMaxSubstitutions();
}
