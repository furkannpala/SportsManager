package com.sportsmanager.ui;

import com.sportsmanager.core.Team;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assigns and caches a unique logo (Image) for each team.
 *
 * All available logos are loaded eagerly on first getInstance() call.
 * Only successfully loaded images are used for assignment — this prevents
 * random fallback-to-initial issues caused by intermittent load failures.
 *
 * Call {@link #assign(List)} once after league generation.
 * Then retrieve logos anywhere with {@link #createLogoNode(Team, double)}.
 */
public class LogoManager {

    private static final int    LOGO_COUNT = 45;
    private static final String BASE       = "/com/sportsmanager/assets/logo_%d.png";

    private static LogoManager instance;

    /** All successfully pre-loaded images. */
    private final List<Image> available = new ArrayList<>();

    /** Parallel list: file index (1..LOGO_COUNT) for each entry in {@link #available}. */
    private final List<Integer> availableIndices = new ArrayList<>();

    /** teamId → assigned Image */
    private final Map<String, Image> logos = new HashMap<>();

    /** teamId → file index of the assigned logo (so saves can persist exact logo). */
    private final Map<String, Integer> logoFileIndices = new HashMap<>();

    private LogoManager() {
        preloadAll();
    }

    public static LogoManager getInstance() {
        if (instance == null) instance = new LogoManager();
        return instance;
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    /** Loads all logo files once; stores only the ones that succeed. */
    private void preloadAll() {
        for (int i = 1; i <= LOGO_COUNT; i++) {
            Image img = loadImage(String.format(BASE, i));
            if (img != null) {
                available.add(img);
                availableIndices.add(i);
            }
        }
    }

    private Image loadImage(String path) {
        try {
            var stream = LogoManager.class.getResourceAsStream(path);
            if (stream == null) return null;
            Image img = new Image(stream);
            return img.isError() ? null : img;
        } catch (Exception e) {
            return null;
        }
    }

    // ── Assignment ────────────────────────────────────────────────────────────

    /**
     * Shuffles the available logo pool and assigns one unique logo per team.
     * If there are more teams than logos, logos are reused (cycled).
     * Safe to call multiple times — clears previous assignments.
     */
    public void assign(List<Team> teams) {
        logos.clear();
        logoFileIndices.clear();
        if (available.isEmpty()) return; // no logos loaded — all teams use fallback

        // Shuffle a list of indices into available[] so we can record both image and file index.
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < available.size(); i++) order.add(i);
        Collections.shuffle(order);

        for (int i = 0; i < teams.size(); i++) {
            int slot = order.get(i % order.size());
            logos.put(teams.get(i).getTeamId(), available.get(slot));
            logoFileIndices.put(teams.get(i).getTeamId(), availableIndices.get(slot));
        }
    }

    /**
     * Restores logo assignments from a previously saved game.
     * Each map entry is (teamId → original file index from BASE template).
     * Teams whose file index is no longer available silently fall back to the letter logo.
     */
    public void restoreAssignments(Map<String, Integer> savedAssignments) {
        logos.clear();
        logoFileIndices.clear();
        if (savedAssignments == null) return;

        for (Map.Entry<String, Integer> e : savedAssignments.entrySet()) {
            int fileIdx = e.getValue();
            int slot = availableIndices.indexOf(fileIdx);
            if (slot >= 0) {
                logos.put(e.getKey(), available.get(slot));
                logoFileIndices.put(e.getKey(), fileIdx);
            }
        }
    }

    /** Snapshot of current teamId → logo file index assignments (for save files). */
    public Map<String, Integer> getAssignments() {
        return new HashMap<>(logoFileIndices);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** Returns the assigned logo for a team, or null if not assigned. */
    public Image getLogo(Team team) {
        return logos.get(team.getTeamId());
    }

    /**
     * Creates a logo Node for the given team at the given size.
     * Uses ImageView if a logo is assigned, otherwise falls back to a letter label.
     */
    public Node createLogoNode(Team team, double size) {
        Image img = getLogo(team);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setPreserveRatio(true);
            return iv;
        }
        // Fallback: first letter of team name
        Label lbl = new Label(team.getTeamName().substring(0, 1).toUpperCase());
        lbl.setStyle(
                "-fx-font-size: " + (size * 0.5) + "px; -fx-font-weight: bold;"
                + " -fx-text-fill: #00d2ff; -fx-background-color: #0f3460;"
                + " -fx-background-radius: 50; -fx-alignment: center;"
                + " -fx-min-width: " + size + "; -fx-min-height: " + size + ";"
                + " -fx-max-width: " + size + "; -fx-max-height: " + size + ";");
        return lbl;
    }

    /** Resets all assignments for a new game (keeps preloaded images). */
    public void reset() {
        logos.clear();
    }
}
