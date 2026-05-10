package com.sportsmanager.handball;

import com.sportsmanager.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Simulates a 60-minute handball match (2 × 30 min) minute by minute.
 * Calibrated for ~28–32 goals per team per game.
 */
public class HandballMatchEngine implements MatchEngine {

    private static final int    PERIODS         = 2;
    private static final int    PERIOD_DURATION = 30;
    private static final int    FIELD_SIZE      = 7;
    private static final double BASE_EVENT_PROB = 0.70;

    // Event thresholds (cumulative 0–1): fraction of events that resolve as each type
    private static final double T_GOAL    = 0.55;
    private static final double T_YELLOW  = 0.57;
    private static final double T_TWO_MIN = 0.65;  // handball 2-minute suspension
    private static final double T_RED     = 0.66;
    private static final double T_FOUL    = 0.75;

    private final Random random;

    public HandballMatchEngine()             { this.random = new Random(); }
    public HandballMatchEngine(Random random) { this.random = random; }

    // ── One-shot ──────────────────────────────────────────────────────────────

    @Override
    public MatchResult simulateMatch(Team home, Team away) {
        MatchState state = initMatch(home, away);
        return simulateToEnd(state, home, away);
    }

    // ── Period-by-period ──────────────────────────────────────────────────────

    @Override
    public MatchState initMatch(Team home, Team away) {
        resetSquadStamina(home);
        resetSquadStamina(away);
        MatchState state = new MatchState(PERIODS, home.getTeamId(), away.getTeamId(), HandballSport.MAX_PLAYERS_ON_COURT);
        state.setHomeActivePlayers(FIELD_SIZE);
        state.setAwayActivePlayers(FIELD_SIZE);
        distributeSquad(home.getAvailablePlayers(), state.getHomeFieldPlayers(), state.getHomeBenchPlayers());
        distributeSquad(away.getAvailablePlayers(), state.getAwayFieldPlayers(), state.getAwayBenchPlayers());

        if (home.getFormation() != null)
            seedPlayingPositions(state, state.getHomeFieldPlayers(), home.getFormation().getPositionSlots());
        if (away.getFormation() != null)
            seedPlayingPositions(state, state.getAwayFieldPlayers(), away.getFormation().getPositionSlots());

        return state;
    }

