package com.sportsmanager.football;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogue of all position-specific training options in football.
 *
 * Every position always has BALANCED as option 0.
 * Special options follow in the order specified by the product requirements.
 */
public final class FootballTrainingOptions {

    // ── Shared ────────────────────────────────────────────────────────────────────

    public static final PositionalTrainingOption BALANCED = opt(
            "BALANCED", "Balanced Training",
            "Slowly but evenly develops all attributes.",
            List.of(), 10, 16);

    // ── Goalkeeper ───────────────────────────────────────────────────────────────

    static final PositionalTrainingOption GK_DIVING = opt(
            "GK_DIVING", "Diving & Catching",
            "Reflex dives and ball-handling technique.",
            List.of("diving", "handling"), 3, 5);

    static final PositionalTrainingOption GK_REFLEXES = opt(
            "GK_REFLEXES", "Reflex Drills",
            "Rapid reaction and explosive shot-stopping.",
            List.of("reflexes", "pace"), 3, 4);

    static final PositionalTrainingOption GK_DIST = opt(
            "GK_DIST", "Distribution",
            "Long throws, passing accuracy, and quick restarts.",
            List.of("handling", "kicking", "pace"), 4, 6);

    // ── Centre Back ───────────────────────────────────────────────────────────────

    static final PositionalTrainingOption CB_DEF = opt(
            "CB_DEF", "Defensive Dominance",
            "Strength and positional defending.",
            List.of("physical", "defending"), 3, 5);

    static final PositionalTrainingOption CB_BUILD = opt(
            "CB_BUILD", "Build-Up Play",
            "Distribution from the back and positioning.",
            List.of("passing", "pace"), 3, 4);

    // ── Full Back (LB / RB) ───────────────────────────────────────────────────────

    static final PositionalTrainingOption LB_DEF = opt(
            "LB_DEF", "Defensive Tracking",
            "Defensive recovery runs and marking.",
            List.of("defending", "pace"), 3, 4);

    static final PositionalTrainingOption LB_ATK = opt(
            "LB_ATK", "Overlapping Runs",
            "Wing bursts and crossing quality.",
            List.of("passing", "pace"), 3, 4);

    static final PositionalTrainingOption LB_PHY = opt(
            "LB_PHY", "Physical Conditioning",
            "Stamina and defensive solidity.",
            List.of("physical", "defending"), 4, 5);

    // ── Defensive Midfielder ────────────────────────────────────────────────────

    static final PositionalTrainingOption CDM_BOX = opt(
            "CDM_BOX", "Box-to-Box",
            "Shooting and passing combination.",
            List.of("shooting", "passing"), 3, 5);

    // ── Central Midfielder ──────────────────────────────────────────────────────

    static final PositionalTrainingOption CM_PASS = opt(
            "CM_PASS", "Creative Passing",
            "Passing accuracy and dribbling combination.",
            List.of("passing", "dribbling"), 3, 5);

    static final PositionalTrainingOption CM_SHOOT = opt(
            "CM_SHOOT", "Goal Threat",
            "Shooting and dribbling focus.",
            List.of("shooting", "dribbling"), 3, 5);

    static final PositionalTrainingOption CM_COMPLETE = opt(
            "CM_COMPLETE", "Complete Midfielder",
            "Shooting, dribbling, and pace triangle.",
            List.of("shooting", "dribbling", "pace"), 4, 6);

    // ── Attacking Midfielder ────────────────────────────────────────────────────

    static final PositionalTrainingOption CAM_FINISH = opt(
            "CAM_FINISH", "Clinical Finishing",
            "Shooting and passing in the final third.",
            List.of("shooting", "passing"), 3, 5);

    static final PositionalTrainingOption CAM_SPEED = opt(
            "CAM_SPEED", "Speed & Vision",
            "Acceleration and passing range.",
            List.of("pace", "passing"), 3, 4);

    // ── Striker / Centre Forward ────────────────────────────────────────────────

    static final PositionalTrainingOption ST_PACE = opt(
            "ST_PACE", "Pace & Finish",
            "Sprint burst and shot power.",
            List.of("shooting", "pace"), 3, 5);

