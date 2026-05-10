package com.sportsmanager.handball;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class HandballPositionTest {

    @Test
    void goalkeeper_flags_are_correct() {
        assertTrue(HandballPosition.GOALKEEPER.isGoalkeeper());
        assertFalse(HandballPosition.GOALKEEPER.isWing());
        assertFalse(HandballPosition.GOALKEEPER.isBack());
        assertFalse(HandballPosition.GOALKEEPER.isPivot());
    }

    @Test
    void pivot_flags_are_correct() {
        assertTrue(HandballPosition.PIVOT.isPivot());
        assertFalse(HandballPosition.PIVOT.isGoalkeeper());
        assertFalse(HandballPosition.PIVOT.isWing());
    }

    @Test
    void wing_flags_are_correct() {
        assertTrue(HandballPosition.LEFT_WING.isWing());
        assertTrue(HandballPosition.RIGHT_WING.isWing());
        assertTrue(HandballPosition.LEFT_WING.isAttacking());
        assertFalse(HandballPosition.LEFT_WING.isBack());
    }

    @Test
    void center_back_is_both_attacking_and_defensive() {
        assertTrue(HandballPosition.CENTER_BACK.isAttacking());
        assertTrue(HandballPosition.CENTER_BACK.isDefensive());
        assertTrue(HandballPosition.CENTER_BACK.isBack());
    }

    @Test
    void left_back_and_right_back_are_defensive() {
        assertTrue(HandballPosition.LEFT_BACK.isDefensive());
        assertTrue(HandballPosition.RIGHT_BACK.isDefensive());
        assertFalse(HandballPosition.LEFT_BACK.isWing());
    }

    @ParameterizedTest
    @EnumSource(HandballPosition.class)
    void all_positions_weighted_attributes_sum_to_one(HandballPosition pos) {
        double sum = pos.getWeightedAttributes().values().stream()
                .mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sum, 0.001,
                pos.name() + " weights do not sum to 1.0");
    }

    @Test
    void same_position_out_of_position_penalty_is_zero() {
        for (HandballPosition pos : HandballPosition.values()) {
            assertEquals(0, HandballPosition.getOutOfPositionPenalty(pos, pos));
        }
    }

    @Test
    void goalkeeper_mismatch_penalty_is_25() {
        assertEquals(25, HandballPosition.getOutOfPositionPenalty(HandballPosition.GOALKEEPER, HandballPosition.PIVOT));
        assertEquals(25, HandballPosition.getOutOfPositionPenalty(HandballPosition.LEFT_WING, HandballPosition.GOALKEEPER));
        assertEquals(25, HandballPosition.getOutOfPositionPenalty(HandballPosition.CENTER_BACK, HandballPosition.GOALKEEPER));
    }

    @Test
    void mirrored_wings_penalty_is_3() {
        assertEquals(3, HandballPosition.getOutOfPositionPenalty(HandballPosition.LEFT_WING, HandballPosition.RIGHT_WING));
        assertEquals(3, HandballPosition.getOutOfPositionPenalty(HandballPosition.RIGHT_WING, HandballPosition.LEFT_WING));
    }

    @Test
    void mirrored_backs_penalty_is_3() {
        assertEquals(3, HandballPosition.getOutOfPositionPenalty(HandballPosition.LEFT_BACK, HandballPosition.RIGHT_BACK));
    }

    @Test
    void pivot_to_wing_penalty_is_12() {
        assertEquals(12, HandballPosition.getOutOfPositionPenalty(HandballPosition.PIVOT, HandballPosition.LEFT_WING));
        assertEquals(12, HandballPosition.getOutOfPositionPenalty(HandballPosition.RIGHT_WING, HandballPosition.PIVOT));
    }
}
