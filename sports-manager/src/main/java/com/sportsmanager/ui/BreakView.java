package com.sportsmanager.ui;

import com.sportsmanager.core.*;
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
 * Screen 7 — Half-time / quarter break view with substitutions and tactic changes.
 */
public class BreakView extends VBox {

    private final SeasonState state;
    private final Match match;
    private final MatchState matchState;
    private final MatchEngine engine;
    private final Team userTeam;
    private Label subsRemainingLabel;
    private Label errorLabel;

    public BreakView(Match match, MatchState matchState, MatchEngine engine) {
        this.state = GameManager.getInstance().getState();
        this.match = match;
        this.matchState = matchState;
        this.engine = engine;
        this.userTeam = state.getUserTeam();
        setSpacing(16);
        setPadding(new Insets(20));
        buildUI();
    }

    private void buildUI() {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        Sport sport = state.getCurrentSport();

        // Score header
        HBox scoreHeader = new HBox(16);
        scoreHeader.setAlignment(Pos.CENTER);
        scoreHeader.getStyleClass().add("scoreboard");

        Label homeName = new Label(home.getTeamName());
        homeName.setStyle("-fx-text-fill: #00d2ff; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label score = new Label(matchState.getHomeScore() + " - " + matchState.getAwayScore());
        score.getStyleClass().add("score-text");
        score.setStyle("-fx-font-size: 36px;");
        Label awayName = new Label(away.getTeamName());
        awayName.setStyle("-fx-text-fill: #e94560; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label periodInfo = new Label("Half-Time Break");
        periodInfo.getStyleClass().add("text-muted");
        periodInfo.setStyle("-fx-font-size: 14px;");

        VBox scoreBox = new VBox(4);
        scoreBox.setAlignment(Pos.CENTER);
        HBox scoreRow = new HBox(16);
        scoreRow.setAlignment(Pos.CENTER);
        scoreRow.getChildren().addAll(homeName, score, awayName);
        scoreBox.getChildren().addAll(scoreRow, periodInfo);
        scoreHeader.getChildren().add(scoreBox);

        // Content: Sub panel + Tactic panel + Summary
        HBox content = new HBox(16);
        VBox.setVgrow(content, Priority.ALWAYS);

        // Substitution panel
        VBox subPanel = createSubstitutionPanel(sport);
        HBox.setHgrow(subPanel, Priority.ALWAYS);

        // Tactic panel
        VBox tacticPanel = createTacticPanel(sport);

        // Period summary
        VBox summaryPanel = createPeriodSummary();
        HBox.setHgrow(summaryPanel, Priority.ALWAYS);

        content.getChildren().addAll(subPanel, tacticPanel, summaryPanel);

        // Continue button
        HBox controlBar = new HBox(12);
        controlBar.setAlignment(Pos.CENTER);

        Button continueBtn = new Button("▶ Continue Match");
        continueBtn.getStyleClass().add("btn-primary");
        continueBtn.setOnAction(e -> {
            matchState.setPeriodOver(false);
            engine.simulatePeriod(matchState, match.getHomeTeam(), match.getAwayTeam());

            if (matchState.isMatchOver()) {
                // Show match end in LiveMatchView
                LiveMatchView lmv = new LiveMatchView(match);
                ViewManager.getInstance().switchView(lmv);
            } else if (matchState.isPeriodOver()) {
                // Another break
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine));
            }
        });

        Button simToEnd = new Button("⏩ Simulate to End");
        simToEnd.getStyleClass().add("btn-secondary");
        simToEnd.setOnAction(e -> {
            MatchResult result = engine.simulateToEnd(matchState, match.getHomeTeam(), match.getAwayTeam());
            finishMatch(result);
        });

        controlBar.getChildren().addAll(continueBtn, simToEnd);

        getChildren().addAll(scoreHeader, content, controlBar);
    }

