package com.pierfrancescosoffritti.eyeswapper;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPhotoPath;

    @Bind(R.id.progress_bar) View spinner;
    @Bind(R.id.face_overlay) FaceOverlayView mFaceOverlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultAction() {

                    @Override
                    public void onGranted() {
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(MainActivity.this,
                                "Sorry, we need the Storage and Camera Permission",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getInstance().unregister(this);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.take_pic)
    public void takePic(View view) {
        dispatchTakePictureIntent();

        spinner.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.load_bitmap)
    public void loadBitmap(View view) {

        spinner.setVisibility(View.VISIBLE);

        Glide.with(getApplicationContext())
                .load(R.raw.face)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(200, 200) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        mFaceOverlayView.setBitmap(resource);
                    }
                });
    }

    @SuppressWarnings("unused")
    @OnTextChanged(R.id.offset_x_et)
    void onOffsetXChanged(CharSequence text) {
        try {
            mFaceOverlayView.setOffsetX(Long.parseLong(text.toString()));
            Log.d("MainActivity", "offsetX: " + text.toString());
        } catch (NumberFormatException e) {
            if(!text.toString().isEmpty())
                Toast.makeText(MainActivity.this, "Please, write only numbers :\\", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    @OnTextChanged(R.id.offset_y_et)
    void onOffsetYChanged(CharSequence text) {
        try {
            mFaceOverlayView.setOffsetY(Long.parseLong(text.toString()));
            Log.d("MainActivity", "offsetY: " + text.toString());
        } catch (NumberFormatException e) {
            if(!text.toString().isEmpty())
                Toast.makeText(MainActivity.this, "Please, write only numbers :\\", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onImageReady(ImageReadyEvent e) {
        spinner.setVisibility(View.GONE);
    }

    private File createImageFile() throws IOException {
        String imageFileName = "capture_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            Glide.with(getApplicationContext())
                    .load(mCurrentPhotoPath)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(200,200) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            mFaceOverlayView.setBitmap(resource);
                        }
                    });
        }

        else
            spinner.setVisibility(View.GONE);
    }
}
