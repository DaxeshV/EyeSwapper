package com.pierfrancescosoffritti.eyeswapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Created by  Pierfrancesco on 20/12/2015.
 */
public class WorkerTask extends AsyncTask {

    private FaceOverlayView mView;
    private Bitmap mBitmap;
    private Context mContext;

    private SparseArray<Face> mFaces;

    public WorkerTask(FaceOverlayView view, Bitmap bitmap) {
        mView = view;
        mBitmap = bitmap;

        mContext = view.getContext();
    }

    @Override
    protected Object doInBackground(Object[] params) {
        FaceDetector detector = new FaceDetector.Builder( mContext )
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        mBitmap = mBitmap.copy(mBitmap.getConfig(), true);

        if (!detector.isOperational()) {

        } else {
            Frame frame = new Frame.Builder().setBitmap(mBitmap).build();
            mFaces = detector.detect(frame);
            detector.release();

            modifyBitmap();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object params) {
        mView.setSwappedBitmap(mBitmap);
        mView.setFaces(mFaces);

        mView.invalidate();

        EventBus.getInstance().post(new ImageReadyEvent());
    }

    private void modifyBitmap() {
        MyFace[] faces = getFaces();

        // debug
        //paintRed(faces);

        drawFaces(faces);
    }

    private MyFace[] getFaces() {

        if(mFaces.size() <= 0)
            return null;

        MyFace[] faces = new MyFace[mFaces.size()];

        for(int i=0; i<mFaces.size(); i++) {
            Face face = mFaces.get(i);
            if(face == null)
                continue;

            faces[i] = new MyFace(mBitmap, face, face.getLandmarks());
        }

        return faces;
    }

    private void drawFaces(MyFace[] faces) {

        if(faces == null || faces.length <= 0 || faces[0] == null)
            return;

        for (int i=0; i<faces.length-1; i++){

            MyFace face1 = faces[i];
            MyFace face2 = faces[i+1];

            for(MyLandmark landmark1 : face1.getLandmarks()) {
                MyLandmark landmark2 = face2.getLandmark(landmark1.getType());

                if(landmark2 == null)
                    continue;

                Log.d("landmanrk1", "width = " + landmark1.getWidth() + ", height = " + landmark1.getHeight());
                Log.d("landmanrk2", "width = " +landmark2.getWidth() +", height = " +landmark2.getHeight());

                Bitmap landmarkBitmap1 = Bitmap.createScaledBitmap(landmark1.getImage(), (int) landmark2.getWidth(), (int) landmark2.getHeight(), false);
                Bitmap landmarkBitmap2 = Bitmap.createScaledBitmap(landmark2.getImage(), (int) landmark1.getWidth(), (int) landmark1.getHeight(), false);

                // 1st face
                for(float y = landmark1.getPosition().y; y < landmark1.getPosition().y + landmark1.getHeight(); y++)
                    for(float x = landmark1.getPosition().x; x < landmark1.getPosition().x + landmark1.getWidth(); x++) {
                        if(((int) (x - landmark1.getPosition().x)) < landmarkBitmap2.getWidth() && ((int) (y - landmark1.getPosition().y)) < landmarkBitmap2.getHeight())
                            mBitmap.setPixel( (int) x, (int) y,  landmarkBitmap2.getPixel(((int) (x - landmark1.getPosition().x)), ((int) (y - landmark1.getPosition().y))));
                    }

                // 2nd face
                for(float y = landmark2.getPosition().y; y < landmark2.getPosition().y + landmark2.getHeight(); y++)
                    for(float x = landmark2.getPosition().x; x < landmark2.getPosition().x + landmark2.getWidth(); x++) {
                        if(((int) (x - landmark2.getPosition().x)) < landmarkBitmap1.getWidth() && ((int) (y - landmark2.getPosition().y)) < landmarkBitmap1.getHeight())
                            mBitmap.setPixel( (int) x, (int) y,  landmarkBitmap1.getPixel(((int) (x - landmark2.getPosition().x)), ((int) (y - landmark2.getPosition().y))));
                    }

            }

            i++;
        }
    }
}
