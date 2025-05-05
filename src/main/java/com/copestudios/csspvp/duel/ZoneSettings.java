package com.copestudios.csspvp.duel;

public class ZoneSettings {

    private boolean dropItems;
    private int notchAppleDelay;
    private int enderpearlDelay;

    public ZoneSettings(boolean dropItems, int notchAppleDelay, int enderpearlDelay) {
        this.dropItems = dropItems;
        this.notchAppleDelay = notchAppleDelay;
        this.enderpearlDelay = enderpearlDelay;
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
}