package com.sportsmanager.handball;

import com.sportsmanager.core.MatchEvent;
import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.MatchState;
import com.sportsmanager.core.Team;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HandballMatchEngineTest {

    private Team buildTeam(String id) {
        Team team = new Team(id, id);
        HandballPlayerFactory factory = new HandballPlayerFactory(new Random(42));
        team.addPlayer(factory.createPlayer("GK", 26, HandballPosition.GOALKEEPER, 70));
        HandballPosition[] outfield = {
            HandballPosition.LEFT_WING, HandballPosition.RIGHT_WING,
            HandballPosition.LEFT_BACK, HandballPosition.CENTER_BACK,
            HandballPosition.RIGHT_BACK, HandballPosition.PIVOT
        };
        for (int i = 0; i < 9; i++) {
            team.addPlayer(factory.createPlayer("P" + i, 24, outfield[i % outfield.length], 70));
        }
        team.setFormation(HandballFormation.H_6_0);
        team.setTactic(HandballTactic.BALANCED);
        return team;
    }

    @Test
    void simulate_match_returns_non_null_result() {
        HandballMatchEngine engine = new HandballMatchEngine(new Random(1));
        MatchResult result = engine.simulateMatch(buildTeam("home"), buildTeam("away"));
        assertNotNull(result);
    }

    @Test
    void scores_are_non_negative_across_multiple_seeds() {
        for (int seed = 0; seed < 5; seed++) {
            HandballMatchEngine engine = new HandballMatchEngine(new Random(seed));
            MatchResult r = engine.simulateMatch(buildTeam("H"), buildTeam("A"));
            assertTrue(r.getHomeScore() >= 0, "Home score negative for seed " + seed);
            assertTrue(r.getAwayScore() >= 0, "Away score negative for seed " + seed);
        }
    }

    @Test
    void event_list_is_not_empty_after_match() {
        HandballMatchEngine engine = new HandballMatchEngine(new Random(7));
        MatchResult result = engine.simulateMatch(buildTeam("H"), buildTeam("A"));
        assertFalse(result.getEvents().isEmpty());
    }

    @Test
    void goal_events_match_final_score() {
        HandballMatchEngine engine = new HandballMatchEngine(new Random(3));
        Team home = buildTeam("home");
        Team away = buildTeam("away");
        MatchResult result = engine.simulateMatch(home, away);

        long homeGoals = result.getEvents().stream()
                .filter(e -> e instanceof HandballMatchEvent hme
                        && hme.getEventType() == HandballEventType.GOAL
                        && "home".equals(hme.getTeamId()))
                .count();
        long awayGoals = result.getEvents().stream()
                .filter(e -> e instanceof HandballMatchEvent hme
                        && hme.getEventType() == HandballEventType.GOAL
                        && "away".equals(hme.getTeamId()))
                .count();

        assertEquals(result.getHomeScore(), (int) homeGoals,
                "Home GOAL events should match home score");
        assertEquals(result.getAwayScore(), (int) awayGoals,
                "Away GOAL events should match away score");
    }

    @Test
    void init_match_populates_field_players() {
        HandballMatchEngine engine = new HandballMatchEngine(new Random(1));
        Team home = buildTeam("H");
        Team away = buildTeam("A");
        MatchState state = engine.initMatch(home, away);

        assertFalse(state.getHomeFieldPlayers().isEmpty(),
                "Home field players should be populated");
        assertFalse(state.getAwayFieldPlayers().isEmpty(),
                "Away field players should be populated");
    }

    @Test
    void field_players_capped_at_seven_per_team() {
        HandballMatchEngine engine = new HandballMatchEngine(new Random(1));
        MatchState state = engine.initMatch(buildTeam("H"), buildTeam("A"));

        assertTrue(state.getHomeFieldPlayers().size() <= 7);
        assertTrue(state.getAwayFieldPlayers().size() <= 7);
    }

    @Test
    void period_advances_after_first_period_simulation() {
        HandballMatchEngine engine = new HandballMatchEngine(new Random(10));
        Team home = buildTeam("H");
        Team away = buildTeam("A");
        MatchState state = engine.initMatch(home, away);
        assertEquals(1, state.getCurrentPeriod());
        engine.simulatePeriod(state, home, away);
        assertTrue(state.getCurrentPeriod() >= 2 || state.isMatchOver(),
                "Should advance past period 1");
    }

    @Test
    void match_is_over_after_both_periods() {
        HandballMatchEngine engine = new HandballMatchEngine(new Random(5));
        Team home = buildTeam("H");
        Team away = buildTeam("A");
        MatchState state = engine.initMatch(home, away);
        engine.simulatePeriod(state, home, away);
        engine.simulatePeriod(state, home, away);
        assertTrue(state.isMatchOver());
    }

    @Test
    void finalize_match_returns_state_scores() {
        HandballMatchEngine engine = new HandballMatchEngine(new Random(5));
        Team home = buildTeam("H");
        Team away = buildTeam("A");
        MatchState state = engine.initMatch(home, away);
        engine.simulatePeriod(state, home, away);
        engine.simulatePeriod(state, home, away);
        MatchResult result = engine.finalizeMatch(state);

        assertEquals(state.getHomeScore(), result.getHomeScore());
        assertEquals(state.getAwayScore(), result.getAwayScore());
    }

    @Test
    void all_events_have_non_null_involved_player() {
        HandballMatchEngine engine = new HandballMatchEngine(new Random(99));
        MatchResult result = engine.simulateMatch(buildTeam("H"), buildTeam("A"));
        for (MatchEvent e : result.getEvents()) {
            assertNotNull(e.getInvolvedPlayer(),
                    "Event at minute " + e.getMinute() + " has null involvedPlayer");
        }
    }
}
