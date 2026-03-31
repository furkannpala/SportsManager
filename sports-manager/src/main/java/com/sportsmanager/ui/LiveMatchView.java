package com.sportsmanager.ui;

import com.sportsmanager.core.*;
import com.sportsmanager.football.FootballEventType;
import com.sportsmanager.football.FootballMatchEvent;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.league.FootballLeague;
import com.sportsmanager.league.Match;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Screen 6 — Live match simulation view with scoreboard, commentary, stats.
 */
public class LiveMatchView extends VBox {

    private final SeasonState state;
    private final Match match;
    private final Team home;
    private final Team away;
    private final MatchEngine engine;
    private MatchState matchState;

    private Label homeScoreLabel;
    private Label awayScoreLabel;
    private Label periodLabel;
    private ProgressBar matchProgress;
    private VBox commentaryBox;
    private VBox statsBox;
    private HBox controlsBox;

    public LiveMatchView(Match match) {
        this.state = GameManager.getInstance().getState();
        this.match = match;
        this.home = match.getHomeTeam();
        this.away = match.getAwayTeam();
        this.engine = state.getCurrentSport().createMatchEngine();
        this.matchState = engine.initMatch(home, away);

        setSpacing(12);
        setPadding(new Insets(16));
        buildUI();
    }

    private void buildUI() {
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

        // Period and progress
        HBox periodBar = new HBox(12);
        periodBar.setAlignment(Pos.CENTER);

        periodLabel = new Label("1st Half — 0'");
        periodLabel.getStyleClass().add("text-muted");
        periodLabel.setStyle("-fx-font-size: 13px;");

        int totalMinutes = state.getCurrentSport().getMatchPeriodCount()
                * state.getCurrentSport().getMatchPeriodDurationMinutes();
        matchProgress = new ProgressBar(0);
        matchProgress.setPrefWidth(300);
        matchProgress.setPrefHeight(8);

        periodBar.getChildren().addAll(periodLabel, matchProgress);

        // Center content: commentary + stats
        HBox centerContent = new HBox(16);
        VBox.setVgrow(centerContent, Priority.ALWAYS);

        // Stats panel (left)
        statsBox = new VBox(10);
        statsBox.getStyleClass().add("card");
        statsBox.setPrefWidth(200);
        statsBox.setMinWidth(180);
        Label statsTitle = new Label("📊 Match Stats");
        statsTitle.getStyleClass().add("section-label");
        statsBox.getChildren().add(statsTitle);
        updateStats();

        // Commentary (center)
        commentaryBox = new VBox(6);
        commentaryBox.setPadding(new Insets(8));

        ScrollPane commentaryScroll = new ScrollPane(commentaryBox);
        commentaryScroll.setFitToWidth(true);
        commentaryScroll.setVvalue(1.0);
        HBox.setHgrow(commentaryScroll, Priority.ALWAYS);

        Label commTitle = new Label("📝 Live Commentary");
        commTitle.getStyleClass().add("section-label");
        commTitle.setPadding(new Insets(0, 0, 4, 0));

        VBox commWrapper = new VBox(4);
        commWrapper.getChildren().addAll(commTitle, commentaryScroll);
        HBox.setHgrow(commWrapper, Priority.ALWAYS);
        VBox.setVgrow(commentaryScroll, Priority.ALWAYS);

        centerContent.getChildren().addAll(statsBox, commWrapper);

        // Controls
        controlsBox = new HBox(12);
        controlsBox.setAlignment(Pos.CENTER);

        Button playHalf = new Button("▶ Play to Halftime");
        playHalf.getStyleClass().add("btn-primary");
        playHalf.setOnAction(e -> {
            engine.simulatePeriod(matchState, home, away);
            updateUI();

            if (matchState.isMatchOver()) {
                showMatchEnd();
            } else if (matchState.isPeriodOver()) {
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine));
            }
        });

        Button simToEnd = new Button("⏩ Simulate to End");
        simToEnd.getStyleClass().add("btn-secondary");
        simToEnd.setOnAction(e -> {
            MatchResult result = engine.simulateToEnd(matchState, home, away);
            updateUI();
            showMatchEnd();
        });

        controlsBox.getChildren().addAll(playHalf, simToEnd);

        getChildren().addAll(scoreboard, periodBar, centerContent, controlsBox);
    }

    private void updateUI() {
        homeScoreLabel.setText(String.valueOf(matchState.getHomeScore()));
        awayScoreLabel.setText(String.valueOf(matchState.getAwayScore()));

        int totalMinutes = state.getCurrentSport().getMatchPeriodCount()
                * state.getCurrentSport().getMatchPeriodDurationMinutes();
        double progress = (double) matchState.getCurrentMinute() / totalMinutes;
        matchProgress.setProgress(Math.min(1.0, progress));

        String periodText = matchState.getCurrentPeriod() <= 1 ? "1st Half" : "2nd Half";
        periodLabel.setText(periodText + " — " + matchState.getCurrentMinute() + "'");

        updateCommentary();
        updateStats();
    }

    private void updateCommentary() {
        commentaryBox.getChildren().clear();
        List<MatchEvent> events = matchState.getEvents();

        for (MatchEvent event : events) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label desc = new Label(event.getDescription());
            desc.setWrapText(true);

            if (event instanceof FootballMatchEvent fme) {
                switch (fme.getEventType()) {
                    case GOAL -> {
                        row.getStyleClass().add("commentary-goal");
                        desc.setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold;");
                    }
                    case YELLOW_CARD -> {
                        row.getStyleClass().add("commentary-card-yellow");
                        desc.setStyle("-fx-text-fill: #ffd740;");
                    }
                    case RED_CARD -> {
                        row.getStyleClass().add("commentary-card-red");
                        desc.setStyle("-fx-text-fill: #ff5252; -fx-font-weight: bold;");
                    }
                    default -> {
                        row.getStyleClass().add("commentary-normal");
                        desc.getStyleClass().add("text-normal");
                        desc.setStyle("-fx-font-size: 12px;");
                    }
                }
            } else {
                row.getStyleClass().add("commentary-normal");
                desc.getStyleClass().add("text-normal");
            }

            row.getChildren().add(desc);
            commentaryBox.getChildren().add(row);
        }
    }

    private void updateStats() {
        // Keep the title
        if (statsBox.getChildren().size() > 1) {
            statsBox.getChildren().remove(1, statsBox.getChildren().size());
        }

        List<MatchEvent> events = matchState.getEvents();

        // Calculate stats
        int homeGoals = 0, awayGoals = 0;
        int homeFouls = 0, awayFouls = 0;
        int homeCards = 0, awayCards = 0;
        int homeOffsides = 0, awayOffsides = 0;

        for (MatchEvent ev : events) {
            if (ev instanceof FootballMatchEvent fme) {
                boolean isHome = fme.getTeamId().equals(home.getTeamId());
                switch (fme.getEventType()) {
                    case GOAL -> { if (isHome) homeGoals++; else awayGoals++; }
                    case FOUL -> { if (isHome) homeFouls++; else awayFouls++; }
                    case YELLOW_CARD, RED_CARD -> { if (isHome) homeCards++; else awayCards++; }
                    case OFFSIDE -> { if (isHome) homeOffsides++; else awayOffsides++; }
                    default -> {}
                }
            }
        }

        // Possession estimate
        double homeOVR = home.getSquad().stream().mapToInt(Player::getOverallRating).average().orElse(50);
        double awayOVR = away.getSquad().stream().mapToInt(Player::getOverallRating).average().orElse(50);
        int homePoss = (int) (homeOVR / (homeOVR + awayOVR) * 100);

        addStatRow("Possession", homePoss + "%", (100 - homePoss) + "%", homePoss / 100.0);
        addStatRow("Shots", String.valueOf(homeGoals * 3 + 2), String.valueOf(awayGoals * 3 + 2), 0.5);
        addStatRow("Fouls", String.valueOf(homeFouls), String.valueOf(awayFouls),
                homeFouls + awayFouls > 0 ? (double) homeFouls / (homeFouls + awayFouls) : 0.5);
        addStatRow("Cards", String.valueOf(homeCards), String.valueOf(awayCards), 0.5);
        addStatRow("Offsides", String.valueOf(homeOffsides), String.valueOf(awayOffsides), 0.5);
    }

    private void addStatRow(String label, String homeVal, String awayVal, double ratio) {
        VBox statRow = new VBox(2);
        statRow.setPadding(new Insets(4, 0, 4, 0));

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

        statRow.getChildren().add(values);
        statsBox.getChildren().add(statRow);
    }

    private void showMatchEnd() {
        controlsBox.getChildren().clear();

        // Finalize
        MatchResult result = engine.finalizeMatch(matchState);

        // Record result in league
        SeasonState ss = GameManager.getInstance().getState();
        FootballLeague league = (FootballLeague) ss.getLeague();
        league.recordMatchResult(match, result);

        // Play all other matches this week
        int currentWeek = ss.getCurrentWeek();
        var fixture = ss.getCurrentFixture();
        if (currentWeek <= fixture.getTotalWeeks()) {
            var mw = fixture.getWeek(currentWeek);
            MatchEngine simEngine = ss.getCurrentSport().createMatchEngine();
            for (var m : mw.getMatches()) {
                if (m != match && m.getStatus() == com.sportsmanager.league.MatchStatus.UNPLAYED) {
                    MatchResult r = simEngine.simulateMatch(m.getHomeTeam(), m.getAwayTeam());
                    league.recordMatchResult(m, r);
                }
            }
            mw.setCompleted(true);
        }

        // Advance
        GameManager.getInstance().advanceGameCycle();

        // Refresh sidebar
        Sidebar sidebar = ViewManager.getInstance().getSidebar();
        if (sidebar != null) sidebar.refresh();

        // Show final score overlay
        VBox endPanel = new VBox(12);
        endPanel.setAlignment(Pos.CENTER);

        Label finalLabel = new Label("Full Time!");
        finalLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ffd740;");

        Label finalScore = new Label(matchState.getHomeScore() + " - " + matchState.getAwayScore());
        finalScore.getStyleClass().add("score-text");

        Button backBtn = new Button("Back to Dashboard");
        backBtn.getStyleClass().add("btn-primary");
        backBtn.setOnAction(e -> ViewManager.getInstance().switchView(new DashboardView()));

        endPanel.getChildren().addAll(finalLabel, finalScore, backBtn);
        controlsBox.getChildren().add(endPanel);
    }
}
