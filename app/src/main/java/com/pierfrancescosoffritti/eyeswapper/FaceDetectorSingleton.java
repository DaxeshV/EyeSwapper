package com.pierfrancescosoffritti.eyeswapper;

import android.content.Context;

import com.google.android.gms.vision.face.FaceDetector;

/**
 * Created by  Pierfrancesco on 22/12/2015.
 */
public class FaceDetectorSingleton {

    private static com.google.android.gms.vision.face.FaceDetector mFaceDetector;

    public static FaceDetector getInstance(Context context) {
        if(mFaceDetector == null)
            mFaceDetector = new com.google.android.gms.vision.face.FaceDetector.Builder( context )
                .setTrackingEnabled(false)
                .setLandmarkType(com.google.android.gms.vision.face.FaceDetector.ALL_LANDMARKS)
                .setMode(com.google.android.gms.vision.face.FaceDetector.ACCURATE_MODE)
                .build();

        return mFaceDetector;
    }
}
