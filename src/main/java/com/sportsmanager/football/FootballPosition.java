package com.sportsmanager.football;

import com.sportsmanager.core.Position;

import java.util.LinkedHashMap;
import java.util.Map;

public enum FootballPosition implements Position {

    GOALKEEPER("Goalkeeper", true, false, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("reflexes",    0.30);
            w.put("diving",      0.25);
            w.put("handling",    0.20);
            w.put("positioning", 0.15);
            w.put("pace",        0.05);
            w.put("kicking",     0.05);
            return w;
        }
    },

    CENTRE_BACK("Centre Back", true, false, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("defending", 0.40);
            w.put("physical",  0.25);
            w.put("pace",      0.15);
            w.put("passing",   0.10);
            w.put("dribbling", 0.05);
            w.put("shooting",  0.05);
            return w;
        }
    },

    LEFT_BACK("Left Back", true, false, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("defending", 0.30);
            w.put("pace",      0.25);
            w.put("passing",   0.20);
            w.put("physical",  0.15);
            w.put("dribbling", 0.05);
            w.put("shooting",  0.05);
            return w;
        }
    },

    RIGHT_BACK("Right Back", true, false, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("defending", 0.30);
            w.put("pace",      0.25);
            w.put("passing",   0.20);
            w.put("physical",  0.15);
            w.put("dribbling", 0.05);
            w.put("shooting",  0.05);
            return w;
        }
    },

    DEFENSIVE_MIDFIELDER("Defensive Midfielder", false, true, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("passing",   0.25);
            w.put("defending", 0.25);
            w.put("physical",  0.20);
            w.put("pace",      0.15);
            w.put("dribbling", 0.10);
            w.put("shooting",  0.05);
            return w;
        }
    },

    CENTRAL_MIDFIELDER("Central Midfielder", false, true, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("passing",   0.25);
            w.put("dribbling", 0.20);
            w.put("pace",      0.20);
            w.put("physical",  0.15);
            w.put("defending", 0.15);
            w.put("shooting",  0.05);
            return w;
        }
    },

    LEFT_MIDFIELDER("Left Midfielder", false, true, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("passing",   0.25);
            w.put("pace",      0.25);
            w.put("dribbling", 0.20);
            w.put("physical",  0.15);
            w.put("defending", 0.10);
            w.put("shooting",  0.05);
            return w;
        }
    },

    RIGHT_MIDFIELDER("Right Midfielder", false, true, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("passing",   0.25);
            w.put("pace",      0.25);
            w.put("dribbling", 0.20);
            w.put("physical",  0.15);
            w.put("defending", 0.10);
            w.put("shooting",  0.05);
            return w;
        }
    },

    ATTACKING_MIDFIELDER("Attacking Midfielder", false, true, false) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("dribbling", 0.25);
            w.put("passing",   0.25);
            w.put("shooting",  0.20);
            w.put("pace",      0.15);
            w.put("physical",  0.10);
            w.put("defending", 0.05);
            return w;
        }
    },

    LEFT_WINGER("Left Winger", false, false, true) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("pace",      0.30);
            w.put("dribbling", 0.25);
            w.put("shooting",  0.20);
            w.put("passing",   0.15);
            w.put("physical",  0.05);
            w.put("defending", 0.05);
            return w;
        }
    },

    RIGHT_WINGER("Right Winger", false, false, true) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("pace",      0.30);
            w.put("dribbling", 0.25);
            w.put("shooting",  0.20);
            w.put("passing",   0.15);
            w.put("physical",  0.05);
            w.put("defending", 0.05);
            return w;
        }
    },

    STRIKER("Striker", false, false, true) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("shooting",  0.35);
            w.put("pace",      0.25);
            w.put("physical",  0.15);
            w.put("dribbling", 0.15);
            w.put("passing",   0.05);
            w.put("defending", 0.05);
            return w;
        }
    },

    CENTRE_FORWARD("Centre Forward", false, false, true) {
        @Override
        public Map<String, Double> getWeightedAttributes() {
            Map<String, Double> w = new LinkedHashMap<>();
            w.put("shooting",  0.30);
            w.put("physical",  0.25);
            w.put("pace",      0.20);
            w.put("dribbling", 0.10);
            w.put("passing",   0.10);
            w.put("defending", 0.05);
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

    // ── Out-of-position penalty ───────────────────────────────────────────────

    /**
     * OVR points lost for playing at {@code playing} when natural position is
     * {@code natural}.
     *
     * Scale:
     *   Same position                →  0
     *   Same zone, mirrored wing     →  3
     *   Same zone, different role    →  5
     *   One zone away (def↔mid etc.) → 10
     *   Two zones away (def↔att)     → 20
     *   Any GK mismatch              → 25
     */
    public static int getOutOfPositionPenalty(FootballPosition natural, FootballPosition playing) {
        if (natural == playing) return 0;
        if (natural == GOALKEEPER || playing == GOALKEEPER) return 25;

        int near = nearNeighborPenalty(natural, playing);
        if (near >= 0) return near;

        int zoneDiff = Math.abs(zoneIndex(natural) - zoneIndex(playing));
        return switch (zoneDiff) {
            case 0  -> sameZonePenalty(natural, playing);
            case 1  -> 10;
            default -> 20;
        };
    }

    private static int nearNeighborPenalty(FootballPosition n, FootballPosition p) {
        if (pair(n, p, LEFT_WINGER,  LEFT_MIDFIELDER))  return 2;
        if (pair(n, p, RIGHT_WINGER, RIGHT_MIDFIELDER)) return 2;
        if (pair(n, p, STRIKER,      CENTRE_FORWARD))   return 2;
        if (pair(n, p, ATTACKING_MIDFIELDER, CENTRAL_MIDFIELDER)) return 3;
        return -1;
    }

    private static boolean pair(FootballPosition a, FootballPosition b,
                                 FootballPosition x, FootballPosition y) {
        return (a == x && b == y) || (a == y && b == x);
    }

    private static int zoneIndex(FootballPosition pos) {
        if (pos.isDefensive()) return 0;
        if (pos.isMidfield())  return 1;
        return 2;
    }

    private static int sameZonePenalty(FootballPosition natural, FootballPosition playing) {
        // Mirrored wing-back / winger swap — almost identical demands
        if ((natural == LEFT_BACK  && playing == RIGHT_BACK)
         || (natural == RIGHT_BACK && playing == LEFT_BACK)
         || (natural == LEFT_WINGER  && playing == RIGHT_WINGER)
         || (natural == RIGHT_WINGER && playing == LEFT_WINGER)
         || (natural == LEFT_MIDFIELDER  && playing == RIGHT_MIDFIELDER)
         || (natural == RIGHT_MIDFIELDER && playing == LEFT_MIDFIELDER)) return 3;
        // CB ↔ full-back — still defensive but slightly different demands
        if ((natural == CENTRE_BACK && (playing == LEFT_BACK || playing == RIGHT_BACK))
         || ((natural == LEFT_BACK || natural == RIGHT_BACK) && playing == CENTRE_BACK)) return 5;
        return 5;
    }
}
