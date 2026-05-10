package com.sportsmanager.football;

import com.sportsmanager.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Simulates a 90-minute football match minute by minute.
 *
 * Positional awareness: at match start, each field player is assigned their
 * formation-slot position via MatchState.setPlayingPosition(). Tactical swaps
 * during the match update those assignments. All strength/role calculations use
 * the assigned (effective) position — not the player's natural position —
 * so an out-of-position player genuinely hurts or helps their team differently.
 */
public class FootballMatchEngine implements MatchEngine {

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final int    PERIODS          = 2;
    private static final int    PERIOD_DURATION  = 45;
    private static final double BASE_EVENT_PROB  = 0.18;
    private static final double POST_INJURY_PROB = 0.007;

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

    // ── One-shot ──────────────────────────────────────────────────────────────

    @Override
    public MatchResult simulateMatch(Team home, Team away) {
        MatchState state = initMatch(home, away);
        return simulateToEnd(state, home, away);
    }

    // ── Period-by-period ──────────────────────────────────────────────────────

    private static final int FIELD_SIZE = 11;

    @Override
    public MatchState initMatch(Team home, Team away) {
        resetSquadStamina(home);
        resetSquadStamina(away);
        MatchState state = new MatchState(
                PERIODS, home.getTeamId(), away.getTeamId(),
                FootballSport.MAX_PLAYERS_ON_FIELD);
        distributeSquad(home.getAvailablePlayers(),
                state.getHomeFieldPlayers(), state.getHomeBenchPlayers());
        distributeSquad(away.getAvailablePlayers(),
                state.getAwayFieldPlayers(), state.getAwayBenchPlayers());

        if (home.getFormation() != null)
            seedPlayingPositions(state, state.getHomeFieldPlayers(),
                    home.getFormation().getPositionSlots());
        if (away.getFormation() != null)
            seedPlayingPositions(state, state.getAwayFieldPlayers(),
                    away.getFormation().getPositionSlots());

        return state;
    }

    private void seedPlayingPositions(MatchState state,
                                      List<Player> fieldPlayers,
                                      List<Position> slots) {
        for (int i = 0; i < fieldPlayers.size() && i < slots.size(); i++) {
            state.setPlayingPosition(fieldPlayers.get(i), slots.get(i));
        }
    }

    private void resetSquadStamina(Team team) {
        for (Player p : team.getSquad()) {
            if (p instanceof FootballPlayer fp) fp.recoverStamina(25.0);
        }
    }

