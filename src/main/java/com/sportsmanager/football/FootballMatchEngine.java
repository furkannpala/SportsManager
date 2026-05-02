package com.sportsmanager.football;

import com.sportsmanager.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Simulates a 90-minute football match minute by minute.
*/
public class FootballMatchEngine implements MatchEngine {

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final int    PERIODS          = 2;
    private static final int    PERIOD_DURATION  = 45;
    private static final double BASE_EVENT_PROB  = 0.18;  // per minute, per team
    private static final double POST_INJURY_PROB = 0.007; // fatigue injury after match (~1 per 3 matches total)

    // Event type thresholds (cumulative, per 90 min × 0.18 ≈ 16 events/team)
    // GOAL   : band 0.08  → ~1.3 goals/team
    // YELLOW : band 0.049 → ~0.8 yellows/team  (~1-2 per match total)
    // RED    : band 0.004 → very rare (~1 per 10+ matches)
    // INJURY : band 0.006 → very rare in-match
    // OFFSIDE: band 0.032 → ~2-3/team
    // FOUL   : band 0.062 → ~3-4/team
    // remainder → no loggable event
    private static final double T_GOAL        = 0.08;
    private static final double T_YELLOW      = 0.12;
    private static final double T_RED         = 0.13;
    private static final double T_INJURY      = 0.14;
    private static final double T_OFFSIDE     = 0.24;
    private static final double T_FOUL        = 0.32;

    private final Random random;

    public FootballMatchEngine() {
        this.random = new Random();
    }

    public FootballMatchEngine(Random random) {
        this.random = random;
    }

    // One-shot

    @Override
    public MatchResult simulateMatch(Team home, Team away) {
        MatchState state = initMatch(home, away);
        return simulateToEnd(state, home, away);
    }

    // Period-by-period

    private static final int FIELD_SIZE = 11;

    @Override
    public MatchState initMatch(Team home, Team away) {
        // Reset stamina for the entire squad so the new match starts fresh
        resetSquadStamina(home);
        resetSquadStamina(away);
        MatchState state = new MatchState(PERIODS, home.getTeamId(), away.getTeamId());
        distributeSquad(home.getAvailablePlayers(), state.getHomeFieldPlayers(), state.getHomeBenchPlayers());
        distributeSquad(away.getAvailablePlayers(), state.getAwayFieldPlayers(), state.getAwayBenchPlayers());
        return state;
    }

    private void resetSquadStamina(Team team) {
        for (Player p : team.getSquad()) {
            if (p instanceof FootballPlayer fp) fp.recoverStamina(25.0);
        }
    }

    private void distributeSquad(List<Player> available, List<Player> field, List<Player> bench) {
        for (int i = 0; i < available.size(); i++) {
            if (i < FIELD_SIZE) field.add(available.get(i));
            else                bench.add(available.get(i));
        }
    }

    @Override
    public void simulatePeriod(MatchState state, Team home, Team away) {
        if (state.isMatchOver()) return;
        state.setPeriodOver(false);
        while (!state.isPeriodOver() && !state.isMatchOver()) {
            simulateMinute(state, home, away);
        }
    }

