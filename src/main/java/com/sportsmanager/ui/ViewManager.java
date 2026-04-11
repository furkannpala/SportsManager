package com.sportsmanager.ui;

import com.sportsmanager.game.SeasonState;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Singleton that manages view switching in the main content area.
 */
public class ViewManager {

    private static ViewManager instance;

    private StackPane contentArea;
    private Sidebar sidebar;

    private ViewManager() {}

    public static ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void setSidebar(Sidebar sidebar) {
        this.sidebar = sidebar;
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

    public void switchView(Node view) {
        if (contentArea != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    public SeasonState getSeasonState() {
        return com.sportsmanager.game.GameManager.getInstance().getState();
    }

    static void resetForTesting() { instance = null; }
}
