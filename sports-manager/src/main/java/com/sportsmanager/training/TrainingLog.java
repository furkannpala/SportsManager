package com.sportsmanager.training;

import com.sportsmanager.core.Player;


public class TrainingLog {

    private final Player player;
    private final String attributeName;
    private final int gain;

    public TrainingLog(Player player, String attributeName, int gain) {
        this.player        = player;
        this.attributeName = attributeName;
        this.gain          = gain;
    }

    public Player getPlayer()         { return player; }
    public String getAttributeName()  { return attributeName; }
    public int getGain()              { return gain; }

    @Override
    public String toString() {
        return player.getName() + "  +" + gain + " " + attributeName;
    }
}
