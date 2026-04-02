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

    /** teamId → assigned Image */
    private final Map<String, Image> logos = new HashMap<>();

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
            if (img != null) available.add(img);
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
        if (available.isEmpty()) return; // no logos loaded — all teams use fallback

        List<Image> pool = new ArrayList<>(available);
        Collections.shuffle(pool);

        for (int i = 0; i < teams.size(); i++) {
            logos.put(teams.get(i).getTeamId(), pool.get(i % pool.size()));
        }
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
