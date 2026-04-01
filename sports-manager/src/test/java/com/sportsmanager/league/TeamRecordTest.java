package com.sportsmanager.league;

import com.sportsmanager.core.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeamRecordTest {

    private TeamRecord record;
    private Team opponent;

    @BeforeEach
    void setUp() {
        record   = new TeamRecord();
        opponent = new Team("O", "Opponent");
    }

    @Test
    void initialStateIsAllZero() {
        assertEquals(0, record.getPlayed());
        assertEquals(0, record.getWon());
        assertEquals(0, record.getDrawn());
        assertEquals(0, record.getLost());
        assertEquals(0, record.getGoalsFor());
        assertEquals(0, record.getGoalsAgainst());
        assertEquals(0, record.getPoints());
        assertEquals(0, record.getGoalDifference());
    }

    @Test
    void incrementsWorkCorrectly() {
        record.incrementPlayed();
        record.incrementWins();
        record.addGoals(2, 1);
        record.addPoints(3);

        assertEquals(1, record.getPlayed());
        assertEquals(1, record.getWon());
        assertEquals(2, record.getGoalsFor());
        assertEquals(1, record.getGoalsAgainst());
        assertEquals(1, record.getGoalDifference());
        assertEquals(3, record.getPoints());
    }

    @Test
    void h2hWinGives3Points() {
        record.addH2H(opponent, 2, 0);
        assertEquals(3, record.getH2HPoints(opponent));
        assertEquals(2, record.getH2HGoalDifference(opponent));
    }

    @Test
    void h2hDrawGives1Point() {
        record.addH2H(opponent, 1, 1);
        assertEquals(1, record.getH2HPoints(opponent));
        assertEquals(0, record.getH2HGoalDifference(opponent));
    }

    @Test
    void h2hLossGives0Points() {
        record.addH2H(opponent, 0, 2);
        assertEquals(0, record.getH2HPoints(opponent));
        assertEquals(-2, record.getH2HGoalDifference(opponent));
    }

    @Test
    void h2hAccumulatesOverMultipleMatches() {
        record.addH2H(opponent, 2, 1); // win: 3pts, GD +1
        record.addH2H(opponent, 0, 1); // loss: 0pts, GD -1
        assertEquals(3, record.getH2HPoints(opponent));
        assertEquals(0, record.getH2HGoalDifference(opponent));
    }

    @Test
    void h2hAgainstUnknownOpponentReturns0() {
        Team unknown = new Team("X", "Unknown");
        assertEquals(0, record.getH2HPoints(unknown));
        assertEquals(0, record.getH2HGoalDifference(unknown));
    }

    @Test
    void resetClearsEverything() {
        record.incrementPlayed();
        record.incrementWins();
        record.addGoals(3, 1);
        record.addPoints(3);
        record.addH2H(opponent, 3, 1);

        record.reset();

        assertEquals(0, record.getPlayed());
        assertEquals(0, record.getPoints());
        assertEquals(0, record.getGoalsFor());
        assertEquals(0, record.getH2HPoints(opponent));
    }
}
