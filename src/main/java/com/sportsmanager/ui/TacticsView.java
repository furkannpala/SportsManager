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
 * Half-time tactics panel. Left: formation pitch with player names. Right: controls.
 */
public class TacticsView extends HBox {

    public TacticsView(Match match, MatchState matchState, MatchEngine engine) {
        SeasonState state = GameManager.getInstance().getState();
        Sport sport = state.getCurrentSport();
        Team userTeam = state.getUserTeam();
        boolean isHome = userTeam == match.getHomeTeam();

        List<Player> fieldPlayers = isHome
                ? matchState.getHomeFieldPlayers()
                : matchState.getAwayFieldPlayers();

        setSpacing(0);

        // ── Left: pitch ──────────────────────────────────────────────────────────
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(20, 12, 20, 20));
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setStyle("-fx-background-color: #16213e;");
        leftPanel.setPrefWidth(300);
        leftPanel.setMinWidth(300);

        Label title = new Label("Tactics");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        FormationPitchView pitchView = new FormationPitchView(null);
        pitchView.redrawWithPlayers(userTeam.getFormation(), fieldPlayers, null, null);
        StackPane pitchWrapper = new StackPane(pitchView);
        pitchWrapper.setAlignment(Pos.CENTER);

        Button backBtn = new Button("← Back to Half-Time");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e ->
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine)));

        leftPanel.getChildren().addAll(title, pitchWrapper, backBtn);

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
                    pitchView.redrawWithPlayers(f, fieldPlayers, null, null);
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
}
