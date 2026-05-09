package com.sportsmanager.handball;

import com.sportsmanager.core.Formation;
import com.sportsmanager.core.Position;

import java.util.List;

import static com.sportsmanager.handball.HandballPosition.*;

public enum HandballFormation implements Formation {

    /**
     * Flat 6-player defensive line — backs and pivot hold the line, wings tucked in last.
     * Slots: GK | LB CB RB | PIVOT | RW LW
     */
    H_6_0("6-0 Flat") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER, LEFT_BACK, CENTER_BACK, RIGHT_BACK, PIVOT, RIGHT_WING, LEFT_WING);
        }
        @Override public int getDefenderCount()   { return 4; }
        @Override public int getMidfielderCount() { return 0; }
        @Override public int getAttackerCount()   { return 2; }
    },

    /**
     * 5 in the defensive line, pivot released as the lone forward.
     * Slots: GK | LW LB CB RB RW | PIVOT
     */
    H_5_1("5-1") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER, LEFT_WING, LEFT_BACK, CENTER_BACK, RIGHT_BACK, RIGHT_WING, PIVOT);
        }
        @Override public int getDefenderCount()   { return 3; }
        @Override public int getMidfielderCount() { return 2; }
        @Override public int getAttackerCount()   { return 1; }
    },

    /**
     * 4 backs defend, pivot and wings push forward in pairs.
     * Slots: GK | LB CB RB | LW PIVOT RW
     */
    H_4_2("4-2") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER, LEFT_BACK, CENTER_BACK, RIGHT_BACK, LEFT_WING, PIVOT, RIGHT_WING);
        }
        @Override public int getDefenderCount()   { return 3; }
        @Override public int getMidfielderCount() { return 1; }
        @Override public int getAttackerCount()   { return 2; }
    },

    /**
     * 3 backs defend, wings in mid, pivot as advanced playmaker.
     * Slots: GK | LB CB RB | LW RW | PIVOT
     */
    H_3_2_1("3-2-1") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER, LEFT_BACK, CENTER_BACK, RIGHT_BACK, LEFT_WING, RIGHT_WING, PIVOT);
        }
        @Override public int getDefenderCount()   { return 2; }
        @Override public int getMidfielderCount() { return 2; }
        @Override public int getAttackerCount()   { return 2; }
    },

    /**
     * Attacking 3-3 — center back leads the line, wings and pivot all pushed high.
     * Slots: GK | CB LB RB | LW RW | PIVOT
     */
    H_3_3("3-3") {
        @Override public List<Position> getPositionSlots() {
            return List.of(GOALKEEPER, CENTER_BACK, LEFT_BACK, RIGHT_BACK, LEFT_WING, RIGHT_WING, PIVOT);
        }
        @Override public int getDefenderCount()   { return 1; }
        @Override public int getMidfielderCount() { return 2; }
        @Override public int getAttackerCount()   { return 3; }
    };

    private final String formationName;

    HandballFormation(String formationName) { this.formationName = formationName; }

    @Override public String getFormationName() { return formationName; }
}
