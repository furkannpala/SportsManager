package com.sportsmanager.handball;

import com.sportsmanager.core.*;

import java.util.Arrays;
import java.util.List;

public class HandballSport implements Sport {

    public static final int MAX_PLAYERS_ON_COURT    = 7;
    public static final int MAX_SUBSTITUTIONS       = 5;
    public static final int PERIOD_COUNT            = 2;
    public static final int PERIOD_DURATION_MINUTES = 30;

    @Override public String getSportName() { return "Handball"; }

    @Override
    public List<Position> getPositions() {
        return Arrays.asList(HandballPosition.values());
    }

    @Override
    public List<Formation> getFormations() {
        return Arrays.asList(HandballFormation.values());
    }

    @Override
    public List<Tactic> getTactics() {
        return Arrays.asList(HandballTactic.values());
    }

    @Override
    public List<String> getPlayerAttributeNames() {
        return List.of("speed", "throwing", "jumping", "agility", "defending", "physical");
    }

    @Override
    public List<String> getAttributeNamesForPosition(Position position) {
        if (position == HandballPosition.GOALKEEPER) {
            return List.of("speed", "reflexes", "diving", "reach", "positioning", "physical");
        }
        return getPlayerAttributeNames();
    }

    @Override
    public List<TrainingSession> getTrainingOptions() {
        return List.of(
                session("Throwing Practice",     "Improve throwing accuracy and power.",           TrainingCategory.ATTACKING),
                session("Defensive Drills",      "Strengthen blocking, positioning and defence.",  TrainingCategory.DEFENDING),
                session("Tactical Workshop",     "Work on passing patterns and set plays.",        TrainingCategory.TACTICAL),
                session("Physical Conditioning", "Boost speed, stamina and physical fitness.",     TrainingCategory.FITNESS)
        );
    }

    @Override
    public MatchEngine createMatchEngine() {
        return new HandballMatchEngine();
    }

    @Override public int getMatchPeriodCount()            { return PERIOD_COUNT; }
    @Override public int getMatchPeriodDurationMinutes()  { return PERIOD_DURATION_MINUTES; }
    @Override public int getMaxSubstitutions()            { return MAX_SUBSTITUTIONS; }

    @Override
    public List<String> getAttributesForCategory(TrainingCategory focus) {
        return switch (focus) {
            case ATTACKING -> List.of("throwing", "agility", "speed");
            case DEFENDING -> List.of("defending", "physical", "reflexes", "diving", "reach", "positioning");
            case TACTICAL  -> List.of("agility", "throwing", "positioning");
            case FITNESS   -> List.of("speed", "physical");
        };
    }

    private static TrainingSession session(String name, String description, TrainingCategory category) {
        return new TrainingSession() {
            @Override public String getName()               { return name; }
            @Override public String getDescription()        { return description; }
            @Override public TrainingCategory getCategory() { return category; }
        };
    }
}
