package com.victor.zxing.library.module;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import com.victor.zxing.library.BuildConfig;
import com.victor.zxing.library.R;
import com.victor.zxing.library.common.ActionUtils;
import com.victor.zxing.library.common.QrUtils;
import com.victor.zxing.library.interfaces.OnQrScanListener;
import com.victor.zxing.library.zxing.camera.CameraManager;
import com.victor.zxing.library.zxing.decoding.CaptureActivityHandler;
import com.victor.zxing.library.zxing.decoding.InactivityTimer;
import com.victor.zxing.library.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by victor on 2017/11/20.
 */

public class ZxingScanHelper implements SurfaceHolder.Callback{
    private String TAG = "ZxingScanHelper";
    private static final int REQUEST_PERMISSION_CAMERA = 1000;
    private static final int REQUEST_PERMISSION_PHOTO = 1001;
    private static final long VIBRATE_DURATION = 200L;

    public Activity mActivity;
    private CaptureActivityHandler handler;
    private ViewfinderView mViewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private OnQrScanListener mOnQrScanListener;

    public ZxingScanHelper (Activity activity,ViewfinderView viewfinderView,OnQrScanListener listener) {
        mActivity = activity;
        mViewfinderView = viewfinderView;
        mOnQrScanListener = listener;
        init();
    }

    private void init () {
        hasSurface = false;
        inactivityTimer = new InactivityTimer(mActivity);
        CameraManager.init(mActivity);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = mActivity.getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    public void onResume() {
        Log.d(TAG, "xxxxxxxxxxxxxxxxxxxonResume");
        SurfaceView surfaceView = (SurfaceView) mActivity.findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        final AudioManager audioService = (AudioManager) mActivity.getSystemService(mActivity.AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    public void onPause () {
        Log.d(TAG, "xxxxxxxxxxxxxxxxxxxonPause");
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    public void onDestroy () {
        inactivityTimer.shutdown();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
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
                Result result = QrUtils.decodeImage(path);
                if (result != null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, result.getText());
                    handleDecode(result, null);
                } else {
                    new AlertDialog.Builder(mActivity)
                            .setTitle("提示")
                            .setMessage("此图片无法识别")
                            .setPositiveButton("确定", null)
                            .show();
                }
            } else {
                if (BuildConfig.DEBUG) Log.e(TAG, "image path not found");
                Toast.makeText(mActivity, "图片路径未找到", Toast.LENGTH_SHORT).show();
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
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        if (mOnQrScanListener != null) {
            mOnQrScanListener.OnQrScan(resultString);
        }
    }



    /**
     * 打开相册
     */
    public void openGallery() {
        ActionUtils.startActivityForGallery(mActivity, ActionUtils.PHOTO_REQUEST_GALLERY);
    }

    public void drawViewfinder() {
        mViewfinderView.drawViewfinder();
    }

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) mActivity.getSystemService(mActivity.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    public void restartPreview() {
        // 当界面跳转时 handler 可能为null
        if (handler != null) {
            Message restartMessage = Message.obtain();
            restartMessage.what = R.id.restart_preview;
            handler.handleMessage(restartMessage);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    public ViewfinderView getViewfinderView() {
        return mViewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

}
