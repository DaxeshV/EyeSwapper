package com.pierfrancescosoffritti.eyeswapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

/**
 * Created by  Pierfrancesco on 19/12/2015.
 */
public class FaceOverlayView extends View {

    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;

    private Paint paint;

    public FaceOverlayView(Context context) {
        this(context, null);

        init();
    }

    public FaceOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

        init();
    }

    public FaceOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

    }

    public void setBitmap( Bitmap bitmap ) {

        FaceDetector detector = new FaceDetector.Builder( getContext() )
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        mBitmap = bitmap.copy(bitmap.getConfig(), true);

        if (!detector.isOperational()) {
            //Handle contingency
        } else {
            Frame frame = new Frame.Builder().setBitmap(mBitmap).build();
            mFaces = detector.detect(frame);
            detector.release();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if ((mBitmap != null) && (mFaces != null)) {
            double scale = drawBitmap(canvas);
//            drawFaceBox(canvas, scale);
//            drawFaceLandmarks(canvas, scale);
        }
    }

    private double drawBitmap( Canvas canvas ) {
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);


        MyFace[] faces = getFaces(scale);

//        if(faces != null && faces.length > 0) {
//            for (int i = 0; i < faces.length; i++) {
//                MyFace face = faces[i];
//                if(face != null) {
//                    for (MyLandmark landmark : face.getLandmarks()) {
//                        if(landmark != null) {
//                            for (float y = landmark.getPosition().y; y < landmark.getPosition().y + landmark.getHeight(); y++)
//                                for (float x = landmark.getPosition().x; x < landmark.getPosition().x + landmark.getWidth(); x++) {
//                                    if (x < mBitmap.getWidth() && y < mBitmap.getHeight())
//                                        mBitmap.setPixel((int) x, (int) y, Color.RED);
//                                }
//                        }
//                    }
//                }
//            }
//        }

        drawFaces(faces);

        Rect destBounds = new Rect( 0, 0, (int) ( imageWidth * scale ), (int) ( imageHeight * scale ) );
        canvas.drawBitmap(mBitmap, null, destBounds, null);
        return scale;
    }

    private MyFace[] getFaces(double scale) {

        if(mFaces.size() <= 0)
            return null;

        MyFace[] faces = new MyFace[mFaces.size()];

        for(int i=0; i<mFaces.size(); i++) {
            Face face = mFaces.get(i);
            if(face == null)
                continue;

            faces[i] = new MyFace(mBitmap, face, face.getLandmarks(), scale);
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

                Log.d("landmanrk1", "width = " +landmark1.getWidth() +", height = " +landmark1.getHeight());
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

//            Bitmap faceBitmap1 = Bitmap.createScaledBitmap(face1.getImage(), (int) face2.getWidth(), (int) face2.getHeight(), false);
//            Bitmap faceBitmap2 = Bitmap.createScaledBitmap(face2.getImage(), (int) face1.getWidth(), (int) face1.getHeight(), false);
//
//            // 1st face
//            for(float y = face1.getPosition().y; y < face1.getPosition().y + face1.getHeight(); y++)
//                for(float x = face1.getPosition().x; x < face1.getPosition().x + face1.getWidth(); x++) {
//                    if(((int) (x - face1.getPosition().x)) < faceBitmap2.getWidth() && ((int) (y - face1.getPosition().y)) < faceBitmap2.getHeight())
//                        mBitmap.setPixel( (int) x, (int) y,  faceBitmap2.getPixel(((int) (x - face1.getPosition().x)), ((int) (y - face1.getPosition().y))));
//                }
//
//            // 2nd face
//            for(float y = face2.getPosition().y; y < face2.getPosition().y + face2.getHeight(); y++)
//                for(float x = face2.getPosition().x; x < face2.getPosition().x + face2.getWidth(); x++) {
//                    if(((int) (x - face2.getPosition().x)) < faceBitmap1.getWidth() && ((int) (y - face2.getPosition().y)) < faceBitmap1.getHeight())
//                        mBitmap.setPixel( (int) x, (int) y,  faceBitmap1.getPixel(((int) (x - face2.getPosition().x)), ((int) (y - face2.getPosition().y))));
//                }

            i++;
        }
    }

    private void drawFaceBox(Canvas canvas, double scale) {
        float left = 0;
        float top = 0;
        float right = 0;
        float bottom = 0;

        for( int i = 0; i < mFaces.size(); i++ ) {
            Face face = mFaces.valueAt(i);

            left = (float) ( face.getPosition().x * scale );
            top = (float) ( face.getPosition().y * scale );
            right = (float) scale * ( face.getPosition().x + face.getWidth() );
            bottom = (float) scale * ( face.getPosition().y + face.getHeight() );

            canvas.drawRect( left, top, right, bottom, paint );
        }
    }

    private void drawFaceLandmarks( Canvas canvas, double scale ) {
        Paint paint = new Paint();
        paint.setColor( Color.GREEN );
        paint.setStyle( Paint.Style.STROKE );
        paint.setStrokeWidth( 5 );

        for( int i = 0; i < mFaces.size(); i++ ) {
            Face face = mFaces.valueAt(i);

            for ( Landmark landmark : face.getLandmarks() ) {
                int cx = (int) ( landmark.getPosition().x * scale );
                int cy = (int) ( landmark.getPosition().y * scale );
                canvas.drawCircle( cx, cy, (float) (20*scale), paint );
            }

        }
    }
}