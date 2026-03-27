package com.sportsmanager.ui;

import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Shared navigation sidebar shown after game starts.
 */
public class Sidebar extends VBox {

    private Button activeButton;

    public Sidebar() {
        getStyleClass().add("sidebar");
        setPrefWidth(220);
        setMinWidth(220);
        setMaxWidth(220);
        buildUI();
    }

    private void buildUI() {
        getChildren().clear();

        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("sidebar-header");

        Label logo = new Label("⚽");
        logo.getStyleClass().add("sidebar-logo");

        Label title = new Label("Sports Manager");
        title.getStyleClass().add("sidebar-title");

        SeasonState state = GameManager.getInstance().getState();
        if (state != null) {
            Label teamLabel = new Label(state.getUserTeam().getTeamName());
            teamLabel.getStyleClass().add("sidebar-subtitle");
            teamLabel.setWrapText(true);

            Label seasonLabel = new Label("Season " + state.getSeasonNumber()
                    + "  •  Week " + state.getCurrentWeek());
            seasonLabel.getStyleClass().add("sidebar-subtitle");

            header.getChildren().addAll(logo, title, teamLabel, seasonLabel);
        } else {
            header.getChildren().addAll(logo, title);
        }

        getChildren().add(header);

        // Separator
        Region sep = new Region();
        sep.getStyleClass().add("sidebar-separator");
        sep.setPrefWidth(Double.MAX_VALUE);
        getChildren().add(sep);

        // Nav buttons
        if (state != null) {
            addNavButton("🏠  Dashboard", () -> {
                ViewManager.getInstance().switchView(new DashboardView());
            });
            addNavButton("👥  Squad", () -> {
                ViewManager.getInstance().switchView(new SquadManagementView());
            });
            addNavButton("📅  Fixture", () -> {
                ViewManager.getInstance().switchView(new FixtureView());
            });
            addNavButton("🏆  League Table", () -> {
                ViewManager.getInstance().switchView(new StandingsView());
            });

            // Spacer
            Region spacer = new Region();
            VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            getChildren().add(spacer);

            // Season info
            Region sep2 = new Region();
            sep2.getStyleClass().add("sidebar-separator");
            sep2.setPrefWidth(Double.MAX_VALUE);
            getChildren().add(sep2);

            addNavButton("🔄  New Game", () -> {
                ViewManager.getInstance().switchView(new SportSelectionView());
            });
        }
    }

    private void addNavButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> {
            setActive(btn);
            action.run();
        });
        getChildren().add(btn);
    }

    public void setActive(Button btn) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-btn-active");
        }
        activeButton = btn;
        btn.getStyleClass().add("sidebar-btn-active");
    }

    public void refresh() {
        buildUI();
    }
}
