package com.sportsmanager.handball;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandballTacticTest {

    @Test
    void attacking_has_positive_goal_modifier() {
        assertEquals(0.20, HandballTactic.ATTACKING.getGoalProbabilityModifier(), 0.001);
        assertEquals(0.15, HandballTactic.ATTACKING.getConcedeProbabilityModifier(), 0.001);
    }

    @Test
    void defensive_has_negative_modifiers() {
        assertEquals(-0.15, HandballTactic.DEFENSIVE.getGoalProbabilityModifier(), 0.001);
        assertEquals(-0.20, HandballTactic.DEFENSIVE.getConcedeProbabilityModifier(), 0.001);
    }

    @Test
    void balanced_has_zero_modifiers() {
        assertEquals(0.0, HandballTactic.BALANCED.getGoalProbabilityModifier(), 0.001);
        assertEquals(0.0, HandballTactic.BALANCED.getConcedeProbabilityModifier(), 0.001);
    }

    @Test
    void fast_break_has_positive_goal_and_negative_concede() {
        assertEquals(0.15, HandballTactic.FAST_BREAK.getGoalProbabilityModifier(), 0.001);
        assertEquals(-0.05, HandballTactic.FAST_BREAK.getConcedeProbabilityModifier(), 0.001);
    }

    @Test
    void all_four_tactics_exist() {
        assertEquals(4, HandballTactic.values().length);
    }
}
