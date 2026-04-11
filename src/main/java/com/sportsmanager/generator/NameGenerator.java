package com.sportsmanager.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class NameGenerator {

    private static final String[] FIRST_NAMES = {
        "James", "Carlos", "Luca", "Marco", "David", "Ali", "Yusuf", "Kenji",
        "Rafael", "Andre", "Ivan", "Stefan", "Omar", "Mehmet", "Gabriel",
        "Santiago", "Lukas", "Emil", "Takeshi", "Ricardo", "Paulo", "Niko",
        "Adrian", "Felipe", "Artur", "Diego", "Sergio", "Mateo", "Bruno",
        "Fabio", "Lorenzo", "Tomas", "Viktor", "Andrei", "Hamid", "Jamal",
        "Samuel", "Noah", "Elias", "Jonas", "Leon", "Max", "Jan", "Finn",
        "Luis", "Hugo", "Pedro", "Rui", "Danilo", "Cristian"
    };

    private static final String[] LAST_NAMES = {
        "Silva", "Santos", "Costa", "Müller", "Schmidt", "García", "López",
        "Martínez", "Fernández", "Rossi", "Ferrari", "Esposito", "Tanaka",
        "Yamamoto", "Kobayashi", "Kim", "Lee", "Park", "Chen", "Wang",
        "Petrov", "Ivanov", "Novak", "Kovač", "Popescu", "Yıldız", "Kaya",
        "Demir", "Şahin", "Çelik", "Hassan", "Ahmed", "Ali", "Khan",
        "Patel", "Jones", "Smith", "Williams", "Brown", "Taylor", "Wilson",
        "Johnson", "Davis", "Miller", "Moore", "Anderson", "Jackson", "Martin",
        "White", "Harris"
    };

    private static final String[] TEAM_NAMES = {
        "Golden Lions", "Thunder FC", "Red Eagles", "Storm United", "Iron Hawks",
        "Blue Wolves", "Phoenix Rising", "Silver Stars", "Dark Knights",
        "Royal Cobras", "Emerald City", "Crimson Tide", "Steel Falcons",
        "Neon Tigers", "Arctic Bears", "Desert Foxes", "Ocean Sharks",
        "Mountain Rams", "Copper Bulls", "Jade Dragons"
    };

    private final Set<String> usedPlayerNames = new HashSet<>();
    private final List<String> shuffledTeamNames;
    private int teamNameIndex = 0;
    private final Random random = new Random();

    public NameGenerator() {
        shuffledTeamNames = new ArrayList<>(List.of(TEAM_NAMES));
        Collections.shuffle(shuffledTeamNames);
    }

    /** Generates a unique full name for a player or coach. */
    public String generatePersonName() {
        String name;
        int attempts = 0;
        do {
            String first = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String last  = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            name = first + " " + last;
            attempts++;
        } while (usedPlayerNames.contains(name) && attempts < 1000);
        usedPlayerNames.add(name);
        return name;
    }

    /** Returns the next unique team name from the shuffled pool. */
    public String generateTeamName() {
        if (teamNameIndex >= shuffledTeamNames.size()) {
            Collections.shuffle(shuffledTeamNames);
            teamNameIndex = 0;
        }
        return shuffledTeamNames.get(teamNameIndex++);
    }

    /** Resets used-name tracking (call at the start of a new game). */
    public void reset() {
        usedPlayerNames.clear();
        teamNameIndex = 0;
        Collections.shuffle(shuffledTeamNames);
    }
}
