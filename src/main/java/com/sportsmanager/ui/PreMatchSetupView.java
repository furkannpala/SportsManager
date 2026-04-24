package com.sportsmanager.ui;

import com.sportsmanager.core.*;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.league.Match;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 5 — Pre-match setup with lineup, formation, and tactic selection.
 */
public class PreMatchSetupView extends StackPane {

    private final SeasonState state;
    private final Match match;
    private final Team userTeam;

    private Player selectedPlayer = null;
    private HBox selectedRow = null;
    private final List<HBox> playerRows = new ArrayList<>();
    private VBox lineupCard;
    private Label hintLabel;
    private VBox overlayLayer;
    private FormationPitchView pitchView;

    public PreMatchSetupView(Match match) {
        this.state = GameManager.getInstance().getState();
        this.match = match;
        this.userTeam = state.getUserTeam();

        VBox main = new VBox(16);
        main.setPadding(new Insets(20));

        overlayLayer = new VBox();
        overlayLayer.setVisible(false);
        overlayLayer.setAlignment(Pos.CENTER);
        overlayLayer.setStyle("-fx-background-color: rgba(5,5,20,0.78);");

        buildUI(main);
        getChildren().addAll(main, overlayLayer);
    }

    private void buildUI(VBox root) {
        int currentWeek = state.getCurrentWeek();
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

        // ── Header ───────────────────────────────────────────────────
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER);

        Label weekLbl = new Label("Week " + currentWeek);
        weekLbl.getStyleClass().add("text-muted");
        weekLbl.setStyle("-fx-font-size: 14px;");

        Label homeName = new Label(home.getTeamName());
        homeName.getStyleClass().add("subtitle-label");

        Label vs = new Label("VS");
        vs.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label awayName = new Label(away.getTeamName());
        awayName.getStyleClass().add("subtitle-label");

        header.getChildren().addAll(weekLbl, homeName, vs, awayName);

        // ── Content ───────────────────────────────────────────────────
        HBox content = new HBox(16);

        lineupCard = new VBox(8);
        lineupCard.getStyleClass().add("card");
        lineupCard.setPrefWidth(350);

        hintLabel = new Label("Click a player to select, then click another to swap.");
        hintLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px; -fx-padding: 0 0 4 0;");

        refreshLineupCard();

        ScrollPane lineupScroll = new ScrollPane(lineupCard);
        lineupScroll.setFitToWidth(true);
        HBox.setHgrow(lineupScroll, Priority.ALWAYS);

        // ── Tactic card ───────────────────────────────────────────────
        VBox tacticCard = new VBox(10);
        tacticCard.getStyleClass().add("card");
        tacticCard.setPrefWidth(300);

        Label tacticTitle = new Label("Tactics");
        tacticTitle.getStyleClass().add("section-label");
        tacticCard.getChildren().add(tacticTitle);

        // ── Formation pitch preview ───────────────────────────────────
        pitchView = new FormationPitchView(userTeam.getFormation());
        refreshPitchView();
        StackPane pitchWrapper = new StackPane(pitchView);
        pitchWrapper.setAlignment(Pos.CENTER);
        tacticCard.getChildren().add(pitchWrapper);

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
        formationBox.setMaxWidth(Double.MAX_VALUE);
        formationBox.setOnAction(e -> {
            String sel = formationBox.getValue();
            for (Formation f : sport.getFormations()) {
                if (f.getFormationName().equals(sel)) {
                    userTeam.setFormation(f);
                    refreshPitchView();
                    break;
                }
            }
        });

        tacticCard.getChildren().addAll(formLabel, formationBox);

        Label tacticBtnTitle = new Label("Playing Style");
        tacticBtnTitle.getStyleClass().add("text-muted");
        tacticBtnTitle.setPadding(new Insets(8, 0, 0, 0));
        tacticCard.getChildren().add(tacticBtnTitle);

