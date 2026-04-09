package com.sportsmanager.ui;

import com.sportsmanager.core.*;
import com.sportsmanager.football.FootballEventType;
import com.sportsmanager.football.FootballMatchEvent;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.league.Match;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Screen — Half-time substitution panel, opened from BreakView.
 */
public class SubstitutionView extends VBox {

    public SubstitutionView(Match match, MatchState matchState, MatchEngine engine) {
        SeasonState state = GameManager.getInstance().getState();
        Sport sport = state.getCurrentSport();
        Team userTeam = state.getUserTeam();
        boolean isHome = userTeam == match.getHomeTeam();

        setSpacing(14);
        setPadding(new Insets(24));

        // ── Header ──────────────────────────────────────────────────────────────
        Label title = new Label("Substitutions");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        int maxSubs  = sport.getMaxSubstitutions();
        int usedSubs = isHome ? matchState.getHomeSubsUsed() : matchState.getAwaySubsUsed();
        Label remaining = new Label("Remaining: " + (maxSubs - usedSubs) + " / " + maxSubs);
        remaining.getStyleClass().add("text-muted");

        // ── Selectors ───────────────────────────────────────────────────────────
        List<Player> fieldPlayers = isHome
                ? matchState.getHomeFieldPlayers()
                : matchState.getAwayFieldPlayers();

        Label outLabel = new Label("Player Out");
        outLabel.getStyleClass().add("text-muted");

        ComboBox<String> outBox = new ComboBox<>();
        for (Player p : fieldPlayers) {
            String fatigue = p.getAge() > 30 ? " (Tired)" : " (Fresh)";
            outBox.getItems().add(p.getName() + " — OVR " + p.getOverallRating() + fatigue);
        }
        outBox.setMaxWidth(Double.MAX_VALUE);

        Label inLabel = new Label("Player In");
        inLabel.getStyleClass().add("text-muted");

        ComboBox<String> inBox = new ComboBox<>();
        for (Player p : userTeam.getSquad()) {
            if (!fieldPlayers.contains(p) && p.isAvailable()) {
                inBox.getItems().add(p.getName() + " — OVR " + p.getOverallRating());
            }
        }
        inBox.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px;");

        Button subBtn = new Button("Confirm Substitution");
        subBtn.getStyleClass().add("btn-primary");
        subBtn.setMaxWidth(Double.MAX_VALUE);
        subBtn.setOnAction(e -> {
            int outIdx = outBox.getSelectionModel().getSelectedIndex();
            int inIdx  = inBox.getSelectionModel().getSelectedIndex();
            if (outIdx < 0 || inIdx < 0) {
                errorLabel.setText("Select both players.");
                return;
            }

            Player outPlayer = fieldPlayers.get(outIdx);
            int benchIdx = 0;
            Player inPlayer = null;
            for (Player p : userTeam.getSquad()) {
                if (!fieldPlayers.contains(p) && p.isAvailable()) {
                    if (benchIdx == inIdx) { inPlayer = p; break; }
                    benchIdx++;
                }
            }
            if (inPlayer == null) { errorLabel.setText("Invalid selection."); return; }

            boolean ok = matchState.makeSubstitution(userTeam.getTeamId(), outPlayer, inPlayer, maxSubs);
            if (ok) {
                matchState.addEvent(new FootballMatchEvent(
                        FootballEventType.SUBSTITUTION,
                        matchState.getCurrentMinute(),
                        outPlayer, inPlayer,
                        userTeam.getTeamId()));
                // Refresh this view to reflect updated counts
                ViewManager.getInstance().switchView(new SubstitutionView(match, matchState, engine));
            } else {
                errorLabel.setText("Substitution failed — no subs remaining or invalid.");
            }
        });

        // ── Back button ─────────────────────────────────────────────────────────
        Button backBtn = new Button("← Back to Half-Time");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e ->
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine)));

        getChildren().addAll(title, remaining, outLabel, outBox, inLabel, inBox, subBtn, errorLabel, backBtn);
    }
}