    @Override
    public void simulateMinute(MatchState state, Team home, Team away) {
        if (state.isMatchOver() || state.isPeriodOver()) return;

        int period = state.getCurrentPeriod();
        int end    = period * PERIOD_DURATION;
        int minute = state.getCurrentMinute() + 1;

        // Guard: skip past minutes already simulated
        int start = (period - 1) * PERIOD_DURATION + 1;
        if (minute < start) minute = start;

        state.setCurrentMinute(minute);

        FootballTactic homeTactic  = resolveTactic(home);
        FootballTactic awayTactic  = resolveTactic(away);
        List<FootballPlayer> homeP = footballPlayers(state.getHomeFieldPlayers());
        List<FootballPlayer> awayP = footballPlayers(state.getAwayFieldPlayers());

        double homeAtk = attackStrength(homeP, homeTactic, state.getHomeActivePlayers());
        double awayAtk = attackStrength(awayP, awayTactic, state.getAwayActivePlayers());
        double homeDef = defenseStrength(homeP, homeTactic, state.getHomeActivePlayers());
        double awayDef = defenseStrength(awayP, awayTactic, state.getAwayActivePlayers());

        // Home attacks
        double homeProb = BASE_EVENT_PROB * (1 + homeTactic.getGoalProbabilityModifier())
                * strengthFactor(homeAtk, awayDef);
        if (random.nextDouble() < homeProb) {
            state.addHomePossessionTick();
            FootballMatchEvent e = generateEvent(minute, homeP, awayP, home.getTeamId(), homeAtk, awayDef, state, true);
            if (e != null) {
                state.addEvent(e);
                if (e.getEventType() == FootballEventType.GOAL)     { state.incrementHomeScore(); applyFormGoal(e); }
                if (e.getEventType() == FootballEventType.RED_CARD) { adjustForm(e.getInvolvedPlayer(), -0.8); e.getInvolvedPlayer().applySuspension(generateSuspensionDuration()); state.decrementHomeActivePlayers(); removeSendOffPlayer(e, state.getHomeFieldPlayers()); }
                if (e.getEventType() == FootballEventType.INJURY)   { adjustForm(e.getInvolvedPlayer(), -0.3); removeSendOffPlayer(e, state.getHomeFieldPlayers()); }
                if (e.getEventType() == FootballEventType.YELLOW_CARD) {
                    adjustForm(e.getInvolvedPlayer(), -0.2);
                    if (state.recordYellowCard(e.getInvolvedPlayer())) {
                        e.getInvolvedPlayer().applySuspension(generateSuspensionDuration());
                        FootballMatchEvent red = new FootballMatchEvent(
                                FootballEventType.RED_CARD, minute, e.getInvolvedPlayer(), home.getTeamId(), true);
                        state.addEvent(red);
                        state.decrementHomeActivePlayers();
                        removeSendOffPlayer(red, state.getHomeFieldPlayers());
                    }
                }
            }
        }

        // Away attacks
        double awayProb = BASE_EVENT_PROB * (1 + awayTactic.getGoalProbabilityModifier())
                * strengthFactor(awayAtk, homeDef);
        if (random.nextDouble() < awayProb) {
            state.addAwayPossessionTick();
            FootballMatchEvent e = generateEvent(minute, awayP, homeP, away.getTeamId(), awayAtk, homeDef, state, false);
            if (e != null) {
                state.addEvent(e);
                if (e.getEventType() == FootballEventType.GOAL)     { state.incrementAwayScore(); applyFormGoal(e); }
                if (e.getEventType() == FootballEventType.RED_CARD) { adjustForm(e.getInvolvedPlayer(), -0.8); e.getInvolvedPlayer().applySuspension(generateSuspensionDuration()); state.decrementAwayActivePlayers(); removeSendOffPlayer(e, state.getAwayFieldPlayers()); }
                if (e.getEventType() == FootballEventType.INJURY)   { adjustForm(e.getInvolvedPlayer(), -0.3); removeSendOffPlayer(e, state.getAwayFieldPlayers()); }
                if (e.getEventType() == FootballEventType.YELLOW_CARD) {
                    adjustForm(e.getInvolvedPlayer(), -0.2);
                    if (state.recordYellowCard(e.getInvolvedPlayer())) {
                        e.getInvolvedPlayer().applySuspension(generateSuspensionDuration());
                        FootballMatchEvent red = new FootballMatchEvent(
                                FootballEventType.RED_CARD, minute, e.getInvolvedPlayer(), away.getTeamId(), true);
                        state.addEvent(red);
                        state.decrementAwayActivePlayers();
                        removeSendOffPlayer(red, state.getAwayFieldPlayers());
                    }
                }
            }
        }

        // Drain stamina each minute
        drainStamina(homeP);
        drainStamina(awayP);

        // Period / match end
        if (minute >= end) {
            if (period >= PERIODS) {
                applyPostMatchInjuries(footballPlayers(state.getHomeFieldPlayers()));
                applyPostMatchInjuries(footballPlayers(state.getAwayFieldPlayers()));
                state.setMatchOver(true);
            } else {
                state.setCurrentPeriod(period + 1);
            }
            state.setPeriodOver(true);
        }
    }

    @Override
    public MatchResult finalizeMatch(MatchState state) {
        return new MatchResult(state.getHomeScore(), state.getAwayScore(),
                new ArrayList<>(state.getEvents()));
    }

    // Event generation

