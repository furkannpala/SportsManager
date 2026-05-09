package com.sportsmanager.ui;

import com.sportsmanager.core.MatchEvent;
import com.sportsmanager.core.MatchState;
import com.sportsmanager.core.Team;
import com.sportsmanager.football.FootballEventType;
import com.sportsmanager.football.FootballMatchEvent;
import com.sportsmanager.handball.HandballEventType;
import com.sportsmanager.handball.HandballMatchEvent;
import com.sportsmanager.league.Match;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.Comparator;
import java.util.List;

/**
 * Screen shown after a match finishes — full-time score and chronological event timeline.
 */
public class MatchSummaryView extends VBox {

    public MatchSummaryView(Match match, MatchState matchState) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

        setSpacing(16);
        setPadding(new Insets(24));
        setAlignment(Pos.TOP_CENTER);

        // Full Time header
        Label fullTime = new Label("Full Time!");
        fullTime.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #ffd740;");
        fullTime.setAlignment(Pos.CENTER);

        // Team names + score
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

        // Column headers
        HBox colHeaders = new HBox();
        colHeaders.setPadding(new Insets(0, 8, 0, 8));

        Label homeHeader = new Label(home.getTeamName());
        homeHeader.setStyle("-fx-text-fill: #00d2ff; -fx-font-size: 12px; -fx-font-weight: bold;");
        homeHeader.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(homeHeader, Priority.ALWAYS);

        Label awayHeader = new Label(away.getTeamName());
        awayHeader.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12px; -fx-font-weight: bold;");
        awayHeader.setAlignment(Pos.CENTER_RIGHT);
        awayHeader.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(awayHeader, Priority.ALWAYS);

        colHeaders.getChildren().addAll(homeHeader, awayHeader);

        // Timeline
        VBox timeline = new VBox(4);
        timeline.setPadding(new Insets(4, 8, 4, 8));

        List<MatchEvent> events = matchState.getEvents().stream()
                .filter(e -> {
                    if (e instanceof FootballMatchEvent fme) return isKeyFootballEvent(fme.getEventType());
                    if (e instanceof HandballMatchEvent hme) return isKeyHandballEvent(hme.getEventType());
                    return false;
                })
                .sorted(Comparator.comparingInt(MatchEvent::getMinute))
                .toList();

        if (events.isEmpty()) {
            Label none = new Label("No key events recorded.");
            none.getStyleClass().add("text-muted");
            none.setAlignment(Pos.CENTER);
            none.setMaxWidth(Double.MAX_VALUE);
            timeline.getChildren().add(none);
        } else {
            for (MatchEvent ev : events) {
                boolean isHome = ev.getTeamId().equals(home.getTeamId());
                timeline.getChildren().add(buildEventRow(ev, isHome));
            }
        }

        ScrollPane scroll = new ScrollPane(timeline);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Back button
        Button backBtn = new Button("Back to Dashboard");
        backBtn.getStyleClass().add("btn-primary");
        backBtn.setOnAction(e -> ViewManager.getInstance().switchView(new DashboardView()));

