package com.victor.zxing.library.module;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.victor.zxing.library.BuildConfig;
import com.victor.zxing.library.R;
import com.victor.zxing.library.common.ActionUtils;
import com.victor.zxing.library.interfaces.OnQrScanListener;
import com.victor.zxing.library.interfaces.OnScannerCompletionListener;
import com.victor.zxing.library.zxing.decoding.QRDecode;
import com.victor.zxing.library.zxing.view.ScannerView;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by victor on 2017/11/20.
 */

public class ZxingScanHelper implements OnScannerCompletionListener {
    private String TAG = "ZxingScanHelper";
    private static final int REQUEST_PERMISSION_CAMERA = 1000;
    private static final int REQUEST_PERMISSION_PHOTO = 1001;
    private static final long VIBRATE_DURATION = 200L;

    public Activity mActivity;
    private ScannerView mScannerView;
    private OnQrScanListener mOnQrScanListener;

    public ZxingScanHelper (Activity activity,ScannerView scannerView,OnQrScanListener listener) {
        mActivity = activity;
        mScannerView = scannerView;
        mOnQrScanListener = listener;
        init();
    }

    private void init () {
        mScannerView.setOnScannerCompletionListener(this);
        mScannerView.setMediaResId(R.raw.beep);//设置扫描成功的声音
        mScannerView.setDrawTextColor(Color.RED);

        //显示扫描成功后的缩略图
        mScannerView.isShowResThumbnail(false);
        //全屏识别
        mScannerView.isScanFullScreen(false);
        //隐藏扫描框
        mScannerView.isHideLaserFrame(false);
//        mScannerView.isScanInvert(true);//扫描反色二维码
//        mScannerView.setCameraFacing(CameraFacing.FRONT);
//        mScannerView.setLaserMoveSpeed(1);//速度

//        mScannerView.setLaserFrameTopMargin(100);//扫描框与屏幕上方距离
//        mScannerView.setLaserFrameSize(400, 400);//扫描框大小
//        mScannerView.setLaserFrameCornerLength(25);//设置4角长度
//        mScannerView.setLaserLineHeight(5);//设置扫描线高度
//        mScannerView.setLaserFrameCornerWidth(5);

        mScannerView.setLaserFrameBoundColor(mActivity.getResources().getColor(R.color.colorAccent));//扫描框四角颜色
        mScannerView.setDrawTextColor(mActivity.getResources().getColor(R.color.colorAccent));//扫描框四角颜色

//        mScannerView.setLaserLineResId(R.mipmap.wx_scan_line);//线图
//
//        mScannerView.setLaserGridLineResId(R.mipmap.zfb_grid_scan_line);//网格图
//        mScannerView.setLaserFrameBoundColor(0xFF26CEFF);//支付宝颜色

        mScannerView.setLaserColor(mActivity.getResources().getColor(R.color.colorAccent));//设置扫描线颜色
    }

    public void onResume() {
        mScannerView.onResume();
    }

    public void onPause () {
        mScannerView.onPause();
    }

    public void onDestroy () {

    }

    public void setDrawText (String text) {
        if (mScannerView != null) {
            mScannerView.setDrawText(text, true);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == mActivity.RESULT_OK
                && data != null
                && requestCode == ActionUtils.PHOTO_REQUEST_GALLERY) {
            Uri inputUri = data.getData();
            String path = null;

            if (URLUtil.isFileUrl(inputUri.toString())) {
                // 小米手机直接返回的文件路径
                path = inputUri.getPath();
            } else {
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = mActivity.getContentResolver().query(inputUri, proj, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                }
            }
            if (!TextUtils.isEmpty(path)) {
                QRDecode.decodeQR(path, this);
            } else {
                Toast.makeText(mActivity, "mage path not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        String resultString = result.getText();
        if (mOnQrScanListener != null) {
            mOnQrScanListener.OnQrScan(resultString);
        }
    }

    public void restartPreviewAfterDelay(long delayMS) {
        mScannerView.restartPreviewAfterDelay(delayMS);
    }



    /**
     * 打开相册
     */
    public void openGallery() {
        ActionUtils.startActivityForGallery(mActivity, ActionUtils.PHOTO_REQUEST_GALLERY);
    }


    public void restartPreview() {
    }


    @Override
    public void onScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode) {
        handleDecode(rawResult,barcode);
        restartPreviewAfterDelay(2000);
    }
}
