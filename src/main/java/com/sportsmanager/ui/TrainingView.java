package com.sportsmanager.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Training screen — placeholder for a future milestone.
 */
public class TrainingView extends VBox {

    public TrainingView() {
        setAlignment(Pos.CENTER);
        setSpacing(16);
        setPadding(new Insets(60));

        Label icon = new Label("💪");
        icon.setStyle("-fx-font-size: 64px;");

        Label title = new Label("Training");
        title.getStyleClass().add("title-label");

        Label message = new Label("Training will be available in the next version.");
        message.getStyleClass().add("text-muted");
        message.setStyle("-fx-font-size: 16px;");

        getChildren().addAll(icon, title, message);
    }
}
