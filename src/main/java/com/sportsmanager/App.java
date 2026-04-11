package com.sportsmanager;

import com.sportsmanager.ui.SportSelectionView;
import com.sportsmanager.ui.ViewManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * JavaFX application entry point.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Root layout: sidebar (added later) + content area
        HBox root = new HBox();
        root.setStyle("-fx-background-color: #0f0f1a;");

        StackPane contentArea = new StackPane();
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        root.getChildren().add(contentArea);

        // Register content area with ViewManager
        ViewManager.getInstance().setContentArea(contentArea);

        // Show initial view — sport selection (no sidebar yet)
        contentArea.getChildren().add(new SportSelectionView());

        // Scene
        Scene scene = new Scene(root, 1200, 780);
        scene.getStylesheets().add(
                getClass().getResource("/com/sportsmanager/styles.css").toExternalForm()
        );

        stage.setTitle("Sports Manager");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
