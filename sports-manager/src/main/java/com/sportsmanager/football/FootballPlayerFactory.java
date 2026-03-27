package com.sportsmanager.football;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Position;
import com.sportsmanager.generator.PlayerFactory;

import java.util.Random;

public class FootballPlayerFactory implements PlayerFactory {

    private static final int STD_DEV  = 8;   // spread around tierMean
    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 100;

    private final Random random;

    public FootballPlayerFactory() {
        this.random = new Random();
    }

    public FootballPlayerFactory(Random random) {
        this.random = random;
    }

    @Override
    public Player createPlayer(String name, int age, Position position, int tierMean) {
        FootballPosition fp = (position instanceof FootballPosition)
                ? (FootballPosition) position
                : FootballPosition.STRIKER; // fallback

        if (fp == FootballPosition.GOALKEEPER) {
            return FootballPlayer.createGoalkeeper(
                    name, age,
                    gauss(tierMean),  // pace
                    gauss(tierMean),  // diving
                    gauss(tierMean),  // handling
                    gauss(tierMean),  // kicking
                    gauss(tierMean),  // reflexes
                    gauss(tierMean)   // positioning
            );
        }

        return FootballPlayer.createOutfield(
                name, age, fp,
                gauss(tierMean),  // pace
                gauss(tierMean),  // shooting
                gauss(tierMean),  // passing
                gauss(tierMean),  // dribbling
                gauss(tierMean),  // defending
                gauss(tierMean)   // physical
        );
    }

    private int gauss(int mean) {
        int value = (int) Math.round(mean + random.nextGaussian() * STD_DEV);
        return Math.max(MIN_ATTR, Math.min(MAX_ATTR, value));
    }
}
