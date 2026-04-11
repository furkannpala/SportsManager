package com.sportsmanager.training;

import com.sportsmanager.core.TrainingCategory;


public class Coach {

    private final String name;
    private int age;
    private int skillLevel;    // 1–100
    private int experience;    // 1–100
    private TrainingCategory specialization;

    public Coach(String name, int age, int skillLevel, int experience,
                 TrainingCategory specialization) {
        this.name           = name;
        this.age            = age;
        this.skillLevel     = clamp(skillLevel, 1, 100);
        this.experience     = clamp(experience, 1, 100);
        this.specialization = specialization;
    }



    public String getName()                   { return name; }
    public int getAge()                       { return age; }
    public int getSkillLevel()                { return skillLevel; }
    public int getExperience()                { return experience; }
    public TrainingCategory getSpecialization() { return specialization; }



    public void setAge(int age)               { this.age = age; }
    public void setSkillLevel(int skillLevel) { this.skillLevel = clamp(skillLevel, 1, 100); }
    public void setExperience(int exp)        { this.experience = clamp(exp, 1, 100); }
    public void setSpecialization(TrainingCategory s) { this.specialization = s; }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public String toString() {
        return name + " (age " + age + ", skill " + skillLevel
                + ", exp " + experience + ", spec " + specialization + ")";
    }
}