    private VBox createSubstitutionPanel(Sport sport) {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("card");

        Label title = new Label("🔄 Substitutions");
        title.getStyleClass().add("section-label");

        boolean isHome = userTeam == match.getHomeTeam();
        int maxSubs = sport.getMaxSubstitutions();
        int usedSubs = isHome ? matchState.getHomeSubsUsed() : matchState.getAwaySubsUsed();

        subsRemainingLabel = new Label("Remaining: " + (maxSubs - usedSubs) + " / " + maxSubs);
        subsRemainingLabel.getStyleClass().add("text-muted");

        // Player Out selector
        Label outLabel = new Label("Player Out:");
        outLabel.getStyleClass().add("text-muted");

        ComboBox<String> outBox = new ComboBox<>();
        List<Player> fieldPlayers = isHome ? matchState.getHomeFieldPlayers() : matchState.getAwayFieldPlayers();
        for (Player p : fieldPlayers) {
            String fatigue = p.getAge() > 30 ? " (Tired)" : " (Fresh)";
            outBox.getItems().add(p.getName() + fatigue);
        }
        outBox.setMaxWidth(Double.MAX_VALUE);

        // Player In selector
        Label inLabel = new Label("Player In:");
        inLabel.getStyleClass().add("text-muted");

        ComboBox<String> inBox = new ComboBox<>();
        for (Player p : userTeam.getSquad()) {
            if (!fieldPlayers.contains(p) && p.isAvailable()) {
                inBox.getItems().add(p.getName() + " (OVR " + p.getOverallRating() + ")");
            }
        }
        inBox.setMaxWidth(Double.MAX_VALUE);

        // Error label
        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px;");

        Button subBtn = new Button("Make Substitution");
        subBtn.getStyleClass().add("btn-secondary");
        subBtn.setOnAction(e -> {
            int outIdx = outBox.getSelectionModel().getSelectedIndex();
            int inIdx = inBox.getSelectionModel().getSelectedIndex();
            if (outIdx < 0 || inIdx < 0) {
                errorLabel.setText("Select both players");
                return;
            }

            Player outPlayer = fieldPlayers.get(outIdx);

            // Find the bench player
            int benchIdx = 0;
            Player inPlayer = null;
            for (Player p : userTeam.getSquad()) {
                if (!fieldPlayers.contains(p) && p.isAvailable()) {
                    if (benchIdx == inIdx) {
                        inPlayer = p;
                        break;
                    }
                    benchIdx++;
                }
            }

            if (inPlayer == null) {
                errorLabel.setText("Invalid player selection");
                return;
            }

            boolean success = matchState.makeSubstitution(userTeam.getTeamId(), outPlayer, inPlayer, maxSubs);
            if (success) {
                errorLabel.setText("");
                // Rebuild the panel
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine));
            } else {
                errorLabel.setText("Substitution failed — no subs remaining or invalid");
            }
        });

        panel.getChildren().addAll(title, subsRemainingLabel, outLabel, outBox, inLabel, inBox, subBtn, errorLabel);
        return panel;
    }

    private VBox createTacticPanel(Sport sport) {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("card");
        panel.setPrefWidth(250);

        Label title = new Label("⚙️ Tactics");
        title.getStyleClass().add("section-label");
        panel.getChildren().add(title);

        // Formation
        Label formLabel = new Label("Formation");
        formLabel.getStyleClass().add("text-muted");

        ComboBox<String> formationBox = new ComboBox<>();
        for (Formation f : sport.getFormations()) {
            formationBox.getItems().add(f.getFormationName());
        }
        if (userTeam.getFormation() != null) {
            formationBox.setValue(userTeam.getFormation().getFormationName());
        }
        formationBox.setMaxWidth(Double.MAX_VALUE);
        formationBox.setOnAction(e -> {
            String sel = formationBox.getValue();
            for (Formation f : sport.getFormations()) {
                if (f.getFormationName().equals(sel)) {
                    userTeam.setFormation(f);
                    break;
                }
            }
        });

        panel.getChildren().addAll(formLabel, formationBox);

        // Tactic buttons
        Label tacticLabel = new Label("Playing Style");
        tacticLabel.getStyleClass().add("text-muted");
        tacticLabel.setPadding(new Insets(8, 0, 0, 0));
        panel.getChildren().add(tacticLabel);

        for (Tactic t : sport.getTactics()) {
            Button btn = new Button(t.getTacticName());
            btn.getStyleClass().add("btn-tactic");
            btn.setMaxWidth(Double.MAX_VALUE);
            if (userTeam.getTactic() != null && userTeam.getTactic().getTacticName().equals(t.getTacticName())) {
                btn.getStyleClass().add("btn-tactic-active");
            }
            btn.setOnAction(e -> {
                userTeam.setTactic(t);
                // Refresh
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine));
            });
            panel.getChildren().add(btn);
        }

        return panel;
    }

    private VBox createPeriodSummary() {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("card");

        Label title = new Label("📋 Period Summary");
        title.getStyleClass().add("section-label");
        panel.getChildren().add(title);

        List<MatchEvent> events = matchState.getEvents();
        boolean hasEvents = false;

        for (MatchEvent ev : events) {
            if (ev instanceof com.sportsmanager.football.FootballMatchEvent fme) {
                if (fme.getEventType() == com.sportsmanager.football.FootballEventType.GOAL
                        || fme.getEventType() == com.sportsmanager.football.FootballEventType.YELLOW_CARD
                        || fme.getEventType() == com.sportsmanager.football.FootballEventType.RED_CARD) {
                    Label eventLbl = new Label(ev.getDescription());
                    eventLbl.getStyleClass().add("text-normal");
                    eventLbl.setStyle("-fx-font-size: 12px;");
                    eventLbl.setWrapText(true);
                    panel.getChildren().add(eventLbl);
                    hasEvents = true;
                }
            }
        }

        if (!hasEvents) {
            Label noEvents = new Label("No key events yet");
            noEvents.getStyleClass().add("text-muted");
            panel.getChildren().add(noEvents);
        }

        return panel;
    }

    private void finishMatch(MatchResult result) {
        SeasonState ss = GameManager.getInstance().getState();
        FootballLeague league = (FootballLeague) ss.getLeague();
        league.recordMatchResult(match, result);

        // Play other matches
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

        ViewManager.getInstance().switchView(new DashboardView());
    }
}
