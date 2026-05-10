package com.sportsmanager.handball;

import com.sportsmanager.core.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandballFormationTest {

    @ParameterizedTest
    @EnumSource(HandballFormation.class)
    void each_formation_has_exactly_7_position_slots(HandballFormation f) {
        assertEquals(7, f.getPositionSlots().size(),
                f.name() + " should have 7 slots");
    }

    @ParameterizedTest
    @EnumSource(HandballFormation.class)
    void each_formation_has_exactly_one_goalkeeper(HandballFormation f) {
        long gkCount = f.getPositionSlots().stream()
                .filter(p -> p == HandballPosition.GOALKEEPER)
                .count();
        assertEquals(1, gkCount, f.name() + " should have exactly 1 goalkeeper slot");
    }

    @ParameterizedTest
    @EnumSource(HandballFormation.class)
    void defender_midfielder_attacker_counts_sum_to_6(HandballFormation f) {
        int total = f.getDefenderCount() + f.getMidfielderCount() + f.getAttackerCount();
        assertEquals(6, total, f.name() + " outfield counts should sum to 6");
    }

    @Test
    void h_6_0_flat_has_correct_name_and_shape() {
        assertEquals("6-0 Flat", HandballFormation.H_6_0.getFormationName());
        assertEquals(4, HandballFormation.H_6_0.getDefenderCount());
        assertEquals(0, HandballFormation.H_6_0.getMidfielderCount());
        assertEquals(2, HandballFormation.H_6_0.getAttackerCount());
    }

    @Test
    void h_3_3_has_most_attackers() {
        assertEquals(3, HandballFormation.H_3_3.getAttackerCount());
        assertEquals(1, HandballFormation.H_3_3.getDefenderCount());
    }

    @Test
    void all_5_formations_exist() {
        assertEquals(5, HandballFormation.values().length);
    }

    @ParameterizedTest
    @EnumSource(HandballFormation.class)
    void all_slots_are_valid_handball_positions(HandballFormation f) {
        List<Position> slots = f.getPositionSlots();
        for (Position slot : slots) {
            assertInstanceOf(HandballPosition.class, slot,
                    f.name() + " contains a non-handball position");
        }
    }
}
