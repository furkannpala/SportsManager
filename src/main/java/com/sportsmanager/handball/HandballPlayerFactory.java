package com.sportsmanager.handball;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Position;
import com.sportsmanager.generator.PlayerFactory;

import java.util.Random;

public class HandballPlayerFactory implements PlayerFactory {

    private static final int STD_DEV  = 8;
    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 100;

    private final Random random;

    public HandballPlayerFactory()             { this.random = new Random(); }
    public HandballPlayerFactory(Random random) { this.random = random; }

    @Override
    public Player createPlayer(String name, int age, Position position, int tierMean) {
        HandballPosition hp = (position instanceof HandballPosition)
                ? (HandballPosition) position
                : HandballPosition.CENTER_BACK;

        if (hp.isGoalkeeper()) {
            return HandballPlayer.createGoalkeeper(
                    name, age,
                    gauss(scale(tierMean, 0.80)),   // speed
                    gauss(scale(tierMean, 1.20)),   // reflexes — most important
                    gauss(scale(tierMean, 1.10)),   // diving
                    gauss(scale(tierMean, 1.15)),   // reach
                    gauss(scale(tierMean, 1.08)),   // positioning
                    gauss(scale(tierMean, 1.00))    // physical
            );
        }

        double[] f = attributeFactors(hp);
        return HandballPlayer.createOutfield(
                name, age, hp,
                gauss(scale(tierMean, f[0])),   // speed
                gauss(scale(tierMean, f[1])),   // throwing
                gauss(scale(tierMean, f[2])),   // jumping
                gauss(scale(tierMean, f[3])),   // agility
                gauss(scale(tierMean, f[4])),   // defending
                gauss(scale(tierMean, f[5]))    // physical
        );
    }

    /** Scale factors: [speed, throwing, jumping, agility, defending, physical] */
    private double[] attributeFactors(HandballPosition pos) {
        return switch (pos) {
            case LEFT_BACK,
                 RIGHT_BACK  -> new double[]{0.95, 1.20, 1.10, 0.90, 0.85, 1.15};
            case CENTER_BACK -> new double[]{1.05, 1.15, 0.95, 1.25, 0.80, 0.90};
            case LEFT_WING,
                 RIGHT_WING  -> new double[]{1.30, 1.10, 1.05, 1.20, 0.60, 0.80};
            case PIVOT       -> new double[]{0.85, 1.05, 1.25, 0.90, 1.05, 1.30};
            default          -> new double[]{1.0,  1.0,  1.0,  1.0,  1.0,  1.0};
        };
    }

    private int scale(int tierMean, double factor) {
        return Math.max(MIN_ATTR, Math.min(MAX_ATTR, (int) Math.round(tierMean * factor)));
    }

    private int gauss(int mean) {
        return Math.max(MIN_ATTR, Math.min(MAX_ATTR,
                (int) Math.round(mean + random.nextGaussian() * STD_DEV)));
    }
}
