package com.sportsmanager.ui;

import com.sportsmanager.core.*;
import com.sportsmanager.football.FootballEventType;
import com.sportsmanager.football.FootballMatchEvent;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.league.FootballLeague;
import com.sportsmanager.league.Match;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;

/**
 * Screen 6 — Live match view with real-time simulation and in-match overlays.
 */
public class LiveMatchView extends StackPane {

    private static final double[] SPEEDS     = { 1.0, 0.5, 0.25, 0.1 };
    private static final String[] SPEED_LBLS = { "1×", "2×", "4×", "10×" };

    private final SeasonState state;
    private final Match match;
    private final Team home;
    private final Team away;
    private final MatchEngine engine;
    private final MatchState matchState;
    private final int totalMinutes;

    // Scoreboard labels
    private Label homeScoreLabel;
    private Label awayScoreLabel;
    private Label periodLabel;
    private ProgressBar matchProgress;

    // Commentary
    private ScrollPane commentaryScroll;
    private VBox commentaryBox;
    private int lastRenderedEvents = 0;

    // Stats
    private VBox statsBox;

    // Ticker
    private Timeline ticker;
    private int speedIndex = 0;
    private boolean wasRunning = false;

    // Controls
    private Button kickOffBtn;
    private Button pauseResumeBtn;
    private Button subsBtn;
    private Button tacticsBtn;
    private Button skipHalfBtn;

    // Overlay layer (shown over the match content)
    private VBox overlayLayer;

    public LiveMatchView(Match match) {
        this.state        = GameManager.getInstance().getState();
        this.match        = match;
        this.home         = match.getHomeTeam();
        this.away         = match.getAwayTeam();
        this.engine       = state.getCurrentSport().createMatchEngine();
        this.matchState   = engine.initMatch(home, away);
        this.totalMinutes = state.getCurrentSport().getMatchPeriodCount()
                          * state.getCurrentSport().getMatchPeriodDurationMinutes();
        build();
    }

    public LiveMatchView(Match match, MatchState matchState, MatchEngine engine) {
        this.state        = GameManager.getInstance().getState();
        this.match        = match;
        this.home         = match.getHomeTeam();
        this.away         = match.getAwayTeam();
        this.engine       = engine;
        this.matchState   = matchState;
        this.totalMinutes = state.getCurrentSport().getMatchPeriodCount()
                          * state.getCurrentSport().getMatchPeriodDurationMinutes();
        build();
        lastRenderedEvents = 0;
        appendNewCommentary();
        refreshUI();
    }

    // ── Build ────────────────────────────────────────────────────────────────────

    private void build() {
        VBox main = buildMain();
        overlayLayer = new VBox();
        overlayLayer.setVisible(false);
        overlayLayer.setAlignment(Pos.CENTER);
        overlayLayer.setStyle("-fx-background-color: rgba(10,10,30,0.82);");

        getChildren().addAll(main, overlayLayer);
        // Disable sidebar navigation during a live match
        Sidebar sidebar = ViewManager.getInstance().getSidebar();
        if (sidebar != null) sidebar.setDisable(true);
    }

