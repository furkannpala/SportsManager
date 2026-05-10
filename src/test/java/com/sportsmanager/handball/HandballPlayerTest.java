package com.sportsmanager.handball;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandballPlayerTest {

    private HandballPlayer outfield() {
        return HandballPlayer.createOutfield("Kerem", 23, HandballPosition.LEFT_BACK,
                70, 75, 65, 68, 60, 72);
    }

    private HandballPlayer goalkeeper() {
        return HandballPlayer.createGoalkeeper("Oğuz", 28,
                60, 80, 78, 75, 72, 65);
    }

    @Test
    void outfield_overall_rating_is_in_valid_range() {
        HandballPlayer p = outfield();
        int ovr = p.getOverallRating();
        assertTrue(ovr >= 1 && ovr <= 100, "OVR out of range: " + ovr);
    }

    @Test
    void goalkeeper_overall_uses_gk_weighted_attributes() {
        HandballPlayer gk = goalkeeper();
        int ovr = gk.getOverallRating();
        assertTrue(ovr >= 1 && ovr <= 100, "GK OVR out of range: " + ovr);
    }

    @Test
    void outfield_attributes_return_correct_values() {
        HandballPlayer p = outfield();
        assertAll(
                () -> assertEquals(70, p.getSpeed()),
                () -> assertEquals(75, p.getThrowing()),
                () -> assertEquals(65, p.getJumping()),
                () -> assertEquals(68, p.getAgility()),
                () -> assertEquals(60, p.getDefending()),
                () -> assertEquals(72, p.getPhysical())
        );
    }

    @Test
    void goalkeeper_attributes_return_correct_values() {
        HandballPlayer gk = goalkeeper();
        assertAll(
                () -> assertEquals(60, gk.getSpeed()),
                () -> assertEquals(80, gk.getReflexes()),
                () -> assertEquals(78, gk.getDiving()),
                () -> assertEquals(75, gk.getReach()),
                () -> assertEquals(72, gk.getPositioning()),
                () -> assertEquals(65, gk.getPhysical())
        );
    }

    @Test
    void unknown_attribute_returns_zero() {
        assertEquals(0, outfield().getAttributeValue("nonexistent"));
        assertEquals(0, goalkeeper().getAttributeValue("pace"));
    }

    @Test
    void increase_attribute_clamps_at_100() {
        HandballPlayer p = outfield();
        p.increaseAttribute("throwing", 999);
        assertEquals(100, p.getThrowing());
    }

    @Test
    void increase_attribute_applies_normal_gain() {
        HandballPlayer p = outfield();
        p.increaseAttribute("throwing", 5);
        assertEquals(80, p.getThrowing());
    }

    @Test
    void apply_injury_marks_player_unavailable() {
        HandballPlayer p = outfield();
        p.applyInjury(3);
        assertTrue(p.isInjured());
        assertEquals(3, p.getInjuryGamesRemaining());
        assertFalse(p.isAvailable());
    }

    @Test
    void decrement_injury_recovers_player_after_last_game() {
        HandballPlayer p = outfield();
        p.applyInjury(1);
        p.decrementInjury();
        assertFalse(p.isInjured());
        assertTrue(p.isAvailable());
    }

    @Test
    void apply_injury_with_zero_games_is_ignored() {
        HandballPlayer p = outfield();
        p.applyInjury(0);
        assertFalse(p.isInjured());
        assertTrue(p.isAvailable());
    }

    @Test
    void apply_suspension_marks_player_unavailable() {
        HandballPlayer p = outfield();
        p.applySuspension(2);
        assertTrue(p.isSuspended());
        assertFalse(p.isAvailable());
    }

    @Test
    void effective_overall_at_natural_position_equals_full_ovr() {
        HandballPlayer p = outfield();
        assertEquals(p.getOverallRating(), p.getEffectiveOverall(HandballPosition.LEFT_BACK));
    }

    @Test
    void effective_overall_with_goalkeeper_mismatch_is_25() {
        HandballPlayer p = outfield();
        assertEquals(25, p.getEffectiveOverall(HandballPosition.GOALKEEPER));
    }

    @Test
    void effective_overall_out_of_position_is_lower_than_natural() {
        HandballPlayer p = outfield();
        assertTrue(p.getEffectiveOverall(HandballPosition.PIVOT) < p.getOverallRating());
    }

    @Test
    void is_out_of_position_detects_mismatch() {
        HandballPlayer p = outfield();
        assertTrue(p.isOutOfPosition(HandballPosition.PIVOT));
        assertFalse(p.isOutOfPosition(HandballPosition.LEFT_BACK));
    }

    @Test
    void form_drifts_toward_neutral_from_above() {
        HandballPlayer p = outfield();
        p.setForm(9.5);
        p.driftForm();
        assertTrue(p.getForm() < 9.5);
    }

    @Test
    void form_drifts_toward_neutral_from_below() {
        HandballPlayer p = outfield();
        p.setForm(5.5);
        p.driftForm();
        assertTrue(p.getForm() > 5.5);
    }

    @Test
    void stamina_drains_and_recovers() {
        HandballPlayer p = outfield();
        p.drainStamina(30.0);
        assertTrue(p.getCurrentStamina() < 100);
        p.recoverStamina(30.0);
        assertEquals(100, p.getCurrentStamina());
    }

    @Test
    void reset_stamina_restores_to_full() {
        HandballPlayer p = outfield();
        p.drainStamina(50.0);
        p.resetStamina();
        assertEquals(100, p.getCurrentStamina());
    }
}
