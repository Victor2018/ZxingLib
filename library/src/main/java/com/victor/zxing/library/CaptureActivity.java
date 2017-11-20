package com.victor.zxing.library;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.victor.zxing.library.common.ActionUtils;
import com.victor.zxing.library.common.QrUtils;
import com.victor.zxing.library.interfaces.OnQrScanListener;
import com.victor.zxing.library.module.ZxingScanHelper;
import com.victor.zxing.library.zxing.camera.CameraManager;
import com.victor.zxing.library.zxing.decoding.CaptureActivityHandler;
import com.victor.zxing.library.zxing.decoding.InactivityTimer;
import com.victor.zxing.library.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

public class CaptureActivity extends Activity implements View.OnClickListener,OnQrScanListener{

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private CaptureActivity mActivity;

    private ViewfinderView viewfinderView;
    private ImageView mIbBack,mIbFlash;
    private TextView mTvgallery;

    private boolean flashLightOpen = false;

    private ZxingScanHelper mZxingScanHelper;
    private android.support.v7.app.AlertDialog mDialog;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mActivity = this;

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mZxingScanHelper = new ZxingScanHelper(this,viewfinderView,this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mZxingScanHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIbFlash != null) {
            mIbFlash.setImageResource(R.drawable.ic_flash_off_white_24dp);
        }
        mZxingScanHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        mZxingScanHelper.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mZxingScanHelper.onActivityResult(requestCode,resultCode,data);
    }

    protected void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.qr_camera);

        mIbBack = (ImageView) findViewById(R.id.ib_back);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        mIbFlash = (ImageButton) findViewById(R.id.ib_flash);
        mTvgallery = (TextView) findViewById(R.id.tv_gallery);

        mIbBack.setOnClickListener(this);
        mIbFlash.setOnClickListener(this);
        mTvgallery.setOnClickListener(this);
    }

    /**
     * 切换散光灯状态
     */
    public void toggleFlashLight() {
        if (flashLightOpen) {
            setFlashLightOpen(false);
        } else {
            setFlashLightOpen(true);
        }
    }

    /**
     * 设置闪光灯是否打开
     * @param open
     */
    public void setFlashLightOpen(boolean open) {
        if (flashLightOpen == open) return;
        flashLightOpen = !flashLightOpen;
        CameraManager.get().setFlashLight(open);
    }

    public void handleResult(String resultString) {
        if (resultString.equals("")) {
            Toast.makeText(getApplicationContext(), R.string.scan_failed, Toast.LENGTH_SHORT).show();
            mZxingScanHelper.restartPreview();
        } else {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("result", resultString);
            resultIntent.putExtras(bundle);
            setResult(RESULT_OK, resultIntent);
        }
        finish();
    }

    @Override
    public void OnQrScan(String resultString) {
        handleResult(resultString);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ib_back) {
            finish();

        } else if (i == R.id.tv_gallery) {
            mZxingScanHelper.openGallery();

        } else if (i == R.id.ib_flash) {
            if (flashLightOpen) {
                mIbFlash.setImageResource(R.drawable.ic_flash_off_white_24dp);
            } else {
                mIbFlash.setImageResource(R.drawable.ic_flash_on_white_24dp);
            }
            toggleFlashLight();
        }
    }
}