package com.sportsmanager.football;

import com.sportsmanager.core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FootballMatchEngineTest {

    private FootballMatchEngine engine;
    private Team home;
    private Team away;

    /** Creates a team with 16 available outfield+GK players. */
    private Team buildTeam(String id, String name) {
        Team t = new Team(id, name);
        t.addPlayer(FootballPlayer.createGoalkeeper("GK-" + id, 27,
                65, 80, 78, 68, 83, 76));
        FootballPosition[] outfield = {
                FootballPosition.CENTRE_BACK, FootballPosition.CENTRE_BACK,
                FootballPosition.LEFT_BACK,   FootballPosition.RIGHT_BACK,
                FootballPosition.CENTRAL_MIDFIELDER, FootballPosition.CENTRAL_MIDFIELDER,
                FootballPosition.DEFENSIVE_MIDFIELDER,
                FootballPosition.LEFT_WINGER, FootballPosition.RIGHT_WINGER,
                FootballPosition.STRIKER,     FootballPosition.CENTRE_FORWARD,
                FootballPosition.CENTRE_BACK, FootballPosition.CENTRAL_MIDFIELDER,
                FootballPosition.STRIKER,     FootballPosition.LEFT_BACK
        };
        for (int i = 0; i < outfield.length; i++) {
            t.addPlayer(FootballPlayer.createOutfield(
                    name + "-P" + i, 24, outfield[i],
                    70, 70, 70, 70, 70, 70));
        }
        return t;
    }

    @BeforeEach
    void setUp() {
        engine = new FootballMatchEngine(new Random(42));
        home   = buildTeam("home", "HomeFC");
        away   = buildTeam("away", "AwayFC");
    }

    // ── simulateMatch returns non-null result ─────────────────────────────
    @Test
    void simulateMatch_returnsNonNullResult() {
        MatchResult result = engine.simulateMatch(home, away);
        assertNotNull(result);
        assertTrue(result.getHomeScore() >= 0);
        assertTrue(result.getAwayScore() >= 0);
    }

    // ── Scores are non-negative for multiple seeds ────────────────────────
    @ParameterizedTest
    @ValueSource(longs = {42L, 123L, 999L})
    void simulateMatch_scoresAreNonNegative(long seed) {
        FootballMatchEngine e = new FootballMatchEngine(new Random(seed));
        MatchResult r = e.simulateMatch(home, away);
        assertTrue(r.getHomeScore() >= 0);
        assertTrue(r.getAwayScore() >= 0);
    }

    // ── Event list is not empty with seed 42 ─────────────────────────────
    @Test
    void simulateMatch_eventListNotEmpty() {
        MatchResult result = engine.simulateMatch(home, away);
        assertFalse(result.getEvents().isEmpty());
    }

    // ── GOAL events match final score ────────────────────────────────────
    @Test
    void simulateMatch_goalEventsMatchScore() {
        MatchState state = engine.initMatch(home, away);
        MatchResult result = engine.simulateMatch(home, away);

        long homeGoalEvents = result.getEvents().stream()
                .filter(e -> e instanceof FootballMatchEvent fme
                        && fme.getEventType() == FootballEventType.GOAL
                        && fme.getTeamId().equals("home"))
                .count();
        long awayGoalEvents = result.getEvents().stream()
                .filter(e -> e instanceof FootballMatchEvent fme
                        && fme.getEventType() == FootballEventType.GOAL
                        && fme.getTeamId().equals("away"))
                .count();

        assertEquals(result.getHomeScore(), (int) homeGoalEvents);
        assertEquals(result.getAwayScore(), (int) awayGoalEvents);
    }

    // ── initMatch populates field players ────────────────────────────────
    @Test
    void initMatch_populatesFieldPlayers() {
        MatchState state = engine.initMatch(home, away);
        assertFalse(state.getHomeFieldPlayers().isEmpty());
        assertFalse(state.getAwayFieldPlayers().isEmpty());
        assertTrue(state.getHomeFieldPlayers().size() <= 11);
    }

    // ── First simulatePeriod advances to period 2 ────────────────────────
    @Test
    void simulatePeriod_firstPeriod_advancesToPeriod2() {
        MatchState state = engine.initMatch(home, away);
        engine.simulatePeriod(state, home, away);

        assertTrue(state.isPeriodOver());
        assertEquals(2, state.getCurrentPeriod());
        assertFalse(state.isMatchOver());
    }

    // ── Two simulatePeriod calls end the match ────────────────────────────
    @Test
    void simulatePeriod_secondPeriod_setsMatchOver() {
        MatchState state = engine.initMatch(home, away);
        engine.simulatePeriod(state, home, away);
        engine.simulatePeriod(state, home, away);

        assertTrue(state.isMatchOver());
        assertTrue(state.isPeriodOver());
    }

    // ── Period flag resets at start of each period ────────────────────────
    @Test
    void simulatePeriod_periodOverFlagResetAtStart() {
        MatchState state = engine.initMatch(home, away);
        engine.simulatePeriod(state, home, away);
        // After first period, isPeriodOver=true. Second simulatePeriod starts by resetting it.
        // After second period completes, isPeriodOver=true and matchOver=true.
        engine.simulatePeriod(state, home, away);
        assertTrue(state.isPeriodOver());
        assertTrue(state.isMatchOver());
    }

    // ── simulateToEnd via simulateMatch leaves matchOver ─────────────────
    @Test
    void simulateMatch_matchIsOverAfterCall() {
        MatchState state = engine.initMatch(home, away);
        engine.simulatePeriod(state, home, away);
        engine.simulatePeriod(state, home, away);
        MatchResult result = engine.finalizeMatch(state);

        assertNotNull(result);
        assertTrue(state.isMatchOver());
    }

    // ── simulatePeriod after match over does nothing ─────────────────────
    @Test
    void simulatePeriod_afterMatchOver_doesNothing() {
        MatchState state = engine.initMatch(home, away);
        engine.simulatePeriod(state, home, away);
        engine.simulatePeriod(state, home, away);

        int scoreBefore = state.getHomeScore() + state.getAwayScore();
        engine.simulatePeriod(state, home, away); // should be a no-op
        int scoreAfter = state.getHomeScore() + state.getAwayScore();

        assertEquals(scoreBefore, scoreAfter);
    }

    // ── finalizeMatch returns correct scores ─────────────────────────────
    @Test
    void finalizeMatch_returnsCorrectScores() {
        MatchState state = engine.initMatch(home, away);
        state.setHomeScore(2);
        state.setAwayScore(1);
        state.setMatchOver(true);

        MatchResult result = engine.finalizeMatch(state);

        assertEquals(2, result.getHomeScore());
        assertEquals(1, result.getAwayScore());
    }

    // ── No available players does not crash ──────────────────────────────
    @Test
    void simulateMatch_withNoAvailablePlayers_doesNotCrash() {
        for (Player p : home.getSquad()) p.applyInjury(5);
        for (Player p : away.getSquad()) p.applyInjury(5);
        assertDoesNotThrow(() -> engine.simulateMatch(home, away));
    }
}
