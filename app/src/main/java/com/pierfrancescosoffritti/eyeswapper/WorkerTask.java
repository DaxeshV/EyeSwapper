package com.pierfrancescosoffritti.eyeswapper;

import android.graphics.Bitmap;
import android.graphics.Color;
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

    private FaceDetector mFaceDetector;

    private SparseArray<Face> mFaces;

    private float maxOffsetX = Float.MAX_VALUE;
    private float maxOffsetY = Float.MAX_VALUE;

    public WorkerTask(FaceOverlayView view, Bitmap bitmap, FaceDetector detector) {
        mView = view;
        mBitmap = bitmap;

        mFaceDetector = detector;
    }

    @Override
    protected Object doInBackground(Object[] params) {

//        mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
//        mBitmap.setHasAlpha(true);
        mBitmap = mBitmap.copy(mBitmap.getConfig(), true);

        if (!mFaceDetector.isOperational()) {

        } else {
            Frame frame = new Frame.Builder().setBitmap(mBitmap).build();
            mFaces = mFaceDetector.detect(frame);
//            mFaceDetector.release();

            if(mFaces.size() > 0);
                modifyBitmap();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object params) {
        mView.setSwappedBitmap(mBitmap);
        mView.setFaces(mFaces);

        mView.invalidate();

        EventBus.getInstance().post(new ImageReadyEvent(mFaces.size() > 1 ? maxOffsetX : 0, mFaces.size() > 1 ? maxOffsetY : 0));
    }

    private void modifyBitmap() {
        MyFace[] faces = getFaces();

        if(faces.length > 1)
            swapEyes(faces);
        else if(faces.length == 1)
            paintRed(faces);
    }

    private MyFace[] getFaces() {

        if(mFaces.size() <= 0)
            return null;

        MyFace[] faces = new MyFace[mFaces.size()];

        for(int i=0; i<mFaces.size(); i++) {
            Face face = mFaces.get(i);
            if(face == null) {
                continue;
            }

            faces[i] = new MyFace(mBitmap, face, face.getLandmarks(), mView.getOffsets());
        }

        return faces;
    }

    private void paintRed(MyFace[] faces) {
        MyFace face = faces[0];

        if(face == null) {
            Log.e("WorkerTask", "face == null");
            return;
        }

        Bitmap foreverAlone = Assets.getForeverAlone((int) face.getWidth(), (int) face.getHeight());

        for(float y = face.getPosition().y; y < face.getPosition().y + foreverAlone.getHeight(); y++)
            for(float x = face.getPosition().x; x < face.getPosition().x + foreverAlone.getWidth(); x++) {
//                    mBitmap.setPixel( (int) x, (int) y, Color.TRANSPARENT);
                int pixel = foreverAlone.getPixel(((int) (x - face.getPosition().x)), ((int) (y - face.getPosition().y)));
                int alpha = (pixel >> 24) & 0xFF;
                if(alpha > 0)
                    mBitmap.setPixel((int) x, (int) y, pixel);
            }
    }

    private void swapEyes(MyFace[] faces) {

        if(faces == null || faces.length <= 0 || faces[0] == null)
            return;

        for (int i=0; i<faces.length-1; i++){

            MyFace face1 = faces[i];
            MyFace face2 = faces[i+1];

            for(MyLandmark landmark1 : face1.getLandmarks()) {
                MyLandmark landmark2 = face2.getLandmark(landmark1.getType());

                if(landmark2 == null)
                    continue;

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

                maxOffsetX = Math.min(Math.min(landmark1.getMaxOffsetX(), landmark2.getMaxOffsetX()), maxOffsetX);
                maxOffsetY  = Math.min(Math.min(landmark1.getMaxOffsetY(), landmark2.getMaxOffsetY()), maxOffsetY);
            }

            i++;
        }
    }
}
