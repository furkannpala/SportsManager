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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.Comparator;
import java.util.List;

/**
 * Screen 7 — Half-time break: minimal timeline + buttons for substitutions and tactics.
 */
public class BreakView extends VBox {

    private final SeasonState state;
    final Match match;
    final MatchState matchState;
    final MatchEngine engine;

    public BreakView(Match match, MatchState matchState, MatchEngine engine) {
        this.state      = GameManager.getInstance().getState();
        this.match      = match;
        this.matchState = matchState;
        this.engine     = engine;
        setSpacing(16);
        setPadding(new Insets(24));
        buildUI();
    }

    private void buildUI() {
        Team home  = match.getHomeTeam();
        Team away  = match.getAwayTeam();
        Sport sport = state.getCurrentSport();
        Team user  = state.getUserTeam();

        // ── Score header ────────────────────────────────────────────────────────
        Label halftimeLabel = new Label("Half-Time Break");
        halftimeLabel.getStyleClass().add("text-muted");
        halftimeLabel.setStyle("-fx-font-size: 13px;");
        halftimeLabel.setAlignment(Pos.CENTER);
        halftimeLabel.setMaxWidth(Double.MAX_VALUE);

        HBox scoreRow = new HBox(20);
        scoreRow.setAlignment(Pos.CENTER);
        scoreRow.getStyleClass().add("scoreboard");

        Label homeName = new Label(home.getTeamName());
        homeName.setStyle("-fx-text-fill: #00d2ff; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label scoreLabel = new Label(matchState.getHomeScore() + " - " + matchState.getAwayScore());
        scoreLabel.getStyleClass().add("score-text");
        Label awayName = new Label(away.getTeamName());
        awayName.setStyle("-fx-text-fill: #e94560; -fx-font-size: 18px; -fx-font-weight: bold;");
        scoreRow.getChildren().addAll(homeName, scoreLabel, awayName);

        // ── Minimal timeline ────────────────────────────────────────────────────
        VBox timelineCard = new VBox(4);
        timelineCard.getStyleClass().add("card");
        timelineCard.setPadding(new Insets(12));

        // Column headers
        HBox colHeaders = new HBox();
        Label homeHeader = new Label(home.getTeamName());
        homeHeader.getStyleClass().add("text-muted");
        homeHeader.setStyle("-fx-font-size: 11px;");
        homeHeader.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(homeHeader, Priority.ALWAYS);

        Label awayHeader = new Label(away.getTeamName());
        awayHeader.getStyleClass().add("text-muted");
        awayHeader.setStyle("-fx-font-size: 11px;");
        awayHeader.setAlignment(Pos.CENTER_RIGHT);
        awayHeader.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(awayHeader, Priority.ALWAYS);
        colHeaders.getChildren().addAll(homeHeader, awayHeader);
        timelineCard.getChildren().add(colHeaders);

        // Separator
        Region sep = new Region();
        sep.setStyle("-fx-background-color: #333355;");
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);
        timelineCard.getChildren().add(sep);

        // Events
        List<MatchEvent> events = matchState.getEvents().stream()
                .filter(e -> e instanceof FootballMatchEvent fme && isKeyEvent(fme.getEventType()))
                .sorted(Comparator.comparingInt(MatchEvent::getMinute))
                .toList();

        if (events.isEmpty()) {
            Label none = new Label("No key events this half.");
            none.getStyleClass().add("text-muted");
            none.setStyle("-fx-font-size: 12px;");
            none.setMaxWidth(Double.MAX_VALUE);
            none.setAlignment(Pos.CENTER);
            timelineCard.getChildren().add(none);
        } else {
            for (MatchEvent ev : events) {
                FootballMatchEvent fme = (FootballMatchEvent) ev;
                boolean isHome = fme.getTeamId().equals(home.getTeamId());
                timelineCard.getChildren().add(buildMinimalRow(fme, isHome));
            }
        }

        ScrollPane scroll = new ScrollPane(timelineCard);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(200);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // ── Action buttons (substitutions + tactics) ────────────────────────────
        int maxSubs  = sport.getMaxSubstitutions();
        boolean isHomeTeam = user == home;
        int usedSubs = isHomeTeam ? matchState.getHomeSubsUsed() : matchState.getAwaySubsUsed();
        int remaining = maxSubs - usedSubs;

        Button subsBtn = new Button("Substitutions  (" + remaining + " left)");
        subsBtn.getStyleClass().add("btn-secondary");
        subsBtn.setMaxWidth(Double.MAX_VALUE);
        subsBtn.setOnAction(e ->
                ViewManager.getInstance().switchView(new SubstitutionView(match, matchState, engine)));

        Button tacticsBtn = new Button("Tactics");
        tacticsBtn.getStyleClass().add("btn-secondary");
        tacticsBtn.setMaxWidth(Double.MAX_VALUE);
        tacticsBtn.setOnAction(e ->
                ViewManager.getInstance().switchView(new TacticsView(match, matchState, engine)));

        HBox actionRow = new HBox(12, subsBtn, tacticsBtn);
        actionRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(subsBtn, Priority.ALWAYS);
        HBox.setHgrow(tacticsBtn, Priority.ALWAYS);

        // ── Control buttons ─────────────────────────────────────────────────────
        Button continueBtn = new Button("▶ Continue Match");
        continueBtn.getStyleClass().add("btn-primary");
        continueBtn.setOnAction(e -> {
            matchState.setPeriodOver(false);
            engine.simulatePeriod(matchState, home, away);
            if (matchState.isMatchOver()) {
                MatchResult result = engine.finalizeMatch(matchState);
                finishMatch(result);
            } else if (matchState.isPeriodOver()) {
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine));
            }
        });

        Button simToEnd = new Button("⏩ Simulate to End");
        simToEnd.getStyleClass().add("btn-secondary");
        simToEnd.setOnAction(e -> {
            MatchResult result = engine.simulateToEnd(matchState, home, away);
            finishMatch(result);
        });

        HBox controlRow = new HBox(12, continueBtn, simToEnd);
        controlRow.setAlignment(Pos.CENTER);

        getChildren().addAll(halftimeLabel, scoreRow, scroll, actionRow, controlRow);
    }

    private HBox buildMinimalRow(FootballMatchEvent fme, boolean isHome) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(2, 0, 2, 0));
        row.setAlignment(Pos.CENTER);

        Node icon = createIcon(fme.getEventType());

        Label text = new Label(fme.getDescription());
        text.setWrapText(true);
        text.setStyle(descStyle(fme.getEventType()));
        text.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (isHome) {
            HBox.setHgrow(text, Priority.SOMETIMES);
            row.getChildren().addAll(icon, text, spacer);
        } else {
            text.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(text, Priority.SOMETIMES);
            row.getChildren().addAll(spacer, text, icon);
        }
        return row;
    }

    private Node createIcon(FootballEventType type) {
        return switch (type) {
            case GOAL -> {
                Circle ball = new Circle(9);
                ball.setFill(Color.web("#1a1a2e"));
                ball.setStroke(Color.web("#00e676"));
                ball.setStrokeWidth(2.5);
                Circle inner = new Circle(4);
                inner.setFill(Color.web("#00e676"));
                StackPane sp = new StackPane(ball, inner);
                sp.setMinSize(20, 20);
                sp.setMaxSize(20, 20);
                yield sp;
            }
            case YELLOW_CARD -> {
                Rectangle card = new Rectangle(12, 17);
                card.setFill(Color.web("#ffd740"));
                card.setArcWidth(3);
                card.setArcHeight(3);
                StackPane sp = new StackPane(card);
                sp.setMinSize(20, 20);
                sp.setMaxSize(20, 20);
                yield sp;
            }
            case RED_CARD -> {
                Rectangle card = new Rectangle(12, 17);
                card.setFill(Color.web("#ff5252"));
                card.setArcWidth(3);
                card.setArcHeight(3);
                StackPane sp = new StackPane(card);
                sp.setMinSize(20, 20);
                sp.setMaxSize(20, 20);
                yield sp;
            }
            case SUBSTITUTION -> {
                Label up   = new Label("↑");
                up.setStyle("-fx-text-fill: #00e676; -fx-font-size: 11px; -fx-font-weight: bold;");
                Label down = new Label("↓");
                down.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 11px; -fx-font-weight: bold;");
                VBox arrows = new VBox(up, down);
                arrows.setAlignment(Pos.CENTER);
                arrows.setMinSize(20, 20);
                arrows.setMaxSize(20, 20);
                yield arrows;
            }
            default -> {
                StackPane sp = new StackPane();
                sp.setMinSize(20, 20);
                sp.setMaxSize(20, 20);
                yield sp;
            }
        };
    }

    private String descStyle(FootballEventType type) {
        return switch (type) {
            case GOAL         -> "-fx-text-fill: #00e676; -fx-font-weight: bold; -fx-font-size: 12px;";
            case YELLOW_CARD  -> "-fx-text-fill: #ffd740; -fx-font-size: 12px;";
            case RED_CARD     -> "-fx-text-fill: #ff5252; -fx-font-weight: bold; -fx-font-size: 12px;";
            case SUBSTITUTION -> "-fx-text-fill: #aaaacc; -fx-font-size: 12px;";
            default           -> "-fx-font-size: 12px;";
        };
    }

    private boolean isKeyEvent(FootballEventType type) {
        return type == FootballEventType.GOAL
                || type == FootballEventType.YELLOW_CARD
                || type == FootballEventType.RED_CARD
                || type == FootballEventType.SUBSTITUTION;
    }

    void finishMatch(MatchResult result) {
        SeasonState ss = GameManager.getInstance().getState();
        FootballLeague league = (FootballLeague) ss.getLeague();
        league.recordMatchResult(match, result);

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

        GameManager.getInstance().advanceGameCycle();
        Sidebar sidebar = ViewManager.getInstance().getSidebar();
        if (sidebar != null) sidebar.refresh();

        ViewManager.getInstance().switchView(new MatchSummaryView(match, matchState));
    }
}
