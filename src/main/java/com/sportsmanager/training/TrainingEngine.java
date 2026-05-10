package com.sportsmanager.training;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Position;
import com.sportsmanager.game.SeasonState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class TrainingEngine {

    private static final double BASE_GAIN       = 1.4;
    private static final double BALANCED_FACTOR = 0.30;

    private static final Random RANDOM = new Random();

    private TrainingEngine() {}

    public static void executeWeeklyTraining(SeasonState state) {
        Map<Player, PlayerTrainingPlan> plans = state.getTrainingPlans();
        if (plans.isEmpty()) return;

        List<Player> finished = new ArrayList<>();

        for (Map.Entry<Player, PlayerTrainingPlan> entry : new ArrayList<>(plans.entrySet())) {
            Player player = entry.getKey();
            PlayerTrainingPlan plan = entry.getValue();

            if (!player.isAvailable()) continue;

            applyGain(player, plan.getOption());

            if (plan.tick()) finished.add(player);
        }

        finished.forEach(plans::remove);

        for (Player p : state.getUserTeam().getSquad()) {
            p.driftForm();

            if (p.getAge() >= 30 && !plans.containsKey(p)) {
                if (RANDOM.nextDouble() < 0.15) {
                    List<String> attrs = new ArrayList<>(p.getPosition().getWeightedAttributes().keySet());
                    String randomAttr = attrs.get(RANDOM.nextInt(attrs.size()));
                    if (p.getAttributeValue(randomAttr) > 1) {
                        p.decreaseAttribute(randomAttr, 1);
                    }
                }
            }
        }
    }

    private static void applyGain(Player player, PositionalTrainingOption option) {
        double ageFactor     = ageFactor(player.getAge());
        double formFactor    = formFactor(player.getForm());
        double sessionFactor = option.isBalanced() ? BALANCED_FACTOR : 1.0;

        Position pos = player.getPosition();
        List<String> attrs = option.isBalanced()
                ? new ArrayList<>(pos.getWeightedAttributes().keySet())
                : option.getAttributes();

        for (String attr : attrs) {
            if (player.getAttributeValue(attr) >= 100) continue;

            double posFactor = positionalFactor(pos, attr);
            double baseGain  = BASE_GAIN * ageFactor * formFactor * posFactor * sessionFactor;

            double noise = baseGain * (RANDOM.nextDouble() * 0.4 - 0.2);
            double gain  = Math.max(0, baseGain + noise);

            int whole  = (int) gain;
            double frac = gain - whole;
            int actual = whole + (RANDOM.nextDouble() < frac ? 1 : 0);
            if (actual > 0) player.increaseAttribute(attr, actual);
        }
    }

    public static double ageFactor(int age) {
        if (age < 24) return 1.40;
        if (age < 30) return 1.00;
        return 0.0;
    }

    public static double formFactor(double form) {
        if (form >= 8.5) return 1.50;
        if (form >= 7.5) return 1.20;
        if (form >= 6.5) return 1.00;
        if (form >= 5.5) return 0.65;
        return 0.35;
    }

    static double positionalFactor(Position pos, String attr) {
        Map<String, Double> weights = pos.getWeightedAttributes();
        double w = weights.getOrDefault(attr, 1.0 / Math.max(1, weights.size()));
        return Math.max(0.30, Math.min(1.80, w * 6.0));
    }
}
