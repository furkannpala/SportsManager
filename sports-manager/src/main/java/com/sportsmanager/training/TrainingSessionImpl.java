package com.sportsmanager.training;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.core.TrainingCategory;
import com.sportsmanager.core.TrainingSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class TrainingSessionImpl implements TrainingSession {

    private static final double MAX_DAILY_GAIN = 2.0;
    private static final double MAX_VARIANCE   = 0.4;
    private static final int    MAX_ATTRIBUTE  = 100;

    private final TrainingCategory focus;
    private final Coach assignedCoach;
    private final Sport sport;
    private final List<TrainingLog> sessionLog;
    private final Random random;

    public TrainingSessionImpl(TrainingCategory focus, Coach assignedCoach, Sport sport) {
        this.focus         = focus;
        this.assignedCoach = assignedCoach;
        this.sport         = sport;
        this.sessionLog    = new ArrayList<>();
        this.random        = new Random();
    }


    @Override
    public String getName() {
        return focus.name().charAt(0)
                + focus.name().substring(1).toLowerCase()
                + " Training";
    }

    @Override
    public String getDescription() {
        return "Training session focused on " + focus.name().toLowerCase()
                + " attributes, led by " + assignedCoach.getName() + ".";
    }




    public void executeTraining(Team team) {
        sessionLog.clear();

        List<String> attributesToTrain = sport.getAttributesForCategory(focus);
        if (attributesToTrain.isEmpty()) return;

        double baseGain       = (assignedCoach.getSkillLevel() / 100.0) * MAX_DAILY_GAIN;
        double specMultiplier = (focus == assignedCoach.getSpecialization()) ? 1.5 : 1.0;
        double variance       = (1.0 - assignedCoach.getExperience() / 100.0) * MAX_VARIANCE;

        for (Player player : team.getSquad()) {
            if (!player.isAvailable()) continue; // skip injured players

            double ageFactor     = getAgeFactor(player.getAge());
            double varianceFactor = (random.nextDouble() * 2 - 1) * variance;
            double rawGain       = (baseGain * specMultiplier * ageFactor) + varianceFactor;

            for (String attr : attributesToTrain) {
                int currentVal = player.getAttributeValue(attr);
                int gain = (int) Math.round(
                        Math.max(0, Math.min(rawGain, MAX_ATTRIBUTE - currentVal)));

                if (gain > 0) {
                    player.increaseAttribute(attr, gain);
                    sessionLog.add(new TrainingLog(player, attr, gain));
                }
            }
        }
    }



    @Override
    public TrainingCategory getCategory()      { return focus; }

    public TrainingCategory getFocus()         { return focus; }
    public Coach getAssignedCoach()            { return assignedCoach; }
    public List<TrainingLog> getSessionLog()   { return Collections.unmodifiableList(sessionLog); }



    private double getAgeFactor(int age) {
        if (age < 21) return 1.3;  // rapid youth development
        if (age < 28) return 1.0;  // peak years
        if (age < 32) return 0.7;  // slight decline
        return 0.3;                // maintenance only
    }
}
