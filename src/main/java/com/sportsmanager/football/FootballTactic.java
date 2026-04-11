package com.sportsmanager.football;

import com.sportsmanager.core.Tactic;

public enum FootballTactic implements Tactic {
    ATTACKING("Attacking", 0.20, 0.15),
    BALANCED("Balanced", 0.00, 0.00),
    DEFENSIVE("Defensive", -0.15, -0.20),
    COUNTER_ATTACK("Counter-Attack", 0.10, -0.05);

    private final String tacticName;
    private final double goalProbabilityModifier;
    private final double concedeProbabilityModifier;

    FootballTactic(String tacticName,
                   double goalProbabilityModifier,
                   double concedeProbabilityModifier) {
        this.tacticName                = tacticName;
        this.goalProbabilityModifier   = goalProbabilityModifier;
        this.concedeProbabilityModifier = concedeProbabilityModifier;
    }

    @Override
    public String getTacticName() {
        return tacticName;
    }

    @Override
    public double getGoalProbabilityModifier() {
        return goalProbabilityModifier;
    }

    @Override
    public double getConcedeProbabilityModifier() {
        return concedeProbabilityModifier;
    }
}
