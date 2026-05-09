package com.sportsmanager.ui;

import com.sportsmanager.core.*;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.league.Match;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Half-time tactics panel.
 *
 * Left: Formation pitch with click-to-swap player positions.
 *   1st click  — selects a player (green highlight, status label updated).
 *   2nd click  — swaps the two players in MatchState (field list + playing
 *                position map). The engine uses the new positions from the
 *                next simulated minute onward. No permanent Team data touched.
 *   Re-click   — deselects the currently selected player.
 *
 * Right: Formation ComboBox + tactic style buttons.
 */
public class TacticsView extends HBox {

    private final Match match;
    private final MatchState matchState;
    private final MatchEngine engine;
    private final Sport sport;
    private final Team userTeam;
    private final String teamId;
    private final List<Player> fieldPlayers;

    private FormationPitchView pitchView;
    private Player pendingSwap = null;
    private Label swapStatusLabel;

    public TacticsView(Match match, MatchState matchState, MatchEngine engine) {
        this.match        = match;
        this.matchState   = matchState;
        this.engine       = engine;
        SeasonState state = GameManager.getInstance().getState();
        this.sport        = state.getCurrentSport();
        this.userTeam     = state.getUserTeam();
        this.teamId       = userTeam.getTeamId();
        boolean isHome    = userTeam == match.getHomeTeam();
        this.fieldPlayers = isHome
                ? matchState.getHomeFieldPlayers()
                : matchState.getAwayFieldPlayers();

        setSpacing(0);
        buildUI();
    }

    // ── Build ────────────────────────────────────────────────────────────────────

    private void buildUI() {
        // ── Left: pitch ──────────────────────────────────────────────────────────
        VBox leftPanel = new VBox(8);
        leftPanel.setPadding(new Insets(20, 12, 20, 20));
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setStyle("-fx-background-color: #16213e;");
        leftPanel.setPrefWidth(300);
        leftPanel.setMinWidth(300);

        Label title = new Label("Tactics");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        swapStatusLabel = new Label("Click a player to select, then click another to swap positions.");
        swapStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888899;");
        swapStatusLabel.setWrapText(true);
        swapStatusLabel.setMaxWidth(260);

        pitchView = new FormationPitchView(null);
        refreshPitch();
        StackPane pitchWrapper = new StackPane(pitchView);
        pitchWrapper.setAlignment(Pos.CENTER);

        Button backBtn = new Button("← Back to Half-Time");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e ->
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine)));

        leftPanel.getChildren().addAll(title, swapStatusLabel, pitchWrapper, backBtn);

        // ── Right: controls ──────────────────────────────────────────────────────
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(20, 20, 20, 12));
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        Label formLabel = new Label("Formation");
        formLabel.getStyleClass().add("text-muted");

        ComboBox<String> formationBox = new ComboBox<>();
        for (Formation f : sport.getFormations()) formationBox.getItems().add(f.getFormationName());
        if (userTeam.getFormation() != null)
            formationBox.setValue(userTeam.getFormation().getFormationName());
        formationBox.setMaxWidth(Double.MAX_VALUE);
        formationBox.setOnAction(e -> {
            String sel = formationBox.getValue();
            for (Formation f : sport.getFormations()) {
                if (f.getFormationName().equals(sel)) {
                    userTeam.setFormation(f);
                    refreshPitch();
                    break;
                }
            }
        });

        Label styleLabel = new Label("Playing Style");
        styleLabel.getStyleClass().add("text-muted");
        styleLabel.setPadding(new Insets(8, 0, 0, 0));

        VBox tacticButtons = new VBox(8);
        for (Tactic t : sport.getTactics()) {
            Button btn = new Button(t.getTacticName());
            btn.getStyleClass().add("btn-tactic");
            btn.setMaxWidth(Double.MAX_VALUE);
            boolean active = userTeam.getTactic() != null
                    && userTeam.getTactic().getTacticName().equals(t.getTacticName());
            if (active) btn.getStyleClass().add("btn-tactic-active");
            btn.setOnAction(e -> {
                userTeam.setTactic(t);
                ViewManager.getInstance().switchView(new TacticsView(match, matchState, engine));
            });
            tacticButtons.getChildren().add(btn);
        }

        rightPanel.getChildren().addAll(formLabel, formationBox, styleLabel, tacticButtons);
        getChildren().addAll(leftPanel, rightPanel);
    }

    // ── Pitch ────────────────────────────────────────────────────────────────────

    private void refreshPitch() {
        pitchView.redrawWithPlayers(
                userTeam.getFormation(), fieldPlayers, pendingSwap, this::onPitchPlayerClick);
    }

    /**
     * MouseClick handler wired into every player node on the pitch.
     * 1st call  → selects the player (pendingSwap set, green highlight shown).
     * Same player again → deselects.
     * Different player → swaps in MatchState and redraws.
     */
    private void onPitchPlayerClick(Player clicked) {
        if (pendingSwap == null) {
            pendingSwap = clicked;
        } else if (pendingSwap == clicked) {
            pendingSwap = null;
        } else {
            matchState.swapFieldPositions(teamId, pendingSwap, clicked);
            pendingSwap = null;
        }
        updateSwapStatus();
        refreshPitch();
    }

    private void updateSwapStatus() {
        if (pendingSwap != null) {
            swapStatusLabel.setText(
                    "Selected: " + pendingSwap.getName() + " — click another player to swap.");
            swapStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #00e676;");
        } else {
            swapStatusLabel.setText(
                    "Click a player to select, then click another to swap positions.");
            swapStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888899;");
        }
    }
}
