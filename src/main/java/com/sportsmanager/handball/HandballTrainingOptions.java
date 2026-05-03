package com.sportsmanager.handball;

import com.sportsmanager.football.PositionalTrainingOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HandballTrainingOptions {

    public static final PositionalTrainingOption BALANCED = opt(
            "H_BALANCED", "Balanced Training",
            "Slowly but evenly develops all attributes.",
            List.of(), 10, 16);

    // ── Goalkeeper ───────────────────────────────────────────────────────────────

    static final PositionalTrainingOption HGK_REFLEXES = opt(
            "HGK_REFLEXES", "Reflex Training",
            "Fast reaction saves and shot-reading.",
            List.of("reflexes", "diving"), 3, 5);

    static final PositionalTrainingOption HGK_REACH = opt(
            "HGK_REACH", "Reach & Positioning",
            "Maximise coverage with optimal body placement.",
            List.of("reach", "positioning"), 3, 5);

    static final PositionalTrainingOption HGK_DIST = opt(
            "HGK_DIST", "Quick Distribution",
            "Fast throw-outs to launch counter-attacks.",
            List.of("speed", "physical"), 4, 6);

    // ── Wing (LW / RW) ───────────────────────────────────────────────────────────

    static final PositionalTrainingOption HW_SPRINT = opt(
            "HW_SPRINT", "Wing Sprint",
            "Explosive pace and agility on the flanks.",
            List.of("speed", "agility"), 3, 4);

    static final PositionalTrainingOption HW_FINISH = opt(
            "HW_FINISH", "Finishing",
            "Accuracy and jump power for wing shots.",
            List.of("throwing", "jumping"), 3, 5);

    static final PositionalTrainingOption HW_COMPLETE = opt(
            "HW_COMPLETE", "Complete Wing",
            "All-round wing game: speed, throwing, and agility.",
            List.of("speed", "throwing", "agility"), 4, 6);

    // ── Back (LB / RB) ───────────────────────────────────────────────────────────

    static final PositionalTrainingOption HB_THROW = opt(
            "HB_THROW", "Power Shooting",
            "Heavy long-range throwing and physical strength.",
            List.of("throwing", "physical"), 3, 5);

    static final PositionalTrainingOption HB_DRIVE = opt(
            "HB_DRIVE", "Driving Play",
            "Speed and throwing combination for break plays.",
            List.of("speed", "throwing"), 3, 4);

    static final PositionalTrainingOption HB_DEF = opt(
            "HB_DEF", "Defensive Back",
            "Defensive tracking and physical presence.",
            List.of("defending", "physical"), 4, 5);

    // ── Center Back ──────────────────────────────────────────────────────────────

    static final PositionalTrainingOption HCB_VISION = opt(
            "HCB_VISION", "Playmaker",
            "Agility and throwing for creative offensive play.",
            List.of("throwing", "agility"), 3, 5);

    static final PositionalTrainingOption HCB_ATTACK = opt(
            "HCB_ATTACK", "Attack Leader",
            "Speed and throwing for decisive break-through.",
            List.of("throwing", "speed"), 3, 4);

    static final PositionalTrainingOption HCB_COMPLETE = opt(
            "HCB_COMPLETE", "Complete Playmaker",
            "Full offensive toolkit: throw, agility, and pace.",
            List.of("throwing", "agility", "speed"), 4, 6);

    // ── Pivot ────────────────────────────────────────────────────────────────────

    static final PositionalTrainingOption HP_SCREEN = opt(
            "HP_SCREEN", "Screen Play",
            "Physical blocks and jump advantage at the 6m line.",
            List.of("physical", "jumping"), 3, 5);

    static final PositionalTrainingOption HP_FINISH = opt(
            "HP_FINISH", "Pivot Finishing",
            "Close-range throwing and agility in tight spaces.",
            List.of("throwing", "agility"), 3, 4);

    static final PositionalTrainingOption HP_COMPLETE = opt(
            "HP_COMPLETE", "Complete Pivot",
            "All-round pivot game: physical, jump, and throw.",
            List.of("physical", "jumping", "throwing"), 4, 6);

    // ── Registry ─────────────────────────────────────────────────────────────────

    private static final Map<String, PositionalTrainingOption> BY_ID = new HashMap<>();
    static {
        for (PositionalTrainingOption o : List.of(
                BALANCED,
                HGK_REFLEXES, HGK_REACH, HGK_DIST,
                HW_SPRINT, HW_FINISH, HW_COMPLETE,
                HB_THROW, HB_DRIVE, HB_DEF,
                HCB_VISION, HCB_ATTACK, HCB_COMPLETE,
                HP_SCREEN, HP_FINISH, HP_COMPLETE
        )) BY_ID.put(o.getId(), o);
    }

    public static PositionalTrainingOption findById(String id) { return BY_ID.get(id); }

    public static List<PositionalTrainingOption> getFor(HandballPosition pos) {
        List<PositionalTrainingOption> list = new ArrayList<>();
        list.add(BALANCED);
        switch (pos) {
            case GOALKEEPER              -> list.addAll(List.of(HGK_REFLEXES, HGK_REACH, HGK_DIST));
            case LEFT_WING, RIGHT_WING   -> list.addAll(List.of(HW_SPRINT, HW_FINISH, HW_COMPLETE));
            case LEFT_BACK, RIGHT_BACK   -> list.addAll(List.of(HB_THROW, HB_DRIVE, HB_DEF));
            case CENTER_BACK             -> list.addAll(List.of(HCB_VISION, HCB_ATTACK, HCB_COMPLETE));
            case PIVOT                   -> list.addAll(List.of(HP_SCREEN, HP_FINISH, HP_COMPLETE));
        }
        return list;
    }

    private static PositionalTrainingOption opt(String id, String name, String desc,
                                                 List<String> attrs, int min, int max) {
        return new PositionalTrainingOption(id, name, desc, attrs, min, max);
    }

    private HandballTrainingOptions() {}
}
