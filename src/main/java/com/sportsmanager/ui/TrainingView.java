package com.sportsmanager.ui;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Position;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.football.FootballTrainingOptions;
import com.sportsmanager.football.PositionalTrainingOption;
import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.handball.HandballPlayer;
import com.sportsmanager.handball.HandballPosition;
import com.sportsmanager.handball.HandballTrainingOptions;
import com.sportsmanager.training.PlayerTrainingPlan;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class TrainingView extends HBox {

    private static final String C_FORM_VG  = "#145214";
    private static final String C_FORM_G   = "#27ae60";
    private static final String C_FORM_MID = "#b8860b";
    private static final String C_FORM_BAD = "#c0392b";
    private static final String C_FORM_VB  = "#6b0000";

    private final SeasonState state;
    private final Team        userTeam;
    private final Sport       sport;

    private Player selectedPlayer = null;

    private VBox playerListBox;
    private VBox rightPanel;

    public TrainingView() {
        this.state    = GameManager.getInstance().getState();
        this.userTeam = state.getUserTeam();
        this.sport    = state.getCurrentSport();
        setSpacing(0);
        setPrefWidth(Double.MAX_VALUE);
        build();
    }

    // ── Build ─────────────────────────────────────────────────────────────────────

    private void build() {
        getChildren().clear();

        VBox leftWrapper = new VBox(0);
        leftWrapper.setPrefWidth(420);
        leftWrapper.setMinWidth(380);
        leftWrapper.setMaxWidth(460);
        leftWrapper.setStyle("-fx-background-color: #16213e; -fx-border-color: #2a2a4a; -fx-border-width: 0 1 0 0;");

        HBox leftHeader = new HBox();
        leftHeader.setPadding(new Insets(14, 16, 14, 16));
        leftHeader.setStyle("-fx-background-color: #1a1a3a;");
        Label squadLbl = new Label("Squad");
        squadLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");
        Label weekLbl = new Label("Week " + state.getCurrentWeek());
        weekLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666688;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        leftHeader.getChildren().addAll(squadLbl, sp, weekLbl);

        playerListBox = new VBox(0);
        ScrollPane listScroll = new ScrollPane(playerListBox);
        listScroll.setFitToWidth(true);
        listScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        leftWrapper.getChildren().addAll(leftHeader, listScroll);

        rightPanel = new VBox(0);
        rightPanel.setMinWidth(420);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        rightPanel.setStyle("-fx-background-color: #12122a;");

        getChildren().addAll(leftWrapper, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        refreshPlayerList();
        showPlaceholder();
    }

    // ── Player list ───────────────────────────────────────────────────────────────

    private void refreshPlayerList() {
        playerListBox.getChildren().clear();
        for (Player p : userTeam.getSquad()) {
            playerListBox.getChildren().add(buildPlayerRow(p));
        }
    }

    private HBox buildPlayerRow(Player p) {
        boolean selected = p == selectedPlayer;
        String bg = selected ? "#222250" : "#16213e";

        HBox row = new HBox(8);
        row.setPadding(new Insets(8, 14, 8, 14));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + bg + "; -fx-cursor: hand;");

        Label posBadge = positionBadge(p.getPosition());

        Label nameLbl = new Label(p.getName());
        nameLbl.setStyle("-fx-text-fill: " + (selected ? "#ffffff" : "#ccccee")
                + "; -fx-font-size: 12px; -fx-font-weight: " + (selected ? "bold" : "normal") + ";");
        nameLbl.setPrefWidth(140);
        nameLbl.setMinWidth(100);

        int ovr = p.getOverallRating();
        Label ovrLbl = new Label(String.valueOf(ovr));
        ovrLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + ovrColor(ovr) + ";");
        ovrLbl.setMinWidth(28);

        Label fBadge = formBadge(p);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        PlayerTrainingPlan plan = state.getTrainingPlan(p);
        Label statusLbl;
        if (p.isInjured()) {
            statusLbl = new Label("Injured");
            statusLbl.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 10px; -fx-font-weight: bold;");
        } else if (plan != null) {
            statusLbl = p.getAge() >= 30
                    ? new Label("Maintenance")
                    : new Label(plan.getWeeksRemaining() + "w left");
            statusLbl.setStyle("-fx-text-fill: #00d2ff; -fx-font-size: 10px;");
        } else {
            statusLbl = new Label("Free");
            statusLbl.setStyle("-fx-text-fill: #555577; -fx-font-size: 10px;");
        }

        row.getChildren().addAll(posBadge, nameLbl, ovrLbl, fBadge, spacer, statusLbl);

        row.setOnMouseClicked(e -> selectPlayer(p));
        row.setOnMouseEntered(e -> { if (p != selectedPlayer) row.setStyle("-fx-background-color: #1e1e40; -fx-cursor: hand;"); });
        row.setOnMouseExited(e  -> { if (p != selectedPlayer) row.setStyle("-fx-background-color: " + bg + "; -fx-cursor: hand;"); });
        return row;
    }

    private void selectPlayer(Player p) {
        selectedPlayer = p;
        refreshPlayerList();
        showPlayerDetail(p);
    }

    // ── Placeholder ───────────────────────────────────────────────────────────────

    private void showPlaceholder() {
        rightPanel.getChildren().clear();
        VBox center = new VBox(10);
        center.setAlignment(Pos.CENTER);
        VBox.setVgrow(center, Priority.ALWAYS);

        Label icon  = new Label(sport instanceof com.sportsmanager.handball.HandballSport ? "🤾" : "⚽");
        icon.setStyle("-fx-font-size: 48px;");

        Label line1 = new Label("Select a player from the list");
        line1.setStyle("-fx-text-fill: #888899; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label line2 = new Label("to assign a training plan");
        line2.setStyle("-fx-text-fill: #555577; -fx-font-size: 12px;");

        center.getChildren().addAll(icon, line1, line2);
        rightPanel.getChildren().add(center);
    }

    // ── Player detail ─────────────────────────────────────────────────────────────

    private void showPlayerDetail(Player p) {
        rightPanel.getChildren().clear();

        VBox infoCard = buildInfoCard(p);

        Label optTitle = new Label("Training Options");
        optTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;"
                + " -fx-padding: 14 16 8 16;");

        VBox optionsList = new VBox(8);
        optionsList.setPadding(new Insets(0, 16, 16, 16));

        List<PositionalTrainingOption> options = trainingOptionsFor(p);
        PlayerTrainingPlan activePlan = state.getTrainingPlan(p);

        for (PositionalTrainingOption opt : options) {
            boolean isActive = activePlan != null
                    && activePlan.getOption().getId().equals(opt.getId());
            optionsList.getChildren().add(buildOptionCard(p, opt, activePlan, isActive));
        }

        VBox content = new VBox(0, infoCard, optTitle, optionsList);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        rightPanel.getChildren().add(scroll);
    }

    // ── Info card ─────────────────────────────────────────────────────────────────

    private VBox buildInfoCard(Player p) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("-fx-background-color: #1a1a3a; -fx-border-color: #2a2a4a; -fx-border-width: 0 0 1 0;");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label(p.getName());
        nameLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label posBadge = positionBadge(p.getPosition());

        Label ageLbl = new Label(p.getAge() + " yrs");
        ageLbl.setStyle("-fx-text-fill: #888899; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int ovr = p.getOverallRating();
        Label ovrLbl = new Label("OVR  " + ovr);
        ovrLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + ovrColor(ovr) + ";");

        top.getChildren().addAll(nameLbl, posBadge, ageLbl, spacer, ovrLbl);

        HBox statsRow = buildStatsRow(p);

        HBox bottomRow = new HBox(12);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        HBox formBox = new HBox(8);
        formBox.setAlignment(Pos.CENTER_LEFT);
        Label formTitleLbl = new Label("Form:");
        formTitleLbl.setStyle("-fx-text-fill: #888899; -fx-font-size: 12px;");
        formBox.getChildren().addAll(formTitleLbl, formBadge(p));

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        Label devLbl = new Label(developmentHint(p.getAge()));
        devLbl.setStyle("-fx-text-fill: #666688; -fx-font-size: 11px;");

        bottomRow.getChildren().addAll(formBox, sp2, devLbl);
        card.getChildren().addAll(top, statsRow, bottomRow);

        if (p.isInjured()) {
            Label injLbl = new Label("⛔  Injured — Cannot train  (" + p.getInjuryGamesRemaining() + " matches)");
            injLbl.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 12px; "
                    + "-fx-background-color: #2a1010; -fx-background-radius: 6; -fx-padding: 6 10;");
            injLbl.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().add(injLbl);
        }

        return card;
    }

    private HBox buildStatsRow(Player p) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        List<String> attrs = sport.getAttributeNamesForPosition(p.getPosition());
        for (String attr : attrs) {
            row.getChildren().add(statBox(statAbbr(attr), p.getAttributeValue(attr)));
        }
        return row;
    }

    private VBox statBox(String label, int value) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(6, 10, 6, 10));
        box.setStyle("-fx-background-color: #0f1030; -fx-background-radius: 6;"
                + " -fx-border-color: #1e1e40; -fx-border-radius: 6; -fx-border-width: 1;");
        box.setMinWidth(54);
        HBox.setHgrow(box, Priority.ALWAYS);

        Label valLbl = new Label(String.valueOf(value));
        valLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + statColor(value) + ";");

        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #666688;");

        box.getChildren().addAll(valLbl, nameLbl);
        return box;
    }

    // ── Training option card ──────────────────────────────────────────────────────

    private HBox buildOptionCard(Player p, PositionalTrainingOption opt,
                                  PlayerTrainingPlan activePlan, boolean isActive) {
        String cardBg     = isActive ? "#0d1f0d" : "#1a1a2e";
        String borderStyle = isActive
                ? "-fx-border-color: #27ae60; -fx-border-width: 1.5; -fx-border-radius: 8;"
                : "-fx-border-color: #2a2a4a; -fx-border-width: 1; -fx-border-radius: 8;";

        HBox card = new HBox(14);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; " + borderStyle);

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLbl = new Label(opt.getName());
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                + (isActive ? "#00e676" : "#ddddff") + ";");

        Label descLbl = new Label(opt.getDescription());
        descLbl.setStyle("-fx-text-fill: #666688; -fx-font-size: 10px;");
        descLbl.setWrapText(true);

        HBox attrRow = new HBox(5);
        attrRow.setAlignment(Pos.CENTER_LEFT);
        if (opt.isBalanced()) {
            attrRow.getChildren().add(attrBadge("ALL ATTRS", "#555577"));
        } else {
            for (String a : opt.getAttributes()) {
                attrRow.getChildren().add(attrBadge(statAbbr(a), attrColor(a)));
            }
        }

        String durationDisplay;
        if (p.getAge() >= 30) {
            durationDisplay = "Maintenance";
        } else {
            double fF = com.sportsmanager.training.TrainingEngine.formFactor(p.getForm());
            double aF = com.sportsmanager.training.TrainingEngine.ageFactor(p.getAge());
            int expectedWeeks = (int) Math.max(1, Math.round(
                    ((opt.getMinWeeks() + opt.getMaxWeeks()) / 2.0) * (1.0 / fF) * (1.0 / aF)));
            durationDisplay = expectedWeeks + " week" + (expectedWeeks == 1 ? "" : "s");
        }
        Label durationLbl = new Label("Duration: " + durationDisplay);
        durationLbl.setStyle("-fx-text-fill: #555577; -fx-font-size: 10px;");

        info.getChildren().addAll(nameLbl, descLbl, attrRow, durationLbl);

        VBox right = new VBox(5);
        right.setAlignment(Pos.CENTER_RIGHT);
        right.setMinWidth(120);

        if (isActive) {
            int total     = activePlan.getTotalWeeks();
            int remaining = activePlan.getWeeksRemaining();
            int done      = total - remaining;

            Label progressLbl;
            HBox bars = new HBox(2);
            bars.setAlignment(Pos.CENTER_RIGHT);

            if (p.getAge() >= 30) {
                progressLbl = new Label("Maintenance");
                progressLbl.setStyle("-fx-text-fill: #00d2ff; -fx-font-size: 11px; -fx-font-weight: bold;");
                Rectangle bar = new Rectangle(12, 6);
                bar.setArcWidth(3); bar.setArcHeight(3);
                bar.setFill(Color.web("#27ae60"));
                bars.getChildren().add(bar);
            } else {
                progressLbl = new Label(remaining + " week" + (remaining == 1 ? "" : "s") + " left");
                progressLbl.setStyle("-fx-text-fill: #00d2ff; -fx-font-size: 11px; -fx-font-weight: bold;");
                for (int i = 0; i < total; i++) {
                    Rectangle bar = new Rectangle(8, 6);
                    bar.setArcWidth(3); bar.setArcHeight(3);
                    bar.setFill(Color.web(i < done ? "#27ae60" : "#2a2a4a"));
                    bars.getChildren().add(bar);
                }
            }

            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("btn-secondary");
            cancelBtn.setStyle("-fx-font-size: 10px; -fx-padding: 3 8;");
            cancelBtn.setOnAction(e -> {
                state.cancelTraining(p);
                showPlayerDetail(p);
                refreshPlayerList();
            });
            right.getChildren().addAll(progressLbl, bars, cancelBtn);

        } else {
            Button assignBtn = new Button(activePlan != null ? "Switch" : "Assign");
            assignBtn.getStyleClass().add(activePlan != null ? "btn-secondary" : "btn-primary");
            assignBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 14;");
            assignBtn.setDisable(p.isInjured());
            assignBtn.setOnAction(e -> {
                state.assignTraining(p, new PlayerTrainingPlan(p, opt));
                showPlayerDetail(p);
                refreshPlayerList();
            });
            right.getChildren().add(assignBtn);
        }

        card.getChildren().addAll(info, right);

        if (!p.isInjured() && !isActive) {
            card.setOnMouseEntered(ev -> card.setStyle(
                    "-fx-background-color: #20203a; -fx-background-radius: 8; " + borderStyle));
            card.setOnMouseExited(ev -> card.setStyle(
                    "-fx-background-color: " + cardBg + "; -fx-background-radius: 8; " + borderStyle));
        }

        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private List<PositionalTrainingOption> trainingOptionsFor(Player p) {
        if (p instanceof FootballPlayer fp) return FootballTrainingOptions.getFor(fp.getPosition());
        if (p instanceof HandballPlayer hp) return HandballTrainingOptions.getFor(hp.getPosition());
        return List.of(FootballTrainingOptions.BALANCED);
    }

    private Label positionBadge(Position pos) {
        String abbr;
        String badgeClass;

        if (pos instanceof FootballPosition fp) {
            abbr = switch (fp) {
                case GOALKEEPER           -> "GK";
                case CENTRE_BACK          -> "CB";
                case LEFT_BACK            -> "LB";
                case RIGHT_BACK           -> "RB";
                case DEFENSIVE_MIDFIELDER -> "CDM";
                case CENTRAL_MIDFIELDER   -> "CM";
                case LEFT_MIDFIELDER      -> "LM";
                case RIGHT_MIDFIELDER     -> "RM";
                case ATTACKING_MIDFIELDER -> "CAM";
                case LEFT_WINGER          -> "LW";
                case RIGHT_WINGER         -> "RW";
                case STRIKER              -> "ST";
                case CENTRE_FORWARD       -> "CF";
            };
            badgeClass = switch (fp) {
                case GOALKEEPER -> "badge-gk";
                case CENTRE_BACK, LEFT_BACK, RIGHT_BACK -> "badge-def";
                case DEFENSIVE_MIDFIELDER, CENTRAL_MIDFIELDER,
                     LEFT_MIDFIELDER, RIGHT_MIDFIELDER -> "badge-mid";
                default -> "badge-fwd";
            };
        } else if (pos instanceof HandballPosition hp) {
            abbr = switch (hp) {
                case GOALKEEPER  -> "GK";
                case LEFT_WING   -> "LW";
                case LEFT_BACK   -> "LB";
                case CENTER_BACK -> "CB";
                case RIGHT_BACK  -> "RB";
                case RIGHT_WING  -> "RW";
                case PIVOT       -> "PIV";
            };
            badgeClass = switch (hp) {
                case GOALKEEPER  -> "badge-gk";
                case LEFT_BACK, RIGHT_BACK, CENTER_BACK -> "badge-mid";
                case LEFT_WING, RIGHT_WING -> "badge-fwd";
                case PIVOT -> "badge-def";
            };
        } else {
            abbr = "?"; badgeClass = "badge-def";
        }

        Label lbl = new Label(abbr);
        lbl.getStyleClass().addAll("badge", badgeClass);
        return lbl;
    }

    private Label formBadge(Player p) {
        double form = p.getForm();
        String text, color;
        if      (form >= 8.5) { text = "Excellent"; color = C_FORM_VG;  }
        else if (form >= 7.5) { text = "Good";      color = C_FORM_G;   }
        else if (form >= 6.5) { text = "Average";   color = C_FORM_MID; }
        else if (form >= 5.5) { text = "Poor";      color = C_FORM_BAD; }
        else                  { text = "Very Poor"; color = C_FORM_VB;  }
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;"
                + " -fx-background-color: " + color + ";"
                + " -fx-background-radius: 4; -fx-padding: 2 6;");
        return lbl;
    }

    private Label attrBadge(String text, String color) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: white;"
                + " -fx-background-color: " + color + ";"
                + " -fx-background-radius: 3; -fx-padding: 2 5;");
        return lbl;
    }

    private String statAbbr(String attr) {
        return switch (attr) {
            case "pace"        -> "PAC";
            case "shooting"    -> "SHO";
            case "passing"     -> "PAS";
            case "dribbling"   -> "DRI";
            case "defending"   -> "DEF";
            case "physical"    -> "PHY";
            case "diving"      -> "DIV";
            case "handling"    -> "HAN";
            case "kicking"     -> "KIC";
            case "reflexes"    -> "REF";
            case "positioning" -> "POS";
            case "speed"       -> "SPD";
            case "throwing"    -> "THR";
            case "jumping"     -> "JMP";
            case "agility"     -> "AGL";
            case "reach"       -> "RCH";
            default            -> attr.substring(0, Math.min(3, attr.length())).toUpperCase(java.util.Locale.ROOT);
        };
    }

    private String attrColor(String attr) {
        return switch (attr) {
            case "pace", "speed"       -> "#1a6b9a";
            case "shooting", "throwing"-> "#aa1515";
            case "passing", "agility"  -> "#5e1590";
            case "dribbling", "jumping"-> "#0f7a5c";
            case "defending"           -> "#1255a8";
            case "physical"            -> "#7a5200";
            case "diving"              -> "#1a6b9a";
            case "handling", "reach"   -> "#0f7a5c";
            case "kicking"             -> "#7a5200";
            case "reflexes"            -> "#aa1515";
            case "positioning"         -> "#5e1590";
            default                    -> "#444455";
        };
    }

    private String developmentHint(int age) {
        if (age < 24) return "Young — Fast development";
        if (age < 30) return "Prime — Normal development";
        return "Veteran — Maintenance mode";
    }

    private String ovrColor(int ovr) {
        if (ovr >= 80) return "#00e676";
        if (ovr >= 60) return "#00d2ff";
        if (ovr >= 40) return "#ffab40";
        return "#ff5252";
    }

    private String statColor(int val) {
        if (val >= 80) return "#00e676";
        if (val >= 65) return "#00d2ff";
        if (val >= 50) return "#ffab40";
        return "#ff5252";
    }

    public static String formLabel(double form) {
        if (form >= 8.5) return "Excellent";
        if (form >= 7.5) return "Good";
        if (form >= 6.5) return "Average";
        if (form >= 5.5) return "Poor";
        return "Very Poor";
    }

    public static String formColor(double form) {
        if (form >= 8.5) return C_FORM_VG;
        if (form >= 7.5) return C_FORM_G;
        if (form >= 6.5) return C_FORM_MID;
        if (form >= 5.5) return C_FORM_BAD;
        return C_FORM_VB;
    }
}
