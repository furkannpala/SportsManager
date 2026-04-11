package com.sportsmanager.ui;

import com.sportsmanager.core.Team;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.league.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * Screen 8 — Season fixture view with week-by-week match display.
 */
public class FixtureView extends VBox {

    private final SeasonState state;
    private int displayWeek;

    public FixtureView() {
        this.state = GameManager.getInstance().getState();
        this.displayWeek = state.getCurrentWeek();
        setSpacing(16);
        setPadding(new Insets(20));
        buildUI();
    }

    private void buildUI() {
        getChildren().clear();

        Fixture fixture = state.getCurrentFixture();
        Team userTeam = state.getUserTeam();
        int totalWeeks = fixture.getTotalWeeks();

        // Clamp display week
        displayWeek = Math.max(1, Math.min(displayWeek, totalWeeks));

        // Header
        Label title = new Label("📅 Season Fixture");
        title.getStyleClass().add("title-label");

        // Week navigator
        HBox weekNav = new HBox(16);
        weekNav.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("◀");
        prevBtn.getStyleClass().add("btn-secondary");
        prevBtn.setDisable(displayWeek <= 1);
        prevBtn.setOnAction(e -> {
            displayWeek--;
            buildUI();
        });

        Label weekLabel = new Label("Week " + displayWeek);
        weekLabel.getStyleClass().add("subtitle-label");

        Button nextBtn = new Button("▶");
        nextBtn.getStyleClass().add("btn-secondary");
        nextBtn.setDisable(displayWeek >= totalWeeks);
        nextBtn.setOnAction(e -> {
            displayWeek++;
            buildUI();
        });

        weekNav.getChildren().addAll(prevBtn, weekLabel, nextBtn);

        // Match cards
        MatchWeek mw = fixture.getWeek(displayWeek);
        VBox matchList = new VBox(8);
        matchList.setPadding(new Insets(8));

        for (Match m : mw.getMatches()) {
            HBox matchCard = createMatchCard(m, userTeam);
            matchList.getChildren().add(matchCard);
        }

        ScrollPane scroll = new ScrollPane(matchList);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Week progress bar
        HBox progressBar = new HBox(3);
        progressBar.setAlignment(Pos.CENTER);
        progressBar.setPadding(new Insets(8, 0, 0, 0));

        for (int w = 1; w <= totalWeeks; w++) {
            Region dot = new Region();
            MatchWeek week = fixture.getWeek(w);
            if (week.isCompleted()) {
                dot.getStyleClass().add("week-dot-completed");
            } else if (w == state.getCurrentWeek()) {
                dot.getStyleClass().add("week-dot-current");
            } else {
                dot.getStyleClass().add("week-dot-future");
            }
            progressBar.getChildren().add(dot);
        }

        ScrollPane progressScroll = new ScrollPane(progressBar);
        progressScroll.setFitToWidth(true);
        progressScroll.setMaxHeight(30);
        progressScroll.setStyle("-fx-background-color: transparent;");

        getChildren().addAll(title, weekNav, scroll, progressScroll);
    }

    private HBox createMatchCard(Match m, Team userTeam) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 20, 12, 20));

        boolean isUserMatch = m.getHomeTeam() == userTeam || m.getAwayTeam() == userTeam;
        if (isUserMatch) {
            card.getStyleClass().addAll("match-card", "match-card-user");
        } else {
            card.getStyleClass().add("match-card");
        }

        // Home team
        Node homeLogo = LogoManager.getInstance().createLogoNode(m.getHomeTeam(), 28);
        Label homeName = new Label(m.getHomeTeam().getTeamName());
        homeName.getStyleClass().add("text-normal");
        homeName.setStyle("-fx-font-weight: bold;");
        homeName.setAlignment(Pos.CENTER_RIGHT);
        HBox homeBox = new HBox(8, homeLogo, homeName);
        homeBox.setAlignment(Pos.CENTER_RIGHT);
        homeBox.setMinWidth(180);

        // Score or preview
        Label scoreLbl;
        if (m.getStatus() == MatchStatus.FINISHED && m.getResult() != null) {
            scoreLbl = new Label(m.getResult().getHomeScore() + " - " + m.getResult().getAwayScore());
            scoreLbl.setStyle("-fx-text-fill: #00d2ff; -fx-font-weight: bold; -fx-font-size: 16px;");
        } else {
            scoreLbl = new Label("vs");
            scoreLbl.getStyleClass().add("text-muted");
            scoreLbl.setStyle("-fx-font-size: 14px;");
        }
        scoreLbl.setMinWidth(60);
        scoreLbl.setAlignment(Pos.CENTER);

        // Away team
        Node awayLogo = LogoManager.getInstance().createLogoNode(m.getAwayTeam(), 28);
        Label awayName = new Label(m.getAwayTeam().getTeamName());
        awayName.getStyleClass().add("text-normal");
        awayName.setStyle("-fx-font-weight: bold;");
        HBox awayBox = new HBox(8, awayName, awayLogo);
        awayBox.setAlignment(Pos.CENTER_LEFT);
        awayBox.setMinWidth(180);

        card.getChildren().addAll(homeBox, scoreLbl, awayBox);

        // Status icon
        if (m.getStatus() == MatchStatus.FINISHED) {
            Label checkmark = new Label("✓");
            checkmark.setStyle("-fx-text-fill: #00e676; -fx-font-size: 14px;");
            card.getChildren().add(checkmark);
        } else if (isUserMatch && displayWeek == state.getCurrentWeek()) {
            Button playBtn = new Button("Play");
            playBtn.getStyleClass().add("btn-primary");
            playBtn.setStyle("-fx-padding: 6 16; -fx-font-size: 12px;");
            playBtn.setOnAction(e -> {
                ViewManager.getInstance().switchView(new PreMatchSetupView(m));
            });
            card.getChildren().add(playBtn);
        }

        return card;
    }
}
