package com.pierfrancescosoffritti.eyeswapper;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

/**
 * Created by  Pierfrancesco on 23/12/2015.
 */
public class Assets {
    private static Assets ourInstance = new Assets();

    public static Assets getInstance() {
        return ourInstance;
    }

    private Assets() {
    }

    private static Bitmap foreverAlone;

    public static void init(Context context) {
        Glide.with(context)
                .load(R.raw.foreveralone)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(240, 243) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        foreverAlone = resource;
                    }
                });
    }

    public static Bitmap getForeverAlone(int viewWidth, int viewHeight) {

//        double imageWidth = foreverAlone.getWidth();
//        double imageHeight = foreverAlone.getHeight();
//        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        return Bitmap.createScaledBitmap(foreverAlone, (int) (viewWidth), (int) (viewHeight), false);
    }
}
