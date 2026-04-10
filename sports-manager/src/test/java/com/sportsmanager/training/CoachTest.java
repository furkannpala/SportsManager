package com.sportsmanager.training;

import com.sportsmanager.core.TrainingCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoachTest {

    @Test
    void constructorStoresFieldsCorrectly() {
        Coach coach = new Coach("Jose", 60, 85, 90, TrainingCategory.TACTICAL);
        assertEquals("Jose", coach.getName());
        assertEquals(60, coach.getAge());
        assertEquals(85, coach.getSkillLevel());
        assertEquals(90, coach.getExperience());
        assertEquals(TrainingCategory.TACTICAL, coach.getSpecialization());
    }

    @Test
    void skillLevelIsClamped_BelowMin() {
        Coach coach = new Coach("Low", 40, -10, 50, TrainingCategory.FITNESS);
        assertEquals(1, coach.getSkillLevel());
    }

    @Test
    void skillLevelIsClamped_AboveMax() {
        Coach coach = new Coach("High", 40, 999, 50, TrainingCategory.FITNESS);
        assertEquals(100, coach.getSkillLevel());
    }

    @Test
    void experienceIsClamped_BelowMin() {
        Coach coach = new Coach("Rookie", 25, 50, 0, TrainingCategory.ATTACKING);
        assertEquals(1, coach.getExperience());
    }

    @Test
    void experienceIsClamped_AboveMax() {
        Coach coach = new Coach("Veteran", 70, 50, 200, TrainingCategory.DEFENDING);
        assertEquals(100, coach.getExperience());
    }

    @Test
    void setSkillLevelClampsValue() {
        Coach coach = new Coach("Test", 35, 50, 50, TrainingCategory.FITNESS);
        coach.setSkillLevel(150);
        assertEquals(100, coach.getSkillLevel());
        coach.setSkillLevel(-5);
        assertEquals(1, coach.getSkillLevel());
    }

    @Test
    void setExperienceClampsValue() {
        Coach coach = new Coach("Test", 35, 50, 50, TrainingCategory.FITNESS);
        coach.setExperience(200);
        assertEquals(100, coach.getExperience());
        coach.setExperience(0);
        assertEquals(1, coach.getExperience());
    }

    @Test
    void setAgeAndSpecializationWork() {
        Coach coach = new Coach("Test", 35, 50, 50, TrainingCategory.FITNESS);
        coach.setAge(45);
        coach.setSpecialization(TrainingCategory.ATTACKING);
        assertEquals(45, coach.getAge());
        assertEquals(TrainingCategory.ATTACKING, coach.getSpecialization());
    }

    @Test
    void toStringContainsName() {
        Coach coach = new Coach("Pep", 52, 95, 95, TrainingCategory.TACTICAL);
        assertTrue(coach.toString().contains("Pep"));
    }
}