        FlowPane tacticButtons = new FlowPane(8, 8);
        for (Tactic t : sport.getTactics()) {
            Button btn = new Button(t.getTacticName());
            btn.getStyleClass().add("btn-tactic");
            if (userTeam.getTactic() != null && userTeam.getTactic().getTacticName().equals(t.getTacticName())) {
                btn.getStyleClass().add("btn-tactic-active");
            }
            btn.setOnAction(e -> {
                userTeam.setTactic(t);
                tacticButtons.getChildren().forEach(node -> node.getStyleClass().remove("btn-tactic-active"));
                btn.getStyleClass().add("btn-tactic-active");
            });
            tacticButtons.getChildren().add(btn);
        }
        tacticCard.getChildren().add(tacticButtons);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        tacticCard.getChildren().add(spacer);

        Button startMatch = new Button("Start Match ▶");
        startMatch.getStyleClass().add("btn-primary");
        startMatch.setMaxWidth(Double.MAX_VALUE);
        startMatch.setOnAction(e -> handleStartMatch());
        tacticCard.getChildren().add(startMatch);

        content.getChildren().addAll(lineupScroll, tacticCard);
        VBox.setVgrow(content, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Dashboard");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> ViewManager.getInstance().switchView(new DashboardView()));

        root.getChildren().addAll(header, content, backBtn);
        VBox.setVgrow(content, Priority.ALWAYS);
    }

    // ── Pitch refresh ─────────────────────────────────────────────────
    private void refreshPitchView() {
        if (pitchView == null) return;
        List<Player> starters = userTeam.getSquad().subList(0, Math.min(11, userTeam.getSquad().size()));
        pitchView.redrawWithPlayers(userTeam.getFormation(), starters, null, null);
    }

    // ── Lineup refresh & interaction ──────────────────────────────────
    private void refreshLineupCard() {
        lineupCard.getChildren().clear();
        playerRows.clear();
        selectedPlayer = null;
        selectedRow = null;

        Label lineupTitle = new Label("Starting Lineup");
        lineupTitle.getStyleClass().add("section-label");
        lineupCard.getChildren().add(lineupTitle);

        List<Player> squad = userTeam.getSquad();
        for (int i = 0; i < Math.min(11, squad.size()); i++) {
            Player p = squad.get(i);
            HBox row = createPlayerRow(p, true);
            playerRows.add(row);
            lineupCard.getChildren().add(row);
        }

        Label benchTitle = new Label("Substitutes");
        benchTitle.getStyleClass().add("section-label");
        benchTitle.setPadding(new Insets(8, 0, 0, 0));
        lineupCard.getChildren().add(benchTitle);

        for (int i = 11; i < squad.size(); i++) {
            Player p = squad.get(i);
            HBox row = createPlayerRow(p, false);
            playerRows.add(row);
            lineupCard.getChildren().add(row);
        }

        lineupCard.getChildren().add(hintLabel);
        refreshPitchView();
    }

    private void onPlayerClicked(Player clicked, HBox clickedRow) {
        if (selectedPlayer == null) {
            selectedPlayer = clicked;
            selectedRow = clickedRow;
            highlightRow(clickedRow, true);
            hintLabel.setText("Selected: " + clicked.getName() + " — now click the player to swap with.");
        } else if (selectedPlayer == clicked) {
            clearSelection();
        } else {
            int idxA = playerRows.indexOf(selectedRow);
            int idxB = playerRows.indexOf(clickedRow);
            boolean aIsStarter = idxA < 11;
            boolean bIsStarter = idxB < 11;

            if (aIsStarter == bIsStarter) {
                highlightRow(selectedRow, false);
                selectedPlayer = clicked;
                selectedRow = clickedRow;
                highlightRow(clickedRow, true);
                hintLabel.setText("Selected: " + clicked.getName() + " — now click the player to swap with.");
            } else {
                userTeam.swapPlayers(selectedPlayer, clicked);
                hintLabel.setText("Click a player to select, then click another to swap.");
                refreshLineupCard();
            }
        }
    }

    private void clearSelection() {
        if (selectedRow != null) highlightRow(selectedRow, false);
        selectedPlayer = null;
        selectedRow = null;
        hintLabel.setText("Click a player to select, then click another to swap.");
    }

