package com.sportsmanager.ui;

import com.sportsmanager.game.GameManager;
import com.sportsmanager.game.SeasonState;
import com.sportsmanager.save.SaveGameManager;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        SeasonState state = GameManager.getInstance().getState();

        String sportEmoji = "⚽";
        if (state != null) {
            String sportName = state.getCurrentSport().getSportName().toLowerCase();
            if (sportName.contains("handball")) sportEmoji = "🤾";
        }
        Label logo = new Label(sportEmoji);
        logo.getStyleClass().add("sidebar-logo");

        Label title = new Label("Sports Manager");
        title.getStyleClass().add("sidebar-title");
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
            addNavButton("💪  Training", () -> {
                ViewManager.getInstance().switchView(new TrainingView());
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

            addNavButton("💾  Save Game", this::promptSave);

            addNavButton("🏠  Main Menu", this::promptMainMenu);
        }
    }

    private void promptMainMenu() {
        StackPane contentArea = ViewManager.getInstance().getContentArea();
        if (contentArea == null) return;

        // ── Dim backdrop ──────────────────────────────────────────────
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(5,5,20,0.78);");

        // ── Modal card ────────────────────────────────────────────────
        VBox card = new VBox(20);
        card.setAlignment(Pos.TOP_LEFT);
        card.setMaxWidth(400);
        card.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        StackPane.setAlignment(card, Pos.CENTER);
        card.setStyle(
                "-fx-background-color: #12122a;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #e94560;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 12;" +
                "-fx-padding: 28;"
        );

        Label icon = new Label("🏠");
        icon.setStyle("-fx-font-size: 28px;");

        Label title = new Label("Return to Main Menu?");
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #e0e0ff;");

        Label subtitle = new Label("Do you want to save your progress before leaving?");
        subtitle.setStyle("-fx-text-fill: #8888aa; -fx-font-size: 12px;");
        subtitle.setWrapText(true);

        VBox btnGroup = new VBox(8);
        btnGroup.setFillWidth(true);

        Button saveBtn = new Button("💾  Save & Return to Main Menu");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        Button noSaveBtn = new Button("🚪  Exit without Saving");
        noSaveBtn.getStyleClass().add("btn-secondary");
        noSaveBtn.setMaxWidth(Double.MAX_VALUE);

        Button cancelBtn = new Button("← Stay in Game");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);

        cancelBtn.setOnAction(e -> contentArea.getChildren().remove(backdrop));

        saveBtn.setOnAction(e -> {
            contentArea.getChildren().remove(backdrop);
            promptSave();
            goToMainMenu();
        });

        noSaveBtn.setOnAction(e -> {
            contentArea.getChildren().remove(backdrop);
            goToMainMenu();
        });

        btnGroup.getChildren().addAll(saveBtn, noSaveBtn, cancelBtn);
        card.getChildren().addAll(icon, title, subtitle, btnGroup);

        backdrop.getChildren().add(card);
        contentArea.getChildren().add(backdrop);
    }

    private void goToMainMenu() {
        HBox root = (HBox) getScene().getRoot();

        StackPane contentArea = new StackPane();
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        ViewManager.getInstance().setContentArea(contentArea);
        ViewManager.getInstance().setSidebar(null);

        root.getChildren().setAll(contentArea);
        contentArea.getChildren().add(new SportSelectionView());
    }

    private void promptSave() {
        SeasonState state = GameManager.getInstance().getState();
        if (state == null) return;

        String defaultName = state.getCurrentSport().getSportName()
                + " - " + state.getUserTeam().getTeamName()
                + " - S" + state.getSeasonNumber()
                + "W" + state.getCurrentWeek()
                + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"));

        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Enter a name for your save");
        dialog.setContentText("Save name:");

        dialog.showAndWait().ifPresent(name -> {
            if (name.isBlank()) return;
            try {
                SaveGameManager.getInstance().save(name.trim());
                showInfo("Game saved as \"" + name.trim() + "\".");
            } catch (Exception ex) {
                showError(ex.getMessage() == null ? "Unknown error" : ex.getMessage());
            }
        });
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Successful");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Save Failed");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
