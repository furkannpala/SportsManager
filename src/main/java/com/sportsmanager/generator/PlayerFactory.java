package com.sportsmanager.generator;

import com.sportsmanager.core.Player;
import com.sportsmanager.core.Position;

public interface PlayerFactory {


    Player createPlayer(String name, int age, Position position, int tierMean);
}