    private void highlightRow(HBox row, boolean selected) {
        if (selected) {
            row.setStyle(row.getStyle() + " -fx-border-color: #e94560; -fx-border-width: 1.5; -fx-border-radius: 6;");
        } else {
            String base = row.getStyle()
                    .replaceAll("-fx-border-color:[^;]+;", "")
                    .replaceAll("-fx-border-width:[^;]+;", "")
                    .replaceAll("-fx-border-radius:[^;]+;", "");
            row.setStyle(base);
        }
    }

    // ── Warning overlay ───────────────────────────────────────────────

    private void handleStartMatch() {
        List<Player> squad = userTeam.getSquad();
        List<String> unavailable = new ArrayList<>();
        for (int i = 0; i < Math.min(11, squad.size()); i++) {
            Player p = squad.get(i);
            if (!p.isAvailable()) {
                String suffix = p.isSuspended()
                        ? "suspended — " + p.getSuspensionGamesRemaining() + " match" + (p.getSuspensionGamesRemaining() > 1 ? "es" : "")
                        : "injured — " + p.getInjuryGamesRemaining() + " match" + (p.getInjuryGamesRemaining() > 1 ? "es" : "");
                unavailable.add(p.getName() + "  [" + suffix + "]");
            }
        }

        boolean hasAvailableBench = squad.subList(Math.min(11, squad.size()), squad.size())
                .stream().anyMatch(Player::isAvailable);

        if (!unavailable.isEmpty() && hasAvailableBench) {
            showWarningOverlay(unavailable);
        } else {
            ViewManager.getInstance().switchView(new LiveMatchView(match));
        }
    }

