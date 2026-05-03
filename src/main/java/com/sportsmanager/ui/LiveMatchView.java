package com.sportsmanager.ui;

import com.sportsmanager.core.*;
import com.sportsmanager.football.FootballEventType;
import com.sportsmanager.football.FootballMatchEvent;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.handball.HandballPlayer;
import com.sportsmanager.handball.HandballPosition;
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

    // ── Substitution overlay (EA FC style — click only) ──────────────────────────

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

        Player[] selOut = {null};
        Player[] selIn  = {null};

        FormationPitchView pitchView = new FormationPitchView(null);
        VBox benchPanel = new VBox(6);
        ScrollPane benchScroll = new ScrollPane(benchPanel);
        benchScroll.setFitToWidth(true);
        benchScroll.setPrefHeight(280);
        benchScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Label statusLbl = new Label("Click a player on the pitch to substitute");
        statusLbl.setStyle("-fx-text-fill: #aaaacc; -fx-font-size: 11px;");
        statusLbl.setWrapText(true);

        Button confirmBtn = new Button("Confirm Substitution");
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setDisable(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> resumeAfterOverlay());

        Runnable[] refreshPitch = {null};
        Runnable[] refreshBench = {null};

        refreshPitch[0] = () -> pitchView.redrawWithPlayers(
                userTeam.getFormation(), fieldPlayers, selOut[0],
                player -> {
                    if (player == selOut[0]) {
                        selOut[0] = null; selIn[0] = null;
                        statusLbl.setText("Click a player on the pitch to substitute");
                        confirmBtn.setDisable(true);
                    } else {
                        selOut[0] = player; selIn[0] = null;
                        statusLbl.setText("Out: " + player.getName() + "  — pick a bench player →");
                        confirmBtn.setDisable(true);
                    }
                    refreshPitch[0].run();
                    refreshBench[0].run();
                });

        refreshBench[0] = () -> {
            benchPanel.getChildren().clear();
            List<Player> candidates = selOut[0] != null
                    ? filterBenchByZone(bench, getPosition(selOut[0]))
                    : bench;
            if (candidates.isEmpty()) {
                Label none = new Label("No compatible bench players available");
                none.getStyleClass().add("text-muted");
                benchPanel.getChildren().add(none);
                return;
            }
            for (Player p : candidates) {
                benchPanel.getChildren().add(
                        buildBenchCard(p, selOut, selIn, refreshBench, statusLbl, confirmBtn));
            }
        };

        confirmBtn.setOnAction(e -> {
            if (selOut[0] == null || selIn[0] == null) return;
            boolean ok = matchState.makeSubstitution(userTeam.getTeamId(), selOut[0], selIn[0], maxSubs);
            if (!ok) { statusLbl.setText("Substitution failed."); return; }
            matchState.addEvent(new FootballMatchEvent(
                    FootballEventType.SUBSTITUTION, matchState.getCurrentMinute(),
                    selOut[0], selIn[0], userTeam.getTeamId()));
            resumeAfterOverlay();
            showSubstitutionToast(selOut[0], selIn[0]);
        });

        // Layout
        VBox leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(16, 10, 16, 16));
        leftPanel.setAlignment(Pos.TOP_CENTER);
        Label title = new Label("Substitution  ·  " + (maxSubs - usedSubs) + " remaining");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");
        leftPanel.getChildren().addAll(title, new StackPane(pitchView), statusLbl, cancelBtn);

        VBox rightPanel = new VBox(8);
        rightPanel.setPadding(new Insets(16, 16, 16, 10));
        rightPanel.setPrefWidth(280);
        Label benchTitle = new Label("Bench Players");
        benchTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");
        rightPanel.getChildren().addAll(benchTitle, benchScroll, confirmBtn);
        VBox.setVgrow(benchScroll, Priority.ALWAYS);

        HBox content = new HBox(0, leftPanel, rightPanel);
        VBox panel = new VBox(content);
        panel.getStyleClass().add("card");
        panel.setMaxWidth(620);

        refreshPitch[0].run();
        refreshBench[0].run();
        showOverlay(panel);
    }

    private HBox buildBenchCard(Player p,
                                 Player[] selOut, Player[] selIn,
                                 Runnable[] refreshBench,
                                 Label statusLbl, Button confirmBtn) {
        boolean sel   = (p == selIn[0]);
        boolean avail = p.isAvailable();

        HBox card = new HBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setStyle(benchCardStyle(sel, false));

        String[] np = p.getName().split("\\s+");
        String initials = np.length >= 2
                ? ("" + np[0].charAt(0) + np[1].charAt(0)).toUpperCase()
                : p.getName().substring(0, Math.min(2, p.getName().length())).toUpperCase();
        Label avatar = new Label(initials);
        avatar.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #00d2ff;"
                + " -fx-background-color: #0f3460; -fx-background-radius: 50;"
                + " -fx-min-width: 30; -fx-min-height: 30; -fx-max-width: 30; -fx-max-height: 30;"
                + " -fx-alignment: center;");

        VBox info = new VBox(2);
        Label name = new Label(p.getName());
        name.setStyle("-fx-text-fill: " + (sel ? "#00e676" : "#e0e0ff")
                + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label posOvr = new Label(positionName(p) + "  OVR " + p.getOverallRating());
        posOvr.setStyle("-fx-text-fill: #888899; -fx-font-size: 10px;");
        StackPane staminaBar = StaminaBar.create(p, 70, 4);
        info.getChildren().addAll(name, posOvr, staminaBar);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(avatar, info, spacer);

        if (!avail) {
            card.setOpacity(0.5);
            return card;
        }

        Label check = new Label(sel ? "✓" : "");
        check.setStyle("-fx-text-fill: #00e676; -fx-font-size: 14px; -fx-font-weight: bold;");
        card.getChildren().add(check);

        card.setOnMouseClicked(e -> {
            if (selOut[0] == null) return;
            selIn[0] = p;
            confirmBtn.setDisable(false);
            statusLbl.setText("Out: " + selOut[0].getName() + "\nIn:  " + p.getName());
            refreshBench[0].run();
        });
        card.setOnMouseEntered(e -> { if (!sel) card.setStyle(benchCardStyle(false, true)); });
        card.setOnMouseExited(e -> { if (!sel) card.setStyle(benchCardStyle(false, false)); });
        return card;
    }

    private String benchCardStyle(boolean selected, boolean hovered) {
        if (selected)
            return "-fx-background-color: #1a3a1a; -fx-background-radius: 6;"
                    + " -fx-border-color: #00e676; -fx-border-width: 1.5; -fx-border-radius: 6; -fx-cursor: hand;";
        if (hovered)
            return "-fx-background-color: #222240; -fx-background-radius: 6; -fx-cursor: hand;";
        return "-fx-background-color: #1a1a2e; -fx-background-radius: 6; -fx-cursor: hand;";
    }

    private List<Player> filterBenchByZone(List<Player> bench, Position outPos) {
        if (outPos == null) return bench;
        List<Player> same = bench.stream()
                .filter(p -> isSameZone(getPosition(p), outPos))
                .toList();
        return same.isEmpty() ? bench : same;
    }

    // ── Position helpers ──────────────────────────────────────────────────────────

    private Position getPosition(Player p) {
        if (p instanceof FootballPlayer fp) return fp.getPosition();
        if (p instanceof HandballPlayer hp) return hp.getPosition();
        return null;
    }

    private String positionName(Player p) {
        if (p instanceof FootballPlayer fp) return fp.getPosition().getName();
        if (p instanceof HandballPlayer hp) return hp.getPosition().getName();
        return "—";
    }

    private boolean isSameZone(Position a, Position b) {
        if (a == null || b == null) return true;
        if (a instanceof FootballPosition fa && b instanceof FootballPosition fb) {
            if (fa == FootballPosition.GOALKEEPER || fb == FootballPosition.GOALKEEPER) return fa == fb;
            return fa.isDefensive() == fb.isDefensive()
                    && fa.isMidfield()  == fb.isMidfield()
                    && fa.isAttacking() == fb.isAttacking();
        }
        if (a instanceof HandballPosition ha && b instanceof HandballPosition hb) {
            if (ha.isGoalkeeper() || hb.isGoalkeeper()) return ha == hb;
            return ha.isWing()  == hb.isWing()
                    && ha.isBack()  == hb.isBack()
                    && ha.isPivot() == hb.isPivot();
        }
        return true;
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

    // ── Tactics overlay (EA FC style) ────────────────────────────────────────────

    private void openTacticsOverlay() {
        pauseForOverlay();

        Sport sport    = state.getCurrentSport();
        Team userTeam  = state.getUserTeam();
        boolean isHome = userTeam == home;
        List<Player> fieldPlayers = isHome
                ? matchState.getHomeFieldPlayers()
                : matchState.getAwayFieldPlayers();

        // Left: pitch
        FormationPitchView pitchView = new FormationPitchView(null);
        pitchView.redrawWithPlayers(userTeam.getFormation(), fieldPlayers, null, null);

        VBox leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(16, 10, 16, 16));
        leftPanel.setAlignment(Pos.TOP_CENTER);
        Label pitchTitle = new Label("Formation");
        pitchTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");
        leftPanel.getChildren().addAll(pitchTitle, new StackPane(pitchView));

        // Right: controls
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(16, 16, 16, 10));
        rightPanel.setPrefWidth(220);

        Label formLabel = new Label("Formation");
        formLabel.getStyleClass().add("text-muted");

        ComboBox<String> formBox = new ComboBox<>();
        for (Formation f : sport.getFormations()) formBox.getItems().add(f.getFormationName());
        if (userTeam.getFormation() != null) formBox.setValue(userTeam.getFormation().getFormationName());
        formBox.setMaxWidth(Double.MAX_VALUE);
        formBox.setOnAction(e -> {
            for (Formation f : sport.getFormations()) {
                if (f.getFormationName().equals(formBox.getValue())) {
                    userTeam.setFormation(f);
                    pitchView.redrawWithPlayers(f, fieldPlayers, null, null);
                    break;
                }
            }
        });

        Label styleLabel = new Label("Playing Style");
        styleLabel.getStyleClass().add("text-muted");
        styleLabel.setPadding(new Insets(6, 0, 0, 0));

        VBox tacticBtns = new VBox(6);
        for (Tactic t : sport.getTactics()) {
            Button btn = new Button(t.getTacticName());
            btn.getStyleClass().add("btn-tactic");
            btn.setMaxWidth(Double.MAX_VALUE);
            if (userTeam.getTactic() != null
                    && userTeam.getTactic().getTacticName().equals(t.getTacticName()))
                btn.getStyleClass().add("btn-tactic-active");
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

        rightPanel.getChildren().addAll(formLabel, formBox, styleLabel, tacticBtns, doneBtn);

        HBox content = new HBox(0, leftPanel, rightPanel);
        VBox panel = new VBox(content);
        panel.getStyleClass().add("card");
        panel.setMaxWidth(580);

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
