package com.sportsmanager.ui;

import com.sportsmanager.save.SaveGameManager;
import com.sportsmanager.save.SaveGameManager.SaveSummary;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lists every save file under the saves/ directory and lets the user
 * resume any one of them. Reached from SportSelectionView's "Load Game".
 */
public class LoadGameView extends VBox {

    private final VBox saveList;

    public LoadGameView() {
        setAlignment(Pos.TOP_CENTER);
        setSpacing(20);
        setPadding(new Insets(40));

        Label title = new Label("Load Game");
        title.getStyleClass().add("title-label");
        title.setStyle("-fx-font-size: 28px;");

        Label subtitle = new Label("Pick a saved game to continue");
        subtitle.getStyleClass().add("text-muted");
        subtitle.setStyle("-fx-font-size: 13px;");

        saveList = new VBox(10);
        saveList.setAlignment(Pos.TOP_CENTER);
        saveList.setMaxWidth(700);

        ScrollPane scroll = new ScrollPane(saveList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button back = new Button("← Back");
        back.getStyleClass().add("btn-secondary");
        back.setOnAction(e -> ViewManager.getInstance().switchView(new SportSelectionView()));

        getChildren().addAll(title, subtitle, scroll, back);
        refresh();
    }

    private void refresh() {
        saveList.getChildren().clear();

        List<SaveSummary> saves = SaveGameManager.getInstance().listSaves();
        if (saves.isEmpty()) {
            Label empty = new Label("No saved games yet.");
            empty.getStyleClass().add("text-muted");
            empty.setStyle("-fx-font-size: 14px; -fx-padding: 40 0 0 0;");
            saveList.getChildren().add(empty);
            return;
        }

        for (SaveSummary s : saves) {
            saveList.getChildren().add(buildSaveRow(s));
        }
    }

    private HBox buildSaveRow(SaveSummary s) {
        HBox row = new HBox(16);
        row.getStyleClass().add("card");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16));

        VBox info = new VBox(4);
        Label name = new Label(s.saveName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        String sportEmoji = s.sportName() != null && s.sportName().contains("handball") ? "🤾" : "⚽";
        String sportLabel = s.sportName() != null
                ? (s.sportName().substring(0, 1).toUpperCase() + s.sportName().substring(1))
                : "Football";
        Label meta = new Label(
                sportEmoji + " " + sportLabel + "  •  " + s.userTeamName()
                + "  •  Season " + s.seasonNumber() + "  •  Week " + s.currentWeek());
        meta.getStyleClass().add("text-muted");
        meta.setStyle("-fx-font-size: 12px;");

        Label timestamp = new Label("Saved " + formatSavedAt(s.savedAt()));
        timestamp.getStyleClass().add("text-muted");
        timestamp.setStyle("-fx-font-size: 11px;");

        info.getChildren().addAll(name, meta, timestamp);
        HBox.setHgrow(info, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button load = new Button("Load ▶");
        load.getStyleClass().add("btn-primary");
        load.setOnAction(e -> doLoad(s));

        Button delete = new Button("Delete");
        delete.getStyleClass().add("btn-secondary");
        delete.setOnAction(e -> confirmDelete(s));

        row.getChildren().addAll(info, spacer, load, delete);
        return row;
    }

    private String formatSavedAt(String iso) {
        if (iso == null) return "—";
        try {
            LocalDateTime dt = LocalDateTime.parse(iso);
            return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return iso;
        }
    }

    private void doLoad(SaveSummary s) {
        try {
            boolean ok = SaveGameManager.getInstance().load(s.fileBase());
            if (!ok) {
                showError("Load Failed", "Save file could not be read.");
                return;
            }
            transitionToDashboard();
        } catch (Exception ex) {
            showError("Load Failed", ex.getMessage() == null ? "Unknown error" : ex.getMessage());
        }
    }

    private void transitionToDashboard() {
        // Rebuild the root layout with sidebar + content area
        Sidebar sidebar = new Sidebar();
        ViewManager.getInstance().setSidebar(sidebar);

        StackPane contentArea = new StackPane();
        ViewManager.getInstance().setContentArea(contentArea);

        HBox root = (HBox) getScene().getRoot();
        root.getChildren().clear();
        root.getChildren().addAll(sidebar, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        ViewManager.getInstance().switchView(new DashboardView());
    }

    private void confirmDelete(SaveSummary s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Save");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete \"" + s.saveName() + "\"? This cannot be undone.");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                SaveGameManager.getInstance().delete(s.fileBase());
                refresh();
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
