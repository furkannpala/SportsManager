package com.sportsmanager.training;

import com.sportsmanager.core.Player;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.football.PositionalTrainingOption;
import com.sportsmanager.game.SeasonState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Executes one week of training for all players with active plans.
 *
 * Development formula per attribute per week:
 *   gain = BASE_GAIN × ageFactor × formFactor × positionalFactor × sessionFactor + noise
 *
 * Gain is applied probabilistically for fractional values so the
 * expected improvement over N weeks equals N × gain.
 */
public final class TrainingEngine {

    private static final double BASE_GAIN       = 1.4;
    private static final double BALANCED_FACTOR = 0.30; // balanced is much slower

    private static final Random RANDOM = new Random();

    private TrainingEngine() {}

    // ── Public entry point ────────────────────────────────────────────────────────

    public static void executeWeeklyTraining(SeasonState state) {
        Map<Player, PlayerTrainingPlan> plans = state.getTrainingPlans();
        if (plans.isEmpty()) return;

        List<Player> finished = new ArrayList<>();

        for (Map.Entry<Player, PlayerTrainingPlan> entry : new ArrayList<>(plans.entrySet())) {
            Player player = entry.getKey();
            PlayerTrainingPlan plan = entry.getValue();

            if (!(player instanceof FootballPlayer fp)) continue;
            if (!player.isAvailable()) continue; // injured players can't train

            applyGain(fp, plan.getOption());

            if (plan.tick()) finished.add(player);
        }

        finished.forEach(plans::remove);

        // Weekly form drift and slow decline for entire user squad
        for (Player p : state.getUserTeam().getSquad()) {
            if (p instanceof FootballPlayer fp) {
                fp.driftForm();
                
                // Slow decline for Veterans not in active training
                if (fp.getAge() >= 30 && !plans.containsKey(fp)) {
                    if (RANDOM.nextDouble() < 0.15) { // 15% chance to drop one random attribute
                        List<String> attrs = allAttrsFor(fp.getPosition());
                        String randomAttr = attrs.get(RANDOM.nextInt(attrs.size()));
                        if (fp.getAttributeValue(randomAttr) > 1) {
                            fp.decreaseAttribute(randomAttr, 1);
                        }
                    }
                }
            }
        }
    }

    // ── Gain calculation ──────────────────────────────────────────────────────────

    private static void applyGain(FootballPlayer player, PositionalTrainingOption option) {
        double ageFactor     = ageFactor(player.getAge());
        double formFactor    = formFactor(player.getForm());
        double sessionFactor = option.isBalanced() ? BALANCED_FACTOR : 1.0;

        List<String> attrs = option.isBalanced()
                ? allAttrsFor(player.getPosition())
                : option.getAttributes();

        for (String attr : attrs) {
            // Skip attributes that are already maxed out
            if (player.getAttributeValue(attr) >= 100) continue;

            double posFactor = positionalFactor(player.getPosition(), attr);
            double baseGain  = BASE_GAIN * ageFactor * formFactor * posFactor * sessionFactor;

            // Proportional noise (±20% of base gain) so age differences stay visible
            double noise = baseGain * (RANDOM.nextDouble() * 0.4 - 0.2);
            double gain  = Math.max(0, baseGain + noise);

            // Probabilistic application so fractional gains accumulate correctly
            int whole  = (int) gain;
            double frac = gain - whole;
            int actual = whole + (RANDOM.nextDouble() < frac ? 1 : 0);
            if (actual > 0) player.increaseAttribute(attr, actual);
        }
    }

    // ── Age factor ────────────────────────────────────────────────────────────────

    /**
     * Young players develop quickly; old players barely improve (maintenance mode).
     */
    public static double ageFactor(int age) {
        if (age < 24) return 1.40; // Young — Fast development
        if (age < 30) return 1.00; // Prime — Normal development
        return 0.0;                // Veteran — Maintenance mode
    }

    // ── Form factor ───────────────────────────────────────────────────────────────

    /**
     * High form boosts training gains; poor form reduces them significantly.
     * Scale: 5 (very bad) → 0.35, 7.5 (neutral) → 1.0, 10 (very good) → 1.5.
     */
    public static double formFactor(double form) {
        if (form >= 8.5) return 1.50;
        if (form >= 7.5) return 1.20;
        if (form >= 6.5) return 1.00;
        if (form >= 5.5) return 0.65;
        return 0.35;
    }

    // ── Positional factor ─────────────────────────────────────────────────────────

    /**
     * Attributes that are central to a position develop faster.
     * Derived from the position's weighted-attribute map (weight × 6 → normalised ~1.0).
     * Clamped to [0.3, 1.8].
     */
    static double positionalFactor(FootballPosition pos, String attr) {
        Map<String, Double> weights = pos.getWeightedAttributes();
        double w = weights.getOrDefault(attr, 1.0 / 6.0);
        return Math.max(0.30, Math.min(1.80, w * 6.0));
    }

    // ── Attribute list helpers ────────────────────────────────────────────────────

    private static List<String> allAttrsFor(FootballPosition pos) {
        if (pos == FootballPosition.GOALKEEPER) {
            return List.of("pace", "diving", "handling", "kicking", "reflexes", "positioning");
        }
        return List.of("pace", "shooting", "passing", "dribbling", "defending", "physical");
    }
}