    private void showWarningOverlay(List<String> unavailablePlayers) {
        overlayLayer.getChildren().clear();

        // ── Modal card ────────────────────────────────────────────────
        VBox card = new VBox(16);
        card.setAlignment(Pos.TOP_LEFT);
        card.setMaxWidth(420);
        card.setStyle(
                "-fx-background-color: #12122a;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #e94560;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 24;"
        );

        // Title
        Label title = new Label("Lineup Warning");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        // Subtitle
        Label subtitle = new Label("The following players cannot play:");
        subtitle.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");

        // Player list
        VBox playerList = new VBox(6);
        for (String entry : unavailablePlayers) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 10, 6, 10));
            row.setStyle("-fx-background-color: #1a1a3a; -fx-background-radius: 6;");
            Label lbl = new Label(entry);
            lbl.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 12px;");
            row.getChildren().add(lbl);
            playerList.getChildren().add(row);
        }

        // Hint
        Label hint = new Label("You have available substitutes on the bench.\nSwap them before starting or continue anyway.");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        hint.setWrapText(true);

        // Buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button goBackBtn = new Button("Go Back & Fix");
        goBackBtn.getStyleClass().add("btn-secondary");
        goBackBtn.setOnAction(e -> overlayLayer.setVisible(false));

        Button continueBtn = new Button("Start Anyway ▶");
        continueBtn.getStyleClass().add("btn-primary");
        continueBtn.setOnAction(e -> {
            overlayLayer.setVisible(false);
            ViewManager.getInstance().switchView(new LiveMatchView(match));
        });

        buttons.getChildren().addAll(goBackBtn, continueBtn);

        card.getChildren().addAll(title, subtitle, playerList, hint, buttons);

        overlayLayer.getChildren().add(card);
        overlayLayer.setVisible(true);
    }

    // ── Row & badge helpers ───────────────────────────────────────────

    private HBox createPlayerRow(Player p, boolean isStarter) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 10, 6, 10));
        row.setStyle("-fx-background-color: " + (isStarter ? "#1a1a2e" : "#15152a") + "; -fx-background-radius: 6; -fx-cursor: hand;");

        Label name = new Label(p.getName());
        name.getStyleClass().add("text-normal");
        name.setStyle("-fx-font-size: 12px;");
        name.setPrefWidth(130);

        Label badge = createPositionBadge(p);

        Label ovr = new Label(String.valueOf(p.getOverallRating()));
        ovr.setStyle("-fx-text-fill: #00d2ff; -fx-font-weight: bold; -fx-font-size: 12px;");

        StackPane staminaBar = StaminaBar.create(p, 48, 5);

        row.getChildren().addAll(name, badge, ovr, staminaBar);

        if (p.isSuspended()) {
            Label sus = new Label("BAN " + p.getSuspensionGamesRemaining());
            sus.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 11px; -fx-font-weight: bold;");
            row.getChildren().add(sus);
            row.setOpacity(0.5);
        } else if (p.isInjured()) {
            Label inj = new Label("INJ " + p.getInjuryGamesRemaining());
            inj.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 11px; -fx-font-weight: bold;");
            row.getChildren().add(inj);
            row.setOpacity(0.5);
        }

        row.setOnMouseClicked(e -> onPlayerClicked(p, row));
        row.setOnMouseEntered(e -> {
            if (row != selectedRow) {
                String cur = row.getStyle();
                row.setStyle(cur.replace("-fx-background-color: " + (isStarter ? "#1a1a2e" : "#15152a"),
                        "-fx-background-color: " + (isStarter ? "#222240" : "#1c1c38")));
            }
        });
        row.setOnMouseExited(e -> {
            if (row != selectedRow) {
                String cur = row.getStyle();
                row.setStyle(cur.replace("-fx-background-color: " + (isStarter ? "#222240" : "#1c1c38"),
                        "-fx-background-color: " + (isStarter ? "#1a1a2e" : "#15152a")));
            }
        });

        // ── Drag & drop ──
        row.setOnDragDetected(e -> {
            int idx = playerRows.indexOf(row);
            if (idx < 0) return;
            Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString(String.valueOf(idx));
            db.setContent(cc);
            row.setOpacity(0.4);
            e.consume();
        });

        row.setOnDragOver(e -> {
            if (e.getGestureSource() != row && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                row.setStyle(row.getStyle()
                        + " -fx-border-color: #00d2ff; -fx-border-width: 1.5; -fx-border-radius: 6;");
            }
            e.consume();
        });

        row.setOnDragExited(e -> {
            String base = row.getStyle()
                    .replaceAll("-fx-border-color:[^;]+;", "")
                    .replaceAll("-fx-border-width:[^;]+;", "")
                    .replaceAll("-fx-border-radius:[^;]+;", "");
            row.setStyle(base);
            e.consume();
        });

        row.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasString()) {
                int srcIdx = Integer.parseInt(db.getString());
                int tgtIdx = playerRows.indexOf(row);
                if (srcIdx >= 0 && tgtIdx >= 0 && srcIdx != tgtIdx) {
                    List<Player> squad = userTeam.getSquad();
                    userTeam.swapPlayers(squad.get(srcIdx), squad.get(tgtIdx));
                    refreshLineupCard();
                }
                e.setDropCompleted(true);
            }
            e.consume();
        });

        row.setOnDragDone(e -> {
            row.setOpacity(p.isAvailable() ? 1.0 : 0.5);
            e.consume();
        });

        return row;
    }

    private Label createPositionBadge(Player p) {
        String posName = "?";
        String badgeClass = "badge-def";

        if (p instanceof FootballPlayer fp) {
            FootballPosition pos = fp.getPosition();
            posName = getShortPosition(pos);
            if (pos == FootballPosition.GOALKEEPER) badgeClass = "badge-gk";
            else if (pos.isDefensive()) badgeClass = "badge-def";
            else if (pos.isMidfield()) badgeClass = "badge-mid";
            else badgeClass = "badge-fwd";
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
            case LEFT_MIDFIELDER -> "LM";
            case RIGHT_MIDFIELDER -> "RM";
            case LEFT_WINGER -> "LW";
            case RIGHT_WINGER -> "RW";
            case STRIKER -> "ST";
            case CENTRE_FORWARD -> "CF";
        };
    }
}
