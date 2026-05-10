package com.sportsmanager.training;

import java.util.List;
import java.util.Random;

public final class PositionalTrainingOption {

    private static final Random RANDOM = new Random();

    private final String id;
    private final String name;
    private final String description;
    private final List<String> attributes;
    private final int minWeeks;
    private final int maxWeeks;

    public PositionalTrainingOption(String id, String name, String description,
                                    List<String> attributes, int minWeeks, int maxWeeks) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.attributes  = List.copyOf(attributes);
        this.minWeeks    = minWeeks;
        this.maxWeeks    = maxWeeks;
    }

    public String        getId()             { return id; }
    public String        getName()           { return name; }
    public String        getDescription()    { return description; }
    public List<String>  getAttributes()     { return attributes; }
    public boolean       isBalanced()        { return attributes.isEmpty(); }
    public int           getMinWeeks()       { return minWeeks; }
    public int           getMaxWeeks()       { return maxWeeks; }

    public int generateDuration() {
        if (minWeeks == maxWeeks) return minWeeks;
        return minWeeks + RANDOM.nextInt(maxWeeks - minWeeks + 1);
    }

    public String getDurationDisplay() {
        return minWeeks == maxWeeks ? minWeeks + "w" : minWeeks + "–" + maxWeeks + "w";
    }
}
