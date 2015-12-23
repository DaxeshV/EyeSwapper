package com.pierfrancescosoffritti.eyeswapper;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

/**
 * Created by  Pierfrancesco on 20/12/2015.
 */
public class MyLandmark {

    private PointF position;

    private float width;
    private float height;

    private float eyeRotation;

    private Bitmap image;

    private int type;

    private float maxOffsetX;
    private float maxOffsetY;

    private float offsetX = 13 *3;
    private float offsetY = 15 *3;

    public MyLandmark(Bitmap fullImage, Face face, Landmark landmark, float eyeRotation, float ... offset ) {

        if(offset.length == 2) {
            offsetX = offset[0] *3;
            offsetY = offset[1] *3;
        }

        maxOffsetX = Math.min(fullImage.getWidth() - landmark.getPosition().x, fullImage.getWidth() - (fullImage.getWidth() - landmark.getPosition().x));
        maxOffsetY = Math.min(fullImage.getHeight() - landmark.getPosition().y, fullImage.getHeight() - (fullImage.getHeight() - landmark.getPosition().y));

        type = landmark.getType();
        position = new PointF();
        this.eyeRotation = eyeRotation;

        offsetX = (offsetX * face.getWidth()) / fullImage.getWidth();
        offsetY = (offsetY * face.getHeight()) / fullImage.getHeight();

//        maxOffsetX = (maxOffsetX * face.getWidth()) / fullImage.getWidth();
//        maxOffsetY = (maxOffsetY * face.getHeight()) / fullImage.getHeight();

        if(landmark.getType() == Landmark.LEFT_EYE || landmark.getType() == Landmark.RIGHT_EYE) {
            position.x = landmark.getPosition().x - (offsetX);
            position.y = landmark.getPosition().y - (offsetY);

            position.x = position.x < 0 ? 0 : position.x;
            position.y = position.y < 0 ? 0 : position.y;

            width = offsetX * 2;
            height = offsetY * 2;
        }

        if(width <= 0 || height <= 0)
            throw new IllegalStateException("width <= 0 || height <= 0");

        this.image = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);

        for (float y = getPosition().y; y < getPosition().y + getHeight(); y++)
            for (float x = getPosition().x; x < getPosition().x + getWidth(); x++) {
                if (((int) (x - getPosition().x)) < image.getWidth() && ((int) (y - getPosition().y)) < image.getHeight()
                        && x < fullImage.getWidth() && y < fullImage.getHeight())
                    image.setPixel(((int) (x - getPosition().x)), ((int) (y - getPosition().y)), fullImage.getPixel((int) x, (int) y));
            }

        // apply rotation, to have a final rotation of 0
        Matrix matrix = new Matrix();
        matrix.postRotate((float) Math.toDegrees(eyeRotation));

        float newWidth = (float) (width / Math.cos(eyeRotation));
        float newHeight = (float) (height / Math.cos(eyeRotation));

        position.x = position.x - (newWidth - width) / 2;
        position.y = position.y - (newHeight - height) / 2;

        width = newWidth;
        height = newHeight;

        image = Bitmap.createScaledBitmap(image, (int) width, (int) height, true);
        image = Bitmap.createBitmap(image , 0, 0, (int) width, (int) height, matrix, true);

        for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getPixel(x, y);
                int r = (pixel)&0xFF;
                int g = (pixel>>8)&0xFF;
                int b = (pixel>>16)&0xFF;
                int a = (pixel>>24)&0xFF;
                if(r == 0 && g == 0 && b == 0)
                    image.setPixel(x, y, Color.TRANSPARENT);
            }
    }

    public float getMaxOffsetX() {
        return maxOffsetX;
    }

    public void setMaxOffsetX(float maxOffsetX) {
        this.maxOffsetX = maxOffsetX;
    }

    public float getMaxOffsetY() {
        return maxOffsetY;
    }

    public void setMaxOffsetY(float maxOffsetY) {
        this.maxOffsetY = maxOffsetY;
    }

    public PointF getPosition() {
        return position;
    }

    public void setPosition(PointF position) {
        this.position = position;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public int getType() {
        return type;
    }
}
