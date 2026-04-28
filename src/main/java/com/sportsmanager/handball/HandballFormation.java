package com.sportsmanager.handball;

import com.sportsmanager.core.Formation;
import com.sportsmanager.core.Position;

import java.util.List;

import static com.sportsmanager.handball.HandballPosition.*;

public enum HandballFormation implements Formation {

    H_6_0("6-0 Flat") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER, LEFT_WING, LEFT_BACK, CENTER_BACK, RIGHT_BACK, RIGHT_WING, PIVOT);
        }
        @Override public int getDefenderCount()   { return 3; }
        @Override public int getMidfielderCount() { return 1; }
        @Override public int getAttackerCount()   { return 2; }
    },

    H_5_1("5-1") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER, LEFT_WING, LEFT_BACK, CENTER_BACK, RIGHT_BACK, RIGHT_WING, PIVOT);
        }
        @Override public int getDefenderCount()   { return 3; }
        @Override public int getMidfielderCount() { return 1; }
        @Override public int getAttackerCount()   { return 2; }
    },

    H_4_2("4-2") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER, LEFT_BACK, CENTER_BACK, RIGHT_BACK, PIVOT, LEFT_WING, RIGHT_WING);
        }
        @Override public int getDefenderCount()   { return 2; }
        @Override public int getMidfielderCount() { return 2; }
        @Override public int getAttackerCount()   { return 2; }
    },

    H_3_2_1("3-2-1") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER, LEFT_BACK, CENTER_BACK, RIGHT_BACK, LEFT_WING, RIGHT_WING, PIVOT);
        }
        @Override public int getDefenderCount()   { return 2; }
        @Override public int getMidfielderCount() { return 1; }
        @Override public int getAttackerCount()   { return 3; }
    },

    H_3_3("3-3") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER, LEFT_BACK, CENTER_BACK, RIGHT_BACK, LEFT_WING, PIVOT, RIGHT_WING);
        }
        @Override public int getDefenderCount()   { return 2; }
        @Override public int getMidfielderCount() { return 1; }
        @Override public int getAttackerCount()   { return 3; }
    };

    private final String formationName;

    HandballFormation(String formationName) { this.formationName = formationName; }

    @Override public String getFormationName() { return formationName; }
}
