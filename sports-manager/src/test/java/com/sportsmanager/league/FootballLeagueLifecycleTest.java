package com.sportsmanager.league;

import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.Team;
import com.sportsmanager.football.FootballSport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FootballLeague season lifecycle:
 * playMatchWeek, advanceWeek, isSeasonOver, getStandings.
 */
class FootballLeagueLifecycleTest {

    private List<Team> teams;
    private FootballLeague league;

    @BeforeEach
    void setUp() {
        teams = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            teams.add(new Team("T" + i, "Team" + i));
        }
        league = new FootballLeague(teams, new FootballSport());
        league.generateFixture();
    }

    @Test
    void initialWeekIs1() {
        assertEquals(1, league.getCurrentWeek());
    }

    @Test
    void playMatchWeekWithoutFixtureShouldThrow() {
        FootballLeague fresh = new FootballLeague(teams, new FootballSport());
        assertThrows(IllegalStateException.class, fresh::playMatchWeek);
    }

    @Test
    void playMatchWeekCompletesTheWeek() {
        league.playMatchWeek();
        assertTrue(league.getFixture().getWeek(1).isCompleted());
    }

    @Test
    void playMatchWeekSetsMatchesAsFinished() {
        league.playMatchWeek();
        for (Match match : league.getFixture().getWeek(1).getMatches()) {
            assertEquals(MatchStatus.FINISHED, match.getStatus());
            assertNotNull(match.getResult());
        }
    }

    @Test
    void advanceWeekIncrementsCurrentWeek() {
        league.advanceWeek();
        assertEquals(2, league.getCurrentWeek());
    }

    @Test
    void seasonIsNotOverAtWeek1() {
        assertFalse(league.isSeasonOver());
    }

    @Test
    void seasonIsOverAfter38Advances() {
        for (int i = 0; i < 38; i++) {
            league.advanceWeek();
        }
        assertTrue(league.isSeasonOver());
    }

    @Test
    void getStandingsReturns20Teams() {
        assertEquals(20, league.getStandings().size());
    }

    @Test
    void standingsContainAllTeams() {
        List<Team> sorted = league.getStandings();
        assertTrue(sorted.containsAll(teams));
    }

    @Test
    void recordMatchResultUpdatesStandings() {
        Match match = league.getFixture().getWeek(1).getMatches().get(0);
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

        league.recordMatchResult(match,
                new MatchResult(3, 0, List.of()));

        assertEquals(3, league.getStandingsObject().getRecord(home).getPoints());
        assertEquals(0, league.getStandingsObject().getRecord(away).getPoints());
    }

    @Test
    void getTeamsReturnsCopy() {
        List<Team> copy = league.getTeams();
        assertEquals(20, copy.size());
        copy.clear(); // modifying copy should not affect league
        assertEquals(20, league.getTeams().size());
    }
}
