package com.sportsmanager.handball;

import com.sportsmanager.core.Tactic;

public enum HandballTactic implements Tactic {

    ATTACKING("Attacking",     0.20,  0.15),
    BALANCED("Balanced",       0.00,  0.00),
    DEFENSIVE("Defensive",    -0.15, -0.20),
    FAST_BREAK("Fast Break",   0.15, -0.05);

    private final String tacticName;
    private final double goalProbabilityModifier;
    private final double concedeProbabilityModifier;

    HandballTactic(String tacticName, double goalProbMod, double concedeProbMod) {
        this.tacticName                 = tacticName;
        this.goalProbabilityModifier    = goalProbMod;
        this.concedeProbabilityModifier = concedeProbMod;
    }

    @Override public String getTacticName()                  { return tacticName; }
    @Override public double getGoalProbabilityModifier()     { return goalProbabilityModifier; }
    @Override public double getConcedeProbabilityModifier()  { return concedeProbabilityModifier; }
}
