package com.sportsmanager.ui;

import com.sportsmanager.core.Sport;
import com.sportsmanager.football.FootballSport;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Screen 1 — Sport selection (currently football only).
 */
public class SportSelectionView extends VBox {

    private Sport selectedSport;
    private VBox selectedCard;

    public SportSelectionView() {
        setAlignment(Pos.CENTER);
        setSpacing(40);
        setPadding(new Insets(60));

        // Title
        Label title = new Label("⚽ Sports Manager");
        title.getStyleClass().add("title-label");
        title.setStyle("-fx-font-size: 36px;");

        Label subtitle = new Label("Choose your sport to begin");
        subtitle.getStyleClass().add("text-muted");
        subtitle.setStyle("-fx-font-size: 14px;");

        // Sport cards
        HBox sportCards = new HBox(20);
        sportCards.setAlignment(Pos.CENTER);

        List<SportOption> options = List.of(
            new SportOption(new FootballSport(), "⚽", "The beautiful game. Manage a 20-team league,\nbuild your squad, and compete for glory.")
        );

        for (SportOption opt : options) {
            VBox card = createSportCard(opt);
            sportCards.getChildren().add(card);
        }

        // Buttons
        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);

        Button newGame = new Button("New Game");
        newGame.getStyleClass().add("btn-primary");
        newGame.setOnAction(e -> {
            if (selectedSport != null) {
                ViewManager.getInstance().switchView(new TeamAssignmentView(selectedSport));
            }
        });

        Button loadGame = new Button("Load Game");
        loadGame.getStyleClass().add("btn-secondary");
        loadGame.setDisable(true);

        buttons.getChildren().addAll(newGame, loadGame);

        getChildren().addAll(title, subtitle, sportCards, buttons);

        // Auto-select first
        if (!options.isEmpty()) {
            selectedSport = options.get(0).sport;
        }
    }

    private VBox createSportCard(SportOption opt) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("card", "card-selected");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setPrefWidth(280);
        card.setPrefHeight(220);

        Label icon = new Label(opt.icon);
        icon.setStyle("-fx-font-size: 48px;");

        Label name = new Label(opt.sport.getSportName());
        name.getStyleClass().add("subtitle-label");

        Label desc = new Label(opt.description);
        desc.getStyleClass().add("text-muted");
        desc.setWrapText(true);
        desc.setStyle("-fx-text-alignment: center;");

        card.getChildren().addAll(icon, name, desc);

        selectedCard = card;

        card.setOnMouseClicked(e -> {
            if (selectedCard != null) {
                selectedCard.getStyleClass().remove("card-selected");
            }
            card.getStyleClass().add("card-selected");
            selectedCard = card;
            selectedSport = opt.sport;
        });

        return card;
    }

    private record SportOption(Sport sport, String icon, String description) {}
}
