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
import javafx.scene.layout.StackPane;

import java.util.List;

/**
 * Half-time substitution panel (EA FC style).
 * Left: pitch — click a field player to mark as "out".
 * Right: bench cards — click to select "in", then confirm.
 */
public class SubstitutionView extends HBox {

    private final Match match;
    private final MatchState matchState;
    private final MatchEngine engine;
    private final Team userTeam;
    private final boolean isHome;
    private final List<Player> fieldPlayers;
    private final List<Player> bench;
    private final Formation formation;
    private final int maxSubs;

    private Player selectedOut = null;
    private Player selectedIn  = null;

    private final FormationPitchView pitchView;
    private final VBox benchPanel;
    private final Label statusLabel;
    private final Button confirmBtn;

    public SubstitutionView(Match match, MatchState matchState, MatchEngine engine) {
        this.match       = match;
        this.matchState  = matchState;
        this.engine      = engine;

        SeasonState state = GameManager.getInstance().getState();
        Sport sport       = state.getCurrentSport();
        this.userTeam     = state.getUserTeam();
        this.isHome       = userTeam == match.getHomeTeam();
        this.maxSubs      = sport.getMaxSubstitutions();
        this.fieldPlayers = isHome ? matchState.getHomeFieldPlayers() : matchState.getAwayFieldPlayers();
        this.bench        = isHome ? matchState.getHomeBenchPlayers() : matchState.getAwayBenchPlayers();
        this.formation    = userTeam.getFormation();

        setSpacing(0);

        // ── Left: pitch ──────────────────────────────────────────────────────────
        VBox pitchPanel = new VBox(10);
        pitchPanel.setPadding(new Insets(20, 12, 20, 20));
        pitchPanel.setAlignment(Pos.TOP_CENTER);
        pitchPanel.setStyle("-fx-background-color: #16213e;");
        pitchPanel.setPrefWidth(300);
        pitchPanel.setMinWidth(300);

        int usedSubs  = isHome ? matchState.getHomeSubsUsed() : matchState.getAwaySubsUsed();
        int remaining = maxSubs - usedSubs;

        Label title = new Label("Substitutions");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        Label remLabel = new Label("Remaining: " + remaining + " / " + maxSubs);
        remLabel.getStyleClass().add("text-muted");

        pitchView = new FormationPitchView(null);
        StackPane pitchWrapper = new StackPane(pitchView);
        pitchWrapper.setAlignment(Pos.CENTER);

        statusLabel = new Label("Click a player on the pitch to substitute");
        statusLabel.setStyle("-fx-text-fill: #aaaacc; -fx-font-size: 11px;");
        statusLabel.setWrapText(true);

        Button backBtn = new Button("← Back to Half-Time");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e ->
                ViewManager.getInstance().switchView(new BreakView(match, matchState, engine)));

        pitchPanel.getChildren().addAll(title, remLabel, pitchWrapper, statusLabel, backBtn);