    private void distributeSquad(List<Player> available,
                                  List<Player> field, List<Player> bench) {
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

        int start = (period - 1) * PERIOD_DURATION + 1;
        if (minute < start) minute = start;

        state.setCurrentMinute(minute);

        FootballTactic homeTactic  = resolveTactic(home);
        FootballTactic awayTactic  = resolveTactic(away);
        List<FootballPlayer> homeP = footballPlayers(state.getHomeFieldPlayers());
        List<FootballPlayer> awayP = footballPlayers(state.getAwayFieldPlayers());

        double homeAtk = attackStrength(homeP, homeTactic, state.getHomeActivePlayers(), state);
        double awayAtk = attackStrength(awayP, awayTactic, state.getAwayActivePlayers(), state);
        double homeDef = defenseStrength(homeP, homeTactic, state.getHomeActivePlayers(), state);
        double awayDef = defenseStrength(awayP, awayTactic, state.getAwayActivePlayers(), state);

        // Home attacks
        double homeProb = BASE_EVENT_PROB * (1 + homeTactic.getGoalProbabilityModifier())
                * strengthFactor(homeAtk, awayDef);
        if (random.nextDouble() < homeProb) {
            state.addHomePossessionTick();
            FootballMatchEvent e = generateEvent(
                    minute, homeP, awayP, home.getTeamId(), homeAtk, awayDef, state, true);
            if (e != null) {
                state.addEvent(e);
                if (e.getEventType() == FootballEventType.GOAL) {
                    state.incrementHomeScore(); applyFormGoal(e);
                }
                if (e.getEventType() == FootballEventType.RED_CARD) {
                    adjustForm(e.getInvolvedPlayer(), -0.8);
                    e.getInvolvedPlayer().applySuspension(generateSuspensionDuration());
                    state.decrementHomeActivePlayers();
                    removeSendOffPlayer(e, state.getHomeFieldPlayers());
                }
                if (e.getEventType() == FootballEventType.INJURY) {
                    adjustForm(e.getInvolvedPlayer(), -0.3);
                    removeSendOffPlayer(e, state.getHomeFieldPlayers());
                }
                if (e.getEventType() == FootballEventType.YELLOW_CARD) {
                    adjustForm(e.getInvolvedPlayer(), -0.2);
                    if (state.recordYellowCard(e.getInvolvedPlayer(), home.getTeamId())) {
                        e.getInvolvedPlayer().applySuspension(generateSuspensionDuration());
                        FootballMatchEvent red = new FootballMatchEvent(
                                FootballEventType.RED_CARD, minute,
                                e.getInvolvedPlayer(), home.getTeamId(), true);
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
            FootballMatchEvent e = generateEvent(
                    minute, awayP, homeP, away.getTeamId(), awayAtk, homeDef, state, false);
            if (e != null) {
                state.addEvent(e);
                if (e.getEventType() == FootballEventType.GOAL) {
                    state.incrementAwayScore(); applyFormGoal(e);
                }
                if (e.getEventType() == FootballEventType.RED_CARD) {
                    adjustForm(e.getInvolvedPlayer(), -0.8);
                    e.getInvolvedPlayer().applySuspension(generateSuspensionDuration());
                    state.decrementAwayActivePlayers();
                    removeSendOffPlayer(e, state.getAwayFieldPlayers());
                }
                if (e.getEventType() == FootballEventType.INJURY) {
                    adjustForm(e.getInvolvedPlayer(), -0.3);
                    removeSendOffPlayer(e, state.getAwayFieldPlayers());
                }
                if (e.getEventType() == FootballEventType.YELLOW_CARD) {
                    adjustForm(e.getInvolvedPlayer(), -0.2);
                    if (state.recordYellowCard(e.getInvolvedPlayer(), away.getTeamId())) {
                        e.getInvolvedPlayer().applySuspension(generateSuspensionDuration());
                        FootballMatchEvent red = new FootballMatchEvent(
                                FootballEventType.RED_CARD, minute,
                                e.getInvolvedPlayer(), away.getTeamId(), true);
                        state.addEvent(red);
                        state.decrementAwayActivePlayers();
                        removeSendOffPlayer(red, state.getAwayFieldPlayers());
                    }
                }
            }
        }

        drainStamina(homeP, state);
        drainStamina(awayP, state);

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

    // ── Event generation ──────────────────────────────────────────────────────

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
            return simulateGoal(minute, attackers, defenders, teamId,
                    atkStrength, defStrength, state);
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
            FootballPlayer p = attackingPlayer(attackers, state);
            return p == null ? null
                    : new FootballMatchEvent(FootballEventType.OFFSIDE, minute, p, teamId);
        } else if (roll < T_FOUL) {
            FootballPlayer p = randomPlayer(defenders);
            return p == null ? null
                    : new FootballMatchEvent(FootballEventType.FOUL, minute, p, teamId);
        } else {
            return null;
        }
    }

    private FootballMatchEvent simulateGoal(int minute,
                                             List<FootballPlayer> attackers,
                                             List<FootballPlayer> defenders,
                                             String teamId,
                                             double atkStrength,
                                             double defStrength,
                                             MatchState state) {
        FootballPlayer shooter = attackingPlayer(attackers, state);
        if (shooter == null) return null;

        int shooterScore = shooter.getAttributeValue("shooting") + random.nextInt(30);
        double ratio = atkStrength / Math.max(1.0, defStrength);
        shooterScore = (int)(shooterScore * Math.min(1.5, Math.max(0.5, ratio)));

        FootballPlayer gk = findGoalkeeper(defenders, state);
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

        FootballPlayer assister = midfielderOrAttacker(attackers, shooter, state);
        if (assister != null && random.nextDouble() < 0.70) {
            return new FootballMatchEvent(FootballEventType.GOAL, minute,
                    shooter, assister, teamId);
        }
        return new FootballMatchEvent(FootballEventType.GOAL, minute, shooter, teamId);
    }

    // ── Strength calculations (position-aware, effective-OVR-based) ───────────

    private double attackStrength(List<FootballPlayer> players,
                                  FootballTactic tactic, int activeCount,
                                  MatchState state) {
        List<FootballPlayer> attackers = players.stream()
                .filter(p -> {
                    FootballPosition pos = playingPosition(p, state);
                    return pos.isAttacking() || pos.isMidfield();
                })
                .collect(Collectors.toList());

        List<FootballPlayer> group = attackers.isEmpty() ? players : attackers;
        double base = group.stream()
                .mapToInt(p -> p.getEffectiveOverall(playingPosition(p, state)))
                .average().orElse(50);

        return base * (1 + tactic.getGoalProbabilityModifier()) * ((double) activeCount / 11);
    }

    private double defenseStrength(List<FootballPlayer> players,
                                   FootballTactic tactic, int activeCount,
                                   MatchState state) {
        List<FootballPlayer> defenders = players.stream()
                .filter(p -> playingPosition(p, state).isDefensive())
                .collect(Collectors.toList());

        List<FootballPlayer> group = defenders.isEmpty() ? players : defenders;
        double base = group.stream()
                .mapToInt(p -> p.getEffectiveOverall(playingPosition(p, state)))
                .average().orElse(50);

        return base * (1 - tactic.getConcedeProbabilityModifier()) * ((double) activeCount / 11);
    }

    private double strengthFactor(double atk, double def) {
        double ratio = atk / Math.max(1.0, def);
        return Math.min(1.8, Math.max(0.4, ratio));
    }

    // ── Player selection (effective-position-aware) ───────────────────────────

    private FootballPlayer attackingPlayer(List<FootballPlayer> players, MatchState state) {
        List<FootballPlayer> atk = players.stream()
                .filter(p -> playingPosition(p, state).isAttacking())
                .collect(Collectors.toList());
        return atk.isEmpty() ? randomPlayer(players) : atk.get(random.nextInt(atk.size()));
    }

    private FootballPlayer midfielderOrAttacker(List<FootballPlayer> players,
                                                 FootballPlayer exclude,
                                                 MatchState state) {
        List<FootballPlayer> candidates = players.stream()
                .filter(p -> {
                    FootballPosition pos = playingPosition(p, state);
                    return p != exclude && (pos.isMidfield() || pos.isAttacking());
                })
                .collect(Collectors.toList());
        return candidates.isEmpty() ? null : candidates.get(random.nextInt(candidates.size()));
    }

    private FootballPlayer findGoalkeeper(List<FootballPlayer> players, MatchState state) {
        return players.stream()
                .filter(p -> playingPosition(p, state) == FootballPosition.GOALKEEPER)
                .findFirst().orElse(null);
    }

    private FootballPlayer randomPlayer(List<FootballPlayer> players) {
        return players.isEmpty() ? null : players.get(random.nextInt(players.size()));
    }

    // ── Position helper ───────────────────────────────────────────────────────

    /** Returns the assigned playing position, falling back to natural position. */
    private FootballPosition playingPosition(FootballPlayer p, MatchState state) {
        Position pos = state.getPlayingPosition(p);
        return (pos instanceof FootballPosition fp) ? fp : p.getPosition();
    }

    // ── Card / injury events ──────────────────────────────────────────────────

    private void removeSendOffPlayer(FootballMatchEvent event, List<Player> fieldPlayers) {
        fieldPlayers.remove(event.getInvolvedPlayer());
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
        if (roll < 0.60) return 2;
        if (roll < 0.90) return 3;
        return 4;
    }

    private int generateInjuryDuration() {
        double roll = random.nextDouble();
        if (roll < 0.60) return 1 + random.nextInt(2);
        if (roll < 0.90) return 3 + random.nextInt(3);
        return 6 + random.nextInt(5);
    }

    // ── Stamina ───────────────────────────────────────────────────────────────

    private void drainStamina(List<FootballPlayer> players, MatchState state) {
        for (FootballPlayer p : players) {
            // Use the assigned playing position so a winger subbed to striker
            // drains at the striker rate, not the winger rate.
            double base     = baseDrainRate(playingPosition(p, state));
            double physFact = Math.max(20, p.getPhysical()) / 60.0;
            p.drainStamina(base / physFact);
        }
    }

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

    // ── Form ──────────────────────────────────────────────────────────────────

    private void adjustForm(Player p, double delta) {
        if (p instanceof FootballPlayer fp) fp.adjustForm(delta);
    }

    private void applyFormGoal(FootballMatchEvent e) {
        adjustForm(e.getInvolvedPlayer(), +0.3);
        if (e.getSecondaryPlayer() != null) adjustForm(e.getSecondaryPlayer(), +0.15);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private FootballTactic resolveTactic(Team team) {
        if (team.getTactic() instanceof FootballTactic ft) return ft;
        return FootballTactic.BALANCED;
    }

    private List<FootballPlayer> footballPlayers(List<Player> players) {
        List<FootballPlayer> result = new ArrayList<>();
        for (Player p : players)
            if (p instanceof FootballPlayer fp) result.add(fp);
        return result;
    }
}
