package com.pierfrancescosoffritti.eyeswapper;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    protected final static String EXTRA_MESSAGE = "key";

    protected static final int REQUEST_TAKE_PHOTO = 1;
    protected static final int RESULT_LOAD_IMAGE = 2;

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

    @SuppressWarnings("unused")
    @OnClick(R.id.take_pic)
    public void takePic(View view) {
        Intent intent = new Intent(this, ShowPictureActivity.class);
        intent.putExtra(EXTRA_MESSAGE, REQUEST_TAKE_PHOTO);
        startActivity(intent);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.load_bitmap)
    public void loadBitmap(View view) {
        Intent intent = new Intent(this, ShowPictureActivity.class);
        intent.putExtra(EXTRA_MESSAGE, RESULT_LOAD_IMAGE);
        startActivity(intent);
    }
}
