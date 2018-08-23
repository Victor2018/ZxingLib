package com.victor.zxing.library;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.victor.zxing.library.interfaces.OnQrScanListener;
import com.victor.zxing.library.interfaces.OnScannerCompletionListener;
import com.victor.zxing.library.module.ZxingScanHelper;
import com.victor.zxing.library.zxing.view.ScannerView;

public class CaptureActivity extends Activity implements View.OnClickListener,OnQrScanListener{

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private CaptureActivity mActivity;

    private ScannerView mScannerView;
    private ImageView mIbBack,mIbFlash;
    private TextView mTvgallery;

    private boolean flashLightOpen = false;

    private ZxingScanHelper mZxingScanHelper;

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
        mZxingScanHelper = new ZxingScanHelper(this,mScannerView,this);
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

        mScannerView = (ScannerView) findViewById(R.id.scanner_view);
        mIbBack = (ImageView) findViewById(R.id.ib_back);
        mIbFlash = (ImageButton) findViewById(R.id.ib_flash);
        mTvgallery = (TextView) findViewById(R.id.tv_gallery);

        mIbBack.setOnClickListener(this);
        mIbFlash.setOnClickListener(this);
        mTvgallery.setOnClickListener(this);
    }

    private void init () {

    }

    /**
     * 切换散光灯状态
     */
    public void toggleFlashLight() {
        if (flashLightOpen) {
            mScannerView.toggleLight(false);
        } else {
            mScannerView.toggleLight(true);
        }
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