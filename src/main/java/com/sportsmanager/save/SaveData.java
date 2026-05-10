package com.sportsmanager.save;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure data carrier (no behaviour) used by Gson to serialise the entire game
 * state to JSON and back. All cross-references between objects are expressed
 * as IDs (teamId, player index within squad) so the structure is fully flat.
 */
public class SaveData {

    // ── Metadata ─────────────────────────────────────────────────────────────
    public String saveName;
    public String savedAt;       // ISO-8601 timestamp

    // ── Game state ───────────────────────────────────────────────────────────
    public String sport;         // "football"
    public int    seasonNumber;
    public int    currentWeek;
    public String userTeamId;

    public List<TeamDTO> teams = new ArrayList<>();
    public List<MatchWeekDTO> fixture = new ArrayList<>();

    /** teamId → assigned logo file index (1-based). */
    public Map<String, Integer> logoAssignments = new HashMap<>();

    /** Active training plans for the user's squad. */
    public List<TrainingPlanDTO> trainingPlans = new ArrayList<>();

    // ── Nested DTOs ──────────────────────────────────────────────────────────

    public static class TeamDTO {
        public String teamId;
        public String teamName;
        public String formationName; // FootballFormation enum name (e.g. "F_4_3_3"), nullable
        public String tacticName;    // FootballTactic enum name (e.g. "BALANCED"), nullable
        public List<PlayerDTO> players = new ArrayList<>();
    }

    public static class PlayerDTO {
        public String name;
        public int    age;
        public String position;     // position enum name (FootballPosition or HandballPosition)
        public boolean goalkeeper;

        // Football outfield attributes
        public int pace;
        public int shooting;
        public int passing;
        public int dribbling;

        // Shared attribute name across sports
        public int defending;
        public int physical;

        // Football goalkeeper attributes
        public int diving;
        public int handling;
        public int kicking;
        public int reflexes;
        public int positioning;

        // Handball-specific attributes
        public int speed;
        public int throwing;
        public int jumping;
        public int agility;
        public int reach;  // goalkeeper

        public int    injuryGamesRemaining;
        public int    suspensionGamesRemaining;
        public double form = 7.5;
    }

    public static class MatchWeekDTO {
        public int weekNumber;
        public boolean completed;
        public List<MatchDTO> matches = new ArrayList<>();
    }

    public static class MatchDTO {
        public String homeTeamId;
        public String awayTeamId;
        public String status;        // "UNPLAYED" or "FINISHED"
        public boolean hasResult;
        public int homeScore;
        public int awayScore;
    }

    /**
     * Captures one active training assignment.
     * playerTeamId + playerIndex uniquely identify the player within the saved squad.
     */
    public static class TrainingPlanDTO {
        public String playerTeamId;   // must be the user team's id
        public int    playerIndex;    // index within that team's player list
        public String optionId;       // PositionalTrainingOption.getId()
        public int    weeksRemaining;
        public int    totalWeeks;     // Integer.MAX_VALUE means "maintenance"
    }
}