    private FootballMatchEvent generateEvent(int minute,
                                              List<FootballPlayer> attackers,
                                              List<FootballPlayer> defenders,
                                              String teamId,
                                              double atkStrength,
                                              double defStrength,
                                              MatchState state,
                                              boolean isHome) {
        double roll = random.nextDouble();

        if (roll < T_GOAL) {
            if (isHome) state.incrementHomeShots(); else state.incrementAwayShots();
            return simulateGoal(minute, attackers, defenders, teamId, atkStrength, defStrength);
        } else if (roll < T_YELLOW) {
            FootballPlayer p = randomPlayer(attackers);
            return p == null ? null
                    : new FootballMatchEvent(FootballEventType.YELLOW_CARD, minute, p, teamId);
        } else if (roll < T_RED) {
            FootballPlayer p = randomPlayer(attackers);
            return p == null ? null
                    : new FootballMatchEvent(FootballEventType.RED_CARD, minute, p, teamId);
        } else if (roll < T_INJURY) {
            FootballPlayer p = randomPlayer(attackers);
            if (p == null) return null;
            p.applyInjury(generateInjuryDuration());
            return new FootballMatchEvent(FootballEventType.INJURY, minute, p, teamId);
        } else if (roll < T_OFFSIDE) {
            FootballPlayer p = attackingPlayer(attackers);
            return p == null ? null
                    : new FootballMatchEvent(FootballEventType.OFFSIDE, minute, p, teamId);
        } else if (roll < T_FOUL) {
            FootballPlayer p = randomPlayer(defenders);
            return p == null ? null
                    : new FootballMatchEvent(FootballEventType.FOUL, minute, p, teamId);
        } else {
            return null; // no loggable event this minute
        }
    }

    private FootballMatchEvent simulateGoal(int minute,
                                             List<FootballPlayer> attackers,
                                             List<FootballPlayer> defenders,
                                             String teamId,
                                             double atkStrength,
                                             double defStrength) {
        FootballPlayer shooter = attackingPlayer(attackers);
        if (shooter == null) return null;

        int shooterScore = shooter.getAttributeValue("shooting") + random.nextInt(30);

        double ratio = atkStrength / Math.max(1.0, defStrength);
        shooterScore = (int)(shooterScore * Math.min(1.5, Math.max(0.5, ratio)));

        FootballPlayer gk = findGoalkeeper(defenders);
        int keeperScore;
        if (gk != null) {
            keeperScore = gk.getAttributeValue("reflexes") + random.nextInt(30);
        } else {
            FootballPlayer emergency = randomPlayer(defenders);
            keeperScore = (emergency != null)
                    ? emergency.getEmergencyGoalkeeperRating() + random.nextInt(30)
                    : random.nextInt(30);
        }

        if (shooterScore <= keeperScore) return null;

        // Goal confirmed
        FootballPlayer assister = midfielderOrAttacker(attackers, shooter);
        if (assister != null && random.nextDouble() < 0.70) {
            return new FootballMatchEvent(FootballEventType.GOAL, minute, shooter, assister, teamId);
        }
        return new FootballMatchEvent(FootballEventType.GOAL, minute, shooter, teamId);
    }

    private double attackStrength(List<FootballPlayer> players,
                                  FootballTactic tactic, int activeCount) {
        List<FootballPlayer> attackers = players.stream()
                .filter(p -> p.getPosition().isAttacking() || p.getPosition().isMidfield())
                .collect(Collectors.toList());

        double base = attackers.isEmpty()
                ? averageOverall(players)
                : attackers.stream().mapToInt(FootballPlayer::getOverallRating).average().orElse(50);

        return base * (1 + tactic.getGoalProbabilityModifier()) * ((double) activeCount / 11);
    }

    private double defenseStrength(List<FootballPlayer> players,
                                   FootballTactic tactic, int activeCount) {
        List<FootballPlayer> defenders = players.stream()
                .filter(p -> p.getPosition().isDefensive())
                .collect(Collectors.toList());

        double base = defenders.isEmpty()
                ? averageOverall(players)
                : defenders.stream().mapToInt(FootballPlayer::getOverallRating).average().orElse(50);

        return base * (1 - tactic.getConcedeProbabilityModifier()) * ((double) activeCount / 11);
    }

    private double strengthFactor(double atk, double def) {
        double ratio = atk / Math.max(1.0, def);
        return Math.min(1.8, Math.max(0.4, ratio));
    }

    private double averageOverall(List<FootballPlayer> players) {
        return players.stream().mapToInt(FootballPlayer::getOverallRating).average().orElse(50);
    }

    private FootballPlayer attackingPlayer(List<FootballPlayer> players) {
        List<FootballPlayer> atk = players.stream()
                .filter(p -> p.getPosition().isAttacking())
                .collect(Collectors.toList());
        if (!atk.isEmpty()) return atk.get(random.nextInt(atk.size()));
        return randomPlayer(players);
    }

