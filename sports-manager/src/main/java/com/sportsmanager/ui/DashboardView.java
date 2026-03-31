package com.sportsmanager.ui;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Team;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.league.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 3 — Main dashboard after game starts.
 */
public class DashboardView extends VBox {

    private final SeasonState state;

    public DashboardView() {
        this.state = GameManager.getInstance().getState();
        setSpacing(20);
        setPadding(new Insets(24));

        if (state == null) {
            getChildren().add(new Label("No game in progress."));
            return;
        }

        // Check season end
        if (GameManager.getInstance().isSeasonOver()) {
            showSeasonEndOverlay();
            return;
        }

        buildUI();
    }

    private void buildUI() {
        Team userTeam = state.getUserTeam();
        Standings standings = state.getCurrentStandings();
        Fixture fixture = state.getCurrentFixture();
        int currentWeek = state.getCurrentWeek();

        // Welcome header
        Label welcomeLabel = new Label("Dashboard");
        welcomeLabel.getStyleClass().add("title-label");

        Label weekLabel = new Label("Season " + state.getSeasonNumber() + "  •  Week " + currentWeek + " of 38");
        weekLabel.getStyleClass().add("text-muted");
        weekLabel.setStyle("-fx-font-size: 14px;");

        // --- Top summary cards ---
        HBox summaryCards = new HBox(16);
        summaryCards.setAlignment(Pos.CENTER_LEFT);

        // League Position card
        int position = standings.getPosition(userTeam);
        VBox posCard = createSummaryCard("🏆 League Position",
                "#" + position, getPositionSuffix(position));

        // Next Match card
        String nextMatchText = "—";
        String nextMatchSub = "";
        if (currentWeek <= fixture.getTotalWeeks()) {
            MatchWeek mw = fixture.getWeek(currentWeek);
            for (Match m : mw.getMatches()) {
                if (m.getHomeTeam() == userTeam || m.getAwayTeam() == userTeam) {
                    Team opponent = m.getHomeTeam() == userTeam ? m.getAwayTeam() : m.getHomeTeam();
                    String venue = m.getHomeTeam() == userTeam ? "Home" : "Away";
                    nextMatchText = opponent.getTeamName();
                    nextMatchSub = venue + "  •  Week " + currentWeek;
                    break;
                }
            }
        }
        VBox matchCard = createSummaryCard("⚽ Next Match", nextMatchText, nextMatchSub);

        // Form card
        VBox formCard = createFormCard(userTeam, fixture, currentWeek);

        summaryCards.getChildren().addAll(posCard, matchCard, formCard);

        // --- Upcoming Match panel ---
        VBox upcomingMatch = createUpcomingMatchPanel(userTeam, fixture, currentWeek);

        // --- Bottom row: League + Recent Results ---
        HBox bottomRow = new HBox(16);

        // Compact League Standings
        VBox leagueCard = createCompactStandings(standings, userTeam);
        HBox.setHgrow(leagueCard, Priority.ALWAYS);

        // Recent Results
        VBox recentCard = createRecentResults(userTeam, fixture, currentWeek);
        HBox.setHgrow(recentCard, Priority.ALWAYS);

        bottomRow.getChildren().addAll(leagueCard, recentCard);

        ScrollPane scroll = new ScrollPane();
        VBox content = new VBox(20);
        content.setPadding(new Insets(0));
        content.getChildren().addAll(welcomeLabel, weekLabel, summaryCards, upcomingMatch, bottomRow);
        scroll.setContent(content);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().add(scroll);
    }

    private VBox createSummaryCard(String title, String value, String subtitle) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(220);
        card.setPrefHeight(120);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("text-muted");
        titleLbl.setStyle("-fx-font-size: 12px;");

        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add("text-large");

        Label subLbl = new Label(subtitle);
        subLbl.getStyleClass().add("text-muted");

