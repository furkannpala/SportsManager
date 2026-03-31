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
import javafx.scene.layout.*;

import java.util.List;

/**
 * Screen 5 — Pre-match setup with lineup, formation, and tactic selection.
 */
public class PreMatchSetupView extends VBox {

    private final SeasonState state;
    private final Match match;
    private final Team userTeam;

    public PreMatchSetupView(Match match) {
        this.state = GameManager.getInstance().getState();
        this.match = match;
        this.userTeam = state.getUserTeam();
        setSpacing(16);
        setPadding(new Insets(20));
        buildUI();
    }

    private void buildUI() {
        int currentWeek = state.getCurrentWeek();
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

        // Header
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

        // Content: Lineup + Tactic
        HBox content = new HBox(16);

        // Starting lineup
        VBox lineupCard = new VBox(8);
        lineupCard.getStyleClass().add("card");
        lineupCard.setPrefWidth(350);

        Label lineupTitle = new Label("📋 Starting Lineup");
        lineupTitle.getStyleClass().add("section-label");
        lineupCard.getChildren().add(lineupTitle);

        List<Player> squad = userTeam.getSquad();
        int slotIndex = 0;
        for (Player p : squad) {
            if (slotIndex >= 11) break;
            HBox row = createPlayerRow(p, true);
            lineupCard.getChildren().add(row);
            slotIndex++;
        }

        // Bench
        Label benchTitle = new Label("🪑 Substitutes");
        benchTitle.getStyleClass().add("section-label");
        benchTitle.setPadding(new Insets(8, 0, 0, 0));
        lineupCard.getChildren().add(benchTitle);

        for (int i = 11; i < squad.size(); i++) {
            Player p = squad.get(i);
            HBox row = createPlayerRow(p, false);
            lineupCard.getChildren().add(row);
        }

        ScrollPane lineupScroll = new ScrollPane(lineupCard);
        lineupScroll.setFitToWidth(true);
        HBox.setHgrow(lineupScroll, Priority.ALWAYS);

        // Right panel: Tactic
        VBox tacticCard = new VBox(12);
        tacticCard.getStyleClass().add("card");
        tacticCard.setPrefWidth(280);

        Label tacticTitle = new Label("⚙️ Tactics");
        tacticTitle.getStyleClass().add("section-label");
        tacticCard.getChildren().add(tacticTitle);

        // Formation selector
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
                    break;
                }
            }
        });

        tacticCard.getChildren().addAll(formLabel, formationBox);

        // Tactic buttons
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
                tacticButtons.getChildren().forEach(node -> {
                    node.getStyleClass().remove("btn-tactic-active");
                });
                btn.getStyleClass().add("btn-tactic-active");
            });
            tacticButtons.getChildren().add(btn);
        }
        tacticCard.getChildren().add(tacticButtons);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        tacticCard.getChildren().add(spacer);

        // Start Match button
        Button startMatch = new Button("Start Match ▶");
        startMatch.getStyleClass().add("btn-primary");
        startMatch.setMaxWidth(Double.MAX_VALUE);
        startMatch.setOnAction(e -> {
            ViewManager.getInstance().switchView(new LiveMatchView(match));
        });
        tacticCard.getChildren().add(startMatch);

        content.getChildren().addAll(lineupScroll, tacticCard);
        VBox.setVgrow(content, Priority.ALWAYS);

        // Back button
        Button backBtn = new Button("← Back to Dashboard");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setOnAction(e -> ViewManager.getInstance().switchView(new DashboardView()));

        getChildren().addAll(header, content, backBtn);
    }

    private HBox createPlayerRow(Player p, boolean isStarter) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 10, 6, 10));
        row.setStyle("-fx-background-color: " + (isStarter ? "#1a1a2e" : "#15152a") + "; -fx-background-radius: 6;");

        Label name = new Label(p.getName());
        name.getStyleClass().add("text-normal");
        name.setStyle("-fx-font-size: 12px;");
        name.setPrefWidth(140);

        Label badge = createPositionBadge(p);

        Label ovr = new Label(String.valueOf(p.getOverallRating()));
        ovr.setStyle("-fx-text-fill: #00d2ff; -fx-font-weight: bold; -fx-font-size: 12px;");

        row.getChildren().addAll(name, badge, ovr);

        if (!p.isAvailable()) {
            Label inj = new Label("🏥 " + p.getInjuryGamesRemaining());
            inj.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 11px;");
            row.getChildren().add(inj);
            row.setOpacity(0.5);
        }

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
            case LEFT_WINGER -> "LW";
            case RIGHT_WINGER -> "RW";
            case STRIKER -> "ST";
            case CENTRE_FORWARD -> "CF";
        };
    }
}
