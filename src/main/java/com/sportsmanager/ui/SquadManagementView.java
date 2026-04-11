package com.sportsmanager.ui;

import com.sportsmanager.core.*;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Screen 4 — Squad management with player list, detail card, and formation/tactic selectors.
 */
public class SquadManagementView extends HBox {

    private final SeasonState state;
    private final Team userTeam;
    private VBox detailPanel;

    public SquadManagementView() {
        this.state = GameManager.getInstance().getState();
        this.userTeam = state.getUserTeam();
        setSpacing(0);
        setPadding(new Insets(0));
        buildUI();
    }

    private void buildUI() {
        // Left panel — player list
        VBox leftPanel = new VBox(0);
        leftPanel.setPrefWidth(380);
        leftPanel.setMinWidth(350);

        Label title = new Label("  👥 Squad Management");
        title.getStyleClass().add("section-label");
        title.setPadding(new Insets(16, 16, 8, 16));

        VBox playerList = new VBox(2);
        playerList.setPadding(new Insets(8));

        for (Player p : userTeam.getSquad()) {
            HBox row = createPlayerRow(p);
            playerList.getChildren().add(row);
        }

        ScrollPane scrollList = new ScrollPane(playerList);
        scrollList.setFitToWidth(true);
        VBox.setVgrow(scrollList, Priority.ALWAYS);

        // Formation & Tactic selectors at bottom
        VBox bottomControls = createFormationTacticControls();

        leftPanel.getChildren().addAll(title, scrollList, bottomControls);

        // Right panel — detail card (empty initially)
        detailPanel = new VBox(16);
        detailPanel.setPadding(new Insets(20));
        detailPanel.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(detailPanel, Priority.ALWAYS);

        Label selectHint = new Label("Select a player to view details");
        selectHint.getStyleClass().add("text-muted");
        selectHint.setStyle("-fx-font-size: 16px;");
        detailPanel.getChildren().add(selectHint);

        getChildren().addAll(leftPanel, detailPanel);
    }

    private HBox createPlayerRow(Player p) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-cursor: hand;");