        card.getChildren().addAll(titleLbl, valueLbl, subLbl);
        return card;
    }

    private VBox createFormCard(Team team, Fixture fixture, int currentWeek) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(220);
        card.setPrefHeight(120);

        Label titleLbl = new Label("📊 Form");
        titleLbl.getStyleClass().add("text-muted");
        titleLbl.setStyle("-fx-font-size: 12px;");

        HBox formCircles = new HBox(6);
        formCircles.setAlignment(Pos.CENTER_LEFT);

        List<String> results = getRecentForm(team, fixture, currentWeek, 5);
        for (String r : results) {
            Label circle = new Label(r);
            switch (r) {
                case "W" -> circle.getStyleClass().add("form-win");
                case "D" -> circle.getStyleClass().add("form-draw");
                case "L" -> circle.getStyleClass().add("form-loss");
            }
            formCircles.getChildren().add(circle);
        }

        if (results.isEmpty()) {
            Label noData = new Label("No matches played yet");
            noData.getStyleClass().add("text-muted");
            formCircles.getChildren().add(noData);
        }

        card.getChildren().addAll(titleLbl, formCircles);
        return card;
    }

    private List<String> getRecentForm(Team team, Fixture fixture, int currentWeek, int count) {
        List<String> form = new ArrayList<>();
        for (int w = currentWeek - 1; w >= 1 && form.size() < count; w--) {
            MatchWeek mw = fixture.getWeek(w);
            for (Match m : mw.getMatches()) {
                if (m.getStatus() == MatchStatus.FINISHED && m.getResult() != null) {
                    if (m.getHomeTeam() == team) {
                        int gf = m.getResult().getHomeScore();
                        int ga = m.getResult().getAwayScore();
                        form.add(gf > ga ? "W" : gf < ga ? "L" : "D");
                    } else if (m.getAwayTeam() == team) {
                        int gf = m.getResult().getAwayScore();
                        int ga = m.getResult().getHomeScore();
                        form.add(gf > ga ? "W" : gf < ga ? "L" : "D");
                    }
                }
            }
        }
        return form;
    }

    private VBox createUpcomingMatchPanel(Team userTeam, Fixture fixture, int currentWeek) {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("card");

        Label sectionTitle = new Label("⚡ Upcoming Match");
        sectionTitle.getStyleClass().add("section-label");

        if (currentWeek > fixture.getTotalWeeks()) {
            Label noMatch = new Label("Season complete!");
            noMatch.getStyleClass().add("text-muted");
            panel.getChildren().addAll(sectionTitle, noMatch);
            return panel;
        }

        MatchWeek mw = fixture.getWeek(currentWeek);
        Match userMatch = null;
        for (Match m : mw.getMatches()) {
            if (m.getHomeTeam() == userTeam || m.getAwayTeam() == userTeam) {
                userMatch = m;
                break;
            }
        }

        if (userMatch == null || userMatch.getStatus() == MatchStatus.FINISHED) {
            Label noMatch = new Label("No upcoming match this week");
            noMatch.getStyleClass().add("text-muted");
            panel.getChildren().addAll(sectionTitle, noMatch);
            return panel;
        }

        Team home = userMatch.getHomeTeam();
        Team away = userMatch.getAwayTeam();

        HBox matchup = new HBox(24);
        matchup.setAlignment(Pos.CENTER);

        // Home team
        VBox homeBox = new VBox(4);
        homeBox.setAlignment(Pos.CENTER);
        Label homeLogo = createTeamLogo(home);
        Label homeName = new Label(home.getTeamName());
        homeName.getStyleClass().add("text-normal");
        homeName.setStyle("-fx-font-weight: bold;");
        homeBox.getChildren().addAll(homeLogo, homeName);

        Label vs = new Label("VS");
        vs.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        // Away team
        VBox awayBox = new VBox(4);
        awayBox.setAlignment(Pos.CENTER);
        Label awayLogo = createTeamLogo(away);
        Label awayName = new Label(away.getTeamName());
        awayName.getStyleClass().add("text-normal");
        awayName.setStyle("-fx-font-weight: bold;");
        awayBox.getChildren().addAll(awayLogo, awayName);

        matchup.getChildren().addAll(homeBox, vs, awayBox);

        Button playMatch = new Button("Play Match ▶");
        playMatch.getStyleClass().add("btn-primary");
        final Match um = userMatch;
        playMatch.setOnAction(e -> {
            ViewManager.getInstance().switchView(new PreMatchSetupView(um));
        });

        HBox btnBox = new HBox(playMatch);
        btnBox.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(sectionTitle, matchup, btnBox);
        return panel;
    }

    private VBox createCompactStandings(Standings standings, Team userTeam) {
        VBox card = new VBox(0);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(0));

        Label title = new Label("  📊 League Standings (Top 5)");
        title.getStyleClass().add("section-label");
        title.setPadding(new Insets(16, 16, 8, 16));

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("standings-header");
        header.getChildren().addAll(
                standingsCell("#", 30), standingsCell("Team", 150),
                standingsCell("P", 30), standingsCell("W", 30),
                standingsCell("D", 30), standingsCell("L", 30),
                standingsCell("Pts", 40)
        );

        VBox rows = new VBox(0);
        List<Team> sorted = standings.getSortedTeams();
        int userPos = standings.getPosition(userTeam);
        boolean userInTop5 = userPos <= 5;

        for (int i = 0; i < Math.min(5, sorted.size()); i++) {
            Team t = sorted.get(i);
            TeamRecord rec = standings.getRecord(t);
            HBox row = createStandingsRow(i + 1, t, rec, t == userTeam, i % 2 == 1);
            rows.getChildren().add(row);
        }

        // If user not in top 5, show their row
        if (!userInTop5 && userPos <= sorted.size()) {
            Label dots = new Label("  ···");
            dots.getStyleClass().add("text-muted");
            dots.setPadding(new Insets(4, 16, 4, 16));
            rows.getChildren().add(dots);

            TeamRecord rec = standings.getRecord(userTeam);
            rows.getChildren().add(createStandingsRow(userPos, userTeam, rec, true, false));
        }

        card.getChildren().addAll(title, header, rows);
        return card;
    }

    private HBox createStandingsRow(int pos, Team team, TeamRecord rec, boolean isUser, boolean alt) {
        HBox row = new HBox();
        if (isUser) {
            row.getStyleClass().add("standings-row-user");
        } else if (alt) {
            row.getStyleClass().addAll("standings-row", "standings-row-alt");
        } else {
            row.getStyleClass().add("standings-row");
        }

        String trophy = pos == 1 ? "🏆" : String.valueOf(pos);
        row.getChildren().addAll(
                standingsCell(trophy, 30),
                standingsCell(team.getTeamName(), 150),
                standingsCell(String.valueOf(rec.getPlayed()), 30),
                standingsCell(String.valueOf(rec.getWon()), 30),
                standingsCell(String.valueOf(rec.getDrawn()), 30),
                standingsCell(String.valueOf(rec.getLost()), 30),
                standingsCell(String.valueOf(rec.getPoints()), 40)
        );
        return row;
    }

    private Label standingsCell(String text, int width) {
        Label lbl = new Label(text);
        lbl.setMinWidth(width);
        lbl.setPrefWidth(width);
        lbl.getStyleClass().add("text-normal");
        lbl.setStyle("-fx-font-size: 12px;");
        return lbl;
    }

    private VBox createRecentResults(Team userTeam, Fixture fixture, int currentWeek) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label title = new Label("📋 Recent Results");
        title.getStyleClass().add("section-label");
        card.getChildren().add(title);

        int count = 0;
        for (int w = currentWeek - 1; w >= 1 && count < 5; w--) {
            MatchWeek mw = fixture.getWeek(w);
            for (Match m : mw.getMatches()) {
                if (m.getStatus() == MatchStatus.FINISHED && m.getResult() != null) {
                    if (m.getHomeTeam() == userTeam || m.getAwayTeam() == userTeam) {
                        HBox resultRow = new HBox(8);
                        resultRow.setAlignment(Pos.CENTER_LEFT);

                        Label weekLbl = new Label("W" + w);
                        weekLbl.getStyleClass().add("text-muted");
                        weekLbl.setMinWidth(30);

                        Label matchLbl = new Label(m.getHomeTeam().getTeamName() + "  "
                                + m.getResult().getHomeScore() + " - "
                                + m.getResult().getAwayScore() + "  "
                                + m.getAwayTeam().getTeamName());
                        matchLbl.getStyleClass().add("text-normal");
                        matchLbl.setStyle("-fx-font-size: 12px;");

                        resultRow.getChildren().addAll(weekLbl, matchLbl);
                        card.getChildren().add(resultRow);
                        count++;
                    }
                }
            }
        }

        if (count == 0) {
            Label noData = new Label("No results yet");
            noData.getStyleClass().add("text-muted");
            card.getChildren().add(noData);
        }

        return card;
    }

    private Label createTeamLogo(Team team) {
        String init = team.getTeamName().substring(0, 1).toUpperCase();
        Label logo = new Label(init);
        logo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #00d2ff;"
                + " -fx-background-color: #0f3460; -fx-background-radius: 50;"
                + " -fx-min-width: 56; -fx-min-height: 56; -fx-max-width: 56; -fx-max-height: 56;"
                + " -fx-alignment: center;");
        return logo;
    }

    private String getPositionSuffix(int pos) {
        return switch (pos % 10) {
            case 1 -> pos != 11 ? "st" : "th";
            case 2 -> pos != 12 ? "nd" : "th";
            case 3 -> pos != 13 ? "rd" : "th";
            default -> "th";
        };
    }

    private void showSeasonEndOverlay() {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("overlay-bg");

        VBox overlayCard = new VBox(16);
        overlayCard.getStyleClass().add("overlay-card");
        overlayCard.setAlignment(Pos.CENTER);
        overlayCard.setMaxWidth(500);
        overlayCard.setMaxHeight(400);

        Label trophy = new Label("🏆");
        trophy.setStyle("-fx-font-size: 64px;");

        Label title = new Label("Season " + state.getSeasonNumber() + " Complete!");
        title.getStyleClass().add("title-label");

        Standings standings = state.getCurrentStandings();
        List<Team> sorted = standings.getSortedTeams();
        Team champion = sorted.isEmpty() ? null : sorted.get(0);

        Label champLabel = new Label("Champion: " + (champion != null ? champion.getTeamName() : "—"));
        champLabel.getStyleClass().add("subtitle-label");
        champLabel.setStyle("-fx-text-fill: #ffd740;");

        int userPos = standings.getPosition(state.getUserTeam());
        Label userLabel = new Label("Your team finished #" + userPos);
        userLabel.getStyleClass().add("text-normal");
        userLabel.setStyle("-fx-font-size: 16px;");

        Button nextSeason = new Button("Next Season ▶");
        nextSeason.getStyleClass().add("btn-primary");
        nextSeason.setOnAction(e -> {
            GameManager.getInstance().advanceSeason();
            Sidebar sidebar = ViewManager.getInstance().getSidebar();
            if (sidebar != null) sidebar.refresh();
            ViewManager.getInstance().switchView(new DashboardView());
        });

        overlayCard.getChildren().addAll(trophy, title, champLabel, userLabel, nextSeason);
        overlay.getChildren().add(overlayCard);
        getChildren().add(overlay);
    }
}
