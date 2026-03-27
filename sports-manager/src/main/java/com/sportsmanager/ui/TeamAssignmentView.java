package com.sportsmanager.ui;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.football.FootballPlayerFactory;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.generator.NameGenerator;
import com.sportsmanager.generator.TeamGenerator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.*;

/**
 * Screen 2 — Team assignment: generates league, reveals user team.
 */
public class TeamAssignmentView extends VBox {

    private final Sport sport;
    private List<Team> allTeams;
    private Team userTeam;

    public TeamAssignmentView(Sport sport) {
        this.sport = sport;
        setAlignment(Pos.CENTER);
        setSpacing(30);
        setPadding(new Insets(40));

        generateTeams();
        buildUI();
    }

    private void generateTeams() {
        TeamGenerator generator = new TeamGenerator(sport, new FootballPlayerFactory(), new NameGenerator());
        allTeams = generator.generateLeague();
        Random rng = new Random();
        userTeam = allTeams.get(rng.nextInt(allTeams.size()));
    }

    private void buildUI() {
        // Header
        Label header = new Label("Your Team Has Been Assigned!");
        header.getStyleClass().add("title-label");

        Label subheader = new Label("You will manage:");
        subheader.getStyleClass().add("text-muted");
        subheader.setStyle("-fx-font-size: 16px;");

        // Team reveal card
        VBox teamCard = new VBox(12);
        teamCard.getStyleClass().addAll("card", "card-highlight");
        teamCard.setAlignment(Pos.CENTER);
        teamCard.setPadding(new Insets(30));
        teamCard.setMaxWidth(400);

        // Team initial/logo
        String initial = userTeam.getTeamName().substring(0, 1).toUpperCase();
        Label logoLabel = new Label(initial);
        logoLabel.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #00d2ff;"
                + " -fx-background-color: #0f3460; -fx-background-radius: 50;"
                + " -fx-min-width: 90; -fx-min-height: 90; -fx-max-width: 90; -fx-max-height: 90;"
                + " -fx-alignment: center;");

        Label teamName = new Label(userTeam.getTeamName());
        teamName.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Star rating
        double avgOverall = userTeam.getSquad().stream()
                .mapToInt(Player::getOverallRating).average().orElse(0);
        int stars = Math.max(1, Math.min(5, (int) Math.round(avgOverall / 20.0)));
        Label starLabel = new Label("★".repeat(stars) + "☆".repeat(5 - stars));
        starLabel.getStyleClass().add("star-rating");

        // Stats
        Label statsLabel = new Label("Squad: " + userTeam.getSquad().size() + " players  •  Avg Rating: "
                + String.format("%.0f", avgOverall));
        statsLabel.getStyleClass().add("text-muted");
        statsLabel.setStyle("-fx-font-size: 13px;");

        teamCard.getChildren().addAll(logoLabel, teamName, starLabel, statsLabel);

        // Rivals section
        VBox rivalsSection = new VBox(10);
        rivalsSection.setAlignment(Pos.CENTER);
        rivalsSection.setMaxWidth(500);

        Label rivalsTitle = new Label("🏟️ Your Rivals");
        rivalsTitle.getStyleClass().add("section-label");

        HBox rivalsRow = new HBox(12);
        rivalsRow.setAlignment(Pos.CENTER);

        List<Team> sorted = new ArrayList<>(allTeams);
        sorted.sort((a, b) -> {
            double avgA = a.getSquad().stream().mapToInt(Player::getOverallRating).average().orElse(0);
            double avgB = b.getSquad().stream().mapToInt(Player::getOverallRating).average().orElse(0);
            return Double.compare(avgB, avgA);
        });

        int shown = 0;
        for (Team t : sorted) {
            if (t == userTeam) continue;
            if (shown >= 4) break;
            rivalsRow.getChildren().add(createRivalCard(t));
            shown++;
        }

        rivalsSection.getChildren().addAll(rivalsTitle, rivalsRow);

        // Buttons
        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);

        Button beginSeason = new Button("Begin Season");
        beginSeason.getStyleClass().add("btn-primary");
        beginSeason.setOnAction(e -> {
            // Set default formation and tactic for all teams
            if (!sport.getFormations().isEmpty()) {
                for (Team t : allTeams) {
                    if (t.getFormation() == null) t.setFormation(sport.getFormations().get(0));
                }
            }
            if (!sport.getTactics().isEmpty()) {
                for (Team t : allTeams) {
                    if (t.getTactic() == null) t.setTactic(sport.getTactics().get(1)); // Balanced
                }
            }

            GameManager.getInstance().initNewGame(sport, allTeams, userTeam);

            Sidebar sidebar = new Sidebar();
            ViewManager.getInstance().setSidebar(sidebar);

            // Rebuild the root layout with sidebar
            StackPane contentArea = new StackPane();
            ViewManager.getInstance().setContentArea(contentArea);

            HBox root = (HBox) getScene().getRoot();
            root.getChildren().clear();
            root.getChildren().addAll(sidebar, contentArea);
            HBox.setHgrow(contentArea, Priority.ALWAYS);

            ViewManager.getInstance().switchView(new DashboardView());
        });

        Button viewLeague = new Button("View Full League");
        viewLeague.getStyleClass().add("btn-secondary");
        viewLeague.setOnAction(e -> showAllTeams());

        buttons.getChildren().addAll(beginSeason, viewLeague);

        getChildren().addAll(header, subheader, teamCard, rivalsSection, buttons);
    }

    private VBox createRivalCard(Team t) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setPrefWidth(120);

        String init = t.getTeamName().substring(0, 1).toUpperCase();
        Label logo = new Label(init);
        logo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e94560;"
                + " -fx-background-color: #1a1a2e; -fx-background-radius: 50;"
                + " -fx-min-width: 48; -fx-min-height: 48; -fx-max-width: 48; -fx-max-height: 48;"
                + " -fx-alignment: center; -fx-border-color: #e94560; -fx-border-radius: 50;");

        Label name = new Label(t.getTeamName());
        name.getStyleClass().add("text-normal");
        name.setWrapText(true);
        name.setStyle("-fx-text-alignment: center; -fx-font-size: 11px;");

        double avg = t.getSquad().stream().mapToInt(Player::getOverallRating).average().orElse(0);
        Label rating = new Label(String.format("%.0f OVR", avg));
        rating.getStyleClass().add("text-muted");

        card.getChildren().addAll(logo, name, rating);
        return card;
    }

    private void showAllTeams() {
        getChildren().clear();

        Label title = new Label("All Teams");
        title.getStyleClass().add("title-label");

        Button back = new Button("← Back");
        back.getStyleClass().add("btn-secondary");
        back.setOnAction(e -> {
            getChildren().clear();
            buildUI();
        });

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getChildren().addAll(back, title);

        FlowPane grid = new FlowPane(12, 12);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));

        for (Team t : allTeams) {
            VBox card = createRivalCard(t);
            if (t == userTeam) {
                card.getStyleClass().add("card-highlight");
            }
            grid.getChildren().add(card);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(topBar, scroll);
    }
}
