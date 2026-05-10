package com.sportsmanager.handball;

import com.sportsmanager.core.TrainingCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandballSportTest {

    private final HandballSport sport = new HandballSport();

    @Test
    void returns_7_positions() {
        assertEquals(7, sport.getPositions().size());
    }

    @Test
    void returns_5_formations() {
        assertEquals(5, sport.getFormations().size());
    }

    @Test
    void returns_4_tactics() {
        assertEquals(4, sport.getTactics().size());
    }

    @Test
    void returns_4_training_sessions() {
        assertEquals(4, sport.getTrainingOptions().size());
    }

    @Test
    void outfield_attributes_include_throwing_and_has_6_entries() {
        List<String> attrs = sport.getPlayerAttributeNames();
        assertEquals(6, attrs.size());
        assertTrue(attrs.contains("throwing"));
        assertTrue(attrs.contains("speed"));
    }

    @Test
    void goalkeeper_attributes_include_reflexes_and_reach() {
        List<String> gkAttrs = sport.getAttributeNamesForPosition(HandballPosition.GOALKEEPER);
        assertTrue(gkAttrs.contains("reflexes"));
        assertTrue(gkAttrs.contains("reach"));
        assertFalse(gkAttrs.contains("throwing"));
    }

    @Test
    void creates_non_null_handball_match_engine() {
        assertNotNull(sport.createMatchEngine());
        assertInstanceOf(HandballMatchEngine.class, sport.createMatchEngine());
    }

    @Test
    void period_count_is_2_and_duration_is_30_minutes() {
        assertEquals(2, sport.getMatchPeriodCount());
        assertEquals(30, sport.getMatchPeriodDurationMinutes());
    }

    @Test
    void max_substitutions_is_5() {
        assertEquals(5, sport.getMaxSubstitutions());
    }

    @Test
    void starting_lineup_size_is_7() {
        assertEquals(7, sport.getStartingLineupSize());
    }

    @Test
    void attacking_training_category_includes_throwing() {
        List<String> attrs = sport.getAttributesForCategory(TrainingCategory.ATTACKING);
        assertTrue(attrs.contains("throwing"));
    }

    @Test
    void defending_training_category_includes_reflexes() {
        List<String> attrs = sport.getAttributesForCategory(TrainingCategory.DEFENDING);
        assertTrue(attrs.contains("reflexes"));
        assertTrue(attrs.contains("defending"));
    }

    @Test
    void sport_name_is_handball() {
        assertEquals("Handball", sport.getSportName());
    }
}
