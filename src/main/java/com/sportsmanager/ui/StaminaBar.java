package com.sportsmanager.ui;

import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.core.Player;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Thin horizontal stamina bar.
 *
 * Colour thresholds:
 *   ≥ 75 %  → dark green  (full)
 *   ≥ 50 %  → green       (good)
 *   ≥ 30 %  → yellow      (medium)
 *   ≥ 15 %  → red         (bad)
 *   < 15 %  → dark red    (critical)
 */
public final class StaminaBar {

    private static final String C_DARK_GREEN = "#1a7a1a";
    private static final String C_GREEN      = "#27ae60";
    private static final String C_YELLOW     = "#e8b200";
    private static final String C_RED        = "#c0392b";
    private static final String C_DARK_RED   = "#6b0000";
    private static final String C_BG         = "#2a2a3e";

    private StaminaBar() {}

    /**
     * Creates a rounded stamina bar for the given player.
     *
     * @param player  any Player; non-FootballPlayer players show a full bar
     * @param width   total width in pixels
     * @param height  height in pixels (also used as corner radius)
     */
    public static StackPane create(Player player, double width, double height) {
        int stamina = (player instanceof FootballPlayer fp) ? fp.getCurrentStamina() : 100;
        return build(stamina, width, height);
    }

    /** Convenience overload when the stamina value is known directly. */
    public static StackPane create(int stamina, double width, double height) {
        return build(stamina, width, height);
    }

    private static StackPane build(int stamina, double width, double height) {
        double pct    = Math.max(0, Math.min(100, stamina)) / 100.0;
        double radius = height / 2.0;

        Rectangle bg = new Rectangle(width, height);
        bg.setFill(Color.web(C_BG));
        bg.setArcWidth(radius * 2);
        bg.setArcHeight(radius * 2);

        double fillWidth = Math.max(radius * 2, width * pct); // keep rounded ends visible
        Rectangle fill = new Rectangle(fillWidth, height);
        fill.setFill(Color.web(colorFor(stamina)));
        fill.setArcWidth(radius * 2);
        fill.setArcHeight(radius * 2);

        StackPane bar = new StackPane(bg, fill);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setMaxWidth(width);
        bar.setMinWidth(width);
        return bar;
    }

    private static String colorFor(int stamina) {
        if (stamina >= 75) return C_DARK_GREEN;
        if (stamina >= 50) return C_GREEN;
        if (stamina >= 30) return C_YELLOW;
        if (stamina >= 15) return C_RED;
        return C_DARK_RED;
    }
}
