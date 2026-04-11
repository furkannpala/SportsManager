package com.sportsmanager.core;

import com.sportsmanager.football.FootballMatchEvent;
import com.sportsmanager.football.FootballEventType;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchStateTest {

    private static final String HOME_ID = "home";
    private static final String AWAY_ID = "away";
    private static final int MAX_SUBS = 5;

    private MatchState state;

    private FootballPlayer outfield(String name) {
        return FootballPlayer.createOutfield(name, 25, FootballPosition.CENTRAL_MIDFIELDER,
                70, 70, 70, 70, 70, 70);
    }

    @BeforeEach
    void setUp() {
        state = new MatchState(2, HOME_ID, AWAY_ID);
    }

    // ── Valid home substitution ───────────────────────────────────────────
    @Test
    void makeSubstitution_valid_returnsTrue() {
        FootballPlayer out = outfield("Out");
        FootballPlayer in  = outfield("In");
        state.getHomeFieldPlayers().add(out);

        boolean result = state.makeSubstitution(HOME_ID, out, in, MAX_SUBS);

        assertTrue(result);
        assertFalse(state.getHomeFieldPlayers().contains(out));
        assertTrue(state.getHomeFieldPlayers().contains(in));
        assertEquals(1, state.getHomeSubsUsed());
    }

    // ── Max subs reached ─────────────────────────────────────────────────
    @Test
    void makeSubstitution_maxSubsReached_returnsFalse() {
        for (int i = 0; i < MAX_SUBS; i++) {
            FootballPlayer o = outfield("Out" + i);
            FootballPlayer n = outfield("In" + i);
            state.getHomeFieldPlayers().add(o);
            state.makeSubstitution(HOME_ID, o, n, MAX_SUBS);
        }
        FootballPlayer extraOut = outfield("ExtraOut");
        FootballPlayer extraIn  = outfield("ExtraIn");
        state.getHomeFieldPlayers().add(extraOut);

        boolean result = state.makeSubstitution(HOME_ID, extraOut, extraIn, MAX_SUBS);

        assertFalse(result);
    }

    // ── Out player not on pitch ───────────────────────────────────────────
    @Test
    void makeSubstitution_outPlayerNotOnPitch_returnsFalse() {
        FootballPlayer notOnField = outfield("Ghost");
        FootballPlayer in = outfield("In");

        boolean result = state.makeSubstitution(HOME_ID, notOnField, in, MAX_SUBS);

        assertFalse(result);
    }

    // ── In player is injured ─────────────────────────────────────────────
    @Test
    void makeSubstitution_inPlayerInjured_returnsFalse() {
        FootballPlayer out = outfield("Out");
        FootballPlayer in  = outfield("Injured");
        in.applyInjury(2);
        state.getHomeFieldPlayers().add(out);

        boolean result = state.makeSubstitution(HOME_ID, out, in, MAX_SUBS);

        assertFalse(result);
    }

    // ── In player already on pitch ───────────────────────────────────────
    @Test
    void makeSubstitution_inPlayerAlreadyOnPitch_returnsFalse() {
        FootballPlayer out = outfield("Out");
        FootballPlayer in  = outfield("AlreadyOn");
        state.getHomeFieldPlayers().add(out);
        state.getHomeFieldPlayers().add(in);

        boolean result = state.makeSubstitution(HOME_ID, out, in, MAX_SUBS);

        assertFalse(result);
    }

    // ── Wrong team ID ────────────────────────────────────────────────────
    @Test
    void makeSubstitution_wrongTeamId_returnsFalse() {
        FootballPlayer out = outfield("Out");
        FootballPlayer in  = outfield("In");
        state.getHomeFieldPlayers().add(out);

        boolean result = state.makeSubstitution("unknown", out, in, MAX_SUBS);

        assertFalse(result);
    }

    // ── Valid away substitution ───────────────────────────────────────────
    @Test
    void makeSubstitution_awayTeam_valid() {
        FootballPlayer out = outfield("AwayOut");
        FootballPlayer in  = outfield("AwayIn");
        state.getAwayFieldPlayers().add(out);

        boolean result = state.makeSubstitution(AWAY_ID, out, in, MAX_SUBS);

        assertTrue(result);
        assertEquals(1, state.getAwaySubsUsed());
        assertTrue(state.getAwayFieldPlayers().contains(in));
    }

    // ── decrementHomeActivePlayers clamped at 1 ───────────────────────────
    @Test
    void decrementHomeActivePlayers_clampedAt1() {
        state.setHomeActivePlayers(1);
        state.decrementHomeActivePlayers();
        assertEquals(1, state.getHomeActivePlayers());
    }

    // ── incrementHomeScore works ─────────────────────────────────────────
    @Test
    void incrementScore_works() {
        state.incrementHomeScore();
        state.incrementHomeScore();
        state.incrementHomeScore();
        assertEquals(3, state.getHomeScore());
    }

    // ── getEvents returns unmodifiable list ──────────────────────────────
    @Test
    void addEvent_getEvents_unmodifiable() {
        FootballPlayer p = outfield("P");
        state.addEvent(new FootballMatchEvent(FootballEventType.FOUL, 10, p, HOME_ID));

        assertThrows(UnsupportedOperationException.class,
                () -> state.getEvents().add(
                        new FootballMatchEvent(FootballEventType.FOUL, 20, p, HOME_ID)));
    }
}
