package com.sportsmanager.core;

/**
 * Simulates a sports match.
 *
 * Two modes of use:
 *
 * 1. One-shot (backward-compatible):
 *    {@code MatchResult result = engine.simulateMatch(home, away);}
 *
 * 2. Period-by-period (for interactive UI):
 *    {@code MatchState state = engine.initMatch(home, away);}
 *    {@code // while !state.isMatchOver():}
 *    {@code //   engine.simulatePeriod(state, home, away);}
 *    {@code //   // UI pauses here — user may make substitutions / tactic changes}
 *    {@code MatchResult result = engine.finalizeMatch(state);}
 *
 *    Or skip straight to the end at any point:
 *    {@code MatchResult result = engine.simulateToEnd(state, home, away);}
 */
public interface MatchEngine {

    // ── One-shot ──────────────────────────────────────────────────────────────

    MatchResult simulateMatch(Team home, Team away);

    // ── Period-by-period ──────────────────────────────────────────────────────

    /**
     * Creates and initialises a MatchState for the given match.
     * The field-player lists are populated from each team's available players.
     * No simulation is run yet.
     */
    MatchState initMatch(Team home, Team away);

    /**
     * Simulates the current period (from currentMinute to the end of the period).
     * Sets periodOver=true when the period finishes.
     * Sets matchOver=true if there are no more periods.
     *
     * Call this once per period. Between calls the UI may:
     *   - inspect state.getEvents()
     *   - call state.makeSubstitution(...)
     *   - change team tactic
     */
    void simulatePeriod(MatchState state, Team home, Team away);

    /**
     * Converts a finished MatchState into a MatchResult.
     * Should only be called when state.isMatchOver() == true.
     */
    MatchResult finalizeMatch(MatchState state);

    /**
     * Simulates all remaining periods to completion, then returns the result.
     * Convenience method — equivalent to looping simulatePeriod until matchOver,
     * then calling finalizeMatch.
     */
    default MatchResult simulateToEnd(MatchState state, Team home, Team away) {
        while (!state.isMatchOver()) {
            simulatePeriod(state, home, away);
        }
        return finalizeMatch(state);
    }
}