        getChildren().addAll(fullTime, scoreRow, colHeaders, scroll, backBtn);
    }

    private HBox buildEventRow(MatchEvent ev, boolean isHome) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(3, 0, 3, 0));

        Node icon = createIcon(ev);

        Label desc = new Label(ev.getDescription());
        desc.setWrapText(true);
        desc.setStyle(descStyle(ev));
        desc.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (isHome) {
            HBox.setHgrow(desc, Priority.SOMETIMES);
            row.getChildren().addAll(icon, desc, spacer);
            row.setAlignment(Pos.CENTER_LEFT);
        } else {
            desc.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(desc, Priority.SOMETIMES);
            row.getChildren().addAll(spacer, desc, icon);
            row.setAlignment(Pos.CENTER_RIGHT);
        }

        return row;
    }

    private boolean isKeyFootballEvent(FootballEventType type) {
        return type == FootballEventType.GOAL
                || type == FootballEventType.YELLOW_CARD
                || type == FootballEventType.RED_CARD
                || type == FootballEventType.SUBSTITUTION;
    }

    private boolean isKeyHandballEvent(HandballEventType type) {
        return type == HandballEventType.GOAL
                || type == HandballEventType.YELLOW_CARD
                || type == HandballEventType.RED_CARD
                || type == HandballEventType.TWO_MIN_SUSPENSION;
    }

    private Node createIcon(MatchEvent ev) {
        boolean isGoal = (ev instanceof FootballMatchEvent fme && fme.getEventType() == FootballEventType.GOAL)
                || (ev instanceof HandballMatchEvent hme && hme.getEventType() == HandballEventType.GOAL);
        boolean isYellow = (ev instanceof FootballMatchEvent fme && fme.getEventType() == FootballEventType.YELLOW_CARD)
                || (ev instanceof HandballMatchEvent hme && hme.getEventType() == HandballEventType.YELLOW_CARD);
        boolean isRed = (ev instanceof FootballMatchEvent fme && fme.getEventType() == FootballEventType.RED_CARD)
                || (ev instanceof HandballMatchEvent hme && hme.getEventType() == HandballEventType.RED_CARD);
        boolean isSub = ev instanceof FootballMatchEvent fme && fme.getEventType() == FootballEventType.SUBSTITUTION;
        boolean is2Min = ev instanceof HandballMatchEvent hme && hme.getEventType() == HandballEventType.TWO_MIN_SUSPENSION;

        if (isGoal) {
            Circle ball = new Circle(9);
            ball.setFill(Color.web("#1a1a2e"));
            ball.setStroke(Color.web("#00e676"));
            ball.setStrokeWidth(2.5);
            Circle inner = new Circle(4);
            inner.setFill(Color.web("#00e676"));
            StackPane sp = new StackPane(ball, inner);
            sp.setMinSize(20, 20); sp.setMaxSize(20, 20);
            return sp;
        }
        if (isYellow) {
            Rectangle card = new Rectangle(12, 17);
            card.setFill(Color.web("#ffd740")); card.setArcWidth(3); card.setArcHeight(3);
            StackPane sp = new StackPane(card);
            sp.setMinSize(20, 20); sp.setMaxSize(20, 20);
            return sp;
        }
        if (isRed) {
            Rectangle card = new Rectangle(12, 17);
            card.setFill(Color.web("#ff5252")); card.setArcWidth(3); card.setArcHeight(3);
            StackPane sp = new StackPane(card);
            sp.setMinSize(20, 20); sp.setMaxSize(20, 20);
            return sp;
        }
        if (isSub) {
            Label up = new Label("↑");
            up.setStyle("-fx-text-fill: #00e676; -fx-font-size: 11px; -fx-font-weight: bold;");
            Label down = new Label("↓");
            down.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 11px; -fx-font-weight: bold;");
            VBox arrows = new VBox(up, down);
            arrows.setAlignment(Pos.CENTER);
            arrows.setMinSize(20, 20); arrows.setMaxSize(20, 20);
            return arrows;
        }
        if (is2Min) {
            Label lbl = new Label("2'");
            lbl.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 10px; -fx-font-weight: bold;");
            StackPane sp = new StackPane(lbl);
            sp.setMinSize(20, 20); sp.setMaxSize(20, 20);
            return sp;
        }
        StackPane sp = new StackPane();
        sp.setMinSize(20, 20); sp.setMaxSize(20, 20);
        return sp;
    }

    private String descStyle(MatchEvent ev) {
        if (ev instanceof FootballMatchEvent fme) {
            return switch (fme.getEventType()) {
                case GOAL         -> "-fx-text-fill: #00e676; -fx-font-weight: bold; -fx-font-size: 13px;";
                case YELLOW_CARD  -> "-fx-text-fill: #ffd740; -fx-font-size: 13px;";
                case RED_CARD     -> "-fx-text-fill: #ff5252; -fx-font-weight: bold; -fx-font-size: 13px;";
                case SUBSTITUTION -> "-fx-text-fill: #aaaacc; -fx-font-size: 13px;";
                default           -> "-fx-font-size: 12px;";
            };
        }
        if (ev instanceof HandballMatchEvent hme) {
            return switch (hme.getEventType()) {
                case GOAL               -> "-fx-text-fill: #00e676; -fx-font-weight: bold; -fx-font-size: 13px;";
                case YELLOW_CARD        -> "-fx-text-fill: #ffd740; -fx-font-size: 13px;";
                case RED_CARD           -> "-fx-text-fill: #ff5252; -fx-font-weight: bold; -fx-font-size: 13px;";
                case TWO_MIN_SUSPENSION -> "-fx-text-fill: #ff9800; -fx-font-size: 13px;";
                default                 -> "-fx-font-size: 12px;";
            };
        }
        return "-fx-font-size: 12px;";
    }
}
