package com.sportsmanager.ui;

import com.sportsmanager.core.*;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Screen 4 — Squad management with player list, detail card, and formation/tactic selectors.
 * Supports tap-to-select + tap-to-swap between Starting XI and substitutes.
 */
public class SquadManagementView extends HBox {

    private final SeasonState state;
    private final Team userTeam;

    private VBox detailPanel;
    private FormationPitchView squadPitchView;
    private VBox playerListBox;
    private Label swapStatusLabel;

    /** The player currently waiting to be swapped; null when nothing is selected. */
    private Player pendingSwap = null;

    public SquadManagementView() {
        this.state    = GameManager.getInstance().getState();
        this.userTeam = state.getUserTeam();
        setSpacing(0);
        setPadding(new Insets(0));
        buildUI();
    }

    // ── Build ────────────────────────────────────────────────────────────────────

    private void buildUI() {
        // ── Left panel ───────────────────────────────────────────────────────────
        VBox leftPanel = new VBox(0);
        leftPanel.setPrefWidth(380);
        leftPanel.setMinWidth(350);

        Label title = new Label("  👥 Squad Management");
        title.getStyleClass().add("section-label");
        title.setPadding(new Insets(16, 16, 8, 16));

        swapStatusLabel = new Label("  Click a player to select, then click another to swap.");
        swapStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888899;");
        swapStatusLabel.setPadding(new Insets(0, 16, 6, 16));
        swapStatusLabel.setWrapText(true);

        playerListBox = new VBox(2);
        playerListBox.setPadding(new Insets(8));
        rebuildPlayerList();

        ScrollPane scrollList = new ScrollPane(playerListBox);
        scrollList.setFitToWidth(true);
        VBox.setVgrow(scrollList, Priority.ALWAYS);

        VBox bottomControls = createFormationTacticControls();
        leftPanel.getChildren().addAll(title, swapStatusLabel, scrollList, bottomControls);

        // ── Right panel ──────────────────────────────────────────────────────────
        squadPitchView = new FormationPitchView(null);
        refreshPitch();
        StackPane pitchWrapper = new StackPane(squadPitchView);
        pitchWrapper.setAlignment(Pos.CENTER);

        detailPanel = new VBox(16);
        detailPanel.setAlignment(Pos.TOP_CENTER);
        detailPanel.setPadding(new Insets(0, 0, 16, 0));

        Label selectHint = new Label("Select a player to view details");
        selectHint.getStyleClass().add("text-muted");
        selectHint.setStyle("-fx-font-size: 13px;");
        detailPanel.getChildren().add(selectHint);

        ScrollPane cardScroll = new ScrollPane(detailPanel);
        cardScroll.setFitToWidth(true);
        cardScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cardScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(cardScroll, Priority.ALWAYS);

        VBox rightOuter = new VBox(16);
        rightOuter.setPadding(new Insets(20));
        rightOuter.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(rightOuter, Priority.ALWAYS);
        rightOuter.getChildren().addAll(pitchWrapper, cardScroll);

        getChildren().addAll(leftPanel, rightOuter);
    }

    // ── Player list ──────────────────────────────────────────────────────────────

    /** Rebuild the left-side player list, inserting section headers and a divider. */
    private void rebuildPlayerList() {
        playerListBox.getChildren().clear();
        int lineupSize = state.getCurrentSport().getStartingLineupSize();
        List<Player> squad = userTeam.getSquad();

        Label xiHeader = new Label("STARTING XI");
        xiHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #00d2ff;"
                + " -fx-padding: 4 4 2 4;");
        playerListBox.getChildren().add(xiHeader);

