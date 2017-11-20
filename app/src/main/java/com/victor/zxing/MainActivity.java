package com.victor.zxing;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.victor.zxing.library.CaptureActivity;

import java.util.Arrays;
import java.util.List;

import permission.victor.com.library.OnPermissionCallback;
import permission.victor.com.library.PermissionHelper;

public class MainActivity extends AppCompatActivity implements OnPermissionCallback {
    private String TAG = "MainActivity";
    private static final int REQUEST_QR_CODE = 100;

    private PermissionHelper permissionHelper;
    private String[] neededPermission;
    private final static String[] MULTI_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
    };

    private AlertDialog builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize () {
        permissionHelper = PermissionHelper.getInstance(this);
        permissionHelper
                .setForceAccepting(false) // default is false. its here so you know that it exists.
                .request(MULTI_PERMISSIONS);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                MainActivity.this.startActivityForResult(intent, REQUEST_QR_CODE);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.d(TAG, "onPermissionGranted-Permission(s) " + Arrays.toString(permissionName) + " Granted");
    }

    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {
        Log.d("onPermissionDeclined", "Permission(s) " + Arrays.toString(permissionName) + " Declined");
    }

    @Override
    public void onPermissionPreGranted(@NonNull String permissionsName) {
        Log.d("onPermissionPreGranted", "Permission( " + permissionsName + " ) preGranted");
    }

    @Override
    public void onPermissionNeedExplanation(@NonNull String permissionName) {
        neededPermission = PermissionHelper.declinedPermissions(this, MULTI_PERMISSIONS);
        StringBuilder builder = new StringBuilder(neededPermission.length);
        if (neededPermission.length > 0) {
            for (String permission : neededPermission) {
                builder.append(permission).append("\n");
            }
        }
        AlertDialog alert = getAlertDialog(neededPermission, builder.toString());
        if (!alert.isShowing()) {
            alert.show();
        }
    }

    @Override
    public void onPermissionReallyDeclined(@NonNull String permissionName) {
        Log.d("ReallyDeclined", "Permission " + permissionName + " can only be granted from settingsScreen");
    }

    @Override
    public void onNoPermissionNeeded() {
        Log.d("onNoPermissionNeeded", "Permission(s) not needed");
    }

    public AlertDialog getAlertDialog(final String[] permissions, final String permissionName) {
        if (builder == null) {
            builder = new AlertDialog.Builder(this)
                    .setTitle("Permission Needs Explanation")
                    .create();
        }
        builder.setButton(DialogInterface.BUTTON_POSITIVE, "Request", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionHelper.requestAfterExplanation(permissions);
            }
        });
        builder.setMessage("Permissions need explanation (" + permissionName + ")");
        return builder;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_QR_CODE) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String result = bundle.getString("result");
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
