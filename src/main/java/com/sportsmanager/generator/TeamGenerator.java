package com.sportsmanager.generator;

import com.sportsmanager.core.Position;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.core.TrainingCategory;
import com.sportsmanager.training.Coach;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Procedurally generates 20 teams with tier-balanced squads and coaches.

 *   Top (3–4 teams):  tierMean 80–85 – championship contenders
 *   Mid-high (5–6):   tierMean 70–79
 *   Mid (5–6):        tierMean 60–69
 *   Bottom (4–6):     tierMean 50–59 – relegation zone
 *
 */
public class TeamGenerator {

    private final int SQUAD_SIZE;
    private static final int ATTRIBUTE_STD_DEV = 8;

    private final Sport sport;
    private final PlayerFactory playerFactory;
    private final NameGenerator nameGenerator;
    private final Random random;

    public TeamGenerator(Sport sport, PlayerFactory playerFactory,
                         NameGenerator nameGenerator) {
        this.sport          = sport;
        this.playerFactory  = playerFactory;
        this.nameGenerator  = nameGenerator;
        this.random         = new Random();
        this.SQUAD_SIZE     = sport.getSquadSize();
    }

    // ── League generation ─────────────────────────────────────────────────────

    /**
     * Generates exactly 20 teams with tier-based attribute distributions.
     * Returns the list in a random order suitable for the fixture draw.
     */
    public List<Team> generateLeague() {
        List<TierSpec> tiers = buildTierSpecs();
        List<Team> teams = new ArrayList<>();

        for (TierSpec tier : tiers) {
            for (int i = 0; i < tier.count; i++) {
                teams.add(generateTeam(tier.mean));
            }
        }

        // Shuffle so that strong teams are not always at fixed positions
        java.util.Collections.shuffle(teams);
        return teams;
    }




    public Team generateTeam(int tierMean) {
        String teamId   = "team_" + System.nanoTime() + "_" + random.nextInt(9999);
        String teamName = nameGenerator.generateTeamName();
        Team team = new Team(teamId, teamName);

        List<Position> positions = sport.getPositions();
        if (positions.isEmpty()) return team;

        for (int i = 0; i < SQUAD_SIZE; i++) {
            Position pos  = positions.get(i % positions.size());
            String name   = nameGenerator.generatePersonName();
            int age       = 17 + random.nextInt(19); // 17–35
            team.addPlayer(playerFactory.createPlayer(name, age, pos, tierMean));
        }

        return team;
    }




    public Coach generateCoach(int tierMean) {
        String name    = nameGenerator.generatePersonName();
        int age        = 35 + random.nextInt(31);              // 35–65
        int skillLevel = gaussianClamped(tierMean * 0.9, 10, 20, 99);
        int experience = 30 + random.nextInt(61);              // 30–90 (tier-independent)

        List<TrainingCategory> options = List.of(TrainingCategory.values());
        TrainingCategory spec = options.get(random.nextInt(options.size()));

        return new Coach(name, age, skillLevel, experience, spec);
    }

    // ── Private helpers ───────────────────────────────────────────────────────


    private List<TierSpec> buildTierSpecs() {
        List<TierSpec> tiers = new ArrayList<>();
        int total = sport.getLeagueSize();
        
        // Proportions approximately: 10% Elite, 20% Strong, 25% Mid, 25% Lower, 20% Relegation
        int elite = Math.max(1, (int) Math.round(total * 0.10));
        int strong = Math.max(1, (int) Math.round(total * 0.20));
        int mid = Math.max(1, (int) Math.round(total * 0.25));
        int lower = Math.max(1, (int) Math.round(total * 0.25));
        int rel = total - (elite + strong + mid + lower);
        if (rel < 1) { // Fallback just in case
            rel = 1;
            lower = total - (elite + strong + mid + rel);
        }

        tiers.add(new TierSpec(elite, 88));  // Elite — title contenders
        tiers.add(new TierSpec(strong, 79)); // Strong — top-half
        tiers.add(new TierSpec(mid, 70));    // Mid-table
        tiers.add(new TierSpec(lower, 61));  // Lower-mid
        tiers.add(new TierSpec(rel, 50));    // Relegation zone
        return tiers;
    }

    private int gaussianClamped(double mean, double stdDev, int min, int max) {
        int value = (int) Math.round(mean + random.nextGaussian() * stdDev);
        return Math.max(min, Math.min(max, value));
    }



    private static class TierSpec {
        final int count;
        final int mean;
        TierSpec(int count, int mean) { this.count = count; this.mean = mean; }
    }
}
