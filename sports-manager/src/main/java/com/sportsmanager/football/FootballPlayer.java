package com.sportsmanager.football;

import com.sportsmanager.core.Player;

import java.util.Map;

public class FootballPlayer extends Player {

    // --- Outfield attributes ---
    private int pace;
    private int shooting;
    private int passing;
    private int dribbling;
    private int defending;
    private int physical;

    // --- Goalkeeper attributes ---
    private int diving;
    private int handling;
    private int kicking;
    private int reflexes;
    private int positioning;

    private final FootballPosition position;

    private FootballPlayer(String name, int age, FootballPosition position) {
        super(name, age);
        this.position = position;
    }

    /**
     * Creates an outfield player (any position except GOALKEEPER).
     */
    public static FootballPlayer createOutfield(String name, int age, FootballPosition position,
                                                int pace, int shooting, int passing,
                                                int dribbling, int defending, int physical) {
        FootballPlayer p = new FootballPlayer(name, age, position);
        p.pace      = clamp(pace);
        p.shooting  = clamp(shooting);
        p.passing   = clamp(passing);
        p.dribbling = clamp(dribbling);
        p.defending = clamp(defending);
        p.physical  = clamp(physical);
        return p;
    }

    /**
     * Creates a goalkeeper.
     */
    public static FootballPlayer createGoalkeeper(String name, int age,
                                                  int pace, int diving, int handling,
                                                  int kicking, int reflexes, int positioning) {
        FootballPlayer p = new FootballPlayer(name, age, FootballPosition.GOALKEEPER);
        p.pace        = clamp(pace);
        p.diving      = clamp(diving);
        p.handling    = clamp(handling);
        p.kicking     = clamp(kicking);
        p.reflexes    = clamp(reflexes);
        p.positioning = clamp(positioning);
        return p;
    }

    @Override
    public int getOverallRating() {
        Map<String, Double> weights = position.getWeightedAttributes();
        double total = 0.0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            total += getAttributeValue(entry.getKey()) * entry.getValue();
        }
        return clamp((int) Math.round(total));
    }
    @Override
    public int getAttributeValue(String name) {
        return switch (name.toLowerCase()) {
            case "pace"        -> pace;
            case "shooting"    -> shooting;
            case "passing"     -> passing;
            case "dribbling"   -> dribbling;
            case "defending"   -> defending;
            case "physical"    -> physical;
            case "diving"      -> diving;
            case "handling"    -> handling;
            case "kicking"     -> kicking;
            case "reflexes"    -> reflexes;
            case "positioning" -> positioning;
            default            -> 0;
        };
    }
    @Override
    public void increaseAttribute(String attributeName, int amount) {
        int current = getAttributeValue(attributeName);
        int updated = clamp(current + amount);
        switch (attributeName.toLowerCase()) {
            case "pace"        -> pace        = updated;
            case "shooting"    -> shooting    = updated;
            case "passing"     -> passing     = updated;
            case "dribbling"   -> dribbling   = updated;
            case "defending"   -> defending   = updated;
            case "physical"    -> physical    = updated;
            case "diving"      -> diving      = updated;
            case "handling"    -> handling    = updated;
            case "kicking"     -> kicking     = updated;
            case "reflexes"    -> reflexes    = updated;
            case "positioning" -> positioning = updated;
        }
    }
    public FootballPosition getPosition() { return position; }

    public int getPace()        { return pace; }
    public int getShooting()    { return shooting; }
    public int getPassing()     { return passing; }
    public int getDribbling()   { return dribbling; }
    public int getDefending()   { return defending; }
    public int getPhysical()    { return physical; }
    public int getDiving()      { return diving; }
    public int getHandling()    { return handling; }
    public int getKicking()     { return kicking; }
    public int getReflexes()    { return reflexes; }
    public int getPositioning() { return positioning; }

    /**
     * Used by the match engine when an outfield player is forced into goal.
     * Overall is capped at pace * 0.5 to reflect their poor goalkeeping ability.
     */
    public int getEmergencyGoalkeeperRating() {
        return clamp((int) Math.round((defending * 0.5 + physical * 0.3 + pace * 0.2) * 0.6));
    }
    private static int clamp(int value) {
        return Math.max(1, Math.min(100, value));
    }
}
