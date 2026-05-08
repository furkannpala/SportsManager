package com.sportsmanager.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class UserManualView extends StackPane {

    public UserManualView() {
        VBox main = new VBox(20);
        main.setPadding(new Insets(24));

        Label welcomeLabel = new Label("User Manual");
        welcomeLabel.getStyleClass().add("title-label");

        Label introLabel = new Label("Welcome to Sports Manager! Here is a guide on how to play the game.");
        introLabel.getStyleClass().add("text-muted");
        introLabel.setWrapText(true);

        VBox content = new VBox(16);
        
        content.getChildren().addAll(
            createSection("🏠 Dashboard", "Your main hub. Here you can see your league position, next match, form, and recent results at a glance."),
            createSection("👥 Squad", "Manage your team here. You can set up your starting lineup, formation, and tactics before a match. Drag and drop players to swap them."),
            createSection("📅 Fixture", "Check the schedule for the current season. You can see past results and upcoming matches for all teams."),
            createSection("🏆 League Table", "View the current standings of the league. See who is leading and who is struggling at the bottom."),
            createSection("💪 Training", "Improve your players. Assign different training drills to boost specific attributes like Attack, Defense, or Physical condition."),
            createSection("⚽ Match Engine", "When you play a match, you will see a simulation. Watch player stamina, make substitutions when needed, and adjust tactics on the fly."),
            createSection("💾 Save / Load", "Don't forget to save your progress! You can save your game from the sidebar and load it from the main menu.")
        );

        ScrollPane scroll = new ScrollPane();
        VBox scrollContent = new VBox(20);
        scrollContent.setPadding(new Insets(0));
        scrollContent.getChildren().addAll(welcomeLabel, introLabel, content);
        scroll.setContent(scrollContent);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        main.getChildren().add(scroll);
        getChildren().add(main);
    }

    private VBox createSection(String title, String text) {
        VBox section = new VBox(8);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(16));

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label textLbl = new Label(text);
        textLbl.getStyleClass().add("text-normal");
        textLbl.setWrapText(true);

        section.getChildren().addAll(titleLbl, textLbl);
        return section;
    }
}
