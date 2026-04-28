package com.sportsmanager.handball;

import com.sportsmanager.core.Position;

import java.util.LinkedHashMap;
import java.util.Map;

public enum HandballPosition implements Position {

    GOALKEEPER("Goalkeeper") {
        @Override public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("reflexes",    0.35);
            w.put("reach",       0.25);
            w.put("diving",      0.20);
            w.put("positioning", 0.15);
            w.put("speed",       0.03);
            w.put("physical",    0.02);
            return w;
        }
    },

    LEFT_WING("Left Wing") {
        @Override public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("speed",     0.30);
            w.put("throwing",  0.28);
            w.put("agility",   0.20);
            w.put("jumping",   0.10);
            w.put("defending", 0.08);
            w.put("physical",  0.04);
            return w;
        }
    },

    LEFT_BACK("Left Back") {
        @Override public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("throwing",  0.32);
            w.put("physical",  0.22);
            w.put("speed",     0.18);
            w.put("jumping",   0.15);
            w.put("defending", 0.08);
            w.put("agility",   0.05);
            return w;
        }
    },

    CENTER_BACK("Center Back") {
        @Override public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("throwing",  0.28);
            w.put("agility",   0.25);
            w.put("speed",     0.20);
            w.put("defending", 0.12);
            w.put("jumping",   0.10);
            w.put("physical",  0.05);
            return w;
        }
    },

    RIGHT_BACK("Right Back") {
        @Override public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("throwing",  0.32);
            w.put("physical",  0.22);
            w.put("speed",     0.18);
            w.put("jumping",   0.15);
            w.put("defending", 0.08);
            w.put("agility",   0.05);
            return w;
        }
    },

    RIGHT_WING("Right Wing") {
        @Override public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("speed",     0.30);
            w.put("throwing",  0.28);
            w.put("agility",   0.20);
            w.put("jumping",   0.10);
            w.put("defending", 0.08);
            w.put("physical",  0.04);
            return w;
        }
    },

    PIVOT("Pivot") {
        @Override public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("physical",  0.32);
            w.put("jumping",   0.25);
            w.put("throwing",  0.20);
            w.put("defending", 0.13);
            w.put("agility",   0.06);
            w.put("speed",     0.04);
            return w;
        }
    };

    private final String displayName;

    HandballPosition(String displayName) { this.displayName = displayName; }

    @Override public String getName() { return displayName; }

    public boolean isGoalkeeper() { return this == GOALKEEPER; }
    public boolean isWing()       { return this == LEFT_WING || this == RIGHT_WING; }
    public boolean isBack()       { return this == LEFT_BACK || this == CENTER_BACK || this == RIGHT_BACK; }
    public boolean isPivot()      { return this == PIVOT; }
    public boolean isAttacking()  { return this == LEFT_WING || this == RIGHT_WING || this == CENTER_BACK; }
    public boolean isDefensive()  { return this == LEFT_BACK || this == CENTER_BACK || this == RIGHT_BACK || this == PIVOT; }

    public abstract Map<String, Double> getWeightedAttributes();
}