        // Initials avatar
        String initials = getInitials(p.getName());
        Label avatar = new Label(initials);
        avatar.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #00d2ff;"
                + " -fx-background-color: #0f3460; -fx-background-radius: 50;"
                + " -fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36;"
                + " -fx-alignment: center;");

        // Name
        Label name = new Label(p.getName());
        name.getStyleClass().add("text-normal");
        name.setStyle("-fx-font-size: 13px;");
        name.setPrefWidth(130);

        // Position badge
        Label posBadge = createPositionBadge(p);

        // Age
        Label age = new Label(String.valueOf(p.getAge()));
        age.getStyleClass().add("text-muted");
        age.setMinWidth(25);

        // OVR
        Label ovr = new Label(String.valueOf(p.getOverallRating()));
        ovr.setStyle("-fx-text-fill: #00d2ff; -fx-font-weight: bold; -fx-font-size: 13px;");
        ovr.setMinWidth(30);

        // Status dot
        Region statusDot = new Region();
        if (p.isAvailable()) {
            statusDot.getStyleClass().add("status-fit");
        } else {
            statusDot.getStyleClass().add("status-injured");
        }

        row.getChildren().addAll(avatar, name, posBadge, age, ovr, statusDot);

        // Injury / suspension label
        if (p.isSuspended()) {
            Label susLbl = new Label("🚫(" + p.getSuspensionGamesRemaining() + ")");
            susLbl.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 11px;");
            row.getChildren().add(susLbl);
        } else if (!p.isAvailable()) {
            Label injLbl = new Label("🏥(" + p.getInjuryGamesRemaining() + ")");
            injLbl.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 11px;");
            row.getChildren().add(injLbl);
        }

        row.setOnMouseClicked(e -> showPlayerDetail(p));

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #222240; -fx-background-radius: 8; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-cursor: hand;"));

        return row;
    }

    private void showPlayerDetail(Player p) {
        detailPanel.getChildren().clear();

        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setMaxWidth(400);

        // Header
        String initials = getInitials(p.getName());
        Label avatar = new Label(initials);
        avatar.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #00d2ff;"
                + " -fx-background-color: #0f3460; -fx-background-radius: 50;"
                + " -fx-min-width: 64; -fx-min-height: 64; -fx-max-width: 64; -fx-max-height: 64;"
                + " -fx-alignment: center;");

        Label name = new Label(p.getName());
        name.getStyleClass().add("subtitle-label");

        HBox headerInfo = new HBox(8);
        headerInfo.setAlignment(Pos.CENTER);
        Label posBadge = createPositionBadge(p);
        Label ageLbl = new Label("Age: " + p.getAge());
        ageLbl.getStyleClass().add("text-muted");
        Label ovrLbl = new Label("OVR: " + p.getOverallRating());
        ovrLbl.setStyle("-fx-text-fill: #00d2ff; -fx-font-weight: bold;");
        headerInfo.getChildren().addAll(posBadge, ageLbl, ovrLbl);

        // Status
        if (p.isSuspended()) {
            Label susLabel = new Label("🚫 Suspended — " + p.getSuspensionGamesRemaining() + " games remaining");
            susLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 12px;");
            card.getChildren().add(susLabel);
        } else if (!p.isAvailable()) {
            Label injLabel = new Label("🏥 Injured — " + p.getInjuryGamesRemaining() + " games remaining");
            injLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px;");
            card.getChildren().add(injLabel);
        }

        VBox headerBox = new VBox(8);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().addAll(avatar, name, headerInfo);
        card.getChildren().add(0, headerBox);

        // Separator
        Region sep = new Region();
        sep.setStyle("-fx-background-color: #2a2a4a; -fx-pref-height: 1; -fx-max-height: 1;");
        card.getChildren().add(sep);

        // Attributes
        Label attrTitle = new Label("Attributes");
        attrTitle.getStyleClass().add("section-label");
        card.getChildren().add(attrTitle);

        Sport sport = state.getCurrentSport();
        List<String> attrNames;
        if (p instanceof FootballPlayer fp) {
            attrNames = sport.getAttributeNamesForPosition(fp.getPosition());
        } else {
            attrNames = sport.getPlayerAttributeNames();
        }

        for (String attr : attrNames) {
            int val = p.getAttributeValue(attr);
            HBox attrRow = new HBox(8);
            attrRow.setAlignment(Pos.CENTER_LEFT);

            Label attrLabel = new Label(capitalize(attr));
            attrLabel.getStyleClass().add("text-muted");
            attrLabel.setMinWidth(80);

            ProgressBar bar = new ProgressBar(val / 100.0);
            bar.getStyleClass().add("attr-bar");
            bar.setPrefWidth(180);
            bar.setPrefHeight(10);

            Label valLbl = new Label(String.valueOf(val));
            valLbl.setStyle(getAttributeColor(val));
            valLbl.setMinWidth(30);

            attrRow.getChildren().addAll(attrLabel, bar, valLbl);
            card.getChildren().add(attrRow);
        }

        detailPanel.getChildren().add(card);
    }

    private VBox createFormationTacticControls() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(12));
        controls.setStyle("-fx-background-color: #16213e;");

        // Formation
        Label formLabel = new Label("Formation");
        formLabel.getStyleClass().add("text-muted");

        ComboBox<String> formationBox = new ComboBox<>();
        Sport sport = state.getCurrentSport();
        for (Formation f : sport.getFormations()) {
            formationBox.getItems().add(f.getFormationName());
        }
        if (userTeam.getFormation() != null) {
            formationBox.setValue(userTeam.getFormation().getFormationName());
        }
        formationBox.setOnAction(e -> {
            String selected = formationBox.getValue();
            for (Formation f : sport.getFormations()) {
                if (f.getFormationName().equals(selected)) {
                    userTeam.setFormation(f);
                    break;
                }
            }
        });
        formationBox.setMaxWidth(Double.MAX_VALUE);

        // Tactic
        Label tacticLabel = new Label("Tactic");
        tacticLabel.getStyleClass().add("text-muted");

        ComboBox<String> tacticBox = new ComboBox<>();
        for (Tactic t : sport.getTactics()) {
            tacticBox.getItems().add(t.getTacticName());
        }
        if (userTeam.getTactic() != null) {
            tacticBox.setValue(userTeam.getTactic().getTacticName());
        }
        tacticBox.setOnAction(e -> {
            String selected = tacticBox.getValue();
            for (Tactic t : sport.getTactics()) {
                if (t.getTacticName().equals(selected)) {
                    userTeam.setTactic(t);
                    break;
                }
            }
        });
        tacticBox.setMaxWidth(Double.MAX_VALUE);

        controls.getChildren().addAll(formLabel, formationBox, tacticLabel, tacticBox);
        return controls;
    }

    private Label createPositionBadge(Player p) {
        String posName;
        String badgeClass;

        if (p instanceof FootballPlayer fp) {
            FootballPosition pos = fp.getPosition();
            posName = getShortPosition(pos);
            if (pos == FootballPosition.GOALKEEPER) {
                badgeClass = "badge-gk";
            } else if (pos.isDefensive()) {
                badgeClass = "badge-def";
            } else if (pos.isMidfield()) {
                badgeClass = "badge-mid";
            } else {
                badgeClass = "badge-fwd";
            }
        } else {
            posName = "?";
            badgeClass = "badge-def";
        }

        Label badge = new Label(posName);
        badge.getStyleClass().addAll("badge", badgeClass);
        return badge;
    }

    private String getShortPosition(FootballPosition pos) {
        return switch (pos) {
            case GOALKEEPER -> "GK";
            case CENTRE_BACK -> "CB";
            case LEFT_BACK -> "LB";
            case RIGHT_BACK -> "RB";
            case DEFENSIVE_MIDFIELDER -> "CDM";
            case CENTRAL_MIDFIELDER -> "CM";
            case ATTACKING_MIDFIELDER -> "CAM";
            case LEFT_WINGER -> "LW";
            case RIGHT_WINGER -> "RW";
            case STRIKER -> "ST";
            case CENTRE_FORWARD -> "CF";
        };
    }

    private String getInitials(String fullName) {
        String[] parts = fullName.split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String getAttributeColor(int val) {
        if (val >= 80) return "-fx-text-fill: #00e676; -fx-font-weight: bold;";
        if (val >= 60) return "-fx-text-fill: #00d2ff; -fx-font-weight: bold;";
        if (val >= 40) return "-fx-text-fill: #ffab40;";
        return "-fx-text-fill: #ff5252;";
    }
}
