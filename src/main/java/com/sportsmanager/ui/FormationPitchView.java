package com.sportsmanager.ui;

import com.sportsmanager.core.Formation;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Position;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Top-down pitch view. Two modes:
 *  - redraw(formation)               → position abbreviations only (LW, ST, …)
 *  - redrawWithPlayers(…)            → EA FC style: OVR inside circle + surname below,
 *                                      optional click / DnD callbacks per node.
 */
public class FormationPitchView extends Pane {

    static final double W = 260.0;
    static final double H = 360.0;
    static final double R = 16.0;   // circle radius

    private static final Color C_GK  = Color.web("#c8960a");
    private static final Color C_DEF = Color.web("#1255a8");
    private static final Color C_MID = Color.web("#5e1590");
    private static final Color C_ATT = Color.web("#aa1515");

    public FormationPitchView(Formation formation) {
        setPrefSize(W, H);
        setMaxSize(W, H);
        setMinSize(W, H);
        redraw(formation);
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public void redraw(Formation formation) {
        getChildren().clear();
        drawPitch();
        if (formation != null) drawFormation(formation);
    }

    /** Players paired by index with formation slots (slot 0 = GK = player 0). */
    public void redrawWithPlayers(Formation formation, List<Player> players,
                                   Player selectedOut,
                                   Consumer<Player> onPlayerClick) {
        redrawWithPlayers(formation, players, selectedOut, onPlayerClick, null);
    }

    /**
     * Same as above plus a nodeDecorator called on every player node after creation —
     * use it to attach DnD handlers in the caller.
     */
    public void redrawWithPlayers(Formation formation, List<Player> players,
                                   Player selectedOut,
                                   Consumer<Player> onPlayerClick,
                                   BiConsumer<Node, Player> nodeDecorator) {
        getChildren().clear();
        drawPitch();
        if (formation != null)
            drawFormationWithPlayers(formation, players, selectedOut, onPlayerClick, nodeDecorator);
    }

    // ── Pitch background ────────────────────────────────────────────────────────

    private void drawPitch() {
        int stripes = 8;
        double sh = H / stripes;
        for (int i = 0; i < stripes; i++) {
            Rectangle s = new Rectangle(0, i * sh, W, sh);
            s.setFill(Color.web(i % 2 == 0 ? "#1d701d" : "#1a641a"));
            getChildren().add(s);
        }
        addShape(rect(8, 8, W - 16, H - 16));
        addShape(line(8, H / 2, W - 8, H / 2));
        addShape(ellipse(W / 2, H / 2, 34, 22));
        addShape(rect(W / 2 - 54, 8, 108, 48));
        addShape(rect(W / 2 - 54, H - 56, 108, 48));
    }

    private Rectangle rect(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(Color.TRANSPARENT);
        r.setStroke(Color.web("#ffffff70"));
        r.setStrokeWidth(1.2);
        return r;
    }

    private Line line(double x1, double y1, double x2, double y2) {
        Line l = new Line(x1, y1, x2, y2);
        l.setStroke(Color.web("#ffffff70"));
        l.setStrokeWidth(1.2);
        return l;
    }

    private Ellipse ellipse(double cx, double cy, double rx, double ry) {
        Ellipse e = new Ellipse(cx, cy, rx, ry);
        e.setFill(Color.TRANSPARENT);
        e.setStroke(Color.web("#ffffff70"));
        e.setStrokeWidth(1.2);
        return e;
    }

    private void addShape(javafx.scene.shape.Shape s) { getChildren().add(s); }

    // ── Position-only formation (no player data) ────────────────────────────────

    private void drawFormation(Formation formation) {
        List<Position> slots = formation.getPositionSlots();
        int[] counts = parseLayerCounts(formation.getFormationName());
        double[] yPos = computeYPositions(counts.length);
        int idx = 0;

        placePositionNode((FootballPosition) slots.get(idx++), W / 2, yPos[0]);

        for (int layer = 0; layer < counts.length; layer++) {
            List<FootballPosition> row = new ArrayList<>();
            for (int i = 0; i < counts[layer] && idx < slots.size(); i++)
                row.add((FootballPosition) slots.get(idx++));
            row.sort(Comparator.comparingInt(this::xSortKey));
            double y = yPos[layer + 1];
            for (int i = 0; i < row.size(); i++)
                placePositionNode(row.get(i), xPos(i, row.size()), y);
        }
    }

    private void placePositionNode(FootballPosition pos, double x, double y) {
        String text = abbr(pos);
        Circle bg = new Circle(R, nodeColor(pos));
        bg.setStroke(Color.WHITE);
        bg.setStrokeWidth(1.5);
        Text lbl = new Text(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, text.length() >= 3 ? 8.5 : 10.5));
        lbl.setFill(Color.WHITE);
        lbl.setTextAlignment(TextAlignment.CENTER);
        StackPane node = new StackPane(bg, lbl);
        node.setAlignment(Pos.CENTER);
        node.setPrefSize(R * 2, R * 2);
        node.setLayoutX(x - R);
        node.setLayoutY(y - R);
        getChildren().add(node);
    }