    private FootballPlayer midfielderOrAttacker(List<FootballPlayer> players,
                                                 FootballPlayer exclude) {
        List<FootballPlayer> candidates = players.stream()
                .filter(p -> p != exclude && (p.getPosition().isMidfield() || p.getPosition().isAttacking()))
                .collect(Collectors.toList());
        return candidates.isEmpty() ? null : candidates.get(random.nextInt(candidates.size()));
    }

    private FootballPlayer findGoalkeeper(List<FootballPlayer> players) {
        return players.stream()
                .filter(p -> p.getPosition() == FootballPosition.GOALKEEPER)
                .findFirst()
                .orElse(null);
    }

    private FootballPlayer randomPlayer(List<FootballPlayer> players) {
        return players.isEmpty() ? null : players.get(random.nextInt(players.size()));
    }

    /**
     * Removes the player involved in a RED_CARD or INJURY event from the field.
     * The player object is retrieved from the event's involvedPlayer field.
     */
    private void removeSendOffPlayer(FootballMatchEvent event, List<Player> fieldPlayers) {
        Player p = event.getInvolvedPlayer();
        fieldPlayers.remove(p);
    }

    private void applyPostMatchInjuries(List<FootballPlayer> players) {
        for (FootballPlayer p : players) {
            if (p.isAvailable() && random.nextDouble() < POST_INJURY_PROB) {
                p.applyInjury(generateInjuryDuration());
            }
        }
    }
    private int generateSuspensionDuration() {
        double roll = random.nextDouble();
        if (roll < 0.60) return 2; // 1-match ban
        if (roll < 0.90) return 3; // 2-match ban
        return 4;                   // 3-match ban (rare)
    }

    private int generateInjuryDuration() {
        double roll = random.nextDouble();
        if (roll < 0.60) return 1 + random.nextInt(2);
        if (roll < 0.90) return 3 + random.nextInt(3);
        return 6 + random.nextInt(5);
    }
    // ── Stamina ──────────────────────────────────────────────────────────────────

    /**
     * Drains each field player's stamina by one minute's worth.
     * Rate depends on position (wingers tire fastest, defenders slowest)
     * and the player's physical attribute (higher physical → slower drain).
     *
     * Calibration: a winger with physical=60 hits 30 % (yellow) around minute 55,
     * while a CB with physical=90 stays above 50 % for the full 90 minutes.
     */
    private void drainStamina(List<FootballPlayer> players) {
        for (FootballPlayer p : players) {
            double base     = baseDrainRate(p.getPosition());
            // physical 60 → factor 1.0 (baseline); higher physical slows drain
            double physFact = Math.max(20, p.getPhysical()) / 60.0;
            p.drainStamina(base / physFact);
        }
    }

    // ── Form ─────────────────────────────────────────────────────────────────────

    private void adjustForm(com.sportsmanager.core.Player p, double delta) {
        if (p instanceof FootballPlayer fp) fp.adjustForm(delta);
    }

    /** Applies form bonus to goal scorer (+0.3) and assister (+0.15). */
    private void applyFormGoal(FootballMatchEvent e) {
        adjustForm(e.getInvolvedPlayer(), +0.3);
        if (e.getSecondaryPlayer() != null) adjustForm(e.getSecondaryPlayer(), +0.15);
    }

    /** Base stamina drain per minute (at physical = 60). */
    private double baseDrainRate(FootballPosition pos) {
        return switch (pos) {
            case GOALKEEPER           -> 0.25;
            case CENTRE_BACK          -> 0.34;
            case LEFT_BACK,
                 RIGHT_BACK           -> 0.38;
            case DEFENSIVE_MIDFIELDER -> 0.41;
            case CENTRAL_MIDFIELDER   -> 0.44;
            case ATTACKING_MIDFIELDER -> 0.47;
            case LEFT_MIDFIELDER,
                 RIGHT_MIDFIELDER     -> 0.46;
            case LEFT_WINGER,
                 RIGHT_WINGER         -> 0.50;
            case STRIKER              -> 0.47;
            case CENTRE_FORWARD       -> 0.45;
        };
    }

    // ── Utility ──────────────────────────────────────────────────────────────────
    private FootballTactic resolveTactic(Team team) {
        if (team.getTactic() instanceof FootballTactic ft) return ft;
        return FootballTactic.BALANCED;
    }

    private List<FootballPlayer> footballPlayers(List<Player> players) {
        List<FootballPlayer> result = new ArrayList<>();
        for (Player p : players) {
            if (p instanceof FootballPlayer fp) result.add(fp);
        }
        return result;
    }
}
