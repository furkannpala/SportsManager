package com.sportsmanager.core;

import java.util.ArrayList;
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


    default List<String> getAttributesForCategory(TrainingCategory focus) {
        return new ArrayList<>();
    }

    default List<String> getAttributeNamesForPosition(Position position) {
        return getPlayerAttributeNames();
    }
}
