package com.pierfrancescosoffritti.eyeswapper.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

/**
 * Created by  Pierfrancesco on 18/10/2015.
 */
public class SnackBarProvider {
    public static void snackBarRequest(View layout, String text, int color, int length) {
        assert layout != null;

        Snackbar snackbar = Snackbar.make(layout, text, length);

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(color);
        snackbar.show();
    }
}
