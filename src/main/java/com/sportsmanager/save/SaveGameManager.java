package com.sportsmanager.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.football.FootballFormation;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.football.FootballSport;
import com.sportsmanager.football.FootballTactic;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.FootballLeague;
import com.sportsmanager.league.Match;
import com.sportsmanager.league.MatchStatus;
import com.sportsmanager.league.MatchWeek;
import com.sportsmanager.league.Standings;
import com.sportsmanager.ui.LogoManager;
import com.sportsmanager.core.MatchResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Persists / restores complete game state as JSON files on disk.
 * Save files live under &lt;user.home&gt;/.sportsmanager/saves/ and use
 * the .json extension. Each file is a fully self-contained snapshot —
 * loading one rebuilds the league, fixture, standings and squads.
 */
public class SaveGameManager {

    private static final String SAVE_DIR_NAME    = ".sportsmanager";
    private static final String SAVE_SUBDIR_NAME = "saves";
    private static final String EXTENSION        = ".json";

    private static SaveGameManager instance;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private SaveGameManager() {}

    public static SaveGameManager getInstance() {
        if (instance == null) instance = new SaveGameManager();
        return instance;
    }

    // ── Disk layout ──────────────────────────────────────────────────────────

    public Path getSavesDirectory() {
        Path dir = Paths.get(System.getProperty("user.home"), SAVE_DIR_NAME, SAVE_SUBDIR_NAME);
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) { }
        return dir;
    }

    private Path filePath(String saveName) {
        String safe = sanitize(saveName);
        return getSavesDirectory().resolve(safe + EXTENSION);
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9 _.-]", "_").trim();
    }

    // ── List ─────────────────────────────────────────────────────────────────

    /** Returns saved games sorted newest first. */
    public List<SaveSummary> listSaves() {
        List<SaveSummary> out = new ArrayList<>();
        Path dir = getSavesDirectory();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().toLowerCase().endsWith(EXTENSION))
                    .forEach(p -> {
                        try {
                            SaveData data = gson.fromJson(Files.readString(p), SaveData.class);
                            if (data != null) {
                                String fileBase = p.getFileName().toString();
                                fileBase = fileBase.substring(0, fileBase.length() - EXTENSION.length());
                                out.add(new SaveSummary(fileBase,
                                        data.saveName != null ? data.saveName : fileBase,
                                        data.savedAt,
                                        findUserTeamName(data),
                                        data.seasonNumber,
                                        data.currentWeek,
                                        data.sport != null ? data.sport : "football"));
                            }
                        } catch (Exception ignored) { /* skip bad file */ }
                    });
        } catch (IOException ignored) { }

        out.sort(Comparator.comparing((SaveSummary s) ->
                s.savedAt == null ? "" : s.savedAt).reversed());
        return out;
    }

    private String findUserTeamName(SaveData data) {
        if (data.userTeamId == null) return "—";
        for (SaveData.TeamDTO t : data.teams) {
            if (data.userTeamId.equals(t.teamId)) return t.teamName;
        }
        return "—";
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    public void delete(String fileBase) {
        try {
            Files.deleteIfExists(getSavesDirectory().resolve(fileBase + EXTENSION));
        } catch (IOException ignored) { }
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    /**
     * Captures the current GameManager state and writes it to disk.
     * Throws IOException on failure.
     */
    public void save(String saveName) throws IOException {
        SeasonState state = GameManager.getInstance().getState();
        if (state == null) throw new IllegalStateException("No game in progress to save.");

        SaveData data = serialize(state, saveName);
        Files.writeString(filePath(saveName), gson.toJson(data));
    }

    private SaveData serialize(SeasonState state, String saveName) {
        SaveData d = new SaveData();
        d.saveName     = saveName;
        d.savedAt      = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        d.sport        = state.getCurrentSport().getSportName().toLowerCase();
        d.seasonNumber = state.getSeasonNumber();
        d.currentWeek  = state.getCurrentWeek();
        d.userTeamId   = state.getUserTeam().getTeamId();

        for (Team t : state.getAllTeams()) {
            d.teams.add(serializeTeam(t));
        }

        Fixture fixture = state.getCurrentFixture();
        if (fixture != null) {
            for (MatchWeek mw : fixture.getWeeks()) {
                d.fixture.add(serializeWeek(mw));
            }
        }

        d.logoAssignments = LogoManager.getInstance().getAssignments();
        return d;
    }

    private SaveData.TeamDTO serializeTeam(Team team) {
        SaveData.TeamDTO dto = new SaveData.TeamDTO();
        dto.teamId   = team.getTeamId();
        dto.teamName = team.getTeamName();
        if (team.getFormation() instanceof FootballFormation ff) dto.formationName = ff.name();
        if (team.getTactic()    instanceof FootballTactic ft)   dto.tacticName    = ft.name();
        for (Player p : team.getSquad()) {
            dto.players.add(serializePlayer(p));
        }
        return dto;
    }

    private SaveData.PlayerDTO serializePlayer(Player p) {
        SaveData.PlayerDTO dto = new SaveData.PlayerDTO();
        dto.name = p.getName();
        dto.age  = p.getAge();
        dto.injuryGamesRemaining     = p.getInjuryGamesRemaining();
        dto.suspensionGamesRemaining = p.getSuspensionGamesRemaining();

        if (p instanceof FootballPlayer fp) {
            dto.position    = fp.getPosition().name();
            dto.goalkeeper  = fp.getPosition() == FootballPosition.GOALKEEPER;
            dto.pace        = fp.getPace();
            dto.shooting    = fp.getShooting();
            dto.passing     = fp.getPassing();
            dto.dribbling   = fp.getDribbling();
            dto.defending   = fp.getDefending();
            dto.physical    = fp.getPhysical();
            dto.diving      = fp.getDiving();
            dto.handling    = fp.getHandling();
            dto.kicking     = fp.getKicking();
            dto.reflexes    = fp.getReflexes();
            dto.positioning = fp.getPositioning();
            dto.form        = fp.getForm();
        }
        return dto;
    }

    private SaveData.MatchWeekDTO serializeWeek(MatchWeek mw) {
        SaveData.MatchWeekDTO dto = new SaveData.MatchWeekDTO();
        dto.weekNumber = mw.getWeekNumber();
        dto.completed  = mw.isCompleted();
        for (Match m : mw.getMatches()) {
            dto.matches.add(serializeMatch(m));
        }
        return dto;
    }

    private SaveData.MatchDTO serializeMatch(Match m) {
        SaveData.MatchDTO dto = new SaveData.MatchDTO();
        dto.homeTeamId = m.getHomeTeam().getTeamId();
        dto.awayTeamId = m.getAwayTeam().getTeamId();
        dto.status     = m.getStatus().name();
        if (m.getResult() != null) {
            dto.hasResult = true;
            dto.homeScore = m.getResult().getHomeScore();
            dto.awayScore = m.getResult().getAwayScore();
        }
        return dto;
    }

    // ── Load ─────────────────────────────────────────────────────────────────

    /**
     * Loads a save file by its base name (without .json extension) and applies
     * the result to the singleton GameManager + LogoManager. Returns true on success.
     */
    public boolean load(String fileBase) throws IOException {
        Path path = getSavesDirectory().resolve(fileBase + EXTENSION);
        if (!Files.exists(path)) return false;

        SaveData data = gson.fromJson(Files.readString(path), SaveData.class);
        if (data == null) return false;

        Sport sport = resolveSport();
        List<Team> teams = new ArrayList<>();
        for (SaveData.TeamDTO td : data.teams) {
            teams.add(deserializeTeam(td));
        }

        Team userTeam = findTeam(teams, data.userTeamId);
        if (userTeam == null && !teams.isEmpty()) userTeam = teams.get(0);

        FootballLeague league = new FootballLeague(teams, sport);
        Fixture fixture = deserializeFixture(data.fixture, teams);
        league.setFixture(fixture);
        league.setCurrentWeek(data.currentWeek);

        // Rebuild standings from finished matches
        Standings standings = league.getStandingsObject();
        for (MatchWeek mw : fixture.getWeeks()) {
            for (Match m : mw.getMatches()) {
                if (m.getStatus() == MatchStatus.FINISHED && m.getResult() != null) {
                    standings.update(m);
                }
            }
        }

        SeasonState state = new SeasonState();
        state.setCurrentSport(sport);
        state.setLeague(league);
        state.setCurrentFixture(fixture);
        state.setCurrentStandings(standings);
        state.setAllTeams(teams);
        state.setUserTeam(userTeam);
        state.setCurrentWeek(data.currentWeek);
        state.setSeasonNumber(data.seasonNumber);

        GameManager.getInstance().loadGame(state, league);
        LogoManager.getInstance().restoreAssignments(data.logoAssignments);
        return true;
    }

    private Sport resolveSport() {
        // Football is currently the only supported sport.
        return new FootballSport();
    }

    private Team deserializeTeam(SaveData.TeamDTO dto) {
        Team team = new Team(dto.teamId, dto.teamName);
        if (dto.formationName != null) {
            try { team.setFormation(FootballFormation.valueOf(dto.formationName)); }
            catch (IllegalArgumentException ignored) { }
        }
        if (dto.tacticName != null) {
            try { team.setTactic(FootballTactic.valueOf(dto.tacticName)); }
            catch (IllegalArgumentException ignored) { }
        }
        for (SaveData.PlayerDTO pd : dto.players) {
            team.addPlayer(deserializePlayer(pd));
        }
        return team;
    }

    private FootballPlayer deserializePlayer(SaveData.PlayerDTO d) {
        FootballPosition position;
        try {
            position = FootballPosition.valueOf(d.position);
        } catch (IllegalArgumentException | NullPointerException e) {
            position = d.goalkeeper ? FootballPosition.GOALKEEPER : FootballPosition.STRIKER;
        }

        FootballPlayer player;
        if (position == FootballPosition.GOALKEEPER) {
            player = FootballPlayer.createGoalkeeper(
                    d.name, d.age,
                    d.pace, d.diving, d.handling, d.kicking, d.reflexes, d.positioning, d.physical);
        } else {
            player = FootballPlayer.createOutfield(
                    d.name, d.age, position,
                    d.pace, d.shooting, d.passing, d.dribbling, d.defending, d.physical);
        }
        if (d.injuryGamesRemaining > 0)     player.applyInjury(d.injuryGamesRemaining);
        if (d.suspensionGamesRemaining > 0) player.applySuspension(d.suspensionGamesRemaining);
        player.setForm(d.form);
        return player;
    }

    private Fixture deserializeFixture(List<SaveData.MatchWeekDTO> weeks, List<Team> teams) {
        Fixture fixture = new Fixture();
        Map<String, Team> byId = new HashMap<>();
        for (Team t : teams) byId.put(t.getTeamId(), t);

        int totalWeeks = 0;
        for (SaveData.MatchWeekDTO wDto : weeks) {
            List<Match> matches = new ArrayList<>();
            for (SaveData.MatchDTO mDto : wDto.matches) {
                Team home = byId.get(mDto.homeTeamId);
                Team away = byId.get(mDto.awayTeamId);
                if (home == null || away == null) continue;
                Match match = new Match(home, away);
                if (mDto.hasResult) {
                    match.setResult(new MatchResult(mDto.homeScore, mDto.awayScore, List.of()));
                }
                try { match.setStatus(MatchStatus.valueOf(mDto.status)); }
                catch (Exception e) { match.setStatus(MatchStatus.UNPLAYED); }
                matches.add(match);
            }
            MatchWeek mw = new MatchWeek(wDto.weekNumber, matches);
            mw.setCompleted(wDto.completed);
            fixture.addWeek(mw);
            totalWeeks = Math.max(totalWeeks, wDto.weekNumber);
        }
        fixture.setTotalWeeks(totalWeeks);
        return fixture;
    }

    private Team findTeam(List<Team> teams, String teamId) {
        if (teamId == null) return null;
        for (Team t : teams) if (teamId.equals(t.getTeamId())) return t;
        return null;
    }

    // ── Summary record (for UI listing) ──────────────────────────────────────

    public record SaveSummary(
            String fileBase,
            String saveName,
            String savedAt,
            String userTeamName,
            int seasonNumber,
            int currentWeek,
            String sportName
    ) {}
}
