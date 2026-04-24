package com.sportsmanager.football;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FootballPlayerTest {

    // ── Overall rating uses weighted attributes (STRIKER) ─────────────────
    @Test
    void createOutfield_overallRating_usesWeightedAttributes() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "Test", 25, FootballPosition.STRIKER,
                80, 90, 70, 85, 30, 75);
        // STRIKER weights (FootballPosition.STRIKER): shooting 0.35, pace 0.25, physical 0.15, dribbling 0.15, passing 0.05, defending 0.05
        int expected = (int) Math.round(80*0.25 + 90*0.35 + 70*0.05 + 85*0.15 + 30*0.05 + 75*0.15);
        assertEquals(expected, p.getOverallRating());
    }

    // ── Goalkeeper overall rating uses GK weights ─────────────────────────
    @Test
    void createGoalkeeper_overallRating_usesGkWeights() {
        FootballPlayer gk = FootballPlayer.createGoalkeeper(
                "GK", 28, 60, 85, 80, 70, 90, 75, 72);
        // GK weights: reflexes 0.30, diving 0.25, handling 0.20, positioning 0.15, pace 0.05, kicking 0.05
        int expected = (int) Math.round(60*0.05 + 85*0.25 + 80*0.20 + 70*0.05 + 90*0.30 + 75*0.15);
        assertEquals(expected, gk.getOverallRating());
    }

    // ── getAttributeValue returns correct outfield values ─────────────────
    @Test
    void getAttributeValue_outfield_returnsCorrectValues() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "CB", 24, FootballPosition.CENTRE_BACK,
                70, 40, 65, 55, 80, 75);
        assertEquals(70, p.getAttributeValue("pace"));
        assertEquals(40, p.getAttributeValue("shooting"));
        assertEquals(65, p.getAttributeValue("passing"));
        assertEquals(55, p.getAttributeValue("dribbling"));
        assertEquals(80, p.getAttributeValue("defending"));
        assertEquals(75, p.getAttributeValue("physical"));
    }

    // ── GK attributes accessible; outfield attributes return 0 for GK ─────
    @Test
    void getAttributeValue_goalkeeper_returnsGkAttributes() {
        FootballPlayer gk = FootballPlayer.createGoalkeeper(
                "GK", 30, 55, 88, 82, 72, 91, 78, 70);
        assertEquals(88, gk.getAttributeValue("diving"));
        assertEquals(91, gk.getAttributeValue("reflexes"));
        assertEquals(0, gk.getAttributeValue("shooting")); // outfield attr not set for GK
    }

    // ── Unknown attribute returns 0 ──────────────────────────────────────
    @Test
    void getAttributeValue_unknownAttribute_returnsZero() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "P", 22, FootballPosition.STRIKER, 70, 70, 70, 70, 70, 70);
        assertEquals(0, p.getAttributeValue("nonexistent"));
    }

    // ── increaseAttribute is clamped at 100 ───────────────────────────────
    @Test
    void increaseAttribute_clampedAt100() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "P", 22, FootballPosition.STRIKER, 95, 70, 70, 70, 70, 70);
        p.increaseAttribute("pace", 10);
        assertEquals(100, p.getAttributeValue("pace"));
    }

    // ── increaseAttribute normal gain ────────────────────────────────────
    @Test
    void increaseAttribute_normalGain() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "P", 22, FootballPosition.STRIKER, 70, 60, 70, 70, 70, 70);
        p.increaseAttribute("shooting", 5);
        assertEquals(65, p.getAttributeValue("shooting"));
    }

    // ── applyInjury sets injured state ───────────────────────────────────
    @Test
    void applyInjury_setsInjuredAndGamesRemaining() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "P", 22, FootballPosition.STRIKER, 70, 70, 70, 70, 70, 70);
        p.applyInjury(3);
        assertFalse(p.isAvailable());
        assertEquals(3, p.getInjuryGamesRemaining());
        assertTrue(p.isInjured());
    }

    // ── decrementInjury recovers after last game ──────────────────────────
    @Test
    void decrementInjury_recoverAfterLastGame() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "P", 22, FootballPosition.STRIKER, 70, 70, 70, 70, 70, 70);
        p.applyInjury(1);
        p.decrementInjury();
        assertTrue(p.isAvailable());
        assertFalse(p.isInjured());
        assertEquals(0, p.getInjuryGamesRemaining());
    }

    // ── decrementInjury counts down ─────────────────────────────────────
    @Test
    void decrementInjury_multipleGames_countsDown() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "P", 22, FootballPosition.STRIKER, 70, 70, 70, 70, 70, 70);
        p.applyInjury(3);
        p.decrementInjury();
        assertEquals(2, p.getInjuryGamesRemaining());
        assertFalse(p.isAvailable());
    }

    // ── applyInjury(0) is ignored ───────────────────────────────────────
    @Test
    void applyInjury_zeroOrNegative_ignored() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "P", 22, FootballPosition.STRIKER, 70, 70, 70, 70, 70, 70);
        p.applyInjury(0);
        assertTrue(p.isAvailable());
    }

    // ── getEmergencyGoalkeeperRating uses weighted formula ───────────────
    @Test
    void getEmergencyGoalkeeperRating_usesWeightedFormula() {
        // pace=80, defending=40, physical=60
        // formula: (defending*0.5 + physical*0.3 + pace*0.2) * 0.6
        //        = (40*0.5 + 60*0.3 + 80*0.2) * 0.6 = (20+18+16)*0.6 = 54*0.6 = 32.4 → 32
        FootballPlayer p = FootballPlayer.createOutfield(
                "P", 25, FootballPosition.STRIKER, 80, 70, 60, 75, 40, 60);
        int expected = (int) Math.round((40 * 0.5 + 60 * 0.3 + 80 * 0.2) * 0.6);
        assertEquals(expected, p.getEmergencyGoalkeeperRating());
    }

    // ── getEmergencyGoalkeeperRating minimum is 1 ───────────────────────
    @Test
    void getEmergencyGoalkeeperRating_clampedAtMinimum() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "P", 22, FootballPosition.STRIKER, 1, 1, 1, 1, 1, 1);
        assertTrue(p.getEmergencyGoalkeeperRating() >= 1);
    }

    // ── createOutfield clamps attributes ────────────────────────────────
    @Test
    void createOutfield_attributesClamped() {
        FootballPlayer p = FootballPlayer.createOutfield(
                "P", 22, FootballPosition.STRIKER, 150, -5, 70, 70, 70, 70);
        assertEquals(100, p.getAttributeValue("pace"));
        assertEquals(1,   p.getAttributeValue("shooting"));
    }
}
