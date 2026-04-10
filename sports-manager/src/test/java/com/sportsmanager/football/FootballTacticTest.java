package com.sportsmanager.football;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FootballTacticTest {

    // ── ATTACKING has goalProbabilityModifier = 0.20 ──────────────────────
    @Test
    void attacking_goalModifier_is020() {
        assertEquals(0.20, FootballTactic.ATTACKING.getGoalProbabilityModifier(), 1e-9);
    }

    // ── DEFENSIVE has goalProbabilityModifier = -0.15 ────────────────────
    @Test
    void defensive_goalModifier_isNeg015() {
        assertEquals(-0.15, FootballTactic.DEFENSIVE.getGoalProbabilityModifier(), 1e-9);
    }

    // ── BALANCED has both modifiers = 0.0 ────────────────────────────────
    @Test
    void balanced_bothModifiers_areZero() {
        assertEquals(0.0, FootballTactic.BALANCED.getGoalProbabilityModifier(),   1e-9);
        assertEquals(0.0, FootballTactic.BALANCED.getConcedeProbabilityModifier(), 1e-9);
    }

    // ── COUNTER_ATTACK has expected modifier values ───────────────────────
    @Test
    void counterAttack_modifiers_areCorrect() {
        assertEquals(0.10,  FootballTactic.COUNTER_ATTACK.getGoalProbabilityModifier(),   1e-9);
        assertEquals(-0.05, FootballTactic.COUNTER_ATTACK.getConcedeProbabilityModifier(), 1e-9);
    }
}
