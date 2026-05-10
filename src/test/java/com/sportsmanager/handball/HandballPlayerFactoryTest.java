package com.sportsmanager.handball;

import com.sportsmanager.core.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HandballPlayerFactoryTest {

    private final HandballPlayerFactory factory = new HandballPlayerFactory(new Random(42));

    @Test
    void creates_goalkeeper_for_goalkeeper_position() {
        Player p = factory.createPlayer("Ali", 25, HandballPosition.GOALKEEPER, 70);
        assertInstanceOf(HandballPlayer.class, p);
        assertEquals(HandballPosition.GOALKEEPER, ((HandballPlayer) p).getPosition());
    }

    @Test
    void creates_outfield_player_for_center_back() {
        Player p = factory.createPlayer("Ege", 22, HandballPosition.CENTER_BACK, 65);
        assertInstanceOf(HandballPlayer.class, p);
        assertEquals(HandballPosition.CENTER_BACK, ((HandballPlayer) p).getPosition());
    }

    @ParameterizedTest
    @EnumSource(HandballPosition.class)
    void all_attributes_are_in_valid_range_for_every_position(HandballPosition pos) {
        HandballPlayer p = (HandballPlayer) factory.createPlayer("Test", 24, pos, 70);
        p.getPosition().getWeightedAttributes().keySet().forEach(attr -> {
            int val = p.getAttributeValue(attr);
            assertTrue(val >= 1 && val <= 100,
                    "Attribute '" + attr + "' out of range [1,100]: " + val + " for " + pos);
        });
    }

    @Test
    void unknown_position_falls_back_to_center_back() {
        Player p = factory.createPlayer("Y", 20, null, 60);
        assertInstanceOf(HandballPlayer.class, p);
        assertEquals(HandballPosition.CENTER_BACK, ((HandballPlayer) p).getPosition());
    }

    @Test
    void higher_tier_mean_produces_higher_average_overall() {
        int sum75 = 0, sum50 = 0;
        for (int i = 0; i < 50; i++) {
            sum75 += factory.createPlayer("A", 24, HandballPosition.CENTER_BACK, 75).getOverallRating();
            sum50 += factory.createPlayer("B", 24, HandballPosition.CENTER_BACK, 50).getOverallRating();
        }
        assertTrue(sum75 > sum50,
                "tier=75 avg should exceed tier=50 avg over 50 samples");
    }

    @Test
    void goalkeeper_has_reflexes_attribute() {
        HandballPlayer gk = (HandballPlayer) factory.createPlayer("GK", 26, HandballPosition.GOALKEEPER, 75);
        assertTrue(gk.getReflexes() >= 1 && gk.getReflexes() <= 100);
    }

    @Test
    void goalkeeper_reflexes_are_boosted_compared_to_speed() {
        HandballPlayerFactory fixed = new HandballPlayerFactory(new Random(1));
        int totalReflexes = 0, totalSpeed = 0;
        for (int i = 0; i < 100; i++) {
            HandballPlayer gk = (HandballPlayer) fixed.createPlayer("GK", 25, HandballPosition.GOALKEEPER, 60);
            totalReflexes += gk.getReflexes();
            totalSpeed    += gk.getSpeed();
        }
        assertTrue(totalReflexes > totalSpeed,
                "Goalkeeper reflexes should average higher than speed (1.20x vs 0.80x scale)");
    }

    @Test
    void player_name_and_age_are_preserved() {
        Player p = factory.createPlayer("TestName", 27, HandballPosition.PIVOT, 65);
        assertEquals("TestName", p.getName());
        assertEquals(27, p.getAge());
    }
}
