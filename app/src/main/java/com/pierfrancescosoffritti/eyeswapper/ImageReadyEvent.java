package com.pierfrancescosoffritti.eyeswapper;

/**
 * Created by  Pierfrancesco on 20/12/2015.
 */
public class ImageReadyEvent {

    private int maxOffsetX;
    private int maxOffsetY;

    private int facesNumber;

    public ImageReadyEvent(float maxOffsetX, float maxOffsetY, int facesNumber) {
        this.maxOffsetX = (int) maxOffsetX;
        this.maxOffsetY = (int) maxOffsetY;

        this.facesNumber = facesNumber;
    }

    public int getMaxOffsetY() {
        return maxOffsetY;
    }

    public int getMaxOffsetX() {
        return maxOffsetX;
    }

    public int getFacesNumber() {
        return facesNumber;
    }
}