    // ── Player-aware formation (EA FC style) ────────────────────────────────────

    private void drawFormationWithPlayers(Formation formation, List<Player> players,
                                           Player selectedOut,
                                           Consumer<Player> onPlayerClick,
                                           BiConsumer<Node, Player> decorator) {
        List<Position> slots = formation.getPositionSlots();
        int[] counts = parseLayerCounts(formation.getFormationName());
        double[] yPos = computeYPositions(counts.length);
        int idx = 0;

        // GK
        FootballPosition gkPos = (FootballPosition) slots.get(idx);
        Player gkPlayer = idx < players.size() ? players.get(idx) : null;
        placePlayerNode(gkPos, gkPlayer, W / 2, yPos[0],
                gkPlayer != null && gkPlayer == selectedOut, onPlayerClick, decorator);
        idx++;

        for (int layer = 0; layer < counts.length; layer++) {
            List<FootballPosition> rowPos = new ArrayList<>();
            List<Player> rowPlayers = new ArrayList<>();
            for (int i = 0; i < counts[layer] && idx < slots.size(); i++) {
                rowPos.add((FootballPosition) slots.get(idx));
                rowPlayers.add(idx < players.size() ? players.get(idx) : null);
                idx++;
            }
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < rowPos.size(); i++) order.add(i);
            order.sort(Comparator.comparingInt(i -> xSortKey(rowPos.get(i))));

            double y = yPos[layer + 1];
            for (int vi = 0; vi < order.size(); vi++) {
                int orig = order.get(vi);
                Player p = rowPlayers.get(orig);
                placePlayerNode(rowPos.get(orig), p, xPos(vi, order.size()), y,
                        p != null && p == selectedOut, onPlayerClick, decorator);
            }
        }
    }

    /**
     * EA FC-style node: coloured circle with OVR (or pos abbr) inside,
     * player surname below the circle.
     */
    private void placePlayerNode(FootballPosition pos, Player player,
                                  double x, double y,
                                  boolean isSelected,
                                  Consumer<Player> onPlayerClick,
                                  BiConsumer<Node, Player> decorator) {
        Color borderColor = isSelected ? Color.web("#e94560") : Color.WHITE;
        double borderWidth = isSelected ? 3.0 : 1.5;
        Color bgColor = isSelected ? nodeColor(pos).brighter() : nodeColor(pos);

        Circle bg = new Circle(R, bgColor);
        bg.setStroke(borderColor);
        bg.setStrokeWidth(borderWidth);

        // Inside circle: OVR rating when player is known, else position abbr
        String innerText = player != null ? String.valueOf(player.getOverallRating()) : abbr(pos);
        Text innerLbl = new Text(innerText);
        innerLbl.setFont(Font.font("System", FontWeight.BOLD,
                innerText.length() >= 3 ? 8.0 : 10.0));
        innerLbl.setFill(Color.WHITE);
        innerLbl.setTextAlignment(TextAlignment.CENTER);

        StackPane circle = new StackPane(bg, innerLbl);
        circle.setAlignment(Pos.CENTER);
        circle.setPrefSize(R * 2, R * 2);
        circle.setMinSize(R * 2, R * 2);
        circle.setMaxSize(R * 2, R * 2);

        Node node;
        if (player != null) {
            Text nameLbl = new Text(surname(player));
            nameLbl.setFont(Font.font("System", FontWeight.BOLD, 7.5));
            nameLbl.setFill(isSelected ? Color.web("#ffaaaa") : Color.WHITE);
            nameLbl.setTextAlignment(TextAlignment.CENTER);

            // Stamina bar — shows live drain for field players
            StackPane staminaBar = StaminaBar.create(player, R * 2, 3);

            VBox vbox = new VBox(1, circle, nameLbl, staminaBar);
            vbox.setAlignment(Pos.CENTER);
            vbox.setLayoutX(x - R);
            vbox.setLayoutY(y - R);
            if (onPlayerClick != null) {
                vbox.setStyle("-fx-cursor: hand;");
                final Player fp = player;
                vbox.setOnMouseClicked(e -> onPlayerClick.accept(fp));
            }
            if (decorator != null) decorator.accept(vbox, player);
            node = vbox;
        } else {
            circle.setLayoutX(x - R);
            circle.setLayoutY(y - R);
            node = circle;
        }
        getChildren().add(node);
    }

    private String surname(Player player) {
        String name = player.getName();
        String[] parts = name.split("\\s+");
        String last = parts[parts.length - 1];
        return last.length() > 7 ? last.substring(0, 7) : last;
    }

    // ── Y positions ─────────────────────────────────────────────────────────────

    private double[] computeYPositions(int numNonGkLayers) {
        double yGk  = H - 32;   // GK near bottom (leaves room for name label below)
        double yDef = H - 88;   // defender band
        double yAtt = 30;       // attacker band near top
        double[] y  = new double[numNonGkLayers + 1];
        y[0] = yGk;
        if (numNonGkLayers <= 1) {
            y[1] = (yDef + yAtt) / 2.0;
        } else {
            double range = yDef - yAtt;
            for (int i = 0; i < numNonGkLayers; i++) {
                y[i + 1] = yDef - range * i / (numNonGkLayers - 1);
            }
        }
        return y;
    }

    // ── X positions ─────────────────────────────────────────────────────────────

    private double xPos(int index, int total) {
        if (total == 1) return W / 2.0;
        double margin = Math.max(20.0, W / (total * 2.0 + 1));
        double usable = W - 2 * margin;
        return margin + usable * index / (total - 1);
    }

    private int xSortKey(FootballPosition pos) {
        return switch (pos) {
            case LEFT_BACK, LEFT_WINGER  -> 0;
            case LEFT_MIDFIELDER         -> 2;
            case CENTRE_BACK, CENTRAL_MIDFIELDER, DEFENSIVE_MIDFIELDER,
                 ATTACKING_MIDFIELDER, GOALKEEPER, STRIKER, CENTRE_FORWARD -> 5;
            case RIGHT_MIDFIELDER        -> 8;
            case RIGHT_BACK, RIGHT_WINGER -> 10;
        };
    }

    // ── Formation name parsing ───────────────────────────────────────────────────

    private int[] parseLayerCounts(String formationName) {
        String numeric = formationName
                .replaceAll("[^0-9-]", "")
                .replaceAll("-+$", "");
        if (numeric.isEmpty()) return new int[]{4, 3, 3};
        String[] parts = numeric.split("-");
        int[] counts = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try { counts[i] = Integer.parseInt(parts[i]); }
            catch (NumberFormatException e) { counts[i] = 0; }
        }
        return counts;
    }

    // ── Labels & colours ─────────────────────────────────────────────────────────

    private String abbr(FootballPosition pos) {
        return switch (pos) {
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
    }

    private Color nodeColor(FootballPosition pos) {
        if (pos == FootballPosition.GOALKEEPER) return C_GK;
        if (pos.isDefensive())                  return C_DEF;
        if (pos.isMidfield())                   return C_MID;
        return C_ATT;
    }
}