        for (int i = 0; i < squad.size(); i++) {
            if (i == lineupSize) {
                Region divider = new Region();
                divider.setStyle("-fx-background-color: #2a2a4a;");
                divider.setPrefHeight(1);
                divider.setMaxHeight(1);
                VBox.setMargin(divider, new Insets(4, 0, 0, 0));

                Label subHeader = new Label("SUBSTITUTES");
                subHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #888899;"
                        + " -fx-padding: 6 4 2 4;");
                playerListBox.getChildren().addAll(divider, subHeader);
            }
            playerListBox.getChildren().add(createPlayerRow(squad.get(i)));
        }
    }

    // ── Pitch refresh ────────────────────────────────────────────────────────────

    private void refreshPitch() {
        int lineupSize = state.getCurrentSport().getStartingLineupSize();
        List<Player> first11 = userTeam.getSquad().stream()
                .limit(lineupSize)
                .collect(Collectors.toList());
        squadPitchView.redrawWithPlayers(
                userTeam.getFormation(), first11, pendingSwap, this::onPitchPlayerClick);
    }

    // ── Swap logic ───────────────────────────────────────────────────────────────

    /** Called when the user clicks a player node on the right-side pitch. */
    private void onPitchPlayerClick(Player clicked) {
        if (pendingSwap == null) {
            pendingSwap = clicked;
            showPlayerDetail(clicked);
        } else if (pendingSwap == clicked) {
            pendingSwap = null;
        } else {
            userTeam.swapPlayers(pendingSwap, clicked);
            pendingSwap = null;
        }
        updateSwapStatus();
        rebuildPlayerList();
        refreshPitch();
    }

    /** Called when the user clicks a row in the left-side player list. */
    private void handleRowClick(Player p) {
        if (pendingSwap == null) {
            pendingSwap = p;
            showPlayerDetail(p);
        } else if (pendingSwap == p) {
            pendingSwap = null;
            showPlayerDetail(p);
        } else {
            userTeam.swapPlayers(pendingSwap, p);
            pendingSwap = null;
        }
        updateSwapStatus();
        rebuildPlayerList();
        refreshPitch();
    }

    private void updateSwapStatus() {
        if (pendingSwap != null) {
            swapStatusLabel.setText(
                    "  Selected: " + pendingSwap.getName() + " — click another player to swap.");
            swapStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #00e676;");
        } else {
            swapStatusLabel.setText(
                    "  Click a player to select, then click another to swap.");
            swapStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888899;");
        }
    }

    // ── Row builder ──────────────────────────────────────────────────────────────

    private HBox createPlayerRow(Player p) {
        boolean selected = (p == pendingSwap);

        String normalBg = selected ? "#1a3a2e" : "#1a1a2e";
        String hoverBg  = selected ? "#1a4a2e" : "#222240";
        String border   = selected
                ? " -fx-border-color: #00e676; -fx-border-width: 1.5; -fx-border-radius: 8;"
                : "";

        String normalStyle = "-fx-background-color: " + normalBg
                + "; -fx-background-radius: 8; -fx-cursor: hand;" + border;
        String hoverStyle  = "-fx-background-color: " + hoverBg
                + "; -fx-background-radius: 8; -fx-cursor: hand;" + border;

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle(normalStyle);

        // Avatar
        String initials = getInitials(p.getName());
        Label avatar = new Label(initials);
        avatar.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #00d2ff;"
                + " -fx-background-color: #0f3460; -fx-background-radius: 50;"
                + " -fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36;"
                + " -fx-alignment: center;");

        Label name = new Label(p.getName());
        name.getStyleClass().add("text-normal");
        name.setStyle("-fx-font-size: 13px;");
        name.setPrefWidth(130);

        Label posBadge = createPositionBadge(p);

        Label age = new Label(String.valueOf(p.getAge()));
        age.getStyleClass().add("text-muted");
        age.setMinWidth(25);

        Label ovr = new Label(String.valueOf(p.getOverallRating()));
        ovr.setStyle("-fx-text-fill: #00d2ff; -fx-font-weight: bold; -fx-font-size: 13px;");
        ovr.setMinWidth(30);

        Region statusDot = new Region();
        statusDot.getStyleClass().add(p.isAvailable() ? "status-fit" : "status-injured");

        row.getChildren().addAll(avatar, name, posBadge, age, ovr, statusDot);

        if (p.isSuspended()) {
            Label susLbl = new Label("🚫(" + p.getSuspensionGamesRemaining() + ")");
            susLbl.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 11px;");
            row.getChildren().add(susLbl);
        } else if (!p.isAvailable()) {
            Label injLbl = new Label("🏥(" + p.getInjuryGamesRemaining() + ")");
            injLbl.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 11px;");
            row.getChildren().add(injLbl);
        }

        row.setOnMouseClicked(e -> handleRowClick(p));
        row.setOnMouseEntered(e -> row.setStyle(hoverStyle));
        row.setOnMouseExited(e -> row.setStyle(normalStyle));

        return row;
    }

    // ── Player detail card ───────────────────────────────────────────────────────

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

        Region sep = new Region();
        sep.setStyle("-fx-background-color: #2a2a4a; -fx-pref-height: 1; -fx-max-height: 1;");
        card.getChildren().add(sep);

        Label attrTitle = new Label("Attributes");
        attrTitle.getStyleClass().add("section-label");
        card.getChildren().add(attrTitle);

        Sport sport = state.getCurrentSport();
        List<String> attrNames = sport.getAttributeNamesForPosition(p.getPosition());

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

    // ── Formation & Tactic selectors ─────────────────────────────────────────────

    private VBox createFormationTacticControls() {
        VBox controls = new VBox(10);
        controls.setPadding(new Insets(12));
        controls.setStyle("-fx-background-color: #16213e;");

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
                    refreshPitch();
                    break;
                }
            }
        });
        formationBox.setMaxWidth(Double.MAX_VALUE);

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

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private Label createPositionBadge(Player p) {
        Position pos = p.getPosition();
        String posName;
        String badgeClass;

        if (pos instanceof FootballPosition fp) {
            posName = getShortPosition(fp);
            if (fp == FootballPosition.GOALKEEPER) badgeClass = "badge-gk";
            else if (fp.isDefensive())             badgeClass = "badge-def";
            else if (fp.isMidfield())              badgeClass = "badge-mid";
            else                                   badgeClass = "badge-fwd";
        } else {
            posName    = abbreviatePosition(pos);
            badgeClass = genericBadgeClass(pos);
        }

        Label badge = new Label(posName);
        badge.getStyleClass().addAll("badge", badgeClass);
        return badge;
    }

    private String abbreviatePosition(Position pos) {
        if (pos == null) return "?";
        String name = pos.getName();
        if (name.toLowerCase().startsWith("goalkeeper")) return "GK";
        String[] words = name.split("\\s+");
        if (words.length == 1)
            return name.length() >= 3 ? name.substring(0, 3).toUpperCase(java.util.Locale.ROOT)
                                       : name.toUpperCase(java.util.Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        for (String w : words) if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0)));
        String abbr = sb.toString();
        return abbr.length() > 3 ? abbr.substring(0, 3) : abbr;
    }

    private String genericBadgeClass(Position pos) {
        if (pos == null) return "badge-def";
        String name = pos.getName().toLowerCase();
        if (name.contains("goalkeeper")) return "badge-gk";
        if (name.contains("back"))       return "badge-def";
        if (name.contains("wing"))       return "badge-fwd";
        return "badge-mid";
    }

    private String getShortPosition(FootballPosition pos) {
        return switch (pos) {
            case GOALKEEPER           -> "GK";
            case CENTRE_BACK          -> "CB";
            case LEFT_BACK            -> "LB";
            case RIGHT_BACK           -> "RB";
            case DEFENSIVE_MIDFIELDER -> "CDM";
            case CENTRAL_MIDFIELDER   -> "CM";
            case ATTACKING_MIDFIELDER -> "CAM";
            case LEFT_MIDFIELDER      -> "LM";
            case RIGHT_MIDFIELDER     -> "RM";
            case LEFT_WINGER          -> "LW";
            case RIGHT_WINGER         -> "RW";
            case STRIKER              -> "ST";
            case CENTRE_FORWARD       -> "CF";
        };
    }

    private String getInitials(String fullName) {
        String[] parts = fullName.split("\\s+");
        if (parts.length >= 2)
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
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