    private VBox buildMain() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));

        // Scoreboard
        HBox scoreboard = new HBox(20);
        scoreboard.getStyleClass().add("scoreboard");
        scoreboard.setAlignment(Pos.CENTER);

        Label homeNameLbl = new Label(home.getTeamName());
        homeNameLbl.setStyle("-fx-text-fill: #00d2ff; -fx-font-size: 18px; -fx-font-weight: bold;");
        homeScoreLabel = new Label("0");
        homeScoreLabel.getStyleClass().add("score-text");
        Label dash = new Label("-");
        dash.getStyleClass().add("score-dash");
        awayScoreLabel = new Label("0");
        awayScoreLabel.getStyleClass().add("score-text");
        Label awayNameLbl = new Label(away.getTeamName());
        awayNameLbl.setStyle("-fx-text-fill: #e94560; -fx-font-size: 18px; -fx-font-weight: bold;");
        scoreboard.getChildren().addAll(homeNameLbl, homeScoreLabel, dash, awayScoreLabel, awayNameLbl);

        // Period bar
        HBox periodBar = new HBox(12);
        periodBar.setAlignment(Pos.CENTER);
        periodLabel = new Label("1st Half — 0'");
        periodLabel.getStyleClass().add("text-muted");
        periodLabel.setStyle("-fx-font-size: 13px;");
        matchProgress = new ProgressBar(0);
        matchProgress.setPrefWidth(300);
        matchProgress.setPrefHeight(8);
        periodBar.getChildren().addAll(periodLabel, matchProgress);

        // Center: stats + commentary
        HBox center = new HBox(16);
        VBox.setVgrow(center, Priority.ALWAYS);

        statsBox = new VBox(10);
        statsBox.getStyleClass().add("card");
        statsBox.setPrefWidth(200);
        statsBox.setMinWidth(180);
        Label statsTitle = new Label("📊 Match Stats");
        statsTitle.getStyleClass().add("section-label");
        statsBox.getChildren().add(statsTitle);
        updateStats();

        commentaryBox = new VBox(6);
        commentaryBox.setPadding(new Insets(8));
        commentaryScroll = new ScrollPane(commentaryBox);
        commentaryScroll.setFitToWidth(true);
        HBox.setHgrow(commentaryScroll, Priority.ALWAYS);

        Label commTitle = new Label("📝 Live Commentary");
        commTitle.getStyleClass().add("section-label");
        commTitle.setPadding(new Insets(0, 0, 4, 0));
        VBox commWrapper = new VBox(4, commTitle, commentaryScroll);
        HBox.setHgrow(commWrapper, Priority.ALWAYS);
        VBox.setVgrow(commentaryScroll, Priority.ALWAYS);

        center.getChildren().addAll(statsBox, commWrapper);

        // Controls
        kickOffBtn = new Button("⚽ Kick Off");
        kickOffBtn.getStyleClass().add("btn-primary");
        kickOffBtn.setOnAction(e -> startTicker());

        pauseResumeBtn = new Button("⏸ Pause");
        pauseResumeBtn.getStyleClass().add("btn-secondary");
        pauseResumeBtn.setVisible(false);
        pauseResumeBtn.setOnAction(e -> togglePause());

        // Speed buttons
        HBox speedBox = new HBox(4);
        speedBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < SPEED_LBLS.length; i++) {
            ToggleButton tb = new ToggleButton(SPEED_LBLS[i]);
            tb.getStyleClass().add("btn-tactic");
            if (i == 0) tb.getStyleClass().add("btn-tactic-active");
            final int idx = i;
            tb.setOnAction(e -> {
                speedIndex = idx;
                speedBox.getChildren().forEach(n -> n.getStyleClass().remove("btn-tactic-active"));
                tb.getStyleClass().add("btn-tactic-active");
                if (ticker != null && ticker.getStatus() == Timeline.Status.RUNNING) {
                    ticker.stop();
                    buildTicker();
                    ticker.play();
                }
            });
            speedBox.getChildren().add(tb);
        }

        subsBtn = new Button("🔄 Subs");
        subsBtn.getStyleClass().add("btn-secondary");
        subsBtn.setVisible(false);
        subsBtn.setOnAction(e -> openSubstitutionOverlay());

        tacticsBtn = new Button("⚙ Tactics");
        tacticsBtn.getStyleClass().add("btn-secondary");
        tacticsBtn.setVisible(false);
        tacticsBtn.setOnAction(e -> openTacticsOverlay());

        skipHalfBtn = new Button("⏭ Half Time");
        skipHalfBtn.getStyleClass().add("btn-secondary");
        skipHalfBtn.setOnAction(e -> {
            stopTicker();
            engine.simulatePeriod(matchState, home, away);
            refreshUI();
            handleTransition();
        });

        Button simToEnd = new Button("⏩ Full Time");
        simToEnd.getStyleClass().add("btn-secondary");
        simToEnd.setOnAction(e -> {
            stopTicker();
            engine.simulateToEnd(matchState, home, away);
            refreshUI();
            handleTransition();
        });

        // Row 1: playback controls
        HBox playbackRow = new HBox(10, kickOffBtn, pauseResumeBtn, speedBox);
        playbackRow.setAlignment(Pos.CENTER);

        // Row 2: match actions
        HBox actionRow = new HBox(10, subsBtn, tacticsBtn, skipHalfBtn, simToEnd);
        actionRow.setAlignment(Pos.CENTER);

        VBox controls = new VBox(6, playbackRow, actionRow);
        controls.setAlignment(Pos.CENTER);

        root.getChildren().addAll(scoreboard, periodBar, center, controls);
        VBox.setVgrow(center, Priority.ALWAYS);
        return root;
    }

    // ── Ticker ───────────────────────────────────────────────────────────────────

    private void startTicker() {
        kickOffBtn.setVisible(false);
        pauseResumeBtn.setVisible(true);
        subsBtn.setVisible(true);
        tacticsBtn.setVisible(true);
        buildTicker();
        ticker.play();
    }

    private void buildTicker() {
        if (ticker != null) ticker.stop();
        ticker = new Timeline(new KeyFrame(Duration.seconds(SPEEDS[speedIndex]), e -> tick()));
        ticker.setCycleCount(Timeline.INDEFINITE);
    }

    private void tick() {
        engine.simulateMinute(matchState, home, away);
        refreshUI();
        handleTransition();
    }

    private void handleTransition() {
        if (matchState.isMatchOver()) {
            stopTicker();
            showMatchEnd();
        } else if (matchState.isPeriodOver()) {
            stopTicker();
            ViewManager.getInstance().switchView(new BreakView(match, matchState, engine));
        }
    }

    private void togglePause() {
        if (ticker == null) return;
        if (ticker.getStatus() == Timeline.Status.RUNNING) {
            ticker.pause();
            pauseResumeBtn.setText("▶ Resume");
        } else {
            ticker.play();
            pauseResumeBtn.setText("⏸ Pause");
        }
    }

    private void stopTicker() {
        if (ticker != null) ticker.stop();
    }

    private void pauseForOverlay() {
        wasRunning = ticker != null && ticker.getStatus() == Timeline.Status.RUNNING;
        if (wasRunning) ticker.pause();
    }

    private void resumeAfterOverlay() {
        closeOverlay();
        if (wasRunning && ticker != null) {
            ticker.play();
            pauseResumeBtn.setText("⏸ Pause");
        }
    }

    // ── Overlay helpers ──────────────────────────────────────────────────────────

    private void showOverlay(VBox content) {
        overlayLayer.getChildren().setAll(content);
        overlayLayer.setVisible(true);
    }

    private void closeOverlay() {
        overlayLayer.setVisible(false);
        overlayLayer.getChildren().clear();
    }

    // ── Substitution overlay ─────────────────────────────────────────────────────

    private void openSubstitutionOverlay() {
        pauseForOverlay();

        Sport sport    = state.getCurrentSport();
        Team userTeam  = state.getUserTeam();
        boolean isHome = userTeam == home;
        int maxSubs    = sport.getMaxSubstitutions();
        int usedSubs   = isHome ? matchState.getHomeSubsUsed() : matchState.getAwaySubsUsed();

        List<Player> fieldPlayers = isHome
                ? matchState.getHomeFieldPlayers()
                : matchState.getAwayFieldPlayers();

        List<Player> bench = isHome
                ? matchState.getHomeBenchPlayers()
                : matchState.getAwayBenchPlayers();

        VBox panel = new VBox(12);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(24));
        panel.setMaxWidth(440);

        Label title = new Label("Substitution");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        Label remLabel = new Label("Remaining: " + (maxSubs - usedSubs) + " / " + maxSubs);
        remLabel.getStyleClass().add("text-muted");

        // ── Player Out ──────────────────────────────────────────────────────────
        Label outLabel = new Label("Player Out");
        outLabel.getStyleClass().add("text-muted");

        ComboBox<String> outBox = new ComboBox<>();
        for (Player p : fieldPlayers) {
            String pos = positionName(p);
            String fatigue = (p.getAge() > 30) ? " · Tired" : "";
            outBox.getItems().add(p.getName() + "  [" + pos + "]" + fatigue + "  OVR " + p.getOverallRating());
        }
        outBox.setMaxWidth(Double.MAX_VALUE);
        outBox.setPromptText("Select player to remove…");

        // Position badge shown when a player is selected
        Label positionBadge = new Label("");
        positionBadge.setStyle("-fx-text-fill: #ffd740; -fx-font-size: 12px; -fx-font-weight: bold;");

        // ── Player In ───────────────────────────────────────────────────────────
        Label inLabel = new Label("Player In");
        inLabel.getStyleClass().add("text-muted");

        ComboBox<String> inBox = new ComboBox<>();
        inBox.setMaxWidth(Double.MAX_VALUE);
        inBox.setPromptText("First select the player going out…");

        // Backing list for inBox — updated when outBox selection changes
        final List<Player>[] filteredBenchRef = new List[]{ List.of() };

        outBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int idx = newVal.intValue();
            inBox.getItems().clear();
            inBox.getSelectionModel().clearSelection();

            if (idx < 0) {
                positionBadge.setText("");
                filteredBenchRef[0] = List.of();
                return;
            }

            Player outPlayer = fieldPlayers.get(idx);
            FootballPosition outPos = footballPosition(outPlayer);
            positionBadge.setText("Position: " + positionName(outPlayer));

            // Same zone first; fall back to full bench
            List<Player> sameZone = bench.stream()
                    .filter(p -> isSameZone(footballPosition(p), outPos))
                    .toList();
            List<Player> filtered = sameZone.isEmpty() ? bench : sameZone;
            filteredBenchRef[0] = filtered;

            for (Player p : filtered) {
                inBox.getItems().add(p.getName() + "  [" + positionName(p) + "]  OVR " + p.getOverallRating());
            }
            if (filtered.isEmpty()) {
                inBox.setPromptText("No bench players available");
            } else {
                inBox.setPromptText("Select player to add…");
            }
        });

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px;");

        Button confirmBtn = new Button("Confirm");
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> {
            int outIdx = outBox.getSelectionModel().getSelectedIndex();
            int inIdx  = inBox.getSelectionModel().getSelectedIndex();
            if (outIdx < 0 || inIdx < 0) { errorLabel.setText("Select both players."); return; }

            Player outPlayer = fieldPlayers.get(outIdx);
            List<Player> filteredBench = filteredBenchRef[0];
            if (inIdx >= filteredBench.size()) { errorLabel.setText("Invalid selection."); return; }
            Player inPlayer = filteredBench.get(inIdx);

            boolean ok = matchState.makeSubstitution(userTeam.getTeamId(), outPlayer, inPlayer, maxSubs);
            if (!ok) { errorLabel.setText("Substitution failed."); return; }

            matchState.addEvent(new FootballMatchEvent(
                    FootballEventType.SUBSTITUTION,
                    matchState.getCurrentMinute(),
                    outPlayer, inPlayer,
                    userTeam.getTeamId()));

            resumeAfterOverlay();
            showSubstitutionToast(outPlayer, inPlayer);
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> resumeAfterOverlay());

        HBox btnRow = new HBox(8, confirmBtn, cancelBtn);
        HBox.setHgrow(confirmBtn, Priority.ALWAYS);
        HBox.setHgrow(cancelBtn, Priority.ALWAYS);

        panel.getChildren().addAll(title, remLabel,
                outLabel, outBox, positionBadge,
                inLabel, inBox,
                btnRow, errorLabel);
        showOverlay(panel);
    }

    // ── Position helpers ──────────────────────────────────────────────────────────

    private FootballPosition footballPosition(Player p) {
        if (p instanceof FootballPlayer fp) return fp.getPosition();
        return null;
    }

    private String positionName(Player p) {
        FootballPosition pos = footballPosition(p);
        return pos != null ? pos.getName() : "—";
    }

    private boolean isSameZone(FootballPosition a, FootballPosition b) {
        if (a == null || b == null) return true;
        if (a == FootballPosition.GOALKEEPER || b == FootballPosition.GOALKEEPER)
            return a == b;
        return a.isDefensive() == b.isDefensive()
                && a.isMidfield()  == b.isMidfield()
                && a.isAttacking() == b.isAttacking();
    }

    // ── Substitution toast ────────────────────────────────────────────────────────

    private void showSubstitutionToast(Player out, Player in) {
        VBox toast = new VBox(6);
        toast.setAlignment(Pos.CENTER);
        toast.setPadding(new Insets(16, 24, 16, 24));
        toast.setMaxWidth(320);
        toast.setStyle(
                "-fx-background-color: #1a1a3e;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #4444aa;" +
                "-fx-border-radius: 12;" +
                "-fx-border-width: 1.5;");

        Label header = new Label("SUBSTITUTION  " + matchState.getCurrentMinute() + "'");
        header.setStyle("-fx-text-fill: #aaaacc; -fx-font-size: 11px; -fx-font-weight: bold;");

        // OUT row
        HBox outRow = new HBox(10);
        outRow.setAlignment(Pos.CENTER);
        Rectangle outRect = new Rectangle(10, 14);
        outRect.setFill(Color.web("#ff5252"));
        outRect.setArcWidth(2); outRect.setArcHeight(2);
        Label downArrow = new Label("↓");
        downArrow.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label outName = new Label(out.getName());
        outName.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label outOvr = new Label("OVR " + out.getOverallRating());
        outOvr.setStyle("-fx-text-fill: #888899; -fx-font-size: 12px;");
        outRow.getChildren().addAll(downArrow, outRect, outName, outOvr);

        // divider
        Region div = new Region();
        div.setStyle("-fx-background-color: #333355;");
        div.setPrefHeight(1);
        div.setMaxWidth(Double.MAX_VALUE);

        // IN row
        HBox inRow = new HBox(10);
        inRow.setAlignment(Pos.CENTER);
        Rectangle inRect = new Rectangle(10, 14);
        inRect.setFill(Color.web("#00e676"));
        inRect.setArcWidth(2); inRect.setArcHeight(2);
        Label upArrow = new Label("↑");
        upArrow.setStyle("-fx-text-fill: #00e676; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label inName = new Label(in.getName());
        inName.setStyle("-fx-text-fill: #00e676; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label inOvr = new Label("OVR " + in.getOverallRating());
        inOvr.setStyle("-fx-text-fill: #888899; -fx-font-size: 12px;");
        inRow.getChildren().addAll(upArrow, inRect, inName, inOvr);

        toast.getChildren().addAll(header, outRow, div, inRow);

        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 80, 0));
        getChildren().add(toast);

        // Auto-dismiss after 3.5 seconds
        Timeline dismiss = new Timeline(new KeyFrame(Duration.seconds(3.5),
                e -> getChildren().remove(toast)));
        dismiss.play();
    }

    // ── Tactics overlay ──────────────────────────────────────────────────────────

    private void openTacticsOverlay() {
        pauseForOverlay();

        Sport sport   = state.getCurrentSport();
        Team userTeam = state.getUserTeam();

        VBox panel = new VBox(12);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(24));
        panel.setMaxWidth(360);

        Label title = new Label("Tactics");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        Label formLabel = new Label("Formation");
        formLabel.getStyleClass().add("text-muted");

        ComboBox<String> formBox = new ComboBox<>();
        for (Formation f : sport.getFormations()) formBox.getItems().add(f.getFormationName());
        if (userTeam.getFormation() != null) formBox.setValue(userTeam.getFormation().getFormationName());
        formBox.setMaxWidth(Double.MAX_VALUE);
        formBox.setOnAction(e -> {
            for (Formation f : sport.getFormations()) {
                if (f.getFormationName().equals(formBox.getValue())) { userTeam.setFormation(f); break; }
            }
        });

        Label styleLabel = new Label("Playing Style");
        styleLabel.getStyleClass().add("text-muted");

        VBox tacticBtns = new VBox(6);
        for (Tactic t : sport.getTactics()) {
            Button btn = new Button(t.getTacticName());
            btn.getStyleClass().add("btn-tactic");
            btn.setMaxWidth(Double.MAX_VALUE);
            if (userTeam.getTactic() != null &&
                    userTeam.getTactic().getTacticName().equals(t.getTacticName())) {
                btn.getStyleClass().add("btn-tactic-active");
            }
            btn.setOnAction(e -> {
                userTeam.setTactic(t);
                tacticBtns.getChildren().forEach(n -> n.getStyleClass().remove("btn-tactic-active"));
                btn.getStyleClass().add("btn-tactic-active");
            });
            tacticBtns.getChildren().add(btn);
        }

        Button doneBtn = new Button("Done");
        doneBtn.getStyleClass().add("btn-primary");
        doneBtn.setMaxWidth(Double.MAX_VALUE);
        doneBtn.setOnAction(e -> resumeAfterOverlay());

        panel.getChildren().addAll(title, formLabel, formBox, styleLabel, tacticBtns, doneBtn);
        showOverlay(panel);
    }

    // ── UI update ────────────────────────────────────────────────────────────────

    private void refreshUI() {
        homeScoreLabel.setText(String.valueOf(matchState.getHomeScore()));
        awayScoreLabel.setText(String.valueOf(matchState.getAwayScore()));
        double progress = (double) matchState.getCurrentMinute() / totalMinutes;
        matchProgress.setProgress(Math.min(1.0, progress));
        String half = matchState.getCurrentPeriod() <= 1 ? "1st Half" : "2nd Half";
        periodLabel.setText(half + " — " + matchState.getCurrentMinute() + "'");
        appendNewCommentary();
        updateStats();
        if (skipHalfBtn != null) {
            skipHalfBtn.setVisible(matchState.getCurrentPeriod() <= 1);
            skipHalfBtn.setManaged(matchState.getCurrentPeriod() <= 1);
        }
    }

    private void appendNewCommentary() {
        List<MatchEvent> events = matchState.getEvents();
        for (int i = lastRenderedEvents; i < events.size(); i++) {
            MatchEvent event = events.get(i);
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            Label desc = new Label(event.getDescription());
            desc.setWrapText(true);
            if (event instanceof FootballMatchEvent fme) {
                switch (fme.getEventType()) {
                    case GOAL         -> { row.getStyleClass().add("commentary-goal");        desc.setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold;"); }
                    case YELLOW_CARD  -> { row.getStyleClass().add("commentary-card-yellow"); desc.setStyle("-fx-text-fill: #ffd740;"); }
                    case RED_CARD     -> { row.getStyleClass().add("commentary-card-red");    desc.setStyle("-fx-text-fill: #ff5252; -fx-font-weight: bold;"); }
                    case SUBSTITUTION -> { row.getStyleClass().add("commentary-normal");      desc.setStyle("-fx-text-fill: #00d2ff; -fx-font-size: 12px;"); }
                    default           -> { row.getStyleClass().add("commentary-normal");      desc.getStyleClass().add("text-normal"); desc.setStyle("-fx-font-size: 12px;"); }
                }
            } else {
                row.getStyleClass().add("commentary-normal");
                desc.getStyleClass().add("text-normal");
            }
            row.getChildren().add(desc);
            commentaryBox.getChildren().add(row);
        }
        lastRenderedEvents = events.size();
        if (lastRenderedEvents > 0) commentaryScroll.setVvalue(1.0);
    }

    private void updateStats() {
        if (statsBox.getChildren().size() > 1)
            statsBox.getChildren().remove(1, statsBox.getChildren().size());

        int homeFouls = 0, awayFouls = 0, homeCards = 0, awayCards = 0,
                homeOffsides = 0, awayOffsides = 0;
        for (MatchEvent ev : matchState.getEvents()) {
            if (ev instanceof FootballMatchEvent fme) {
                boolean h = fme.getTeamId().equals(home.getTeamId());
                switch (fme.getEventType()) {
                    case FOUL                   -> { if (h) homeFouls++;    else awayFouls++; }
                    case YELLOW_CARD, RED_CARD  -> { if (h) homeCards++;    else awayCards++; }
                    case OFFSIDE                -> { if (h) homeOffsides++; else awayOffsides++; }
                    default -> {}
                }
            }
        }

        int homePoss  = matchState.getHomePossession();
        int homeShots = matchState.getHomeShots();
        int awayShots = matchState.getAwayShots();

        addStatRow("Possession", homePoss + "%", (100 - homePoss) + "%", homePoss / 100.0);
        addStatRow("Shots",      String.valueOf(homeShots), String.valueOf(awayShots),
                homeShots + awayShots > 0 ? (double) homeShots / (homeShots + awayShots) : 0.5);
        addStatRow("Fouls",      String.valueOf(homeFouls), String.valueOf(awayFouls),
                homeFouls + awayFouls > 0 ? (double) homeFouls / (homeFouls + awayFouls) : 0.5);
        addStatRow("Cards",      String.valueOf(homeCards),    String.valueOf(awayCards),    0.5);
        addStatRow("Offsides",   String.valueOf(homeOffsides), String.valueOf(awayOffsides), 0.5);
    }

    private void addStatRow(String label, String homeVal, String awayVal, double ratio) {
        HBox values = new HBox();
        Label homeLbl = new Label(homeVal);
        homeLbl.setStyle("-fx-text-fill: #00d2ff; -fx-font-size: 12px;");
        homeLbl.setMinWidth(30);
        Label nameLbl = new Label(label);
        nameLbl.getStyleClass().add("text-muted");
        nameLbl.setStyle("-fx-font-size: 11px;");
        HBox.setHgrow(nameLbl, Priority.ALWAYS);
        nameLbl.setAlignment(Pos.CENTER);
        nameLbl.setMaxWidth(Double.MAX_VALUE);
        Label awayLbl = new Label(awayVal);
        awayLbl.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12px;");
        awayLbl.setMinWidth(30);
        awayLbl.setAlignment(Pos.CENTER_RIGHT);
        values.getChildren().addAll(homeLbl, nameLbl, awayLbl);
        VBox row = new VBox(2);
        row.setPadding(new Insets(4, 0, 4, 0));
        row.getChildren().add(values);
        statsBox.getChildren().add(row);
    }

    // ── Match end ────────────────────────────────────────────────────────────────

    private void showMatchEnd() {
        MatchResult result = engine.finalizeMatch(matchState);
        SeasonState ss = GameManager.getInstance().getState();
        FootballLeague league = (FootballLeague) ss.getLeague();
        league.recordMatchResult(match, result);

        int week = ss.getCurrentWeek();
        var fixture = ss.getCurrentFixture();
        if (week <= fixture.getTotalWeeks()) {
            var mw = fixture.getWeek(week);
            MatchEngine simEngine = ss.getCurrentSport().createMatchEngine();
            for (var m : mw.getMatches()) {
                if (m != match && m.getStatus() == com.sportsmanager.league.MatchStatus.UNPLAYED) {
                    league.recordMatchResult(m, simEngine.simulateMatch(m.getHomeTeam(), m.getAwayTeam()));
                }
            }
            mw.setCompleted(true);
        }

        GameManager.getInstance().advanceGameCycle();
        Sidebar sidebar = ViewManager.getInstance().getSidebar();
        if (sidebar != null) { sidebar.setDisable(false); sidebar.refresh(); }

        ViewManager.getInstance().switchView(new MatchSummaryView(match, matchState));
    }
}
