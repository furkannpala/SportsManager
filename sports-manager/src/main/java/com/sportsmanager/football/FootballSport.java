package com.sportsmanager.football;

import com.sportsmanager.core.*;

import java.util.Arrays;
import java.util.List;

/**
 * Football-specific implementation of the Sport interface.
 * Centralises all football rules and structural constants.
 * The UI and game engine interact only with the Sport interface —
 * they never depend on this concrete class directly.
 */
public class FootballSport implements Sport {

    public static final int MAX_PLAYERS_ON_FIELD   = 11;
    public static final int MAX_SUBSTITUTIONS      = 5;
    public static final int PERIOD_COUNT           = 2;
    public static final int PERIOD_DURATION_MINUTES = 45;

    @Override
    public String getSportName() {
        return "Football";
    }

    @Override
    public List<Position> getPositions() {
        return Arrays.asList(FootballPosition.values());
    }

    @Override
    public List<Formation> getFormations() {
        return Arrays.asList(FootballFormation.values());
    }

    @Override
    public List<Tactic> getTactics() {
        return Arrays.asList(FootballTactic.values());
    }

    @Override
    public List<String> getPlayerAttributeNames() {
        return List.of("pace", "shooting", "passing", "dribbling", "defending", "physical");
    }

    @Override
    public List<String> getAttributeNamesForPosition(Position position) {
        if (position == FootballPosition.GOALKEEPER) {
            return List.of("pace", "diving", "handling", "kicking", "reflexes", "positioning");
        }
        return getPlayerAttributeNames();
    }

    @Override
    public List<TrainingSession> getTrainingOptions() {
        return List.of(
                session("Finishing Training",    "Improve shooting and movement in the final third.",  TrainingCategory.ATTACKING),
                session("Defensive Drills",      "Strengthen tackling, positioning and defensive shape.", TrainingCategory.DEFENDING),
                session("Tactical Workshop",     "Work on passing patterns, pressing and set pieces.",  TrainingCategory.TACTICAL),
                session("Physical Conditioning", "Boost pace, stamina and overall physical fitness.",   TrainingCategory.FITNESS)
        );
    }

    @Override
    public MatchEngine createMatchEngine() {
        return new FootballMatchEngine();
    }

    @Override
    public int getMatchPeriodCount() {
        return PERIOD_COUNT;
    }

    @Override
    public int getMatchPeriodDurationMinutes() {
        return PERIOD_DURATION_MINUTES;
    }

    @Override
    public int getMaxSubstitutions() {
        return MAX_SUBSTITUTIONS;
    }

    @Override
    public List<String> getAttributesForCategory(TrainingCategory focus) {
        return switch (focus) {
            case ATTACKING -> List.of("shooting", "dribbling", "pace", "kicking");
            case DEFENDING -> List.of("defending", "physical", "diving", "handling", "reflexes", "positioning");
            case TACTICAL  -> List.of("passing", "dribbling", "positioning");
            case FITNESS   -> List.of("pace", "physical");
        };
    }

    private static TrainingSession session(String name, String description,
                                           TrainingCategory category) {
        return new TrainingSession() {
            @Override public String getName()             { return name; }
            @Override public String getDescription()      { return description; }
            @Override public TrainingCategory getCategory() { return category; }
        };
    }
}
