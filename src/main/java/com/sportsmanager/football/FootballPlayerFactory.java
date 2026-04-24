package com.sportsmanager.football;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Position;
import com.sportsmanager.generator.PlayerFactory;

import java.util.Random;

public class FootballPlayerFactory implements PlayerFactory {

    private static final int STD_DEV  = 8;   // tighter spread so positional identity stays clear
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
                : FootballPosition.STRIKER;

        if (fp == FootballPosition.GOALKEEPER) {
            // GK key stats: reflexes > diving > handling > positioning; pace & kicking secondary
            return FootballPlayer.createGoalkeeper(
                    name, age,
                    gauss(scale(tierMean, 0.82)),   // pace  – GKs rarely sprint
                    gauss(scale(tierMean, 1.15)),   // diving
                    gauss(scale(tierMean, 1.10)),   // handling
                    gauss(scale(tierMean, 0.88)),   // kicking
                    gauss(scale(tierMean, 1.20)),   // reflexes – most important
                    gauss(scale(tierMean, 1.12)),   // positioning
                    gauss(scale(tierMean, 1.05))    // physical – needed for stamina drain
            );
        }

        // Outfield: [pace, shooting, passing, dribbling, defending, physical]
        double[] f = attributeFactors(fp);
        return FootballPlayer.createOutfield(
                name, age, fp,
                gauss(scale(tierMean, f[0])),   // pace
                gauss(scale(tierMean, f[1])),   // shooting
                gauss(scale(tierMean, f[2])),   // passing
                gauss(scale(tierMean, f[3])),   // dribbling
                gauss(scale(tierMean, f[4])),   // defending
                gauss(scale(tierMean, f[5]))    // physical (stamina)
        );
    }

    /**
     * Returns scale factors [pace, shooting, passing, dribbling, defending, physical]
     * relative to tierMean.  Values > 1.0 boost the stat, < 1.0 reduce it.
     *
     * Design goals:
     *  - CB:      high defending & physical, low shooting/dribbling
     *  - LB/RB:   good defending & pace, moderate passing, low shooting
     *  - CDM:     high defending & physical, good passing, low shooting
     *  - CM:      good passing & dribbling, balanced defending, low shooting
     *  - CAM:     top passing & dribbling, strong shooting, very low defending
     *  - LM/RM:   top pace & dribbling & passing, moderate shooting, low defending
     *  - LW/RW:   top pace & dribbling, good shooting, very low defending
     *              (LW/RW shoot more than LM/RM; LM/RM pass slightly better)
     *  - ST:      top shooting & pace, good dribbling, very low defending (30–45 range)
     *  - CF:      top shooting & physical, good passing, very low defending
     */
    private double[] attributeFactors(FootballPosition pos) {
        //                               pace   shoot  pass   drib   defend  physical
        return switch (pos) {
            case CENTRE_BACK          -> new double[]{0.88, 0.48, 0.85, 0.72, 1.25, 1.15};
            case LEFT_BACK,
                 RIGHT_BACK           -> new double[]{1.08, 0.65, 1.02, 0.90, 1.12, 1.02};
            case DEFENSIVE_MIDFIELDER -> new double[]{0.95, 0.65, 1.10, 0.85, 1.18, 1.15};
            case CENTRAL_MIDFIELDER   -> new double[]{1.00, 0.88, 1.20, 1.12, 0.88, 1.05};
            case ATTACKING_MIDFIELDER -> new double[]{1.05, 1.12, 1.25, 1.22, 0.52, 0.88};
            case LEFT_MIDFIELDER,
                 RIGHT_MIDFIELDER     -> new double[]{1.18, 0.85, 1.22, 1.18, 0.78, 1.02};
            case LEFT_WINGER,
                 RIGHT_WINGER         -> new double[]{1.25, 1.12, 1.00, 1.25, 0.58, 0.88};
            case STRIKER              -> new double[]{1.12, 1.25, 0.95, 1.08, 0.45, 1.05};
            case CENTRE_FORWARD       -> new double[]{1.05, 1.20, 1.02, 1.00, 0.45, 1.18};
            default                   -> new double[]{1.0,  1.0,  1.0,  1.0,  1.0,  1.0};
        };
    }

    /**
     * Scales tierMean by the given factor and clamps to [MIN_ATTR, MAX_ATTR].
     * This becomes the centre of the Gaussian distribution for that attribute.
     */
    private int scale(int tierMean, double factor) {
        int scaled = (int) Math.round(tierMean * factor);
        return Math.max(MIN_ATTR, Math.min(MAX_ATTR, scaled));
    }

    private int gauss(int mean) {
        int value = (int) Math.round(mean + random.nextGaussian() * STD_DEV);
        return Math.max(MIN_ATTR, Math.min(MAX_ATTR, value));
    }
}
