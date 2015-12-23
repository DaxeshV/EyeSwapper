package com.pierfrancescosoffritti.eyeswapper;

/**
 * Created by  Pierfrancesco on 20/12/2015.
 */
public class ImageReadyEvent {

    private int maxOffsetX;
    private int maxOffsetY;

    public ImageReadyEvent(float maxOffsetX, float maxOffsetY) {
        this.maxOffsetX = (int) maxOffsetX;
        this.maxOffsetY = (int) maxOffsetY;
    }

    public int getMaxOffsetY() {
        return maxOffsetY;
    }

    public int getMaxOffsetX() {
        return maxOffsetX;
    }
}
