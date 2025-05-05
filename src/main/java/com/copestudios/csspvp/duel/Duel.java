package com.copestudios.csspvp.duel;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Duel {

    private final UUID player1;
    private final UUID player2;
    private final DuelZone zone;
    private final Location player1PrevLocation;
    private final Location player2PrevLocation;
    private DuelState state;

    public Duel(Player player1, Player player2, DuelZone zone) {
        this.player1 = player1.getUniqueId();
        this.player2 = player2.getUniqueId();
        this.zone = zone;
        this.player1PrevLocation = player1.getLocation();
        this.player2PrevLocation = player2.getLocation();
        this.state = DuelState.WAITING;
    }

    public UUID getPlayer1() {
        return player1;
    }

    public UUID getPlayer2() {
        return player2;
    }

    public DuelZone getZone() {
        return zone;
    }

    public Location getPlayer1PrevLocation() {
        return player1PrevLocation.clone();
    }

    public Location getPlayer2PrevLocation() {
        return player2PrevLocation.clone();
    }

    public DuelState getState() {
        return state;
    }

    public void setState(DuelState state) {
        this.state = state;
    }

    public boolean isPlayerInDuel(UUID playerId) {
        return player1.equals(playerId) || player2.equals(playerId);
    }

    public UUID getOpponent(UUID playerId) {
        if (player1.equals(playerId)) {
            return player2;
        } else if (player2.equals(playerId)) {
            return player1;
        }
        return null;
    }

    public enum DuelState {
        WAITING,
        COUNTDOWN,
        ACTIVE,
        FINISHED
    }
}