        // ── Right: bench ─────────────────────────────────────────────────────────
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(20, 20, 20, 12));
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        Label benchTitle = new Label("Bench Players");
        benchTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        Label benchHint = new Label("Select a player on the pitch first");
        benchHint.getStyleClass().add("text-muted");
        benchHint.setStyle("-fx-font-size: 11px;");

        benchPanel = new VBox(6);
        ScrollPane benchScroll = new ScrollPane(benchPanel);
        benchScroll.setFitToWidth(true);
        benchScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(benchScroll, Priority.ALWAYS);

        confirmBtn = new Button("Confirm Substitution");
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setDisable(true);
        confirmBtn.setOnAction(e -> handleConfirm());

        rightPanel.getChildren().addAll(benchTitle, benchHint, benchScroll, confirmBtn);

        getChildren().addAll(pitchPanel, rightPanel);

        refreshPitch();
        refreshBench();
    }

    // ── Refresh ──────────────────────────────────────────────────────────────────

    private void refreshPitch() {
        pitchView.redrawWithPlayers(formation, fieldPlayers, selectedOut, player -> {
            if (player == selectedOut) {
                selectedOut = null;
                selectedIn  = null;
                statusLabel.setText("Click a player on the pitch to substitute");
                confirmBtn.setDisable(true);
            } else {
                selectedOut = player;
                selectedIn  = null;
                statusLabel.setText("Out: " + player.getName() + "  — pick a bench player →");
                confirmBtn.setDisable(true);
            }
            refreshPitch();
            refreshBench();
        });
    }

    private void refreshBench() {
        benchPanel.getChildren().clear();

        List<Player> candidates = selectedOut != null
                ? filterByZone(bench, footballPos(selectedOut))
                : bench;

        if (candidates.isEmpty()) {
            Label none = new Label("No compatible bench players available");
            none.getStyleClass().add("text-muted");
            benchPanel.getChildren().add(none);
            return;
        }

        for (Player p : candidates)
            benchPanel.getChildren().add(buildBenchCard(p));
    }

    private HBox buildBenchCard(Player p) {
        boolean sel   = (p == selectedIn);
        boolean avail = p.isAvailable();

        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setStyle(cardStyle(sel, false));

        Label avatar = new Label(initials(p.getName()));
        avatar.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #00d2ff;"
                + " -fx-background-color: #0f3460; -fx-background-radius: 50;"
                + " -fx-min-width: 34; -fx-min-height: 34; -fx-max-width: 34; -fx-max-height: 34;"
                + " -fx-alignment: center;");

        VBox info = new VBox(2);
        Label name = new Label(p.getName());
        name.setStyle("-fx-text-fill: " + (sel ? "#00e676" : "#e0e0ff")
                + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label pos = new Label(posName(p) + "  ·  OVR " + p.getOverallRating());
        pos.setStyle("-fx-text-fill: #888899; -fx-font-size: 11px;");
        StackPane staminaBar = StaminaBar.create(p, 80, 5);
        info.getChildren().addAll(name, pos, staminaBar);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (!avail) {
            String reason = p.isSuspended()
                    ? "Suspended (" + p.getSuspensionGamesRemaining() + ")"
                    : "Injured (" + p.getInjuryGamesRemaining() + ")";
            Label badge = new Label(reason);
            badge.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 10px;");
            card.getChildren().addAll(avatar, info, spacer, badge);
            card.setOpacity(0.5);
        } else {
            Label check = new Label(sel ? "✓" : "");
            check.setStyle("-fx-text-fill: #00e676; -fx-font-size: 16px; -fx-font-weight: bold;");
            card.getChildren().addAll(avatar, info, spacer, check);

            card.setOnMouseClicked(e -> {
                if (selectedOut == null) return;
                selectedIn = p;
                confirmBtn.setDisable(false);
                statusLabel.setText("Out: " + selectedOut.getName() + "\nIn:  " + p.getName());
                refreshBench();
            });
            card.setOnMouseEntered(e -> { if (!sel) card.setStyle(cardStyle(false, true)); });
            card.setOnMouseExited(e -> { if (!sel) card.setStyle(cardStyle(false, false)); });
        }
        return card;
    }

    private String cardStyle(boolean selected, boolean hovered) {
        if (selected)
            return "-fx-background-color: #1a3a1a; -fx-background-radius: 8;"
                    + " -fx-border-color: #00e676; -fx-border-width: 1.5; -fx-border-radius: 8;"
                    + " -fx-cursor: hand;";
        if (hovered)
            return "-fx-background-color: #222240; -fx-background-radius: 8; -fx-cursor: hand;";
        return "-fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-cursor: hand;";
    }

    // ── Confirm ──────────────────────────────────────────────────────────────────

    private void handleConfirm() {
        if (selectedOut == null || selectedIn == null) return;

        boolean ok = matchState.makeSubstitution(userTeam.getTeamId(), selectedOut, selectedIn, maxSubs);
        if (!ok) {
            statusLabel.setText("Substitution failed — no subs remaining or player unavailable.");
            return;
        }
        matchState.addEvent(new FootballMatchEvent(
                FootballEventType.SUBSTITUTION,
                matchState.getCurrentMinute(),
                selectedOut, selectedIn,
                userTeam.getTeamId()));

        ViewManager.getInstance().switchView(new SubstitutionView(match, matchState, engine));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private FootballPosition footballPos(Player p) {
        return p instanceof FootballPlayer fp ? fp.getPosition() : null;
    }

    private String posName(Player p) {
        FootballPosition pos = footballPos(p);
        return pos != null ? pos.getName() : "—";
    }

    private List<Player> filterByZone(List<Player> players, FootballPosition outPos) {
        if (outPos == null) return players;
        List<Player> same = players.stream()
                .filter(p -> isSameZone(footballPos(p), outPos))
                .toList();
        return same.isEmpty() ? players : same;
    }

    private boolean isSameZone(FootballPosition a, FootballPosition b) {
        if (a == null || b == null) return true;
        if (a == FootballPosition.GOALKEEPER || b == FootballPosition.GOALKEEPER) return a == b;
        return a.isDefensive() == b.isDefensive()
                && a.isMidfield()  == b.isMidfield()
                && a.isAttacking() == b.isAttacking();
    }

    private String initials(String fullName) {
        String[] parts = fullName.split("\\s+");
        if (parts.length >= 2)
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
    }
}
