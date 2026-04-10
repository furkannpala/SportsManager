package com.sportsmanager.football;

import com.sportsmanager.core.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FootballFormationTest {

    // ── Every formation has exactly 11 position slots ────────────────────
    @ParameterizedTest
    @EnumSource(FootballFormation.class)
    void getPositionSlots_hasExactly11(FootballFormation formation) {
        assertEquals(11, formation.getPositionSlots().size(),
                formation.getFormationName() + " should have 11 slots");
    }

    // ── Every formation has exactly 1 GOALKEEPER slot ─────────────────────
    @ParameterizedTest
    @EnumSource(FootballFormation.class)
    void getPositionSlots_hasExactlyOneGoalkeeper(FootballFormation formation) {
        long gkCount = formation.getPositionSlots().stream()
                .filter(p -> p == FootballPosition.GOALKEEPER)
                .count();
        assertEquals(1, gkCount,
                formation.getFormationName() + " should have exactly 1 GK slot");
    }

    // ── defenderCount + midfielderCount + attackerCount = 10 ─────────────
    @ParameterizedTest
    @EnumSource(FootballFormation.class)
    void outfieldCounts_sumToTen(FootballFormation formation) {
        int total = formation.getDefenderCount()
                + formation.getMidfielderCount()
                + formation.getAttackerCount();
        assertEquals(10, total,
                formation.getFormationName() + " outfield counts should sum to 10");
    }

    // ── F_4_3_3 has expected counts and name ─────────────────────────────
    @Test
    void f433_hasCorrectCountsAndName() {
        FootballFormation f = FootballFormation.F_4_3_3;
        assertEquals("4-3-3", f.getFormationName());
        assertEquals(4, f.getDefenderCount());
        assertEquals(3, f.getMidfielderCount());
        assertEquals(3, f.getAttackerCount());
    }
}
