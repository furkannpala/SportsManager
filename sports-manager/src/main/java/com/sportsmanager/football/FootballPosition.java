package com.sportsmanager.football;

import com.sportsmanager.core.Position;

import java.util.LinkedHashMap;
import java.util.Map;

public enum FootballPosition implements Position {

    GOALKEEPER("Goalkeeper", true, false, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("defending", 0.40);
            w.put("physical",  0.30);
            w.put("pace",      0.15);
            w.put("passing",   0.15);
            return w;
        }
    },

    CENTRE_BACK("Centre Back", true, false, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("defending", 0.40);
            w.put("physical",  0.25);
            w.put("pace",      0.20);
            w.put("passing",   0.15);
            return w;
        }
    },

    LEFT_BACK("Left Back", true, false, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("defending", 0.35);
            w.put("pace",      0.25);
            w.put("passing",   0.20);
            w.put("physical",  0.20);
            return w;
        }
    },

    RIGHT_BACK("Right Back", true, false, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("defending", 0.35);
            w.put("pace",      0.25);
            w.put("passing",   0.20);
            w.put("physical",  0.20);
            return w;
        }
    },

    DEFENSIVE_MIDFIELDER("Defensive Midfielder", false, true, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("passing",   0.30);
            w.put("defending", 0.25);
            w.put("physical",  0.20);
            w.put("pace",      0.15);
            w.put("dribbling", 0.10);
            return w;
        }
    },

    CENTRAL_MIDFIELDER("Central Midfielder", false, true, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("passing",   0.35);
            w.put("dribbling", 0.25);
            w.put("pace",      0.20);
            w.put("physical",  0.10);
            w.put("defending", 0.10);
            return w;
        }
    },

    ATTACKING_MIDFIELDER("Attacking Midfielder", false, true, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("passing",   0.30);
            w.put("dribbling", 0.30);
            w.put("shooting",  0.20);
            w.put("pace",      0.20);
            return w;
        }
    },

    LEFT_WINGER("Left Winger", false, false, true) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("pace",      0.35);
            w.put("dribbling", 0.30);
            w.put("shooting",  0.20);
            w.put("passing",   0.15);
            return w;
        }
    },

    RIGHT_WINGER("Right Winger", false, false, true) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("pace",      0.35);
            w.put("dribbling", 0.30);
            w.put("shooting",  0.20);
            w.put("passing",   0.15);
            return w;
        }
    },

    STRIKER("Striker", false, false, true) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("shooting",  0.40);
            w.put("pace",      0.30);
            w.put("dribbling", 0.20);
            w.put("physical",  0.10);
            return w;
        }
    },

    CENTRE_FORWARD("Centre Forward", false, false, true) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("shooting",  0.35);
            w.put("physical",  0.25);
            w.put("pace",      0.25);
            w.put("dribbling", 0.15);
            return w;
        }
    };

    private final String displayName;
    private final boolean defensive;
    private final boolean midfield;
    private final boolean attacking;

    FootballPosition(String displayName, boolean defensive, boolean midfield, boolean attacking) {
        this.displayName = displayName;
        this.defensive   = defensive;
        this.midfield    = midfield;
        this.attacking   = attacking;
    }

    // Position interface

    @Override
    public String getName() {
        return displayName;
    }

    // Zone helpers

    public boolean isDefensive() { return defensive; }
    public boolean isMidfield()  { return midfield;  }
    public boolean isAttacking() { return attacking; }

    public abstract Map<String, Double> getWeightedAttributes();
}
