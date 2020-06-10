package com.oginotihiro.cropview.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.oginotihiro.cropview.CropUtil;
import com.oginotihiro.cropview.CropView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int REQUEST_PICK = 9162;

    private CropView cropView;
    private ImageView resultIv;
    private LinearLayout btnlay;
    private Button doneBtn;
    private Button cancelBtn;

    private Bitmap croppedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cropView = (CropView) findViewById(R.id.cropView);
        resultIv = (ImageView) findViewById(R.id.resultIv);
        btnlay = (LinearLayout) findViewById(R.id.btnlay);
        doneBtn = (Button) findViewById(R.id.doneBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);

        doneBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick:
                reset();

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
                startActivityForResult(intent, REQUEST_PICK);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_PICK) {
            Uri source = data.getData();

            cropView.setVisibility(View.VISIBLE);
            btnlay.setVisibility(View.VISIBLE);

            cropView.of(source, null).asSquare().initialize(MainActivity.this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.doneBtn) {
            final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, null, "Please wait…", true, false);

            cropView.setVisibility(View.GONE);
            btnlay.setVisibility(View.GONE);
            resultIv.setVisibility(View.VISIBLE);

            new Thread() {
                public void run() {
                    croppedBitmap = cropView.getOutput();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultIv.setImageBitmap(croppedBitmap);
                        }
                    });

                    Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
                    CropUtil.saveOutput(MainActivity.this, destination, croppedBitmap, 90);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                }
            }.start();
        } else if (id == R.id.cancelBtn) {
            reset();
        }
    }

    private void reset() {
        cropView.setVisibility(View.GONE);
        resultIv.setVisibility(View.GONE);
        btnlay.setVisibility(View.GONE);
        resultIv.setImageBitmap(null);
    }
}