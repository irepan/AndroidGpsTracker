package com.alcatrazstudios.apps.androidtracker.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.alcatrazstudios.apps.androidtracker.R;

public class PermissionsActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 5912;
    private static boolean allPermissions = false;
    private static boolean adminActive=false;
    private final String[] permissions = new String[]{
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.INTERNET,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WAKE_LOCK};
    private final int PERMISSION_CODE = 100;
    private Button permissionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        permissionsButton = (Button) findViewById(R.id.buttonPermissions);

        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    allPermissions=true;
                    returnOk();
                }
            }
        });

        permissionsButton.callOnClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE == requestCode) {
            adminActive=true;
            if (adminActive && allPermissions){
                returnOk();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            allPermissions = true;
            for (int index = 0; index < permissions.length; index++) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    allPermissions = false;
                }
            }
            if (allPermissions) {
                returnOk();
            }
        }
    }
    private void returnOk(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", allPermissions);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}