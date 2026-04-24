package com.sportsmanager.training;

import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.PositionalTrainingOption;

/**
 * Tracks an active training assignment for one player.
 * Lives in SeasonState until weeksRemaining reaches 0.
 */
public final class PlayerTrainingPlan {

    private final FootballPlayer player;
    private final PositionalTrainingOption option;
    private int weeksRemaining;
    private final int totalWeeks;

    /** Creates a new plan; duration is rolled randomly from the option's range. */
    public PlayerTrainingPlan(FootballPlayer player, PositionalTrainingOption option) {
        this.player         = player;
        this.option         = option;
        this.totalWeeks     = option.generateDuration();
        this.weeksRemaining = this.totalWeeks;
    }

    /** For save/load reconstruction. */
    public PlayerTrainingPlan(FootballPlayer player, PositionalTrainingOption option,
                               int weeksRemaining, int totalWeeks) {
        this.player         = player;
        this.option         = option;
        this.weeksRemaining = weeksRemaining;
        this.totalWeeks     = totalWeeks;
    }

    public FootballPlayer           getPlayer()          { return player; }
    public PositionalTrainingOption getOption()          { return option; }
    public int                      getWeeksRemaining()  { return weeksRemaining; }
    public int                      getTotalWeeks()      { return totalWeeks; }
    public boolean                  isComplete()         { return weeksRemaining <= 0; }

    /**
     * Advances the plan by one week.
     * @return true if the plan just completed.
     */
    public boolean tick() {
        if (weeksRemaining > 0) weeksRemaining--;
        return weeksRemaining <= 0;
    }
}
