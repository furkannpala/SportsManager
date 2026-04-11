package com.sportsmanager.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Team {

    private final String teamId;
    private final String teamName;
    private final List<Player> squad;

    private Formation formation;
    private Tactic tactic;

    public Team(String teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.squad = new ArrayList<>();
    }

    // Squad management

    public void addPlayer(Player player) {
        squad.add(player);
    }

    public void removePlayer(Player player) {
        squad.remove(player);
    }


    public List<Player> getSquad() {
        return Collections.unmodifiableList(squad);
    }


    public void swapPlayers(Player a, Player b) {
        int idxA = squad.indexOf(a);
        int idxB = squad.indexOf(b);
        if (idxA >= 0 && idxB >= 0) {
            squad.set(idxA, b);
            squad.set(idxB, a);
        }
    }

    public List<Player> getAvailablePlayers() {
        List<Player> available = new ArrayList<>();
        for (Player p : squad) {
            if (p.isAvailable()) {
                available.add(p);
            }
        }
        return available;
    }

    // Getters & setters

    public String getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public Formation getFormation() {
        return formation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    public Tactic getTactic() {
        return tactic;
    }

    public void setTactic(Tactic tactic) {
        this.tactic = tactic;
    }

    @Override
    public String toString() {
        return teamName;
    }
}
