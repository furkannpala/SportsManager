package com.sportsmanager.league;

import com.sportsmanager.core.Team;
import com.sportsmanager.football.FootballSport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FootballLeague Circle Method fixture generation.
 */
class FixtureGenerationTest {

    private static final int TEAM_COUNT = 20;

    private List<Team> teams;
    private FootballLeague league;

    @BeforeEach
    void setUp() {
        teams = new ArrayList<>();
        for (int i = 1; i <= TEAM_COUNT; i++) {
            teams.add(new Team("T" + i, "Team" + i));
        }
        league = new FootballLeague(teams, new FootballSport());
        league.generateFixture();
    }

    @Test
    void fixtureShouldHave38Weeks() {
        Fixture fixture = league.getFixture();
        assertEquals(38, fixture.getWeeks().size());
        assertEquals(38, fixture.getTotalWeeks());
    }

    @Test
    void eachWeekShouldHave10Matches() {
        Fixture fixture = league.getFixture();
        for (MatchWeek week : fixture.getWeeks()) {
            assertEquals(10, week.getMatches().size(),
                    "Week " + week.getWeekNumber() + " should have 10 matches");
        }
    }

    @Test
    void totalMatchCountShouldBe380() {
        Fixture fixture = league.getFixture();
        long total = fixture.getWeeks().stream()
                .mapToLong(w -> w.getMatches().size())
                .sum();
        assertEquals(380, total);
    }

    @Test
    void eachTeamShouldPlay38MatchesTotal() {
        Fixture fixture = league.getFixture();
        for (Team team : teams) {
            int count = fixture.getMatchesForTeam(team).size();
            assertEquals(38, count, team.getTeamName() + " should play 38 matches");
        }
    }

    @Test
    void eachTeamShouldPlay19HomeAnd19AwayMatches() {
        Fixture fixture = league.getFixture();
        for (Team team : teams) {
            long home = fixture.getMatchesForTeam(team).stream()
                    .filter(m -> m.getHomeTeam().equals(team)).count();
            long away = fixture.getMatchesForTeam(team).stream()
                    .filter(m -> m.getAwayTeam().equals(team)).count();
            assertEquals(19, home, team.getTeamName() + " should have 19 home matches");
            assertEquals(19, away, team.getTeamName() + " should have 19 away matches");
        }
    }

    @Test
    void everyPairShouldMeetExactlyTwice() {
        Fixture fixture = league.getFixture();
        // Count meetings per ordered pair (home, away)
        Map<String, Integer> pairCount = new HashMap<>();
        for (MatchWeek week : fixture.getWeeks()) {
            for (Match match : week.getMatches()) {
                String key = match.getHomeTeam().getTeamId() + "_" + match.getAwayTeam().getTeamId();
                pairCount.merge(key, 1, Integer::sum);
            }
        }
        // Each ordered pair should appear exactly once (so each unordered pair meets twice)
        for (Map.Entry<String, Integer> entry : pairCount.entrySet()) {
            assertEquals(1, entry.getValue(),
                    "Pair " + entry.getKey() + " should appear exactly once as home fixture");
        }
        // Total ordered pairs = 20 * 19 = 380
        assertEquals(380, pairCount.size());
    }

    @Test
    void noTeamShouldPlayItselfInAnyMatch() {
        Fixture fixture = league.getFixture();
        for (MatchWeek week : fixture.getWeeks()) {
            for (Match match : week.getMatches()) {
                assertNotEquals(match.getHomeTeam(), match.getAwayTeam(),
                        "A team cannot play against itself");
            }
        }
    }

    @Test
    void secondHalfShouldBeMirrorOfFirstHalf() {
        Fixture fixture = league.getFixture();
        for (int week = 1; week <= 19; week++) {
            MatchWeek first  = fixture.getWeek(week);
            MatchWeek second = fixture.getWeek(week + 19);
            List<Match> firstMatches  = first.getMatches();
            List<Match> secondMatches = second.getMatches();
            assertEquals(firstMatches.size(), secondMatches.size());
            for (int i = 0; i < firstMatches.size(); i++) {
                assertEquals(firstMatches.get(i).getHomeTeam(), secondMatches.get(i).getAwayTeam(),
                        "Week " + (week + 19) + " match " + i + ": home should be original away");
                assertEquals(firstMatches.get(i).getAwayTeam(), secondMatches.get(i).getHomeTeam(),
                        "Week " + (week + 19) + " match " + i + ": away should be original home");
            }
        }
    }

    @Test
    void noTeamShouldAppearTwiceInSameWeek() {
        Fixture fixture = league.getFixture();
        for (MatchWeek week : fixture.getWeeks()) {
            Map<Team, Integer> appearances = new HashMap<>();
            for (Match match : week.getMatches()) {
                appearances.merge(match.getHomeTeam(), 1, Integer::sum);
                appearances.merge(match.getAwayTeam(), 1, Integer::sum);
            }
            for (Map.Entry<Team, Integer> entry : appearances.entrySet()) {
                assertEquals(1, entry.getValue(),
                        entry.getKey().getTeamName() + " appears more than once in week "
                                + week.getWeekNumber());
            }
        }
    }

    @Test
    void weekNumbersShouldBe1Through38() {
        Fixture fixture = league.getFixture();
        for (int i = 1; i <= 38; i++) {
            MatchWeek week = fixture.getWeek(i);
            assertEquals(i, week.getWeekNumber());
        }
    }

    @Test
    void getWeekWithInvalidIndexShouldThrow() {
        Fixture fixture = league.getFixture();
        assertThrows(IndexOutOfBoundsException.class, () -> fixture.getWeek(0));
        assertThrows(IndexOutOfBoundsException.class, () -> fixture.getWeek(39));
    }

    @Test
    void constructorShouldRejectWrongTeamCount() {
        List<Team> tooFew = teams.subList(0, 10);
        assertThrows(IllegalArgumentException.class,
                () -> new FootballLeague(tooFew, new FootballSport()));
    }

    @Test
    void newMatchesShouldBeUnplayed() {
        Fixture fixture = league.getFixture();
        for (MatchWeek week : fixture.getWeeks()) {
            for (Match match : week.getMatches()) {
                assertEquals(MatchStatus.UNPLAYED, match.getStatus());
                assertNull(match.getResult());
            }
        }
    }
}
