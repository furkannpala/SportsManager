package com.sportsmanager.ui;

import com.sportsmanager.core.*;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.league.Match;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Screen — Half-time tactics panel, opened from BreakView.
 */
public class TacticsView extends VBox {

    public TacticsView(Match match, MatchState matchState, MatchEngine engine) {
        SeasonState state = GameManager.getInstance().getState();
        Sport sport = state.getCurrentSport();
        Team userTeam = state.getUserTeam();

        setSpacing(14);
        setPadding(new Insets(24));

        // ── Header ──────────────────────────────────────────────────────────────
        Label title = new Label("Tactics");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        // ── Formation ───────────────────────────────────────────────────────────
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

        // ── Playing style ────────────────────────────────────────────────────────
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

        // ── Back button ─────────────────────────────────────────────────────────
        Button backBtn = new Button("← Back to Half-Time");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setPadding(new Insets(8, 0, 0, 0));
        backBtn.setOnAction(e ->
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine)));

        getChildren().addAll(title, formLabel, formationBox, styleLabel, tacticButtons, backBtn);
    }
}
