package com.sportsmanager.football;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class FootballPositionTest {

    // ── GOALKEEPER is defensive, not midfield, not attacking ──────────────
    @Test
    void goalkeeper_zoneFlags() {
        assertTrue(FootballPosition.GOALKEEPER.isDefensive());
        assertFalse(FootballPosition.GOALKEEPER.isMidfield());
        assertFalse(FootballPosition.GOALKEEPER.isAttacking());
    }

    // ── STRIKER is attacking, not defensive ───────────────────────────────
    @Test
    void striker_isAttacking() {
        assertTrue(FootballPosition.STRIKER.isAttacking());
        assertFalse(FootballPosition.STRIKER.isDefensive());
    }

    // ── CENTRAL_MIDFIELDER is midfield, not attacking, not defensive ──────
    @Test
    void centralMidfielder_isMidfield() {
        assertTrue(FootballPosition.CENTRAL_MIDFIELDER.isMidfield());
        assertFalse(FootballPosition.CENTRAL_MIDFIELDER.isAttacking());
        assertFalse(FootballPosition.CENTRAL_MIDFIELDER.isDefensive());
    }

    // ── Weighted attributes sum to 1.0 for every position ────────────────
    @ParameterizedTest
    @EnumSource(FootballPosition.class)
    void weightedAttributes_sumToOne(FootballPosition position) {
        double sum = position.getWeightedAttributes().values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        assertEquals(1.0, sum, 1e-9,
                position.getName() + " weights should sum to 1.0");
    }
}
