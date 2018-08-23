package com.victor.zxing.library.zxing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.Result;
import com.victor.zxing.library.common.Scanner;
import com.victor.zxing.library.common.ScannerOptions;
import com.victor.zxing.library.zxing.camera.CameraManager;
import com.victor.zxing.library.zxing.decoding.DecodeThread;

/**
 * 针对扫描任务的Handler，可接收的message有启动扫描（restart_preview）、扫描成功（decode_succeeded）、扫描失败（decode_failed）等等
 * This class handles all the messaging which comprises the state machine for
 * capture.
 */
public class ScannerViewHandler extends Handler {

    public interface HandleDecodeListener {
        void restartPreview();

        void decodeSucceeded(Result rawResult, Bitmap barcode, float scaleFactor);
    }

    private final DecodeThread decodeThread;
    private State state;
    private final CameraManager cameraManager;
    private HandleDecodeListener handleDecodeListener;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public ScannerViewHandler(ScannerOptions scannerOptions, CameraManager cameraManager
            , HandleDecodeListener handleDecodeListener) {
        this.cameraManager = cameraManager;
        this.handleDecodeListener = handleDecodeListener;
        //启动扫描线程
        decodeThread = new DecodeThread(cameraManager, this, scannerOptions.getDecodeFormats()
                , scannerOptions.isCreateQrThumbnail());

        decodeThread.start();
        state = State.SUCCESS;
        //开启相机预览界面
        cameraManager.startPreview();
        //将preview回调函数与decodeHandler绑定、调用viewfinderView
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case Scanner.RESTART_PREVIEW:
                restartPreviewAndDecode();
                break;
            case Scanner.DECODE_SUCCEEDED:
                state = State.SUCCESS;
                Bundle bundle = message.getData();
                Bitmap barcode = null;
                float scaleFactor = 1.0f;
                if (bundle != null) {
                    byte[] compressedBitmap = bundle
                            .getByteArray(DecodeThread.BARCODE_BITMAP);
                    if (compressedBitmap != null && compressedBitmap.length > 0) {
                        barcode = BitmapFactory.decodeByteArray(compressedBitmap,
                                0, compressedBitmap.length, null);
                        barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    scaleFactor = bundle.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
                }
                if (handleDecodeListener != null)
                    handleDecodeListener.decodeSucceeded((Result) message.obj, barcode, scaleFactor);
                break;
            case Scanner.DECODE_FAILED:
                state = State.PREVIEW;
                cameraManager.requestPreviewFrame(decodeThread.getHandler(), Scanner.DECODE);
                break;
            case Scanner.RETURN_SCAN_RESULT:
                break;
            case Scanner.LAUNCH_PRODUCT_QUERY:
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), Scanner.QUIT);
        quit.sendToTarget();
        try {
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        removeMessages(Scanner.DECODE_SUCCEEDED);
        removeMessages(Scanner.DECODE_FAILED);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), Scanner.DECODE);
            if (handleDecodeListener != null)
                handleDecodeListener.restartPreview();
        }
    }
}