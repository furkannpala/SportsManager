package com.sportsmanager.handball;

import com.sportsmanager.core.Player;

import java.util.Map;

public class HandballPlayer extends Player {

    // Outfield attributes
    private int speed;
    private int throwing;
    private int jumping;
    private int agility;
    private int defending;
    private int physical;

    // Goalkeeper attributes
    private int reflexes;
    private int diving;
    private int reach;
    private int positioning;

    private final HandballPosition position;

    private double currentStamina = 100.0;
    private double form           = 7.5;

    private HandballPlayer(String name, int age, HandballPosition position) {
        super(name, age);
        this.position = position;
    }

    public static HandballPlayer createOutfield(String name, int age, HandballPosition position,
                                                int speed, int throwing, int jumping,
                                                int agility, int defending, int physical) {
        HandballPlayer p = new HandballPlayer(name, age, position);
        p.speed     = clamp(speed);
        p.throwing  = clamp(throwing);
        p.jumping   = clamp(jumping);
        p.agility   = clamp(agility);
        p.defending = clamp(defending);
        p.physical  = clamp(physical);
        return p;
    }

    public static HandballPlayer createGoalkeeper(String name, int age,
                                                   int speed, int reflexes, int diving,
                                                   int reach, int positioning, int physical) {
        HandballPlayer p = new HandballPlayer(name, age, HandballPosition.GOALKEEPER);
        p.speed       = clamp(speed);
        p.reflexes    = clamp(reflexes);
        p.diving      = clamp(diving);
        p.reach       = clamp(reach);
        p.positioning = clamp(positioning);
        p.physical    = clamp(physical);
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
            case "speed"       -> speed;
            case "throwing"    -> throwing;
            case "jumping"     -> jumping;
            case "agility"     -> agility;
            case "defending"   -> defending;
            case "physical"    -> physical;
            case "reflexes"    -> reflexes;
            case "diving"      -> diving;
            case "reach"       -> reach;
            case "positioning" -> positioning;
            default            -> 0;
        };
    }

    @Override
    public void increaseAttribute(String attributeName, int amount) {
        int updated = clamp(getAttributeValue(attributeName) + amount);
        switch (attributeName.toLowerCase()) {
            case "speed"       -> speed       = updated;
            case "throwing"    -> throwing    = updated;
            case "jumping"     -> jumping     = updated;
            case "agility"     -> agility     = updated;
            case "defending"   -> defending   = updated;
            case "physical"    -> physical    = updated;
            case "reflexes"    -> reflexes    = updated;
            case "diving"      -> diving      = updated;
            case "reach"       -> reach       = updated;
            case "positioning" -> positioning = updated;
        }
    }

    @Override
    public HandballPosition getPosition() { return position; }

    // ── Stamina ───────────────────────────────────────────────────────────────

    public int getCurrentStamina()       { return (int) Math.round(currentStamina); }
    public void drainStamina(double v)   { currentStamina = Math.max(0.0, currentStamina - v); }
    public void recoverStamina(double v) { currentStamina = Math.min(100.0, currentStamina + v); }
    public void resetStamina()           { currentStamina = 100.0; }

    // ── Form ──────────────────────────────────────────────────────────────────

    public double getForm()              { return form; }
    public void setForm(double f)        { form = Math.max(5.0, Math.min(10.0, f)); }
    public void adjustForm(double delta) { setForm(form + delta); }
    public void driftForm() {
        if (form > 7.5)      form = Math.max(7.5, form - 0.08);
        else if (form < 7.5) form = Math.min(7.5, form + 0.08);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int getSpeed()       { return speed; }
    public int getThrowing()    { return throwing; }
    public int getJumping()     { return jumping; }
    public int getAgility()     { return agility; }
    public int getDefending()   { return defending; }
    public int getPhysical()    { return physical; }
    public int getReflexes()    { return reflexes; }
    public int getDiving()      { return diving; }
    public int getReach()       { return reach; }
    public int getPositioning() { return positioning; }

    private static int clamp(int v) { return Math.max(1, Math.min(100, v)); }
}
