package com.sportsmanager.ui;

import com.sportsmanager.core.Team;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.league.Standings;
import com.sportsmanager.league.TeamRecord;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.Node;

import java.util.List;

/**
 * Screen 9 — Full league standings table.
 */
public class StandingsView extends VBox {

    public StandingsView() {
        SeasonState state = GameManager.getInstance().getState();
        setSpacing(16);
        setPadding(new Insets(20));

        if (state == null) {
            getChildren().add(new Label("No game in progress."));
            return;
        }

        Standings standings = state.getCurrentStandings();
        Team userTeam = state.getUserTeam();
        List<Team> sorted = standings.getSortedTeams();

        // Title
        Label title = new Label("🏆 League Standings");
        title.getStyleClass().add("title-label");

        Label seasonLabel = new Label("Season " + state.getSeasonNumber());
        seasonLabel.getStyleClass().add("text-muted");

        // Table
        VBox table = new VBox(0);

        // Header row
        HBox header = new HBox();
        header.getStyleClass().add("standings-header");
        header.getChildren().addAll(
                cell("#", 35, true),
                cell("Team", 180, true),
                cell("P", 35, true),
                cell("W", 35, true),
                cell("D", 35, true),
                cell("L", 35, true),
                cell("GF", 40, true),
                cell("GA", 40, true),
                cell("GD", 40, true),
                cell("Pts", 45, true)
        );
        table.getChildren().add(header);

        // Data rows
        for (int i = 0; i < sorted.size(); i++) {
            Team team = sorted.get(i);
            TeamRecord rec = standings.getRecord(team);
            boolean isUser = team == userTeam;
            boolean alt = i % 2 == 1;

            HBox row = new HBox();
            if (isUser) {
                row.getStyleClass().add("standings-row-user");
            } else if (alt) {
                row.getStyleClass().addAll("standings-row", "standings-row-alt");
            } else {
                row.getStyleClass().add("standings-row");
            }

            String posText = (i == 0) ? "🏆" : String.valueOf(i + 1);
            Label posLabel = cell(posText, 35, false);

            // Logo + name in an HBox
            Node logoNode = LogoManager.getInstance().createLogoNode(team, 24);
            Label teamLabel = cell(team.getTeamName(), 148, false);
            if (isUser) {
                teamLabel.setStyle("-fx-text-fill: #00d2ff; -fx-font-weight: bold; -fx-font-size: 13px;");
            } else {
                teamLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            }
            HBox teamCell = new HBox(8, logoNode, teamLabel);
            teamCell.setAlignment(Pos.CENTER_LEFT);
            teamCell.setMinWidth(180);
            teamCell.setMaxWidth(180);

            Label ptsLabel = cell(String.valueOf(rec.getPoints()), 45, false);
            ptsLabel.setStyle("-fx-text-fill: #ffd740; -fx-font-weight: bold; -fx-font-size: 13px;");

            row.getChildren().addAll(
                    posLabel,
                    teamCell,
                    cell(String.valueOf(rec.getPlayed()), 35, false),
                    cell(String.valueOf(rec.getWon()), 35, false),
                    cell(String.valueOf(rec.getDrawn()), 35, false),
                    cell(String.valueOf(rec.getLost()), 35, false),
                    cell(String.valueOf(rec.getGoalsFor()), 40, false),
                    cell(String.valueOf(rec.getGoalsAgainst()), 40, false),
                    cell(String.valueOf(rec.getGoalDifference()), 40, false),
                    ptsLabel
            );

            table.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(table);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(title, seasonLabel, scroll);
    }

    private Label cell(String text, int width, boolean isHeader) {
        Label lbl = new Label(text);
        lbl.setMinWidth(width);
        lbl.setPrefWidth(width);
        lbl.setPadding(new Insets(0, 4, 0, 4));

        if (isHeader) {
            lbl.setStyle("-fx-text-fill: #8888aa; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            lbl.getStyleClass().add("text-normal");
            lbl.setStyle("-fx-font-size: 12px;");
        }

        return lbl;
    }
}
