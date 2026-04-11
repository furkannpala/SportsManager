package com.sportsmanager.core;


public abstract class Player {

    private final String name;
    private final int age;


    protected boolean injuryStatus;
    protected int injuryGamesRemaining;

    protected boolean suspensionStatus;
    protected int suspensionGamesRemaining;

    protected Player(String name, int age) {
        this.name = name;
        this.age = age;
        this.injuryStatus = false;
        this.injuryGamesRemaining = 0;
        this.suspensionStatus = false;
        this.suspensionGamesRemaining = 0;
    }

    // Abstract methods — implemented differently by each sport


    public abstract int getOverallRating();


    public abstract int getAttributeValue(String name);

    /** Increases the named attribute by the given amount (clamped to 100 by the caller). */
    public abstract void increaseAttribute(String attributeName, int amount);


    public boolean isAvailable() {
        return injuryGamesRemaining == 0 && suspensionGamesRemaining == 0;
    }


    public void decrementInjury() {
        if (injuryGamesRemaining > 0) {
            injuryGamesRemaining--;
            if (injuryGamesRemaining == 0) {
                injuryStatus = false;
            }
        }
    }


    public void applyInjury(int games) {
        if (games > 0) {
            this.injuryStatus = true;
            this.injuryGamesRemaining = games;
        }
    }



    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void applySuspension(int games) {
        if (games > 0) {
            this.suspensionStatus = true;
            this.suspensionGamesRemaining = games;
        }
    }

    public void decrementSuspension() {
        if (suspensionGamesRemaining > 0) {
            suspensionGamesRemaining--;
            if (suspensionGamesRemaining == 0) {
                suspensionStatus = false;
            }
        }
    }

    public boolean isSuspended() {
        return suspensionStatus;
    }

    public int getSuspensionGamesRemaining() {
        return suspensionGamesRemaining;
    }

    public boolean isInjured() {
        return injuryStatus;
    }

    public int getInjuryGamesRemaining() {
        return injuryGamesRemaining;
    }

    @Override
    public String toString() {
        return name + " (age " + age + ", OVR " + getOverallRating() + ")";
    }
}
