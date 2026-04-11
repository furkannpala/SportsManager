package com.sportsmanager.football;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Position;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FootballPlayerFactoryTest {

    // ── createPlayer with GOALKEEPER position creates a goalkeeper ────────
    @Test
    void createPlayer_goalkeeper_positionIsGoalkeeper() {
        FootballPlayerFactory factory = new FootballPlayerFactory(new Random(42));
        Player p = factory.createPlayer("GK", 25, FootballPosition.GOALKEEPER, 70);

        assertInstanceOf(FootballPlayer.class, p);
        assertEquals(FootballPosition.GOALKEEPER, ((FootballPlayer) p).getPosition());
    }

    // ── createPlayer with outfield position creates correct position ──────
    @Test
    void createPlayer_striker_positionIsStriker() {
        FootballPlayerFactory factory = new FootballPlayerFactory(new Random(42));
        Player p = factory.createPlayer("ST", 22, FootballPosition.STRIKER, 70);

        assertInstanceOf(FootballPlayer.class, p);
        assertEquals(FootballPosition.STRIKER, ((FootballPlayer) p).getPosition());
    }

    // ── All generated attributes are within [1, 100] ─────────────────────
    @Test
    void createPlayer_allAttributesWithinBounds() {
        FootballPlayerFactory factory = new FootballPlayerFactory(new Random(99));
        List<String> attrs = List.of("pace", "shooting", "passing", "dribbling", "defending", "physical");

        for (FootballPosition pos : FootballPosition.values()) {
            int tierMean = (pos == FootballPosition.GOALKEEPER) ? 60 : 70;
            Player p = factory.createPlayer("P", 25, pos, tierMean);
            FootballPlayer fp = (FootballPlayer) p;

            if (pos == FootballPosition.GOALKEEPER) {
                for (String a : List.of("pace", "diving", "handling", "kicking", "reflexes", "positioning")) {
                    int v = fp.getAttributeValue(a);
                    assertTrue(v >= 1 && v <= 100, pos + "." + a + "=" + v + " out of range");
                }
            } else {
                for (String a : attrs) {
                    int v = fp.getAttributeValue(a);
                    assertTrue(v >= 1 && v <= 100, pos + "." + a + "=" + v + " out of range");
                }
            }
        }
    }

    // ── Same seed produces deterministic results ──────────────────────────
    @Test
    void createPlayer_sameSeed_isDeterministic() {
        FootballPlayerFactory f1 = new FootballPlayerFactory(new Random(7));
        FootballPlayerFactory f2 = new FootballPlayerFactory(new Random(7));

        Player p1 = f1.createPlayer("A", 24, FootballPosition.STRIKER, 70);
        Player p2 = f2.createPlayer("A", 24, FootballPosition.STRIKER, 70);

        assertEquals(p1.getOverallRating(), p2.getOverallRating());
    }

    // ── Non-FootballPosition falls back to STRIKER ────────────────────────
    @Test
    void createPlayer_unknownPosition_fallsBackToStriker() {
        FootballPlayerFactory factory = new FootballPlayerFactory(new Random(42));

        // anonymous non-FootballPosition implementation
        Position unknown = () -> "Unknown";
        Player p = factory.createPlayer("X", 20, unknown, 60);

        assertInstanceOf(FootballPlayer.class, p);
        assertEquals(FootballPosition.STRIKER, ((FootballPlayer) p).getPosition());
    }

    // ── High tierMean players outrate low tierMean players on average ─────
    @Test
    void createPlayer_highTierMeanProducesHigherRating() {
        // Use many samples to smooth Gaussian noise
        long sumHigh = 0, sumLow = 0;
        int samples = 100;
        for (int i = 0; i < samples; i++) {
            FootballPlayerFactory fH = new FootballPlayerFactory(new Random(i));
            FootballPlayerFactory fL = new FootballPlayerFactory(new Random(i));
            sumHigh += fH.createPlayer("H", 25, FootballPosition.STRIKER, 80).getOverallRating();
            sumLow  += fL.createPlayer("L", 25, FootballPosition.STRIKER, 40).getOverallRating();
        }
        assertTrue(sumHigh > sumLow, "Mean rating for tierMean=80 should exceed tierMean=40");
    }
}
