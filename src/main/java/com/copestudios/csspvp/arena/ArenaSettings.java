package com.copestudios.csspvp.arena;

public class ArenaSettings {

    private int lives;
    private boolean infiniteLives;
    private boolean dropItems;
    private int notchAppleDelay;
    private int enderpearlDelay;
    private int maxPlayers;
    private int kitDelay;

    public ArenaSettings(int lives, boolean dropItems, int notchAppleDelay, int enderpearlDelay, int maxPlayers, int kitDelay) {
        this.lives = lives;
        this.infiniteLives = lives <= 0;
        this.dropItems = dropItems;
        this.notchAppleDelay = notchAppleDelay;
        this.enderpearlDelay = enderpearlDelay;
        this.maxPlayers = maxPlayers;
        this.kitDelay = kitDelay;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
        this.infiniteLives = lives <= 0;
    }

    public boolean hasInfiniteLives() {
        return infiniteLives;
    }

    public void setInfiniteLives(boolean infiniteLives) {
        this.infiniteLives = infiniteLives;
        if (infiniteLives) {
            this.lives = -1;
        } else if (this.lives <= 0) {
            this.lives = 1;
        }
    }

    public boolean canDropItems() {
        return dropItems;
    }

    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    public int getNotchAppleDelay() {
        return notchAppleDelay;
    }

    public void setNotchAppleDelay(int notchAppleDelay) {
        this.notchAppleDelay = notchAppleDelay;
    }

    public int getEnderpearlDelay() {
        return enderpearlDelay;
    }

    public void setEnderpearlDelay(int enderpearlDelay) {
        this.enderpearlDelay = enderpearlDelay;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getKitDelay() {
        return kitDelay;
    }

    public void setKitDelay(int kitDelay) {
        this.kitDelay = kitDelay;
    }
}