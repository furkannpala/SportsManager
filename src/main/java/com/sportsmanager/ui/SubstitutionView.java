package com.sportsmanager.ui;

import com.sportsmanager.core.*;
import com.sportsmanager.football.FootballEventType;
import com.sportsmanager.football.FootballMatchEvent;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
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
        Sport sport       = state.getCurrentSport();
        Team userTeam     = state.getUserTeam();
        boolean isHome    = userTeam == match.getHomeTeam();

        setSpacing(14);
        setPadding(new Insets(24));

        int maxSubs  = sport.getMaxSubstitutions();
        int usedSubs = isHome ? matchState.getHomeSubsUsed() : matchState.getAwaySubsUsed();

        List<Player> fieldPlayers = isHome
                ? matchState.getHomeFieldPlayers()
                : matchState.getAwayFieldPlayers();

        List<Player> bench = isHome
                ? matchState.getHomeBenchPlayers()
                : matchState.getAwayBenchPlayers();

        // ── Header ──────────────────────────────────────────────────────────────
        Label title = new Label("Substitutions");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        Label remaining = new Label("Remaining: " + (maxSubs - usedSubs) + " / " + maxSubs);
        remaining.getStyleClass().add("text-muted");

        // ── Player Out ──────────────────────────────────────────────────────────
        Label outLabel = new Label("Player Out");
        outLabel.getStyleClass().add("text-muted");

        ComboBox<String> outBox = new ComboBox<>();
        for (Player p : fieldPlayers) {
            String fatigue = p.getAge() > 30 ? " · Tired" : "";
            outBox.getItems().add(p.getName() + "  [" + positionName(p) + "]" + fatigue + "  OVR " + p.getOverallRating());
        }
        outBox.setMaxWidth(Double.MAX_VALUE);
        outBox.setPromptText("Select player to remove…");

        Label positionBadge = new Label("");
        positionBadge.setStyle("-fx-text-fill: #ffd740; -fx-font-size: 12px; -fx-font-weight: bold;");

        // ── Player In ───────────────────────────────────────────────────────────
        Label inLabel = new Label("Player In");
        inLabel.getStyleClass().add("text-muted");

        ComboBox<String> inBox = new ComboBox<>();
        inBox.setMaxWidth(Double.MAX_VALUE);
        inBox.setPromptText("First select the player going out…");

        final List<Player>[] filteredBenchRef = new List[]{ List.of() };

        outBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int idx = newVal.intValue();
            inBox.getItems().clear();
            inBox.getSelectionModel().clearSelection();

            if (idx < 0) {
                positionBadge.setText("");
                filteredBenchRef[0] = List.of();
                return;
            }

            Player outPlayer = fieldPlayers.get(idx);
            FootballPosition outPos = footballPosition(outPlayer);
            positionBadge.setText("Position: " + positionName(outPlayer));

            List<Player> sameZone = bench.stream()
                    .filter(p -> isSameZone(footballPosition(p), outPos))
                    .toList();
            List<Player> filtered = sameZone.isEmpty() ? bench : sameZone;
            filteredBenchRef[0] = filtered;

            for (Player p : filtered) {
                inBox.getItems().add(p.getName() + "  [" + positionName(p) + "]  OVR " + p.getOverallRating());
            }
            inBox.setPromptText(filtered.isEmpty() ? "No bench players available" : "Select player to add…");
        });

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px;");

        Button subBtn = new Button("Confirm Substitution");
        subBtn.getStyleClass().add("btn-primary");
        subBtn.setMaxWidth(Double.MAX_VALUE);
        subBtn.setOnAction(e -> {
            int outIdx = outBox.getSelectionModel().getSelectedIndex();
            int inIdx  = inBox.getSelectionModel().getSelectedIndex();
            if (outIdx < 0 || inIdx < 0) { errorLabel.setText("Select both players."); return; }

            Player outPlayer = fieldPlayers.get(outIdx);
            List<Player> filteredBench = filteredBenchRef[0];
            if (inIdx >= filteredBench.size()) { errorLabel.setText("Invalid selection."); return; }
            Player inPlayer = filteredBench.get(inIdx);

            boolean ok = matchState.makeSubstitution(userTeam.getTeamId(), outPlayer, inPlayer, maxSubs);
            if (!ok) { errorLabel.setText("Substitution failed — no subs remaining or invalid."); return; }

            matchState.addEvent(new FootballMatchEvent(
                    FootballEventType.SUBSTITUTION,
                    matchState.getCurrentMinute(),
                    outPlayer, inPlayer,
                    userTeam.getTeamId()));

            ViewManager.getInstance().switchView(new SubstitutionView(match, matchState, engine));
        });

        // ── Back button ─────────────────────────────────────────────────────────
        Button backBtn = new Button("← Back to Half-Time");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e ->
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine)));

        getChildren().addAll(title, remaining, outLabel, outBox, positionBadge,
                inLabel, inBox, subBtn, errorLabel, backBtn);
    }

    private FootballPosition footballPosition(Player p) {
        if (p instanceof FootballPlayer fp) return fp.getPosition();
        return null;
    }

    private String positionName(Player p) {
        FootballPosition pos = footballPosition(p);
        return pos != null ? pos.getName() : "—";
    }

    private boolean isSameZone(FootballPosition a, FootballPosition b) {
        if (a == null || b == null) return true;
        if (a == FootballPosition.GOALKEEPER || b == FootballPosition.GOALKEEPER)
            return a == b;
        return a.isDefensive() == b.isDefensive()
                && a.isMidfield()  == b.isMidfield()
                && a.isAttacking() == b.isAttacking();
    }
}
