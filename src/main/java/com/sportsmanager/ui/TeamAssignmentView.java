package com.sportsmanager.ui;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.football.FootballPlayerFactory;
import com.sportsmanager.generator.PlayerFactory;
import com.sportsmanager.handball.HandballPlayerFactory;
import com.sportsmanager.handball.HandballSport;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.generator.NameGenerator;
import com.sportsmanager.generator.TeamGenerator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Screen 2 — Team selection: generates league, user picks their team.
 */
public class TeamAssignmentView extends VBox {

    private final Sport sport;
    private List<Team> allTeams;
    private Team selectedTeam = null;

    private VBox selectedCard = null;   // currently highlighted card
    private Button beginSeason;
    private Label selectionHint;

    public TeamAssignmentView(Sport sport) {
        this.sport = sport;
        setAlignment(Pos.TOP_CENTER);
        setSpacing(24);
        setPadding(new Insets(40));

        generateTeams();
        buildUI();
    }

    // ── Generation ────────────────────────────────────────────────────────────

    private void generateTeams() {
        PlayerFactory factory = sport instanceof HandballSport
                ? new HandballPlayerFactory()
                : new FootballPlayerFactory();
        TeamGenerator generator = new TeamGenerator(sport, factory, new NameGenerator());
        allTeams = generator.generateLeague();

        // Assign a unique random logo to each team
        LogoManager.getInstance().assign(allTeams);

        // Sort strongest → weakest so best teams are visible first
        allTeams.sort((a, b) -> {
            double avgA = a.getSquad().stream().mapToInt(Player::getOverallRating).average().orElse(0);
            double avgB = b.getSquad().stream().mapToInt(Player::getOverallRating).average().orElse(0);
            return Double.compare(avgB, avgA);
        });
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private void buildUI() {
        // Header
        Label header = new Label("Choose Your Team");
        header.getStyleClass().add("title-label");

        selectionHint = new Label("Click on a team to select it");
        selectionHint.getStyleClass().add("text-muted");
        selectionHint.setStyle("-fx-font-size: 14px;");

        // Team grid
        FlowPane grid = new FlowPane(16, 16);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(8));

        for (Team t : allTeams) {
            grid.getChildren().add(buildTeamCard(t));
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Bottom bar
        beginSeason = new Button("Begin Season");
        beginSeason.getStyleClass().add("btn-primary");
        beginSeason.setDisable(true);   // enabled after team selection
        beginSeason.setOnAction(e -> startGame());

        getChildren().addAll(header, selectionHint, scroll, beginSeason);
    }

    private VBox buildTeamCard(Team team) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));
        card.setPrefWidth(160);

        // Logo — image if available, letter initial as fallback
        javafx.scene.Node logoNode;
        Image logoImg = LogoManager.getInstance().getLogo(team);
        if (logoImg != null) {
            ImageView iv = new ImageView(logoImg);
            iv.setFitWidth(60);
            iv.setFitHeight(60);
            iv.setPreserveRatio(true);
            logoNode = iv;
        } else {
            String initial = team.getTeamName().substring(0, 1).toUpperCase();
            Label fallback = new Label(initial);
            fallback.setStyle(
                    "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #00d2ff;"
                    + " -fx-background-color: #0f3460; -fx-background-radius: 50;"
                    + " -fx-min-width: 60; -fx-min-height: 60; -fx-max-width: 60; -fx-max-height: 60;"
                    + " -fx-alignment: center;");
            logoNode = fallback;
        }

        Label name = new Label(team.getTeamName());
        name.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);

        double avg = team.getSquad().stream()
                .mapToInt(Player::getOverallRating).average().orElse(0);
        int stars = Math.max(1, Math.min(5, (int) Math.round(avg / 20.0)));

        Label starLabel = new Label("★".repeat(stars) + "☆".repeat(5 - stars));
        starLabel.getStyleClass().add("star-rating");

        Label ovrLabel = new Label(String.format("%.0f OVR", avg));
        ovrLabel.getStyleClass().add("text-muted");
        ovrLabel.setStyle("-fx-font-size: 12px;");

        card.getChildren().addAll(logoNode, name, starLabel, ovrLabel);

        // Click to select
        card.setOnMouseClicked(e -> selectTeam(team, card));
        card.setStyle(card.getStyle() + " -fx-cursor: hand;");

        return card;
    }

    // ── Selection logic ───────────────────────────────────────────────────────

    private void selectTeam(Team team, VBox card) {
        // Deselect previous
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("card-highlight");
        }

        selectedTeam = team;
        selectedCard = card;
        card.getStyleClass().add("card-highlight");

        selectionHint.setText("Selected: " + team.getTeamName() + " — press Begin Season to start");
        beginSeason.setDisable(false);
    }

    // ── Game start ────────────────────────────────────────────────────────────

    private void startGame() {
        if (selectedTeam == null) return;

        // Apply default formation and tactic to all teams
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

        GameManager.getInstance().initNewGame(sport, allTeams, selectedTeam);

        // Rebuild root with sidebar
        Sidebar sidebar = new Sidebar();
        ViewManager.getInstance().setSidebar(sidebar);

        StackPane contentArea = new StackPane();
        ViewManager.getInstance().setContentArea(contentArea);

        HBox root = (HBox) getScene().getRoot();
        root.getChildren().clear();
        root.getChildren().addAll(sidebar, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        ViewManager.getInstance().switchView(new DashboardView());
    }
}