    static final PositionalTrainingOption ST_POWER = opt(
            "ST_POWER", "Target Man",
            "Shot power and physical presence.",
            List.of("shooting", "physical"), 3, 5);

    static final PositionalTrainingOption ST_COMPLETE = opt(
            "ST_COMPLETE", "Complete Forward",
            "Pace, shooting, and link-up play.",
            List.of("pace", "shooting", "passing"), 4, 6);

    // ── Winger (LW / RW) ────────────────────────────────────────────────────────

    static final PositionalTrainingOption LW_DRIB_SHOOT = opt(
            "LW_DRIB_SHOOT", "Wing Play",
            "Dribbling and shooting combination.",
            List.of("dribbling", "shooting"), 3, 5);

    static final PositionalTrainingOption LW_DRIB_PACE = opt(
            "LW_DRIB_PACE", "Wing Sprint",
            "Explosive dribbling and top-end pace.",
            List.of("dribbling", "pace", "shooting"), 4, 6);

    static final PositionalTrainingOption LW_DRIB_PASS = opt(
            "LW_DRIB_PASS", "Technical Winger",
            "Dribbling and crossing quality.",
            List.of("dribbling", "passing"), 3, 4);

    // ── Registry (for save/load) ─────────────────────────────────────────────────

    private static final Map<String, PositionalTrainingOption> BY_ID = new HashMap<>();
    static {
        for (PositionalTrainingOption o : List.of(
                BALANCED,
                GK_DIVING, GK_REFLEXES, GK_DIST,
                CB_DEF, CB_BUILD,
                LB_DEF, LB_ATK, LB_PHY,
                CDM_BOX,
                CM_PASS, CM_SHOOT, CM_COMPLETE,
                CAM_FINISH, CAM_SPEED,
                ST_PACE, ST_POWER, ST_COMPLETE,
                LW_DRIB_SHOOT, LW_DRIB_PACE, LW_DRIB_PASS
        )) BY_ID.put(o.getId(), o);
    }

    public static PositionalTrainingOption findById(String id) {
        return BY_ID.get(id);
    }

    // ── Per-position catalogues ──────────────────────────────────────────────────

    /**
     * Returns the ordered list of training options available for the given position.
     * BALANCED is always option 0.
     */
    public static List<PositionalTrainingOption> getFor(FootballPosition pos) {
        List<PositionalTrainingOption> list = new ArrayList<>();
        list.add(BALANCED);
        switch (pos) {
            case GOALKEEPER -> list.addAll(List.of(GK_DIVING, GK_REFLEXES, GK_DIST));

            case CENTRE_BACK -> list.addAll(List.of(CB_DEF, CB_BUILD));

            case LEFT_BACK,
                 RIGHT_BACK  -> list.addAll(List.of(LB_DEF, LB_ATK, LB_PHY));

            case DEFENSIVE_MIDFIELDER -> list.addAll(List.of(CB_DEF, CB_BUILD, CDM_BOX));

            case CENTRAL_MIDFIELDER,
                 LEFT_MIDFIELDER,
                 RIGHT_MIDFIELDER -> list.addAll(List.of(CM_PASS, CM_SHOOT, CM_COMPLETE));

            case ATTACKING_MIDFIELDER -> list.addAll(List.of(CAM_FINISH, CAM_SPEED));

            case STRIKER,
                 CENTRE_FORWARD -> list.addAll(List.of(ST_PACE, ST_POWER, ST_COMPLETE));

            case LEFT_WINGER,
                 RIGHT_WINGER -> list.addAll(List.of(
                    ST_PACE, ST_POWER, ST_COMPLETE,
                    LW_DRIB_SHOOT, LW_DRIB_PACE, LW_DRIB_PASS));
        }
        return list;
    }

    private static PositionalTrainingOption opt(String id, String name, String desc,
                                                 List<String> attrs, int min, int max) {
        return new PositionalTrainingOption(id, name, desc, attrs, min, max);
    }

    private FootballTrainingOptions() {}
}
