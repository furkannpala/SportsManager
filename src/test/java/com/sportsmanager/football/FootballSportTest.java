package com.sportsmanager.football;

import com.sportsmanager.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FootballSportTest {

    private FootballSport sport;

    @BeforeEach
    void setUp() {
        sport = new FootballSport();
    }

    // ── getPositions returns 13 positions ─────────────────────────────────
    @Test
    void getPositions_returns13() {
        assertEquals(13, sport.getPositions().size());
    }

    // ── getFormations returns 29 formations ───────────────────────────────
    @Test
    void getFormations_returns29() {
        assertEquals(29, sport.getFormations().size());
    }

    // ── getTactics returns 4 tactics ─────────────────────────────────────
    @Test
    void getTactics_returns4() {
        assertEquals(4, sport.getTactics().size());
    }

    // ── getTrainingOptions returns 4 sessions ─────────────────────────────
    @Test
    void getTrainingOptions_returns4() {
        assertEquals(4, sport.getTrainingOptions().size());
    }

    // ── getPlayerAttributeNames returns 6 outfield attributes ────────────
    @Test
    void getPlayerAttributeNames_returns6AndContainsPace() {
        var names = sport.getPlayerAttributeNames();
        assertEquals(6, names.size());
        assertTrue(names.contains("pace"));
        assertTrue(names.contains("shooting"));
    }

    // ── createMatchEngine returns a non-null FootballMatchEngine ──────────
    @Test
    void createMatchEngine_returnsNonNull() {
        MatchEngine engine = sport.createMatchEngine();
        assertNotNull(engine);
        assertInstanceOf(FootballMatchEngine.class, engine);
    }

    // ── Period constants are correct ─────────────────────────────────────
    @Test
    void periodConstants_areCorrect() {
        assertEquals(2,  sport.getMatchPeriodCount());
        assertEquals(45, sport.getMatchPeriodDurationMinutes());
    }

    // ── getMaxSubstitutions returns 5 ────────────────────────────────────
    @Test
    void getMaxSubstitutions_returns5() {
        assertEquals(5, sport.getMaxSubstitutions());
    }
}