    private void seedPlayingPositions(MatchState state, List<Player> fieldPlayers, List<Position> slots) {
        for (int i = 0; i < fieldPlayers.size() && i < slots.size(); i++) {
            state.setPlayingPosition(fieldPlayers.get(i), slots.get(i));
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
        int start  = (period - 1) * PERIOD_DURATION + 1;
        if (minute < start) minute = start;
        state.setCurrentMinute(minute);

        // ── Return players from 2-minute suspensions ───────────────────────────
        for (Player returning : state.pollReturningPlayers(minute)) {
            boolean isHomePlayer = home.getSquad().contains(returning);
            List<Player> field = isHomePlayer ? state.getHomeFieldPlayers() : state.getAwayFieldPlayers();
            if (!field.contains(returning)) {
                field.add(returning);
                if (isHomePlayer) state.setHomeActivePlayers(state.getHomeActivePlayers() + 1);
                else              state.setAwayActivePlayers(state.getAwayActivePlayers() + 1);
            }
        }

        HandballTactic homeTactic = resolveTactic(home);
        HandballTactic awayTactic = resolveTactic(away);
        List<HandballPlayer> homeP = handballPlayers(state.getHomeFieldPlayers());
        List<HandballPlayer> awayP = handballPlayers(state.getAwayFieldPlayers());

        double homeAtk = attackStrength(homeP, homeTactic, state.getHomeActivePlayers(), state);
        double awayAtk = attackStrength(awayP, awayTactic, state.getAwayActivePlayers(), state);
        double homeDef = defenseStrength(homeP, homeTactic, state.getHomeActivePlayers(), state);
        double awayDef = defenseStrength(awayP, awayTactic, state.getAwayActivePlayers(), state);

        // Home attacks
        double homeProb = BASE_EVENT_PROB
                * (1 + homeTactic.getGoalProbabilityModifier())
                * strengthFactor(homeAtk, awayDef);
        if (random.nextDouble() < homeProb) {
            state.addHomePossessionTick();
            HandballMatchEvent e = generateEvent(minute, homeP, awayP, home.getTeamId(), homeAtk, awayDef, state, true);
            if (e != null) { state.addEvent(e); applyEvent(e, state, home, true); }
        }

        // Away attacks
        double awayProb = BASE_EVENT_PROB
                * (1 + awayTactic.getGoalProbabilityModifier())
                * strengthFactor(awayAtk, homeDef);
        if (random.nextDouble() < awayProb) {
            state.addAwayPossessionTick();
            HandballMatchEvent e = generateEvent(minute, awayP, homeP, away.getTeamId(), awayAtk, homeDef, state, false);
            if (e != null) { state.addEvent(e); applyEvent(e, state, away, false); }
        }

        drainStamina(homeP);
        drainStamina(awayP);

        if (minute >= end) {
            if (period >= PERIODS) state.setMatchOver(true);
            else                   state.setCurrentPeriod(period + 1);
            state.setPeriodOver(true);
        }
    }

    @Override
    public MatchResult finalizeMatch(MatchState state) {
        return new MatchResult(state.getHomeScore(), state.getAwayScore(),
                new ArrayList<>(state.getEvents()));
    }

    // ── Event handling ────────────────────────────────────────────────────────

    private void applyEvent(HandballMatchEvent e, MatchState state, Team team, boolean isHome) {
        switch (e.getEventType()) {
            case GOAL -> {
                if (isHome) state.incrementHomeScore(); else state.incrementAwayScore();
                adjustForm(e.getInvolvedPlayer(), +0.3);
                if (e.getSecondaryPlayer() != null) adjustForm(e.getSecondaryPlayer(), +0.15);
            }
            case TWO_MIN_SUSPENSION -> {
                adjustForm(e.getInvolvedPlayer(), -0.2);
                if (isHome) state.decrementHomeActivePlayers(); else state.decrementAwayActivePlayers();
                removeFromField(e.getInvolvedPlayer(),
                        isHome ? state.getHomeFieldPlayers() : state.getAwayFieldPlayers());
                // Register return time so the player comes back after 2 minutes
                state.applyTwoMinuteSuspension(e.getInvolvedPlayer(), e.getMinute());
            }
            case RED_CARD -> {
                adjustForm(e.getInvolvedPlayer(), -0.8);
                e.getInvolvedPlayer().applySuspension(generateSuspensionDuration());
                if (isHome) state.decrementHomeActivePlayers(); else state.decrementAwayActivePlayers();
                removeFromField(e.getInvolvedPlayer(),
                        isHome ? state.getHomeFieldPlayers() : state.getAwayFieldPlayers());
            }
            case YELLOW_CARD -> {
                adjustForm(e.getInvolvedPlayer(), -0.1);
                String teamId = team.getTeamId();
                int teamYellows = state.getTeamYellowCount(teamId); // count BEFORE this card
                boolean secondYellow = state.recordYellowCard(e.getInvolvedPlayer(), teamId);

                if (teamYellows >= 3) {
                    // Team has already hit 3 yellows — escalate this card immediately.
                    // 25% chance it's a very severe foul → direct red card
                    // 75% chance → 2-minute suspension
                    if (random.nextDouble() < 0.25) {
                        HandballMatchEvent red = new HandballMatchEvent(
                                HandballEventType.RED_CARD, e.getMinute(),
                                e.getInvolvedPlayer(), teamId);
                        state.addEvent(red);
                        applyEvent(red, state, team, isHome);
                    } else {
                        HandballMatchEvent twoMin = new HandballMatchEvent(
                                HandballEventType.TWO_MIN_SUSPENSION, e.getMinute(),
                                e.getInvolvedPlayer(), teamId);
                        state.addEvent(twoMin);
                        applyEvent(twoMin, state, team, isHome);
                    }
                } else if (secondYellow) {
                    // Handball rule: 2nd yellow for this player = 2-minute suspension
                    HandballMatchEvent twoMin = new HandballMatchEvent(
                            HandballEventType.TWO_MIN_SUSPENSION, e.getMinute(),
                            e.getInvolvedPlayer(), teamId);
                    state.addEvent(twoMin);
                    applyEvent(twoMin, state, team, isHome);
                }
            }
            default -> { /* FOUL, PENALTY_AWARDED — no state change */ }
        }
    }

    // ── Event generation ──────────────────────────────────────────────────────

    private HandballMatchEvent generateEvent(int minute,
                                              List<HandballPlayer> attackers,
                                              List<HandballPlayer> defenders,
                                              String teamId,
                                              double atkStrength, double defStrength,
                                              MatchState state, boolean isHome) {
        double roll = random.nextDouble();

        if (roll < T_GOAL) {
            if (isHome) state.incrementHomeShots(); else state.incrementAwayShots();
            return simulateShot(minute, attackers, defenders, teamId, atkStrength, defStrength);
        } else if (roll < T_YELLOW) {
            HandballPlayer p = randomFieldPlayer(attackers);
            return p == null ? null
                    : new HandballMatchEvent(HandballEventType.YELLOW_CARD, minute, p, teamId);
        } else if (roll < T_TWO_MIN) {
            HandballPlayer p = randomFieldPlayer(attackers);
            return p == null ? null
                    : new HandballMatchEvent(HandballEventType.TWO_MIN_SUSPENSION, minute, p, teamId);
        } else if (roll < T_RED) {
            HandballPlayer p = randomFieldPlayer(attackers);
            return p == null ? null
                    : new HandballMatchEvent(HandballEventType.RED_CARD, minute, p, teamId);
        } else if (roll < T_FOUL) {
            HandballPlayer p = randomFieldPlayer(defenders);
            return p == null ? null
                    : new HandballMatchEvent(HandballEventType.FOUL, minute, p, teamId);
        }
        return null;
    }

    private HandballMatchEvent simulateShot(int minute,
                                             List<HandballPlayer> attackers,
                                             List<HandballPlayer> defenders,
                                             String teamId,
                                             double atkStrength, double defStrength) {
        HandballPlayer shooter = shootingPlayer(attackers);
        if (shooter == null) return null;

        double ratio  = atkStrength / Math.max(1.0, defStrength);
        int shooterScore = (int)(shooter.getAttributeValue("throwing") + random.nextInt(30));
        shooterScore = (int)(shooterScore * Math.min(1.5, Math.max(0.5, ratio)));

        HandballPlayer gk = findGoalkeeper(defenders);
        int keeperScore;
        if (gk != null) {
            // Lower variance for GK — handball GKs save ~30% of shots
            keeperScore = gk.getAttributeValue("reflexes") + random.nextInt(20);
        } else {
            HandballPlayer emergency = randomFieldPlayer(defenders);
            keeperScore = emergency != null
                    ? (int)(emergency.getAttributeValue("defending") * 0.5) + random.nextInt(20)
                    : random.nextInt(20);
        }

        if (shooterScore <= keeperScore) return null;

        HandballPlayer assister = passingPlayer(attackers, shooter);
        if (assister != null && random.nextDouble() < 0.65) {
            return new HandballMatchEvent(HandballEventType.GOAL, minute, shooter, assister, teamId);
        }
        return new HandballMatchEvent(HandballEventType.GOAL, minute, shooter, teamId);
    }

    // ── Strength calculation ──────────────────────────────────────────────────

    private double attackStrength(List<HandballPlayer> players, HandballTactic tactic,
                                   int activeCount, MatchState state) {
        List<HandballPlayer> attackers = players.stream()
                .filter(p -> !playingPosition(p, state).isGoalkeeper() && playingPosition(p, state).isAttacking())
                .collect(Collectors.toList());
        List<HandballPlayer> group = attackers.isEmpty() ? players : attackers;
        double base = group.stream()
                .mapToInt(p -> p.getEffectiveOverall(playingPosition(p, state)))
                .average().orElse(50);
        return base * (1 + tactic.getGoalProbabilityModifier()) * ((double) activeCount / FIELD_SIZE);
    }

    private double defenseStrength(List<HandballPlayer> players, HandballTactic tactic,
                                    int activeCount, MatchState state) {
        List<HandballPlayer> defenders = players.stream()
                .filter(p -> !playingPosition(p, state).isGoalkeeper() && playingPosition(p, state).isDefensive())
                .collect(Collectors.toList());
        List<HandballPlayer> group = defenders.isEmpty() ? players : defenders;
        double base = group.stream()
                .mapToInt(p -> p.getEffectiveOverall(playingPosition(p, state)))
                .average().orElse(50);
        return base * (1 - tactic.getConcedeProbabilityModifier()) * ((double) activeCount / FIELD_SIZE);
    }

    private HandballPosition playingPosition(HandballPlayer p, MatchState state) {
        Position pos = state.getPlayingPosition(p);
        return (pos instanceof HandballPosition hp) ? hp : p.getPosition();
    }

    private double strengthFactor(double atk, double def) {
        return Math.min(1.8, Math.max(0.4, atk / Math.max(1.0, def)));
    }

    private double average(List<HandballPlayer> players) {
        return players.stream().mapToInt(HandballPlayer::getOverallRating).average().orElse(50);
    }

    // ── Player selection helpers ──────────────────────────────────────────────

    private HandballPlayer shootingPlayer(List<HandballPlayer> players) {
        List<HandballPlayer> shooters = players.stream()
                .filter(p -> !p.getPosition().isGoalkeeper() && (p.getPosition().isWing() || p.getPosition().isBack()))
                .collect(Collectors.toList());
        if (!shooters.isEmpty()) return shooters.get(random.nextInt(shooters.size()));
        return randomFieldPlayer(players);
    }

    private HandballPlayer passingPlayer(List<HandballPlayer> players, HandballPlayer exclude) {
        List<HandballPlayer> candidates = players.stream()
                .filter(p -> p != exclude && !p.getPosition().isGoalkeeper())
                .collect(Collectors.toList());
        return candidates.isEmpty() ? null : candidates.get(random.nextInt(candidates.size()));
    }

    private HandballPlayer findGoalkeeper(List<HandballPlayer> players) {
        return players.stream().filter(p -> p.getPosition().isGoalkeeper()).findFirst().orElse(null);
    }

    private HandballPlayer randomFieldPlayer(List<HandballPlayer> players) {
        List<HandballPlayer> field = players.stream()
                .filter(p -> !p.getPosition().isGoalkeeper())
                .collect(Collectors.toList());
        if (!field.isEmpty()) return field.get(random.nextInt(field.size()));
        return players.isEmpty() ? null : players.get(random.nextInt(players.size()));
    }

    // ── Stamina & form ────────────────────────────────────────────────────────

    private void drainStamina(List<HandballPlayer> players) {
        for (HandballPlayer p : players) {
            double base     = p.getPosition().isGoalkeeper() ? 0.20 : 0.50;
            double physFact = Math.max(20, p.getPhysical()) / 60.0;
            p.drainStamina(base / physFact);
        }
    }

    private void adjustForm(Player p, double delta) {
        if (p instanceof HandballPlayer hp) hp.adjustForm(delta);
    }

    // ── Setup helpers ─────────────────────────────────────────────────────────

    private void resetSquadStamina(Team team) {
        for (Player p : team.getSquad()) {
            if (p instanceof HandballPlayer hp) hp.recoverStamina(25.0);
        }
    }

    private void distributeSquad(List<Player> available, List<Player> field, List<Player> bench) {
        for (int i = 0; i < available.size(); i++) {
            if (i < FIELD_SIZE) field.add(available.get(i));
            else                bench.add(available.get(i));
        }
    }

    private void removeFromField(Player player, List<Player> fieldPlayers) {
        fieldPlayers.remove(player);
    }

    private int generateSuspensionDuration() {
        return random.nextDouble() < 0.70 ? 2 : 3;
    }

    private HandballTactic resolveTactic(Team team) {
        if (team.getTactic() instanceof HandballTactic ht) return ht;
        return HandballTactic.BALANCED;
    }

    private List<HandballPlayer> handballPlayers(List<Player> players) {
        List<HandballPlayer> result = new ArrayList<>();
        for (Player p : players) { if (p instanceof HandballPlayer hp) result.add(hp); }
        return result;
    }
}
