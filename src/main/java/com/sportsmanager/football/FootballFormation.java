package com.sportsmanager.football;

import com.sportsmanager.core.Formation;
import com.sportsmanager.core.Position;

import java.util.List;

import static com.sportsmanager.football.FootballPosition.*;

public enum FootballFormation implements Formation {

    // ── 3-back formations ────────────────────────────────────────────────

    F_3_4_3_DUZ("3-4-3 Flat") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    CENTRE_BACK, CENTRE_BACK, CENTRE_BACK,
                    LEFT_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, RIGHT_MIDFIELDER,
                    LEFT_WINGER, STRIKER, RIGHT_WINGER);
        }
        @Override public int getDefenderCount()   { return 3; }
        @Override public int getMidfielderCount() { return 4; }
        @Override public int getAttackerCount()   { return 3; }
    },

    F_3_4_1_2("3-4-1-2") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    CENTRE_BACK, CENTRE_BACK, CENTRE_BACK,
                    LEFT_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, RIGHT_MIDFIELDER,
                    ATTACKING_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 3; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_3_4_2_1("3-4-2-1") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    CENTRE_BACK, CENTRE_BACK, CENTRE_BACK,
                    LEFT_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, RIGHT_MIDFIELDER,
                    ATTACKING_MIDFIELDER, ATTACKING_MIDFIELDER,
                    STRIKER);
        }
        @Override public int getDefenderCount()   { return 3; }
        @Override public int getMidfielderCount() { return 6; }
        @Override public int getAttackerCount()   { return 1; }
    },

    F_3_1_4_2("3-1-4-2") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    CENTRE_BACK, CENTRE_BACK, CENTRE_BACK,
                    DEFENSIVE_MIDFIELDER,
                    LEFT_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, RIGHT_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 3; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_3_5_2("3-5-2") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    CENTRE_BACK, CENTRE_BACK, CENTRE_BACK,
                    LEFT_MIDFIELDER, DEFENSIVE_MIDFIELDER, ATTACKING_MIDFIELDER, DEFENSIVE_MIDFIELDER, RIGHT_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 3; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 2; }
    },

    // ── 4-back formations ────────────────────────────────────────────────

    F_4_3_3_ATAK("4-3-3 Attack") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, ATTACKING_MIDFIELDER, CENTRAL_MIDFIELDER,
                    LEFT_WINGER, STRIKER, RIGHT_WINGER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 3; }
        @Override public int getAttackerCount()   { return 3; }
    },

    F_4_3_3_DUZ("4-3-3 Flat") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER,
                    LEFT_WINGER, STRIKER, RIGHT_WINGER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 3; }
        @Override public int getAttackerCount()   { return 3; }
    },

    F_4_3_1_2("4-3-1-2") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER,
                    ATTACKING_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 4; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_4_3_2_1("4-3-2-1") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER,
                    ATTACKING_MIDFIELDER, ATTACKING_MIDFIELDER,
                    STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 1; }
    },

    F_4_5_1_ATAK("4-5-1 Attack") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    LEFT_MIDFIELDER, ATTACKING_MIDFIELDER, CENTRAL_MIDFIELDER, ATTACKING_MIDFIELDER, RIGHT_MIDFIELDER,
                    STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 1; }
    },

    F_4_3_3_ROLANTI("4-3-3 Holding") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, DEFENSIVE_MIDFIELDER, CENTRAL_MIDFIELDER,
                    LEFT_WINGER, STRIKER, RIGHT_WINGER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 3; }
        @Override public int getAttackerCount()   { return 3; }
    },

    F_4_5_1_DUZ("4-5-1 Flat") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    LEFT_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, RIGHT_MIDFIELDER,
                    STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 1; }
    },

    F_4_4_2_DUZ("4-4-2 Flat") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    LEFT_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, RIGHT_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 4; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_4_4_1_1_ORTA_SAHA("4-4-1-1 Midfield") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    LEFT_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, RIGHT_MIDFIELDER,
                    ATTACKING_MIDFIELDER,
                    STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 1; }
    },

    F_4_1_2_1_2_GENIS("4-1-2-1-2 Wide") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER,
                    LEFT_MIDFIELDER, RIGHT_MIDFIELDER,
                    ATTACKING_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 4; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_4_1_2_1_2_DAR("4-1-2-1-2 Narrow") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER,
                    CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER,
                    ATTACKING_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 4; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_4_2_2_2("4-2-2-2") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER, DEFENSIVE_MIDFIELDER,
                    ATTACKING_MIDFIELDER, ATTACKING_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 4; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_4_3_3_DEFANSIF("4-3-3 Defensive") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER, CENTRAL_MIDFIELDER, DEFENSIVE_MIDFIELDER,
                    LEFT_WINGER, STRIKER, RIGHT_WINGER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 3; }
        @Override public int getAttackerCount()   { return 3; }
    },

    F_4_4_2_ROLANTI("4-4-2 Holding") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    LEFT_MIDFIELDER, DEFENSIVE_MIDFIELDER, DEFENSIVE_MIDFIELDER, RIGHT_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 4; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_4_2_4("4-2-4") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER,
                    LEFT_WINGER, STRIKER, STRIKER, RIGHT_WINGER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 2; }
        @Override public int getAttackerCount()   { return 4; }
    },

    F_4_1_3_2("4-1-3-2") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER,
                    LEFT_MIDFIELDER, CENTRAL_MIDFIELDER, RIGHT_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 4; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_4_1_4_1("4-1-4-1") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER,
                    LEFT_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, RIGHT_MIDFIELDER,
                    STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 1; }
    },

    F_4_2_1_3("4-2-1-3") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER, DEFENSIVE_MIDFIELDER,
                    ATTACKING_MIDFIELDER,
                    LEFT_WINGER, STRIKER, RIGHT_WINGER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 3; }
        @Override public int getAttackerCount()   { return 3; }
    },

    F_4_2_3_1_DAR("4-2-3-1 Narrow") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER, DEFENSIVE_MIDFIELDER,
                    ATTACKING_MIDFIELDER, ATTACKING_MIDFIELDER, ATTACKING_MIDFIELDER,
                    STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 1; }
    },

    F_4_2_3_1_GENIS("4-2-3-1 Wide") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    DEFENSIVE_MIDFIELDER, DEFENSIVE_MIDFIELDER,
                    LEFT_MIDFIELDER, ATTACKING_MIDFIELDER, RIGHT_MIDFIELDER,
                    STRIKER);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 5; }
        @Override public int getAttackerCount()   { return 1; }
    },

    // ── 5-back formations ────────────────────────────────────────────────

    F_5_2_3("5-2-3") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER,
                    LEFT_WINGER, STRIKER, RIGHT_WINGER);
        }
        @Override public int getDefenderCount()   { return 5; }
        @Override public int getMidfielderCount() { return 2; }
        @Override public int getAttackerCount()   { return 3; }
    },

    F_5_2_1_2("5-2-1-2") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER,
                    ATTACKING_MIDFIELDER,
                    STRIKER, STRIKER);
        }
        @Override public int getDefenderCount()   { return 5; }
        @Override public int getMidfielderCount() { return 3; }
        @Override public int getAttackerCount()   { return 2; }
    },

    F_5_4_1_DUZ("5-4-1 Flat") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    LEFT_MIDFIELDER, CENTRAL_MIDFIELDER, CENTRAL_MIDFIELDER, RIGHT_MIDFIELDER,
                    STRIKER);
        }
        @Override public int getDefenderCount()   { return 5; }
        @Override public int getMidfielderCount() { return 4; }
        @Override public int getAttackerCount()   { return 1; }
    },

    F_5_3_2_ROLANTI("5-3-2 Holding") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER,
                    LEFT_BACK, CENTRE_BACK, CENTRE_BACK, CENTRE_BACK, RIGHT_BACK,
                    CENTRAL_MIDFIELDER, DEFENSIVE_MIDFIELDER, CENTRAL_MIDFIELDER,
                    STRIKER, STRIKER);
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
