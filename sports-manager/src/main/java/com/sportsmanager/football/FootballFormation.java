package com.sportsmanager.football;

import com.sportsmanager.core.Formation;
import com.sportsmanager.core.Position;

import java.util.List;

import static com.sportsmanager.football.FootballPosition.*;

public enum FootballFormation implements Formation {
    F_4_3_3("4-3-3") {
        @Override
        public List<Position> getPositionSlots() {
            return List.of(
                    GOALKEEPER,
                    CENTRE_BACK, CENTRE_BACK, LEFT_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER,
                    LEFT_WINGER, RIGHT_WINGER,
                    STRIKER
            );
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 3; }
        @Override public int getAttackerCount()   { return 3; }
    },

    F_4_4_2("4-4-2") {
        @Override
        public List<Position> getPositionSlots() {
            return List.of(
                    GOALKEEPER,
                    CENTRE_BACK, CENTRE_BACK, LEFT_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, LEFT_WINGER, RIGHT_WINGER,
                    STRIKER, STRIKER
            );
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 4; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_3_5_2("3-5-2") {
        @Override
        public List<Position> getPositionSlots() {
            return List.of(
                    GOALKEEPER,
                    CENTRE_BACK, CENTRE_BACK, CENTRE_BACK,
                    DEFENSIVE_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER,
                    LEFT_WINGER, RIGHT_WINGER,
                    STRIKER, STRIKER
            );
        }
        @Override public int getDefenderCount()   { return 3; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_4_2_3_1("4-2-3-1") {
        @Override
        public List<Position> getPositionSlots() {
            return List.of(
                    GOALKEEPER,
                    CENTRE_BACK, CENTRE_BACK, LEFT_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER, DEFENSIVE_MIDFIELDER,
                    LEFT_WINGER, ATTACKING_MIDFIELDER, RIGHT_WINGER,
                    STRIKER
            );
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 1; }
    },

    F_5_3_2("5-3-2") {
        @Override
        public List<Position> getPositionSlots() {
            return List.of(
                    GOALKEEPER,
                    CENTRE_BACK, CENTRE_BACK, CENTRE_BACK, LEFT_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER,
                    STRIKER, STRIKER
            );
        }
        @Override public int getDefenderCount()   { return 5; }
        @Override public int getMidfielderCount() { return 3; }
        @Override public int getAttackerCount()   { return 2; }
    };

    private final String formationName;

    FootballFormation(String formationName) {
        this.formationName = formationName;
    }

    @Override
    public String getFormationName() {
        return formationName;
    }
}
