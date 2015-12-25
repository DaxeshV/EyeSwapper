package com.pierfrancescosoffritti.eyeswapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

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
    private float[] offsets = {13, 15};

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

    public void setFaces(SparseArray<Face> faces) {
        mFaces = faces;
    }

    public void setBitmap( Bitmap bitmap ) {
        new WorkerTask(this, bitmap, FaceDetectorSingleton.getInstance(getContext())).execute();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    protected void setSwappedBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
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

        Rect destBounds = new Rect( 0, 0, (int) ( imageWidth * scale ), (int) ( imageHeight * scale ) );
        canvas.drawBitmap(mBitmap, null, destBounds, null);
        return scale;
    }

    private void drawFaceBox(Canvas canvas, double scale) {
        float left;
        float top;
        float right;
        float bottom;

        for( int i = 0; i < mFaces.size(); i++ ) {
            Face face = mFaces.valueAt(i);

            left = (float) ( face.getPosition().x * scale );
            top = (float) ( face.getPosition().y * scale );
            right = (float) scale * ( face.getPosition().x + face.getWidth() );
            bottom = (float) scale * ( face.getPosition().y + face.getHeight() );

            canvas.drawRect( left, top, right, bottom, paint);
        }
    }

    private void drawFaceLandmarks( Canvas canvas, double scale ) {
        for( int i = 0; i < mFaces.size(); i++ ) {
            Face face = mFaces.valueAt(i);

            for ( Landmark landmark : face.getLandmarks() ) {
                int cx = (int) ( landmark.getPosition().x * scale );
                int cy = (int) ( landmark.getPosition().y * scale );
                canvas.drawCircle( cx, cy, (float) scale, paint);
            }

        }
    }

    public void setOffsetX(float offsetX) {
        this.offsets[0] = offsetX;
    }

    public void setOffsetY(float offsetY) {
        this.offsets[1] = offsetY;
    }

    public float[] getOffsets() {
        return offsets;
    }
}