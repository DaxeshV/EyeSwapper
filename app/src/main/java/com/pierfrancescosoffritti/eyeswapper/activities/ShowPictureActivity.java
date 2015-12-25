package com.pierfrancescosoffritti.eyeswapper.activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.pierfrancescosoffritti.eyeswapper.EventBus;
import com.pierfrancescosoffritti.eyeswapper.FaceOverlayView;
import com.pierfrancescosoffritti.eyeswapper.ImageReadyEvent;
import com.pierfrancescosoffritti.eyeswapper.R;
import com.pierfrancescosoffritti.eyeswapper.utils.SnackBarProvider;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.techery.progresshint.addition.widget.SeekBar;

public class ShowPictureActivity extends AppCompatActivity {

    private String mCurrentPhotoPath;

    @Bind(R.id.toolbar) Toolbar toolbar;

    @Bind(R.id.progress_bar) View spinner;
    @Bind(R.id.face_overlay) FaceOverlayView mFaceOverlayView;

    @Bind(R.id.seekbar_offset_x) SeekBar mSeekBarOffset_X;
    @Bind(R.id.seekbar_offset_y) SeekBar mSeekBarOffset_Y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);
        ButterKnife.bind(this);

        initToolbar(toolbar);

        switch((int) getIntent().getExtras().get(MainActivity.EXTRA_MESSAGE)) {
            case MainActivity.RESULT_LOAD_IMAGE: {
                spinner.setVisibility(View.VISIBLE);

                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, MainActivity.RESULT_LOAD_IMAGE);
                break;
            }
            case MainActivity.REQUEST_TAKE_PHOTO: {
                spinner.setVisibility(View.VISIBLE);

                dispatchTakePictureIntent();
                break;
            }
        }

        final android.widget.SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
                int id = seekBar.getId();
                int progress = seekBar.getProgress() <= 0 ? 1 : seekBar.getProgress();

                if(id == mSeekBarOffset_X.getId())
                    onOffsetXChanged(progress);
                else if(id == mSeekBarOffset_Y.getId())
                    onOffsetYChanged(progress);
            }
        };

        mSeekBarOffset_X.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mSeekBarOffset_Y.setOnSeekBarChangeListener(mSeekBarChangeListener);
    }

    private void initToolbar(Toolbar toolbar) {
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_picture_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_picture: {
                saveImage(mFaceOverlayView.getBitmap(), "EyesSwapper", "swapped_" +mCurrentPhotoPath.substring(mCurrentPhotoPath.lastIndexOf("/")+1, mCurrentPhotoPath.length()));
                return true;
            }
            case android.R.id.home: {
                this.finish();
                break;
            }

            default:
                super.onOptionsItemSelected(item);
        }

        return false;
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if((int) getIntent().getExtras().get(MainActivity.EXTRA_MESSAGE) == MainActivity.REQUEST_TAKE_PHOTO)
            deleteImage(mCurrentPhotoPath);
    }

    private boolean deleteImage(String path) {
        try {
            return new File(new URI(mCurrentPhotoPath)).delete();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveImage(Bitmap bitmap, String path, String fileName) {

        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root +"/"+path);
        dir.mkdirs();
        File file = new File (dir, fileName);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            // show the pic in gallery
            MediaScannerConnection.scanFile(this, new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onImageReady(ImageReadyEvent e) {
        spinner.setVisibility(View.GONE);

        mSeekBarOffset_X.setMax(e.getMaxOffsetX());
        mSeekBarOffset_Y.setMax(e.getMaxOffsetY());

        boolean enableSeekBar = true;
        if(e.getFacesNumber() <= 1 || (e.getMaxOffsetX() <= 0 && e.getMaxOffsetY() <= 0 ))
            enableSeekBar = false;

        mSeekBarOffset_X.setEnabled(enableSeekBar);
        mSeekBarOffset_Y.setEnabled(enableSeekBar);

        if(e.getFacesNumber() <= 1) {
            String text = "";
            if (e.getFacesNumber() == 0)
                text = "i've found ZERO faces ¯\\_(ツ)_/¯";
            else if (e.getFacesNumber() == 1)
                text = "i've found only ONE face ¯\\_(ツ)_/¯";

            SnackBarProvider.snackBarRequest(
                    findViewById(R.id.show_picture_root_layout),
                    text,
                    ContextCompat.getColor(this, R.color.colorAccent),
                    Snackbar.LENGTH_LONG);
        }
    }

    private boolean refreshBitmap() {

        spinner.setVisibility(View.VISIBLE);
        loadPic();
        return true;
    }

    void onOffsetXChanged(int value) {
        mSeekBarOffset_X.setEnabled(false);
        mSeekBarOffset_Y.setEnabled(false);

        mFaceOverlayView.setOffsetX(value);
        refreshBitmap();
    }

    void onOffsetYChanged(int value) {
        mSeekBarOffset_X.setEnabled(false);
        mSeekBarOffset_Y.setEnabled(false);

        mFaceOverlayView.setOffsetY(value);
        refreshBitmap();
    }

    // ----

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
            loadPic();
        else if (requestCode == MainActivity.RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            mCurrentPhotoPath = cursor.getString(columnIndex);
            cursor.close();

            loadPic();
        }

        else
            this.finish();
    }

    private void loadPic() {
        Glide.with(this)
                .load(mCurrentPhotoPath)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(200, 200) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        mFaceOverlayView.setBitmap(resource);
                    }
                });
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
                startActivityForResult(takePictureIntent, MainActivity.REQUEST_TAKE_PHOTO);
            }
        }
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
}
