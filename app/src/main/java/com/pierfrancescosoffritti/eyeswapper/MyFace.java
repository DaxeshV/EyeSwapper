package com.pierfrancescosoffritti.eyeswapper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  Pierfrancesco on 20/12/2015.
 */
public class MyFace {

    private PointF position;

    private float width;
    private float height;

    private Bitmap image;

    private List<MyLandmark> landmarks;

    public MyFace(Bitmap fullImage, Canvas canvas, Face face, List<Landmark> landmarkList, double scale) {

        landmarks = new ArrayList<>();

        for(Landmark landmark : landmarkList) {
            if(landmark.getType() == Landmark.LEFT_EYE || landmark.getType() == Landmark.RIGHT_EYE)
                landmarks.add(new MyLandmark(fullImage, canvas, face, landmark, scale));
        }

        Log.d("face ", "landmarks: " +landmarks.size());

        // detect with landmarks
//        for (Landmark landmark : landmarkList)
//            if(landmark.getType() == Landmark.RIGHT_EYE)
//                position = landmark.getPosition();
//
//        for(Landmark landmark : landmarkList) {
//            if (landmark.getType() == Landmark.LEFT_EYE)
//                width = landmark.getPosition().x - position.x;
//            if (landmark.getType() == Landmark.BOTTOM_MOUTH)
//                height = landmark.getPosition().y - position.y;
//        }
//
//        // offset
//        position.x -= width/2;
//        position.y -= height/2;
//        width += width/1.2;
//        height += height/1.2;
//
//        Log.d("image ", "position: " +"(" +position.x +", " +position.y +")");
//        Log.d("image ", "width: " +width +" height: " +height);
//
//        this.image = Bitmap.createBitmap((int) width, (int) height, fullImage.getConfig());
//
//        for (float y = getPosition().y; y < getPosition().y + getHeight(); y++)
//            for (float x = getPosition().x; x < getPosition().x + getWidth(); x++) {
//                if (((int) (x - getPosition().x)) < image.getWidth() && ((int) (y - getPosition().y)) < image.getHeight())
//                    image.setPixel(((int) (x - getPosition().x)), ((int) (y - getPosition().y)), fullImage.getPixel((int) x, (int) y));
//            }
    }

    public PointF getPosition() {
        return position;
    }

    public void setPosition(PointF position) {
        this.position = position;
    }

    public void setPositionX(float positionX) {
        this.position.x = positionX;
    }

    public void setPositionY(float positionY) {
        this.position.y = positionY;
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

    public List<MyLandmark> getLandmarks() {
        return landmarks;
    }

    public MyLandmark getLandmark(int type) {
        for(MyLandmark landmark : landmarks)
            if(landmark.getType() == type)
                return landmark;

        return null;
    }
}
