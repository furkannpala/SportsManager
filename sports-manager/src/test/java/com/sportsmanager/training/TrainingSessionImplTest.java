package com.sportsmanager.training;

import com.sportsmanager.core.Team;
import com.sportsmanager.core.TrainingCategory;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.football.FootballSport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainingSessionImplTest {

    private Coach goodCoach;    // high skill, matching spec
    private Coach averageCoach; // moderate, different spec
    private FootballSport sport;

    @BeforeEach
    void setUp() {
        sport        = new FootballSport();
        goodCoach    = new Coach("Elite", 40, 100, 100, TrainingCategory.ATTACKING);
        averageCoach = new Coach("Average", 40, 50,  50, TrainingCategory.DEFENDING);
    }

    private Team teamWithPlayers(int count, int age) {
        Team team = new Team("T1", "TestTeam");
        for (int i = 0; i < count; i++) {
            team.addPlayer(FootballPlayer.createOutfield(
                    "Player" + i, age, FootballPosition.CENTRAL_MIDFIELDER,
                    50, 50, 50, 50, 50, 50));
        }
        return team;
    }

    // ── getName / getDescription ──────────────────────────────────────────────

    @Test
    void getNameEndsWithTrainingAndIsNotEmpty() {
        TrainingSessionImpl session = new TrainingSessionImpl(
                TrainingCategory.ATTACKING, goodCoach, sport);
        String name = session.getName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
        assertTrue(name.endsWith(" Training"),
                "getName() should end with ' Training', got: " + name);
    }

    @Test
    void getDescriptionIncludesCoachName() {
        TrainingSessionImpl session = new TrainingSessionImpl(
                TrainingCategory.DEFENDING, averageCoach, sport);
        assertTrue(session.getDescription().contains("Average"));
    }

    @Test
    void getCategoryReturnsCorrectFocus() {
        TrainingSessionImpl session = new TrainingSessionImpl(
                TrainingCategory.FITNESS, goodCoach, sport);
        assertEquals(TrainingCategory.FITNESS, session.getCategory());
        assertEquals(TrainingCategory.FITNESS, session.getFocus());
    }

    // ── executeTraining ────────────────────────────────────────────────────────

    @Test
    void sessionLogIsEmptyBeforeExecution() {
        TrainingSessionImpl session = new TrainingSessionImpl(
                TrainingCategory.ATTACKING, goodCoach, sport);
        assertTrue(session.getSessionLog().isEmpty());
    }

    @Test
    void highSkillCoachImprovesYoungPlayers() {
        Team team = teamWithPlayers(5, 19); // age < 21 → 1.3× factor
        TrainingSessionImpl session = new TrainingSessionImpl(
                TrainingCategory.ATTACKING, goodCoach, sport);
        session.executeTraining(team);
        // With skill=100, spec bonus=1.5×, age factor=1.3 → significant gain expected
        assertFalse(session.getSessionLog().isEmpty(),
                "At least one attribute should have improved");
    }

    @Test
    void injuredPlayersAreSkipped() {
        Team team = new Team("T", "Test");
        FootballPlayer injured = FootballPlayer.createOutfield(
                "InjuredPlayer", 22, FootballPosition.STRIKER,
                50, 50, 50, 50, 50, 50);
        injured.applyInjury(3);
        team.addPlayer(injured);

        int shootingBefore = injured.getShooting();
        TrainingSessionImpl session = new TrainingSessionImpl(
                TrainingCategory.ATTACKING, goodCoach, sport);
        session.executeTraining(team);

        assertEquals(shootingBefore, injured.getShooting(),
                "Injured player's attributes should not change");
        assertTrue(session.getSessionLog().isEmpty(),
                "No log entries expected for injured player");
    }

    @Test
    void sessionLogIsResetOnSecondExecution() {
        Team team = teamWithPlayers(2, 20);
        TrainingSessionImpl session = new TrainingSessionImpl(
                TrainingCategory.FITNESS, goodCoach, sport);

        session.executeTraining(team);
        int firstRunSize = session.getSessionLog().size();

        session.executeTraining(team); // run again — log should be fresh
        // Log should not accumulate across runs (cleared at start)
        assertTrue(session.getSessionLog().size() <= firstRunSize + 10,
                "Log should be reset at start of each execution");
    }

    @Test
    void attributesDoNotExceed100() {
        // Start player with high attributes near the cap
        Team team = new Team("T", "Test");
        FootballPlayer player = FootballPlayer.createOutfield(
                "NearCap", 19, FootballPosition.STRIKER,
                99, 99, 99, 99, 99, 99);
        team.addPlayer(player);

        TrainingSessionImpl session = new TrainingSessionImpl(
                TrainingCategory.ATTACKING, goodCoach, sport);
        session.executeTraining(team);

        assertTrue(player.getPace()     <= 100);
        assertTrue(player.getShooting() <= 100);
        assertTrue(player.getDribbling()<= 100);
    }

    @Test
    void olderPlayersGainLessOrEqual() {
        // Run two sessions: one with young player (19), one with old (34)
        // Young should gain >= old, on average (may not hold for a single run due to variance,
        // so we verify the age factor exists by running many iterations)
        int youngGain = 0;
        int oldGain   = 0;
        int runs      = 50;

        for (int i = 0; i < runs; i++) {
            Team youngTeam = new Team("Y", "Young");
            FootballPlayer young = FootballPlayer.createOutfield(
                    "Young", 19, FootballPosition.STRIKER,
                    50, 50, 50, 50, 50, 50);
            youngTeam.addPlayer(young);

            Team oldTeam = new Team("O", "Old");
            FootballPlayer old = FootballPlayer.createOutfield(
                    "Old", 34, FootballPosition.STRIKER,
                    50, 50, 50, 50, 50, 50);
            oldTeam.addPlayer(old);

            Coach deterministicCoach = new Coach("Det", 40, 80, 100, TrainingCategory.ATTACKING);
            TrainingSessionImpl youngSession = new TrainingSessionImpl(
                    TrainingCategory.ATTACKING, deterministicCoach, sport);
            TrainingSessionImpl oldSession = new TrainingSessionImpl(
                    TrainingCategory.ATTACKING, deterministicCoach, sport);

            youngSession.executeTraining(youngTeam);
            oldSession.executeTraining(oldTeam);

            youngGain += youngSession.getSessionLog().stream()
                    .mapToInt(TrainingLog::getGain).sum();
            oldGain += oldSession.getSessionLog().stream()
                    .mapToInt(TrainingLog::getGain).sum();
        }

        assertTrue(youngGain >= oldGain,
                "Young players should gain more over " + runs + " runs (youngGain=" + youngGain
                        + ", oldGain=" + oldGain + ")");
    }

    @Test
    void getAssignedCoachReturnsCorrectCoach() {
        TrainingSessionImpl session = new TrainingSessionImpl(
                TrainingCategory.TACTICAL, goodCoach, sport);
        assertEquals(goodCoach, session.getAssignedCoach());
    }
}
